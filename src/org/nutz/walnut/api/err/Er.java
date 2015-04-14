package org.nutz.walnut.api.err;

import org.nutz.web.WebException;
import org.nutz.web.Webs;

public abstract class Er {

    public static WebException create(String key) {
        return Webs.Err.create(key);
    }

    public static WebException create(String key, Object reason) {
        return Webs.Err.create(key, reason);
    }

    public static WebException createf(String key, String fmt, Object... args) {
        return Webs.Err.create(key, String.format(fmt, args));
    }

    public static WebException create(Throwable e, String key, Object reason) {
        return Webs.Err.create(e, key, reason);
    }

    public static WebException wrap(Throwable e) {
        return Webs.Err.wrap(e);
    }

    public static WebException create(Throwable e, String key) {
        return Webs.Err.create(e, key);
    }

}
