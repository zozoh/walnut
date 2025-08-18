package com.site0.walnut.ext.xo.builder;

import java.io.IOException;

import com.site0.walnut.ext.xo.util.XoClientWrapper;

public interface XoClientBuilder<T> {

    XoClientWrapper<T> build() throws IOException;

}
