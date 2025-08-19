package com.site0.walnut.ext.xo.impl.cos;

import java.io.InputStream;
import java.io.Reader;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.nutz.lang.Streams;
import org.nutz.lang.util.ByteInputStream;
import org.nutz.lang.util.NutBean;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectSummary;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.ListObjectsRequest;
import com.qcloud.cos.model.ObjectListing;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.xo.bean.XoBean;
import com.site0.walnut.ext.xo.util.XoClientWrapper;
import com.site0.walnut.ext.xo.util.XoClients;
import com.site0.walnut.util.Wlang;

/**
 * 参见文档 <code>ex-storage.md</code>
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class CosXoService {

    private XoClientWrapper<COSClient> xc;

    public CosXoService(String name, WnIo io, WnObj oHome) {
        this.xc = XoClients.COS.getClient(io, oHome, name);
    }

    public void write(String objKey, String text, NutBean meta) {
        InputStream ins = Wlang.ins(text);
        write(objKey, ins, meta);
    }

    public void write(String objKey, byte[] bs, NutBean meta) {
        InputStream ins = new ByteInputStream(bs);
        write(objKey, ins, meta);
    }

    public void write(String objKey, InputStream ins, NutBean meta) {
        ObjectMetadata metaData = new ObjectMetadata();
        if (null != meta && !meta.isEmpty()) {
            for (Map.Entry<String, Object> en : meta.entrySet()) {
                String key = en.getKey();
                Object val = en.getValue();
                if (null != val) {
                    metaData.addUserMetadata(key, val.toString());
                }
            }
        }
        COSClient client = xc.getClient();
        String bucket = xc.getBucket();
        String obj_path = xc.getObjPath(objKey);

        PutObjectRequest req = new PutObjectRequest(bucket, obj_path, ins, metaData);
        client.putObject(req);
    }

    public List<XoBean> listObj(String objKey) {
        COSClient client = xc.getClient();
        String bucket = xc.getBucket();
        String obj_path = xc.getObjPath(objKey);

        if (null != objKey && objKey.endsWith("/") && !obj_path.endsWith("/")) {
            obj_path = obj_path + "/";
        }

        ListObjectsRequest req = new ListObjectsRequest();
        req.setBucketName(bucket);
        req.setPrefix(obj_path);
        req.setDelimiter("/");
        req.setMaxKeys(1000);

        List<XoBean> list = new LinkedList<>();
        ObjectListing ing = null;
        do {
            ing = client.listObjects(req);

            // common prefix 表示被 delimiter 截断的路径,
            // 如 delimter 设置为/, common prefix
            // 则表示所有子目录的路径
            // List<String> commonPrefixs = ing.getCommonPrefixes();

            // object summary 表示所有列出的 object 列表
            List<COSObjectSummary> summaries = ing.getObjectSummaries();
            for (COSObjectSummary osum : summaries) {
                XoBean xo = new XoBean();
                String key = xc.toMyKey(osum.getKey());
                xo.setKey(key);
                xo.setEtag(osum.getETag());
                xo.setSize(osum.getSize());
                xo.setStorageClass(osum.getStorageClass());
                xo.setLastModified(osum.getLastModified());
                list.add(xo);
            }

            // 下一页
            String nextMarker = ing.getNextMarker();
            req.setMarker(nextMarker);

        } while (ing.isTruncated());

        return list;
    }

    public InputStream read(String objKey) {
        COSClient client = xc.getClient();
        String bucket = xc.getBucket();
        String obj_path = xc.getObjPath(objKey);

        GetObjectRequest getObjectRequest = new GetObjectRequest(bucket, obj_path);
        COSObject xo = client.getObject(getObjectRequest);
        return xo.getObjectContent();
    }

    public String readText(String objKey) {
        InputStream ins = read(objKey);
        Reader r = Streams.utf8r(ins);
        return Streams.readAndClose(r);
    }

    public void deleteObj(String objKey) {
        COSClient client = xc.getClient();
        String bucket = xc.getBucket();
        String obj_path = xc.getObjPath(objKey);
        client.deleteObject(bucket, obj_path);
    }

    public void clear(String objKey) {
        List<XoBean> list = this.listObj(objKey);
        for (XoBean li : list) {
            this.deleteObj(li.getKey());
        }
    }

}
