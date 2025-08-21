package com.site0.walnut.ext.xo.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nutz.lang.Each;
import org.nutz.lang.Streams;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.model.AbortMultipartUploadRequest;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectSummary;
import com.qcloud.cos.model.CompleteMultipartUploadRequest;
import com.qcloud.cos.model.CopyObjectRequest;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.InitiateMultipartUploadRequest;
import com.qcloud.cos.model.InitiateMultipartUploadResult;
import com.qcloud.cos.model.ListObjectsRequest;
import com.qcloud.cos.model.ObjectListing;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PartETag;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.UploadPartRequest;
import com.qcloud.cos.model.UploadPartResult;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.xo.bean.XoBean;
import com.site0.walnut.ext.xo.util.WnIoXoClientGetter;
import com.site0.walnut.ext.xo.util.WnSimpleClientGetter;
import com.site0.walnut.ext.xo.util.XoClientGetter;
import com.site0.walnut.ext.xo.util.XoClientWrapper;
import com.site0.walnut.ext.xo.util.XoClients;

/**
 * 参见文档 <code>ex-storage.md</code>
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class CosXoService extends AbstractXoService {

    private XoClientGetter<COSClient> getter;

    public CosXoService(WnIo io, WnObj oHome, String name) {
        WnIoXoClientGetter<COSClient> getter = new WnIoXoClientGetter<>();
        getter.setIo(io);
        getter.setHome(oHome);
        getter.setName(name);
        getter.setManager(XoClients.COS);
        this.getter = getter;
    }

    public CosXoService(XoClientGetter<COSClient> getter) {
        this.getter = getter;
    }

    public CosXoService(XoClientWrapper<COSClient> client) {
        this.getter = new WnSimpleClientGetter<>(client);
    }

    public boolean equals(Object other) {
        if (!super.equals(other)) {
            return false;
        }
        CosXoService ta = (CosXoService) other;
        if (null == this.getter || null == ta.getter) {
            return false;
        }
        return this.getter.equals(ta.getter);
    }

    private ObjectMetadata __to_cos_meta_data(Map<String, Object> meta) {
        XoMeta xmeta = to_meta_data(meta, false, true);
        ObjectMetadata md = new ObjectMetadata();
        __join_meta_data(md, xmeta);
        return md;
    }

    private ObjectMetadata __join_meta_data(ObjectMetadata md, XoMeta xmeta) {
        if (null != xmeta.mime) {
            md.setContentType(xmeta.mime);
        }
        if (null != xmeta.title) {
            md.setContentDisposition(xmeta.title);
        }
        if (null != xmeta.sha1) {
            md.setETag(xmeta.sha1);
        }
        if (null != xmeta.userMeta && !xmeta.userMeta.isEmpty()) {
            md.setUserMetadata(xmeta.userMeta);
        }
        return md;
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

        ObjectMetadata metaData = __to_cos_meta_data(meta);
        XoClientWrapper<COSClient> xc = getter.get();
        COSClient client = xc.getClient();
        String bucket = xc.getBucket();
        String obj_path = xc.getObjPath(objKey);

        PutObjectRequest req = new PutObjectRequest(bucket,
                                                    obj_path,
                                                    ins,
                                                    metaData);
        client.putObject(req);
    }

    protected void multipartWrite(String objKey,
                                  InputStream ins,
                                  Map<String, Object> meta) {
        XoClientWrapper<COSClient> xc = getter.get();
        COSClient client = xc.getClient();
        String bucket = xc.getBucket();
        String objPath = xc.getObjPath(objKey);

        // 准备元数据
        ObjectMetadata metaData = __to_cos_meta_data(meta);

        // 1. 初始化分块上传
        InitiateMultipartUploadRequest initReq = new InitiateMultipartUploadRequest(bucket,
                                                                                    objPath,
                                                                                    metaData);
        InitiateMultipartUploadResult initResp = client
            .initiateMultipartUpload(initReq);
        String uploadId = initResp.getUploadId();

        // 2. 分块上传
        List<PartETag> partETags = new ArrayList<>();
        int partNumber = 1;
        int partSize = 5 * 1024 * 1024; // 5MB
        byte[] buffer = new byte[partSize];

        try {
            int bytesRead;
            while ((bytesRead = ins.read(buffer)) > 0) {
                // 腾讯云COS要求每次上传必须是完整的partSize（除了最后一块）
                // 所以我们需要复制实际读取的字节
                byte[] partData = new byte[bytesRead];
                System.arraycopy(buffer, 0, partData, 0, bytesRead);
                InputStream partInputStream = new java.io.ByteArrayInputStream(partData);

                UploadPartRequest uploadRequest = new UploadPartRequest();
                uploadRequest.setBucketName(bucket);
                uploadRequest.setKey(objPath);
                uploadRequest.setUploadId(uploadId);
                uploadRequest.setPartNumber(partNumber);
                uploadRequest.setInputStream(partInputStream);
                uploadRequest.setPartSize(bytesRead);

                UploadPartResult uploadResult = client
                    .uploadPart(uploadRequest);
                partETags.add(uploadResult.getPartETag());

                partNumber++;
            }
        }
        catch (IOException e) {
            // 出错时终止上传
            client
                .abortMultipartUpload(new AbortMultipartUploadRequest(bucket,
                                                                      objPath,
                                                                      uploadId));
            throw new RuntimeException("Upload failed", e);
        }
        finally {
            Streams.safeClose(ins);
        }

        // 3. 完成分块上传
        CompleteMultipartUploadRequest compReq = new CompleteMultipartUploadRequest(bucket,
                                                                                    objPath,
                                                                                    uploadId,
                                                                                    partETags);
        client.completeMultipartUpload(compReq);
    }

    @Override
    public int eachObj(String objKey,
                       boolean delimiterBySlash,
                       int limit,
                       Each<XoBean> callback) {
        String delimiter = delimiterBySlash ? "/" : null;
        XoClientWrapper<COSClient> xc = getter.get();
        COSClient client = xc.getClient();
        String bucket = xc.getBucket();
        String prefix = xc.getQueryPrefix(objKey, delimiter);
        boolean prefix_is_dir = delimiterBySlash
                                && null != prefix
                                && prefix.endsWith(delimiter);

        int count = 0;
        int remaining = limit;
        String next = null;
        do {
            int maxKeys = Math.min(remaining, 1000);
            ListObjectsRequest req = new ListObjectsRequest();
            req.setBucketName(bucket);
            req.setPrefix(prefix);
            req.setDelimiter(delimiter);
            req.setMarker(next);
            req.setMaxKeys(maxKeys);

            ObjectListing ing = client.listObjects(req);

            // 获取虚拟目录
            if (delimiterBySlash) {
                for (String dir : ing.getCommonPrefixes()) {
                    XoBean xo = new XoBean();
                    // 查找的就是自己，那么自然什么都不显示
                    if (null != prefix && prefix.equals(dir)) {
                        continue;
                    }
                    xo.setVirtual(true);
                    String key = xc.toMyKey(dir);
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

            // object summary 表示所有列出的 object 列表
            List<COSObjectSummary> summaries = ing.getObjectSummaries();
            for (COSObjectSummary osum : summaries) {
                XoBean xo1 = new XoBean();
                String okey = osum.getKey();
                // 查找的就是自己，那么自然什么都不显示
                if (prefix_is_dir && prefix.equals(okey)) {
                    continue;
                }
                String key = xc.toMyKey(okey);
                xo1.setKey(key);
                xo1.setEtag(osum.getETag());
                xo1.setSize(osum.getSize());
                xo1.setStorageClass(osum.getStorageClass());
                xo1.setLastModified(osum.getLastModified());
                XoBean xo = xo1;
                if (null != callback) {
                    callback.invoke(count, xo, -1);
                }
                count++;
                if (--remaining <= 0)
                    break;
            }

            // 下一页
            next = ing.getNextMarker();

        } while (next != null && remaining > 0);

        return count;
    }

    @Override
    public XoBean getObj(String objKey) {
        XoClientWrapper<COSClient> xc = getter.get();
        COSClient client = xc.getClient();
        String bucket = xc.getBucket();
        String objPath = xc.getObjPath(objKey);

        try {
            ObjectMetadata meta = client.getObjectMetadata(bucket, objPath);

            XoBean xo = new XoBean();
            xo.setKey(objKey); // 原始业务 key
            xo.setEtag(meta.getETag()); // ETag
            xo.setSize(meta.getContentLength()); // 大小
            xo.setLastModified(meta.getLastModified());
            xo.setStorageClass(meta.getStorageClass());
            xo.putAllRawMeta(meta.getRawMetadata());
            xo.putAllUserMeta(meta.getUserMetadata());
            return xo;
        }
        catch (CosServiceException e) {
            if (e.getStatusCode() == 404) {
                return null;
            }
            throw Er.wrap(e);
        }
    }

    @Override
    public void appendMeta(String objKey, Map<String, Object> delta) {
        if (delta == null || delta.isEmpty()) {
            return;
        }

        // 1. 取当前元数据
        XoClientWrapper<COSClient> xc = getter.get();
        COSClient client = xc.getClient();
        String bucket = xc.getBucket();
        String key = xc.getObjPath(objKey);

        // 解析元数据
        XoMeta xmeta = to_meta_data(delta, true, false);

        ObjectMetadata oldMeta;
        try {
            oldMeta = client.getObjectMetadata(bucket, key);
        }
        catch (CosServiceException e) {
            if (e.getStatusCode() == 404) {
                throw Er.create("e.xo.obj.not.exists", objKey);
            }
            throw Er.wrap(e);
        }

        // 2. 合并：先放旧的，再覆盖/删除新的
        Map<String, String> merged = new HashMap<>(oldMeta.getUserMetadata());
        xmeta.userMeta.forEach((k, v) -> {
            if (v == null) {
                merged.remove(k);
            } else {
                merged.put(k, v.toString());
            }
        });

        // 3. 构造新的 ObjectMetadata（仅保留用户元数据）
        ObjectMetadata freshMeta = new ObjectMetadata();
        xmeta.dftMime(oldMeta.getContentType());
        xmeta.dftTitle(oldMeta.getContentDisposition());
        xmeta.dftSha1(oldMeta.getETag());
        __join_meta_data(freshMeta, xmeta);
        freshMeta.setUserMetadata(merged);

        // 4. 执行“拷贝到自身”
        CopyObjectRequest copyReq = new CopyObjectRequest(bucket,
                                                          key,
                                                          bucket,
                                                          key);
        copyReq.setNewObjectMetadata(freshMeta);
        client.copyObject(copyReq);
    }

    @Override
    public void renameObj(String oldKey, String newKey) {
        XoClientWrapper<COSClient> xc = getter.get();
        COSClient client = xc.getClient();
        String bucket = xc.getBucket();
        String src = xc.getObjPath(oldKey);
        String dst = xc.getObjPath(newKey);

        try {
            ObjectMetadata meta = client.getObjectMetadata(bucket, src);
            client.copyObject(new CopyObjectRequest(bucket, src, bucket, dst)
                .withNewObjectMetadata(meta));
            client.deleteObject(bucket, src);
        }
        catch (CosServiceException e) {
            if (e.getStatusCode() == 404) {
                throw Er.create("e.xo.obj.not.exists", oldKey);
            }
            throw Er.wrap(e);
        }

    }

    @Override
    public InputStream read(String objKey) {
        XoClientWrapper<COSClient> xc = getter.get();
        COSClient client = xc.getClient();
        String bucket = xc.getBucket();
        String obj_path = xc.getObjPath(objKey);

        GetObjectRequest getObjectRequest = new GetObjectRequest(bucket,
                                                                 obj_path);
        COSObject xo = client.getObject(getObjectRequest);
        return xo.getObjectContent();
    }

    @Override
    public void deleteObj(String objKey) {
        XoClientWrapper<COSClient> xc = getter.get();
        COSClient client = xc.getClient();
        String bucket = xc.getBucket();
        String obj_path = xc.getObjPath(objKey);
        client.deleteObject(bucket, obj_path);
    }

}
