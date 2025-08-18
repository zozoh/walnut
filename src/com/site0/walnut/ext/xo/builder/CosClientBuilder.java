package com.site0.walnut.ext.xo.builder;

import java.io.IOException;
import java.util.TreeMap;

import org.nutz.lang.util.NutMap;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicSessionCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.region.Region;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.ext.xo.util.XoClientWrapper;
import com.site0.walnut.util.Wlang;
import com.tencent.cloud.CosStsClient;
import com.tencent.cloud.Response;

public class CosClientBuilder extends AbstractXoClientBuilder<COSClient> {

    protected String tmpSecretId;
    protected String tmpSecretKey;
    protected String sessionToken;

    protected String bucket;
    protected String region;
    protected String prefix;
    protected int duration;

    public CosClientBuilder(WnIo io, WnObj oHome, String name) {
        super(io, oHome, name);
    }

    protected String getTokenPath() {
        return ".domain/xo_token/cos_" + name;
    }

    protected String getConfigPath(String name) {
        return ".io/cos/" + name + ".json5";
    }

    protected XoClientWrapper<COSClient> createClient(String clientKey) {
        return new XoClientWrapper<COSClient>(clientKey) {
            protected void _close_client(COSClient client) {
                client.shutdown();
            }
        };
    }

    @Override
    public XoClientWrapper<COSClient> build() throws IOException {
        // 读取缓存的权鉴票据
        String tkPath = getTokenPath();
        WnObj oToken = io.fetch(oHome, tkPath);
        if (null != oToken) {
            tmpSecretId = oToken.getString("secretId");
            tmpSecretKey = oToken.getString("secretKey");
            sessionToken = oToken.getString("sessionToken");
            bucket = oToken.getString("bucket");
            region = oToken.getString("region");
            prefix = oToken.getString("prefix", null);
            re.setBucket(bucket);
            re.setRegion(region);
            re.setPrefix(prefix);
            re.setExpiredAt(oToken.expireTime());

        }
        // 从配置文件重新加载
        else {
            load_from_config();
        }

        // 创建客户端
        COSCredentials cred = new BasicSessionCredentials(tmpSecretId, tmpSecretKey, sessionToken);
        Region cregion = new Region(region);
        ClientConfig cconf = new ClientConfig(cregion);
        COSClient cos = new COSClient(cred, cconf);
        re.setClient(cos);
        return re;
    }

    public void load_from_config() throws IOException {
        // 读取配置信息
        String confPath = getConfigPath(name);
        WnObj oConf = io.check(oHome, confPath);
        NutMap props = io.readJson(oConf, NutMap.class);

        // 初始化配置对象
        String secretId = props.getString("secretId");
        String secretKey = props.getString("secretKey");
        duration = props.getInt("duration", 1800);
        bucket = props.getString("bucket");
        region = props.getString("region");

        re.setBucket(bucket);
        re.setRegion(region);
        re.setPrefix(props.getString("prefix", ""));
        prefix = re.getPrefix();

        String[] allowPrefixes = re.getAllowPrefixes();
        String[] allowActions = props.getArray("allowActions", String.class);
        if (null == allowActions || allowActions.length == 0) {
            allowActions = Wlang.array("*");
        }
        TreeMap<String, Object> config = new TreeMap<>();
        config.put("secretId", secretId);
        config.put("secretKey", secretKey);
        config.put("durationSeconds", duration);
        config.put("bucket", bucket);
        config.put("region", region);
        config.put("allowPrefixes", allowPrefixes);
        config.put("allowActions", allowActions);

        // 获取最新的 secretId/Key/Token
        Response resp = CosStsClient.getCredential(config);

        tmpSecretId = resp.credentials.tmpSecretId;
        tmpSecretKey = resp.credentials.tmpSecretKey;
        sessionToken = resp.credentials.sessionToken;

        long now = System.currentTimeMillis();
        re.setExpiredAt(now + duration * 1000L);

        // 缓存权鉴票据
        NutMap meta = new NutMap();
        meta.put("secretId", tmpSecretId);
        meta.put("secretKey", tmpSecretKey);
        meta.put("sessionToken", sessionToken);
        meta.put("bucket", bucket);
        meta.put("region", region);
        if (re.hasPrefix()) {
            meta.put("prefix", re.getPrefix());
        }
        meta.put("expi", re.getExpiredAt());
        String tkPath = getTokenPath();
        WnObj oToken = io.createIfExists(oHome, tkPath, WnRace.FILE);
        io.appendMeta(oToken, meta);
    }

}