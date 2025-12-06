package com.site0.walnut.ext.xo.provider;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.xo.builder.*;

import software.amazon.awssdk.services.s3.S3Client;

public class S3ClientProvider implements XoClientProvider<S3Client> {

    @Override
    public XoClientBuilder<S3Client> getBuilder(WnIo io, WnObj oHome, String name)
            throws Exception {
        return new S3LongTermClientBuilder(io, oHome, name);
    }

}
