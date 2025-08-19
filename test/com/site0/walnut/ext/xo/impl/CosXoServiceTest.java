package com.site0.walnut.ext.xo.impl.cos;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import org.junit.Test;
import org.nutz.json.Json;
import org.nutz.lang.Files;
import org.nutz.lang.util.NutMap;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicSessionCredentials;
import com.qcloud.cos.model.COSObjectSummary;
import com.qcloud.cos.model.ListObjectsRequest;
import com.qcloud.cos.model.ObjectListing;
import com.qcloud.cos.region.Region;
import com.site0.walnut.BaseSessionTest;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.ext.xo.bean.XoBean;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Ws;
import com.tencent.cloud.CosStsClient;
import com.tencent.cloud.Response;
import com.qcloud.cos.auth.COSCredentials;

public class CosXoServiceTest extends BaseSessionTest {

    private static final String conf_json;

    static {
        conf_json = Files.read("com/site0/walnut/ext/xo/impl/conf/cos_test.json");
    }

    @Override
    protected void on_before() {
        super.on_before();
    }

    protected CosXoService service() {
        return service(null, null);
    }

    protected CosXoService service(String prefix) {
        return service(prefix, null);
    }

    protected CosXoService service(String prefix, String[] allowActions) {
        // 准备域目录
        WnObj oIo = io.createIfNoExists(oMyHome, ".io", WnRace.DIR);
        io.createIfNoExists(oMyHome, ".domain/xo_token", WnRace.DIR);

        // 创建配置文件
        String confName = "test";
        WnObj oConf = io.create(oIo, "cos/" + confName + ".json5", WnRace.FILE);

        // 写入配置文件内容
        NutMap conf = Json.fromJson(NutMap.class, conf_json);
        conf.put("secretId", setup.getConifg("cos-secret-id"));
        conf.put("secretKey", setup.getConifg("cos-secret-key"));
        conf.put("bucket", setup.getConifg("cos-bucket"));
        if (!Ws.isBlank(prefix)) {
            conf.put("prefix", prefix);
        }
        if (null != allowActions && allowActions.length > 0) {
            conf.put("allowActions", allowActions);
        }

        String json = Json.toJson(conf);
        io.writeText(oConf, json);

        // 准备服务类
        return new CosXoService(confName, io, oMyHome);
    }

    @Test
    public void test_00() throws IOException {
        CosXoService cosx = service();

        String key = "pet/a.pet.txt";

        // 首先清除数据
        cosx.clear("pet/");

        List<XoBean> list = cosx.listObj("pet/");
        assertEquals(0, list.size());

        // 创建
        cosx.write(key, "I am A", null);

        // 查询
        list = cosx.listObj("pet/");
        assertEquals(1, list.size());

        // 读取
        String str = cosx.readText(key);
        assertEquals("I am A", str);

        // 覆盖
        cosx.write(key, "I am B", null);
        str = cosx.readText(key);
        assertEquals("I am B", str);

        // 删除
        cosx.deleteObj(key);

        // 确保删除了
        list = cosx.listObj("pet/");
        assertEquals(0, list.size());
    }

    @Test
    public void test_01() throws IOException {
        CosXoService cosx = service("pet/");

        String key = "a.pet.txt";

        // 首先清除数据
        cosx.clear("*");

        List<XoBean> list = cosx.listObj("*");
        assertEquals(0, list.size());

        // 创建
        cosx.write(key, "I am A", null);

        // 查询
        list = cosx.listObj("*");
        assertEquals(1, list.size());
        assertEquals(key, list.get(0).getKey());

        // 读取
        String str = cosx.readText(key);
        assertEquals("I am A", str);

        // 覆盖
        cosx.write(key, "I am B", null);
        str = cosx.readText(key);
        assertEquals("I am B", str);

        // 删除
        cosx.deleteObj(key);

        // 确保删除了
        list = cosx.listObj(null);
        assertEquals(0, list.size());
    }

    @Test
    public void test_02() throws IOException {
        CosXoService cosx = service("pet/");

        String key1 = "a/pet1.txt";
        String key2 = "a/pet2.txt";
        String key3 = "a/pet3.txt";

        // 首先清除数据
        cosx.clear("*");

        List<XoBean> list = cosx.listObj("*");
        assertEquals(0, list.size());

        // 创建
        cosx.write(key1, "I am A", null);
        cosx.write(key2, "I am B", null);
        cosx.write(key3, "I am C", null);

        // 查询
        list = cosx.listObj("a/");
        assertEquals(3, list.size());
        assertEquals(key1, list.get(0).getKey());
        assertEquals(key2, list.get(1).getKey());
        assertEquals(key3, list.get(2).getKey());

        // 读取
        String str1 = cosx.readText(key1);
        assertEquals("I am A", str1);
        String str2 = cosx.readText(key2);
        assertEquals("I am B", str2);
        String str3 = cosx.readText(key3);
        assertEquals("I am C", str3);
    }

    /**
     * 这段代码是整个接口的标准流程示例
     */
    void raw_example() throws IOException {
        // List<XoBean> beans = cosx.listObj("pet");
        TreeMap<String, Object> config = new TreeMap<String, Object>();
        config.put("secretId", setup.getConifg("cos-secret-id"));
        config.put("secretKey", setup.getConifg("cos-secret-key"));
        config.put("bucket", setup.getConifg("cos-bucket"));
        config.put("region", setup.getConifg("cos-region"));
        config.put("durationSeconds", 1000);
        // config.put("allowPrefixes", Wlang.array("*"));
        // config.put("allowActions", Wlang.array("*"));
        config.put("allowPrefixes", Wlang.array("pet/*"));
        config.put("allowActions", Wlang.array("*"));
        System.out.printf("Example:\n%s", Json.toJson(config));
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
        req.setPrefix("pet/");
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
                xo.setKey(osum.getKey());
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

        assertEquals(1, list.size());
    }

}
