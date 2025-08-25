package com.site0.walnut.core.bm.bmv;

import org.nutz.lang.util.NutMap;

import com.qcloud.cos.COSClient;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.ext.xo.builder.AbstractXoClientBuilder;
import com.site0.walnut.ext.xo.builder.CosClientBuilder;
import com.site0.walnut.ext.xo.impl.CosXoService;
import com.site0.walnut.ext.xo.impl.XoService;
import com.site0.walnut.ext.xo.util.XoClientWrapper;

public class CosVXDataSignBMTest extends AbstractVXDataSignBMTest<COSClient> {

    protected AbstractXoClientBuilder<COSClient> _builder(WnIo io) {
        AbstractXoClientBuilder<COSClient> builder = new CosClientBuilder(io,
                                                                          "/test");
        NutMap conf = new NutMap();
        conf.put("secretId", setup.getConifg("cos-secret-id"));
        conf.put("secretKey", setup.getConifg("cos-secret-key"));
        conf.put("bucket", setup.getConifg("cos-bucket"));
        conf.put("region", setup.getConifg("cos-region"));
        conf.put("prefix", "buck/");
        builder.loadConfig(conf);
        return builder;
    }

    protected XoService _make_api(XoClientWrapper<COSClient> _client) {
        return new CosXoService(_client);
    }
}
