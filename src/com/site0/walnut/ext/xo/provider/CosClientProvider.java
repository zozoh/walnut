package com.site0.walnut.ext.xo.provider;

import java.io.IOException;
import com.qcloud.cos.COSClient;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.xo.builder.XoClientBuilder;
import com.site0.walnut.ext.xo.builder.CosClientBuilder;

public class CosClientProvider implements XoClientProvider<COSClient> {

    @Override
    public XoClientBuilder<COSClient> getBuilder(WnIo io, WnObj oHome, String name) throws IOException {
        return new CosClientBuilder(io, oHome, name);
    }

}
