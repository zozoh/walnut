package com.site0.walnut.ext.xo.builder;

import java.io.IOException;

import org.nutz.lang.util.NutMap;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.xo.util.XoClientWrapper;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

public class S3LongTermClientBuilder extends AbstractXoClientBuilder<S3Client> {

    public S3LongTermClientBuilder(WnIo io, WnObj oHome, String name) {
        super(io, oHome, name);
    }

    protected String getTokenPath() {
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

    @Override
    public XoClientWrapper<S3Client> build() throws IOException {
        // 读取配置信息
        String confPath = getConfigPath(name);
        WnObj oConf = io.check(oHome, confPath);
        NutMap props = io.readJson(oConf, NutMap.class);

        // 初始化配置对象
        String accessKey = props.getString("secretId");
        String secretKey = props.getString("secretKey");
        String bucket = props.getString("bucket");
        String _region = props.getString("region");
        Region region = Region.of(_region);

        re.setBucket(bucket);
        re.setRegion(_region);
        re.setPrefix(props.getString("prefix", ""));

        // 客户端是长期的，因此缓存时间搞个1天
        int duration = props.getInt("duration", 86400);
        long now = System.currentTimeMillis();
        re.setExpiredAt(now + duration * 1000L);

        AwsBasicCredentials cred = AwsBasicCredentials.create(accessKey, secretKey);
        StaticCredentialsProvider provider = StaticCredentialsProvider.create(cred);
        S3Client s3 = S3Client.builder().region(region).credentialsProvider(provider).build();
        re.setClient(s3);

        return re;
    }

}
