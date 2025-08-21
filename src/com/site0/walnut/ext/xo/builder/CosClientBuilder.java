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
import com.site0.walnut.util.Ws;
import com.tencent.cloud.CosStsClient;
import com.tencent.cloud.Response;

public class CosClientBuilder extends AbstractXoClientBuilder<COSClient> {

    // -------------------------------------
    // 临时获取的
    // -------------------------------------
    private String tmpSecretId;
    private String tmpSecretKey;
    private String sessionToken;

    // -------------------------------------
    // 配置信息
    // -------------------------------------
    private String secretId;
    private String secretKey;
    private String bucket;
    private String region;
    private String prefix;
    private int duration;
    private String[] allowPrefixes;
    private String[] allowActions;

    public CosClientBuilder(WnIo io, WnObj oHome, String name) {
        super(io, oHome, name);
    }

    public CosClientBuilder(WnIo io, String homePath) {
        super(io, io.fetch(null, homePath), null);
    }

    protected String getTokenPath() {
        if (Ws.isBlank(name)) {
            return null;
        }
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
        String tkPath = getTokenPath();
        // -------------------------------------
        // 直接设置了临时权鉴，可以直接用
        // -------------------------------------
        if (!Ws.isBlank(tmpSecretId)
            && !Ws.isBlank(tmpSecretKey)
            && !Ws.isBlank(sessionToken)) {
            // 返回客户端
            return create_client_by_tmp_secret();
        }
        // -------------------------------------
        // 直接设置了长期权鉴，换取临时权鉴后，可以使用
        // -------------------------------------
        if (!Ws.isBlank(secretId)
            && !Ws.isBlank(secretKey)
            && !Ws.isBlank(bucket)
            && !Ws.isBlank(region)) {
            // 读取临时票据
            prepare_tmp_secret();
            // 缓存权鉴票据
            save_token_cache(tkPath);
            // 返回客户端
            return create_client_by_tmp_secret();
        }
        // -------------------------------------
        // 缓冲 Token 的模式，那么就是通常的动态获取方式
        // -------------------------------------
        WnObj oToken = null;
        // 读取缓存的权鉴票据
        if (!Ws.isBlank(tkPath)) {
            oToken = io.fetch(oHome, tkPath);
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
        }
        // 从配置文件重新加载
        if (null == oToken) {
            // 读取配置文件
            load_from_config();
            // 读取临时票据
            prepare_tmp_secret();
            // 缓存权鉴票据
            save_token_cache(tkPath);
        }

        return create_client_by_tmp_secret();
    }

    private void load_from_config() throws IOException {
        // 读取配置信息
        String confPath = getConfigPath(name);
        WnObj oConf = io.check(oHome, confPath);
        NutMap props = io.readJson(oConf, NutMap.class);

        // 初始化配置对象
        loadConfig(props);
    }

    public void loadConfig(NutMap props) {
        this.secretId = props.getString("secretId");
        this.secretKey = props.getString("secretKey");
        this.duration = props.getInt("duration", 1800);
        this.bucket = props.getString("bucket");
        this.region = props.getString("region");
        this.prefix = XoClientWrapper.tidyPrefix(props.getString("prefix", ""));

        this.allowPrefixes = re.getAllowPrefixes();
        this.allowActions = props.getArray("allowActions", String.class);
        if (null == allowActions || allowActions.length == 0) {
            allowActions = Wlang.array("*");
        }
    }

    private void prepare_tmp_secret() throws IOException {
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
    }

    private void save_token_cache(String tkPath) {
        // 防空
        if (Ws.isBlank(tkPath))
            return;
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
        WnObj oToken = io.createIfExists(oHome, tkPath, WnRace.FILE);
        io.appendMeta(oToken, meta);
    }

    private XoClientWrapper<COSClient> create_client_by_tmp_secret() {
        // 创建客户端
        COSCredentials cred = new BasicSessionCredentials(tmpSecretId,
                                                          tmpSecretKey,
                                                          sessionToken);
        Region _region = new Region(region);
        ClientConfig cconf = new ClientConfig(_region);
        COSClient cos = new COSClient(cred, cconf);
        re.setClient(cos);
        re.setBucket(bucket);
        re.setRegion(region);
        re.setPrefix(prefix);
        // 过期时间提早 10 秒
        long now = System.currentTimeMillis();
        re.setExpiredAt(now + duration * 1000L - 10000L);
        return re;
    }

    public String getTmpSecretId() {
        return tmpSecretId;
    }

    public void setTmpSecretId(String tmpSecretId) {
        this.tmpSecretId = tmpSecretId;
    }

    public String getTmpSecretKey() {
        return tmpSecretKey;
    }

    public void setTmpSecretKey(String tmpSecretKey) {
        this.tmpSecretKey = tmpSecretKey;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public String getSecretId() {
        return secretId;
    }

    public void setSecretId(String secretId) {
        this.secretId = secretId;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String[] getAllowPrefixes() {
        return allowPrefixes;
    }

    public void setAllowPrefixes(String[] allowPrefixes) {
        this.allowPrefixes = allowPrefixes;
    }

    public String[] getAllowActions() {
        return allowActions;
    }

    public void setAllowActions(String[] allowActions) {
        this.allowActions = allowActions;
    }

}