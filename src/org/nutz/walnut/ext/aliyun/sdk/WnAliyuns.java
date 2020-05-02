package org.nutz.walnut.ext.aliyun.sdk;

import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.impl.box.JvmHdlContext;

public abstract class WnAliyuns {

    public static void setConf(WnIo io, JvmHdlContext hc, Class<?> confClass) {
        if (null != confClass) {
            Object conf = io.readJson(hc.oRefer, confClass);
            hc.setv("CONFIG", conf);
        }
    }

    public static <T> T getConf(JvmHdlContext hc, Class<T> classOfT) {
        return hc.getAs("CONFIG", classOfT);
    }

}
