package com.site0.walnut.ext.xo.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nutz.lang.Each;
import org.nutz.lang.Streams;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.xo.bean.XoBean;
import com.site0.walnut.ext.xo.util.WnIoXoClientGetter;
import com.site0.walnut.ext.xo.util.WnSimpleClientGetter;
import com.site0.walnut.ext.xo.util.XoClientGetter;
import com.site0.walnut.ext.xo.util.XoClientWrapper;
import com.site0.walnut.ext.xo.util.XoClients;
import com.site0.walnut.util.Ws;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CommonPrefix;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.MetadataDirective;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import software.amazon.awssdk.services.s3.model.UploadPartResponse;

public class S3XoService extends AbstractXoService<S3Client> {

    public S3XoService(WnIo io, WnObj oHome, String name) {
        WnIoXoClientGetter<S3Client> getter = new WnIoXoClientGetter<>();
        getter.setIo(io);
        getter.setHome(oHome);
        getter.setName(name);
        getter.setManager(XoClients.S3);
        this.getter = getter;
    }

    public S3XoService(XoClientGetter<S3Client> getter) {
        this.getter = getter;
    }

    public S3XoService(XoClientWrapper<S3Client> client) {
        this.getter = new WnSimpleClientGetter<>(client);
    }

    public boolean equals(Object other) {
        if (!super.equals(other)) {
            return false;
        }
        S3XoService ta = (S3XoService) other;
        if (null == this.getter || null == ta.getter) {
            return false;
        }
        return this.getter.equals(ta.getter);
    }

    @Override
    public void write(String objKey,
                      InputStream ins,
                      Map<String, Object> meta) {
        long contentLength = -1;
        try {
            contentLength = ins.available();
        }
        catch (IOException e) {
            throw Er.wrap(e);
        }
        // 未知流大小，采用分片上传,或者超过5M文件
        long M5 = 5 * 1024 * 1024L;
        if (contentLength < 0 || contentLength >= M5) {
            multipartWrite(objKey, ins, meta);
            return;
        }

        XoClientWrapper<S3Client> xc = getter.get();
        S3Client client = xc.getClient();
        String bucket = xc.getBucket();
        String objPath = xc.getObjPath(objKey);

        // 准备元数据
        XoMeta xmeta = to_meta_data(meta, true, true);

        // 创建上传请求
        PutObjectRequest req = PutObjectRequest.builder()
            .bucket(bucket)
            .key(objPath)
            .metadata(xmeta.userMeta)
            .contentType(xmeta.mime)
            .contentDisposition(xmeta.title)
            .build();

        // 准备请求体
        RequestBody body = RequestBody.fromInputStream(ins, contentLength);

        // 上传对象
        client.putObject(req, body);

    }

    protected void multipartWrite(String objKey,
                                  InputStream ins,
                                  Map<String, Object> meta) {
        XoClientWrapper<S3Client> xc = getter.get();
        S3Client client = xc.getClient();
        String bucket = xc.getBucket();
        String objPath = xc.getObjPath(objKey);

        // 准备元数据
        XoMeta xmeta = to_meta_data(meta, true, true);

        // 1. 初始化分块上传
        CreateMultipartUploadRequest createReq = CreateMultipartUploadRequest
            .builder()
            .bucket(bucket)
            .key(objPath)
            .metadata(xmeta.userMeta)
            .contentType(xmeta.mime)
            .contentDisposition(xmeta.title)
            .build();

        CreateMultipartUploadResponse createResp = client
            .createMultipartUpload(createReq);
        String uploadId = createResp.uploadId();

        // 2. 分块上传
        List<CompletedPart> completedParts = new ArrayList<>();
        int partNumber = 1;
        byte[] buffer = new byte[5 * 1024 * 1024]; // 5MB 块

        try {
            int bytesRead;
            while ((bytesRead = ins.read(buffer)) > 0) {
                // 上传单个分块
                UploadPartRequest uploadReq = UploadPartRequest.builder()
                    .bucket(bucket)
                    .key(objPath)
                    .uploadId(uploadId)
                    .partNumber(partNumber)
                    .build();

                ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, 0, bytesRead);
                RequestBody body = RequestBody
                    .fromRemainingByteBuffer(byteBuffer);
                UploadPartResponse uploadResp = client.uploadPart(uploadReq,
                                                                  body);

                // 记录完成的分块
                completedParts.add(CompletedPart.builder()
                    .partNumber(partNumber)
                    .eTag(uploadResp.eTag())
                    .build());

                partNumber++;
            }
        }
        catch (IOException e) {
            // 出错时终止上传
            client.abortMultipartUpload(abortReq -> abortReq.bucket(bucket)
                .key(objPath)
                .uploadId(uploadId));
            throw new RuntimeException("Upload failed", e);
        }
        finally {
            Streams.safeClose(ins);
        }

