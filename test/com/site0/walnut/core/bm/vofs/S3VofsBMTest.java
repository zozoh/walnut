package com.site0.walnut.core.bm.vofs;

import org.nutz.lang.util.NutMap;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.ext.xo.builder.AbstractXoClientBuilder;
import com.site0.walnut.ext.xo.builder.S3LongTermClientBuilder;
import com.site0.walnut.ext.xo.impl.S3XoService;
import com.site0.walnut.ext.xo.impl.XoService;
import com.site0.walnut.ext.xo.util.XoClientWrapper;

import software.amazon.awssdk.services.s3.S3Client;

public class S3VofsBMTest extends AbstractVofsBMTest<S3Client> {

    @Override
    protected AbstractXoClientBuilder<S3Client> _builder(WnIo io) {
        AbstractXoClientBuilder<S3Client> builder = new S3LongTermClientBuilder(io,
                                                                                "/test");
        NutMap conf = new NutMap();
        conf.put("secretId", setup.getConifg("s3-secret-id"));
        conf.put("secretKey", setup.getConifg("s3-secret-key"));
        conf.put("bucket", setup.getConifg("s3-bucket"));
        conf.put("region", setup.getConifg("s3-region"));
        conf.put("prefix", "mnt_home/");
        builder.loadConfig(conf);
        return builder;
    }

    @Override
    protected XoService _make_api(XoClientWrapper<S3Client> _client) {
        return new S3XoService(_client);
    }

}
