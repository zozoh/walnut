package com.site0.walnut.ext.xo.util;

import com.qcloud.cos.COSClient;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.xo.provider.CosClientProvider;
import com.site0.walnut.ext.xo.provider.S3ClientProvider;

import software.amazon.awssdk.services.s3.S3Client;

public abstract class XoClients {

    public static final XoClientManager<COSClient> COS = new XoClientManager<>(new CosClientProvider());

    public static final XoClientManager<S3Client> S3 = new XoClientManager<>(new S3ClientProvider());

    public static String genClientKey(WnObj oHome, String name) {
        return oHome.path() + "::" + name;
    }
}
