package org.nutz.walnut.ext.net.xapi;

import java.io.InputStream;

import org.nutz.walnut.ext.net.xapi.bean.XApiRequest;

public interface XApiCacheObj {

    boolean isMatched();

    <T> T getOutput(Class<T> classOfT);

    <T> T saveAndOutput(InputStream resp, Class<T> classOfT);

}
