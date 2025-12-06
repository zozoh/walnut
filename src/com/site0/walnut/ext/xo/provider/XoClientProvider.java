package com.site0.walnut.ext.xo.provider;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.xo.builder.XoClientBuilder;

public interface XoClientProvider<T> {

    XoClientBuilder<T> getBuilder(WnIo io, WnObj oHome, String name) throws Exception;

}
