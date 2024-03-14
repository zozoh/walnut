package com.site0.walnut.ext.net.xapi;

import java.io.InputStream;

import com.site0.walnut.ext.net.xapi.bean.XApiRequest;

@SuppressWarnings("unused")
public interface XApiCacheObj {

    boolean isMatched();

    <T> T getOutput(Class<T> classOfT);

    <T> T saveAndOutput(InputStream resp, Class<T> classOfT);

}