        // 3. 完成分块上传
        CompletedMultipartUpload completedUpload = CompletedMultipartUpload
            .builder()
            .parts(completedParts)
            .build();

        CompleteMultipartUploadRequest completeReq = CompleteMultipartUploadRequest
            .builder()
            .bucket(bucket)
            .key(objPath)
            .uploadId(uploadId)
            .multipartUpload(completedUpload)
            .build();

        client.completeMultipartUpload(completeReq);
    }

    @Override
    public int eachObj(String objKey,
                       boolean delimiterBySlash,
                       int limit,
                       Each<XoBean> callback) {
        String delimiter = delimiterBySlash ? "/" : null;
        XoClientWrapper<S3Client> xc = getter.get();
        S3Client client = xc.getClient();
        String bucket = xc.getBucket();
        String prefix = xc.getQueryPrefix(objKey, delimiter);
        boolean prefix_is_dir = delimiterBySlash
                                && null != prefix
                                && prefix.endsWith(delimiter);

        int count = 0;
        int remaining = limit;
        String next = null;
        do {
            // 计算本次请求的最大结果数
            int maxKeys = Math.min(remaining, 1000);
            ListObjectsV2Request req = ListObjectsV2Request.builder()
                .bucket(bucket)
                .prefix(prefix)
                .delimiter(delimiter)
                .maxKeys(maxKeys)
                .continuationToken(next)
                .build();

            ListObjectsV2Response resp = client.listObjectsV2(req);

            // 处理目录（Common Prefixes）
            // 获取虚拟目录
            if (delimiterBySlash) {
                for (CommonPrefix dir : resp.commonPrefixes()) {
                    XoBean xo = new XoBean();
                    String dirPrefix = dir.prefix();
                    // 查找的就是自己，那么自然什么都不显示
                    if (null != prefix && prefix.equals(dirPrefix)) {
                        continue;
                    }
                    xo.setVirtual(true);
                    String key = xc.toMyKey(dirPrefix);
                    xo.setKey(key);
                    xo.setSize(0L);
                    if (null != callback) {
                        callback.invoke(count, xo, -1);
                    }
                    count++;
                    if (--remaining <= 0)
                        break;
                }
            }

            // 仅仅目录就满了 ??
            if (remaining <= 0) {
                break;
            }

            // 处理文件对象
            for (S3Object s3obj : resp.contents()) {
                // 跳过目录标记对象
                String okey = s3obj.key();
                // 查找的就是自己，那么自然什么都不显示
                if (prefix_is_dir && prefix.equals(okey)) {
                    continue;
                }
                XoBean xo1 = new XoBean();
                String key = xc.toMyKey(okey);
                xo1.setKey(key);
                xo1.setEtag(s3obj.eTag());
                xo1.setSize(s3obj.size());
                xo1.setStorageClass(s3obj.storageClassAsString());
                Date lm = new Date(s3obj.lastModified().toEpochMilli());
                xo1.setLastModified(lm);

                XoBean xo = xo1;
                if (null != callback) {
                    callback.invoke(count, xo, -1);
                }
                count++;

                if (--remaining <= 0)
                    break;
            }

            // 获取下一页的令牌
            next = resp.nextContinuationToken();

        } while (next != null && remaining > 0);

        return count;
    }

    @Override
    public XoBean getObj(String objKey) {
        XoClientWrapper<S3Client> xc = getter.get();
        S3Client client = xc.getClient();
        String bucket = xc.getBucket();
        String key = xc.getObjPath(objKey);

        try {
            HeadObjectRequest req = HeadObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
            HeadObjectResponse resp = client.headObject(req);

            XoBean xo = new XoBean();
            xo.setKey(objKey); // 原始业务 key
            xo.setEtag(resp.eTag()); // ETag（注意 S3 返回时可能带双引号，如需去掉可自行处理）
            xo.setSize(resp.contentLength());
            xo.setLastModified(new Date(resp.lastModified().toEpochMilli()));
            xo.setStorageClass(resp.storageClassAsString());
            xo.putAllUserMeta(resp.metadata());
            xo.setTitle(resp.contentDisposition());
            return xo;
        }
        catch (NoSuchKeyException e) {
            // 404 对象不存在
            return null;
        }
        catch (Exception e) {
            throw Er.wrap(e);
        }
    }

    @Override
    public void appendMeta(String objKey, Map<String, Object> delta) {
        if (delta == null || delta.isEmpty()) {
            return;
        }

        XoClientWrapper<S3Client> xc = getter.get();
        S3Client client = xc.getClient();
        String bucket = xc.getBucket();
        String key = xc.getObjPath(objKey);

        // 解析元数据
        XoMeta xmeta = to_meta_data(delta, true, false);

        // 1. 取当前元数据
        HeadObjectResponse head;
        try {
            head = client.headObject(b -> b.bucket(bucket).key(key));
        }
        catch (NoSuchKeyException e) {
            throw Er.create("e.xo.obj.not.exists", objKey);
        }
        // 恢复默认值
        xmeta.dftMime(head.contentType());
        xmeta.dftTitle(head.contentDisposition());

        // 2. 合并
        Map<String, String> merged = new HashMap<>(head.metadata()); // 只包含用户级
        xmeta.userMeta.forEach((k, v) -> {
            if (v == null) {
                merged.remove(k);
            } else {
                merged.put(k, v.toString());
            }
        });

        // 3. 拷贝到自身，并写入新的元数据
        CopyObjectRequest copyReq = CopyObjectRequest.builder()
            .sourceBucket(bucket)
            .sourceKey(key)
            .destinationBucket(bucket)
            .destinationKey(key)
            .contentType(xmeta.mime)
            .contentDisposition(xmeta.title)
            .metadata(merged)
            .metadataDirective(MetadataDirective.REPLACE) // 关键：用新元数据
            .build();
        client.copyObject(copyReq);
    }

    @Override
    public void copy(String srcKey, String dstKey) {
        // 防空
        if (Ws.isBlank(srcKey) || Ws.isBlank(dstKey) || srcKey.equals(dstKey)) {
            return;
        }

        XoClientWrapper<S3Client> xc = getter.get();
        S3Client client = xc.getClient();
        String bucket = xc.getBucket();
        String src = xc.getObjPath(srcKey);
        String dst = xc.getObjPath(dstKey);

        // 1. 取当前元数据
        HeadObjectResponse head;
        try {
            head = client.headObject(b -> b.bucket(bucket).key(src));
        }
        catch (NoSuchKeyException e) {
            throw Er.create("e.xo.obj.not.exists", src);
        }
        

        // 3. 拷贝到自身，并写入新的元数据
        CopyObjectRequest copyReq = CopyObjectRequest.builder()
            .sourceBucket(bucket)
            .sourceKey(src)
            .destinationBucket(bucket)
            .destinationKey(dst)
            .contentType(head.contentType())
            .contentDisposition(head.contentDisposition())
            .metadata(head.metadata())
            .build();
        client.copyObject(copyReq);
    }

    @Override
    public void renameObj(String oldKey, String newKey) {
        XoClientWrapper<S3Client> xc = getter.get();
        S3Client client = xc.getClient();
        String bucket = xc.getBucket();
        String src = xc.getObjPath(oldKey);
        String dst = xc.getObjPath(newKey);

        try {
            HeadObjectResponse head = client
                .headObject(b -> b.bucket(bucket).key(src));

            client.copyObject(CopyObjectRequest.builder()
                .sourceBucket(bucket)
                .sourceKey(src)
                .destinationBucket(bucket)
                .destinationKey(dst)
                .metadata(head.metadata())
                .metadataDirective(MetadataDirective.REPLACE)
                .build());
            client.deleteObject(b -> b.bucket(bucket).key(src));
        }
        catch (NoSuchKeyException e) {
            throw Er.create("e.xo.obj.not.exists", oldKey);
        }
    }

    @Override
    public InputStream read(String objKey) {
        XoClientWrapper<S3Client> xc = getter.get();
        S3Client client = xc.getClient();
        String bucket = xc.getBucket();
        String objPath = xc.getObjPath(objKey);

        GetObjectRequest req = GetObjectRequest.builder()
            .bucket(bucket)
            .key(objPath)
            .build();
        ResponseInputStream<GetObjectResponse> resp = client.getObject(req);
        return resp;
    }

    @Override
    public void deleteObj(String objKey) {
        XoClientWrapper<S3Client> xc = getter.get();
        S3Client client = xc.getClient();
        String bucket = xc.getBucket();
        String objPath = xc.getObjPath(objKey);

        DeleteObjectRequest req = DeleteObjectRequest.builder()
            .bucket(bucket)
            .key(objPath)
            .build();

        client.deleteObject(req);
    }

}
