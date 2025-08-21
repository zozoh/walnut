package com.site0.walnut.ext.xo.builder;

import java.io.IOException;

import org.nutz.lang.util.NutMap;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.xo.util.XoClientWrapper;
import com.site0.walnut.util.Ws;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

public class S3LongTermClientBuilder extends AbstractXoClientBuilder<S3Client> {

    // -------------------------------------
    // 配置信息
    // -------------------------------------
    private String secretId;
    private String secretKey;
    private String bucket;
    private String region;
    private String prefix;
    private int duration;
    // String[] allowPrefixes;
    // String[] allowActions;

    public S3LongTermClientBuilder(WnIo io, WnObj oHome, String name) {
        super(io, oHome, name);
    }

    public S3LongTermClientBuilder(WnIo io, String homePath) {
        super(io, io.fetch(null, homePath), null);
    }

    protected String getTokenPath() {
        if (Ws.isBlank(name)) {
            return null;
        }
        return ".domain/xo_token/s3_" + name;
    }

    protected String getConfigPath(String name) {
        return ".io/s3/" + name + ".json5";
    }

    protected XoClientWrapper<S3Client> createClient(String clientKey) {
        return new XoClientWrapper<S3Client>(clientKey) {
            protected void _close_client(S3Client client) {
                client.close();
            }
        };
    }

    public void loadConfig(NutMap props) {
        this.secretId = props.getString("secretId");
        this.secretKey = props.getString("secretKey");
        // 客户端是长期的，因此缓存时间搞个1天
        this.duration = props.getInt("duration", 86400);
        this.bucket = props.getString("bucket");
        this.region = props.getString("region");
        this.prefix = XoClientWrapper.tidyPrefix(props.getString("prefix", ""));

        // this.allowPrefixes = re.getAllowPrefixes();
        // this.allowActions = props.getArray("allowActions", String.class);
        // if (null == allowActions || allowActions.length == 0) {
        // allowActions = Wlang.array("*");
        // }
    }

    @Override
    public XoClientWrapper<S3Client> build() throws IOException {
        // 看看是否需要读取配置文件
        if (!Ws.isBlank(secretId)
            && !Ws.isBlank(secretKey)
            && !Ws.isBlank(bucket)
            && !Ws.isBlank(region)) {
            return creaet_client();
        }

        // 读取配置文件后，创建客户端
        load_from_config();

        return creaet_client();
    }

    private void load_from_config() {
        if (Ws.isBlank(name)) {
            throw Er.create("e.xo.S3LongTerm.load_from_config.NoConfigName");
        }
        // 读取配置信息
        String confPath = getConfigPath(name);
        WnObj oConf = io.check(oHome, confPath);
        NutMap props = io.readJson(oConf, NutMap.class);

        // 初始化配置对象
        this.loadConfig(props);
    }

    private XoClientWrapper<S3Client> creaet_client() {
        Region _region = Region.of(this.region);
        AwsBasicCredentials cred = AwsBasicCredentials.create(secretId,
                                                              secretKey);
        StaticCredentialsProvider provider = StaticCredentialsProvider
            .create(cred);
        S3Client s3 = S3Client.builder()
            .region(_region)
            .credentialsProvider(provider)
            .build();
        re.setClient(s3);
        re.setBucket(bucket);
        re.setRegion(region);
        re.setPrefix(prefix);
        // 客户端是长期的，因此缓存时间搞个1天
        long now = System.currentTimeMillis();
        re.setExpiredAt(now + duration * 1000L);
        return re;
    }

}
