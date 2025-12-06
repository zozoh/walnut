package com.site0.walnut.ext.xo.impl;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import org.nutz.json.Json;
import org.nutz.lang.util.NutMap;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicSessionCredentials;
import com.qcloud.cos.model.COSObjectSummary;
import com.qcloud.cos.model.ListObjectsRequest;
import com.qcloud.cos.model.ObjectListing;
import com.qcloud.cos.region.Region;
import com.site0.walnut.ext.xo.bean.XoBean;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Ws;
import com.tencent.cloud.CosStsClient;
import com.tencent.cloud.Response;
import com.qcloud.cos.auth.COSCredentials;

public class CosXoServiceTest extends AbstractXoServiceTest {

    @Override
    protected String getConfCateName() {
        return "cos";
    }

    @Override
    protected void update_config(String prefix, String[] allowActions, NutMap conf) {
        conf.put("secretId", setup.getConifg("cos-secret-id"));
        conf.put("secretKey", setup.getConifg("cos-secret-key"));
        conf.put("bucket", setup.getConifg("cos-bucket"));
        conf.put("region", setup.getConifg("cos-region"));
        if (!Ws.isBlank(prefix)) {
            conf.put("prefix", prefix);
        }
        if (null != allowActions && allowActions.length > 0) {
            conf.put("allowActions", allowActions);
        }
    }

    @Override
    protected XoService create_service(String confName) {
        return new CosXoService(io, oMyHome, confName);
    }

    /**
     * 这段代码是整个接口的标准流程示例
     */
    // @org.junit.Test
    public void raw_example() throws IOException {
        // List<XoBean> beans = cosx.listObj("pet");
        TreeMap<String, Object> config = new TreeMap<String, Object>();
        config.put("secretId", setup.getConifg("cos-secret-id"));
        config.put("secretKey", setup.getConifg("cos-secret-key"));
        config.put("bucket", setup.getConifg("cos-bucket"));
        config.put("region", setup.getConifg("cos-region"));
        config.put("durationSeconds", 1000);
        config.put("allowPrefixes", Wlang.array("*"));
        // config.put("allowActions", Wlang.array("*"));
        //config.put("allowPrefixes", Wlang.array("folder/"));
        config.put("allowActions", Wlang.array("*"));
        System.out.printf("Example:\n%s\n\n", Json.toJson(config));
        Response resp = CosStsClient.getCredential(config);
        String secretId = resp.credentials.tmpSecretId;
        String secretKey = resp.credentials.tmpSecretKey;
        String sessionToken = resp.credentials.sessionToken;

        COSCredentials cred = new BasicSessionCredentials(secretId, secretKey, sessionToken);
        Region region = new Region("ap-nanjing");
        ClientConfig cc = new ClientConfig(region);
        COSClient client = new COSClient(cred, cc);

        ListObjectsRequest req = new ListObjectsRequest();
        req.setBucketName("dev-test-1251394887");
        req.setPrefix("folder/aaa/bbb/ccc/");
       req.setDelimiter("/");
        req.setMaxKeys(1000);
        List<XoBean> list = new LinkedList<>();
        ObjectListing ing = null;
        do {
            ing = client.listObjects(req);

            // common prefix 表示被 delimiter 截断的路径,
            // 如 delimter 设置为/, common prefix
            // 则表示所有子目录的路径
            for (String dir : ing.getCommonPrefixes()) {
                XoBean xo = new XoBean();
                xo.setKey(dir);          // 例如 folder/aaa/
                xo.setSize(0L);
                System.out.printf("[D]%d) %s\n", list.size(), xo);
                list.add(xo);
            }

            // object summary 表示所有列出的 object 列表
            List<COSObjectSummary> summaries = ing.getObjectSummaries();
            for (COSObjectSummary osum : summaries) {
                XoBean xo = new XoBean();
                xo.setKey(osum.getKey());
                xo.setEtag(osum.getETag());
                xo.setSize(osum.getSize());
                xo.setStorageClass(osum.getStorageClass());
                xo.setLastModified(osum.getLastModified());
                System.out.printf("[F]%d) %s\n", list.size(), xo);
                list.add(xo);
            }

            // 下一页
            String nextMarker = ing.getNextMarker();
            req.setMarker(nextMarker);

        } while (ing.isTruncated());

        assertEquals(1, list.size());
    }

}
