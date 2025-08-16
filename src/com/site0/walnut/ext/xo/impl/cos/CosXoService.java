package com.site0.walnut.ext.xo.impl.cos;

import java.io.IOException;
import java.util.TreeMap;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;

import com.tencent.cloud.CosStsClient;
import com.tencent.cloud.Response;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.region.Region;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wn;

/**
 * 参见文档 <code>ex-storage.md</code>
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class CosXoService {

    private String name;

    private TreeMap<String, Object> config;

    private String bucket;

    private String region;

    private COSClient _client;

    private int duration;

    public CosXoService(String name, NutBean props) {
        this.name = name;
        this.config = new TreeMap<>();

        // 云 api 密钥 SecretId
        this.config.put("secretId", props.getString("SecretId"));
        // 云 api 密钥 SecretKey
        this.config.put("secretKey", props.getString("SecretKey"));

        // 设置域名,可通过此方式设置内网域名
        // config.put("host", "sts.internal.tencentcloudapi.com");

        // 临时密钥有效时长，单位是秒
        this.duration = props.getInt("duration", 1800);
        this.config.put("durationSeconds", duration);

        // 换成你的 bucket
        this.bucket = props.getString("bucket");
        this.config.put("bucket", bucket);
        // 换成 bucket 所在地区
        this.region = props.getString("region");
        this.config.put("region", region);

        // 可以通过 allowPrefixes 指定前缀数组,
        // 例子： a.jpg 或者 a/* 或者 *
        // (使用通配符*存在重大安全风险, 请谨慎评估使用)
        if (props.has("prefix")) {
            String prefix = props.getString("prefix");
            String pfxPath = Wn.appendPath(prefix, "*");
            this.config.put("allowPrefixes", Wlang.array(pfxPath));
        } else {
            config.put("allowPrefixes", Wlang.array("*"));
        }

        // 密钥的权限列表。简单上传和分片需要以下的权限，其他权限列表请看
        // https://cloud.tencent.com/document/product/436/31923
        if (props.has("allowActions ")) {
            this.config.put("allowActions", props.getArray("allowActions", String.class));
        }
    }

    private COSClient get_client(WnIo io, NutBean vars) {
        if (null != this._client) {
            return this._client;
        }
        // 从缓冲读取临时的 secretId/Key/Token
        String path = "~/.domain/xo_token/cos_" + this.name;
        String tkPath = Wn.normalizeFullPath(path, vars);
        WnObj oToken = io.fetch(null, tkPath);

        if (null != oToken) {
            String secretId = oToken.getString("secretId");
            String secretKey = oToken.getString("secretKey");
            // String sessionToken = oToken.getString("sessionToken");

            this._client = __create_client(secretId, secretKey);
            return this._client;
        }

        // 获取最新的 secretId/Key/Token
        try {
            Response resp = CosStsClient.getCredential(config);
            String secretId = resp.credentials.tmpSecretId;
            String secretKey = resp.credentials.tmpSecretKey;
            String sessionToken = resp.credentials.sessionToken;

            NutMap meta = new NutMap();
            meta.put("secretId", secretId);
            meta.put("secretKey", secretKey);
            meta.put("sessionToken", sessionToken);
            meta.put("expi", System.currentTimeMillis() + this.duration * 1000L);
            oToken = io.createIfExists(null, tkPath, WnRace.FILE);
            io.appendMeta(oToken, meta);

            this._client = __create_client(secretId, secretKey);
            return this._client;
        }
        catch (IOException e) {
            throw Er.wrap(e);
        }

    }

    protected COSClient __create_client(String secretId, String secretKey) {
        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
        Region region = new Region(this.region);
        ClientConfig clientConfig = new ClientConfig(region);
        return new COSClient(cred, clientConfig);
    }

}
