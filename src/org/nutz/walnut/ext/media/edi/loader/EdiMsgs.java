package org.nutz.walnut.ext.media.edi.loader;

import java.util.HashMap;

import org.nutz.walnut.api.err.Er;

/**
 * 封装报文解析的高级类的工厂方法
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public abstract class EdiMsgs {

    private static HashMap<String, EdiMsgLoader<?>> loaders = new HashMap<>();

    static {
        loaders.put("IC", new ICLoader());
        loaders.put("CLREGR", new CLREGRLoader());
    }

    public static EdiMsgLoader<?> getLoader(String key) {
        return loaders.get(key);
    }

    public static EdiMsgLoader<?> checkLoader(String key) {
        EdiMsgLoader<?> loader = getLoader(key);
        if (null == loader) {
            throw Er.create("e.cmd.edi.loader.NoDefined", key);
        }
        return loader;
    }

    public static ICLoader getInterchangeLoader() {
        return (ICLoader) loaders.get("IC");
    }

    public static CLREGRLoader getCLREGRLoader() {
        return (CLREGRLoader) loaders.get("CLREGR");
    }

}
