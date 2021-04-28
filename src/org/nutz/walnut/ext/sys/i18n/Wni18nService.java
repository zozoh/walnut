package org.nutz.walnut.ext.sys.i18n;

import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;

@IocBean(create = "reload")
public class Wni18nService {

    @Inject("refer:io")
    private WnIo io;

    @Inject("java:$conf.get('i18n-path','/etc/i18n')")
    private String path;

    // 缓存系统的 i18n
    private Map<String, Map<String, String>> i18ns;

    public Wni18nService() {
        this.i18ns = new HashMap<>();
    }

    public String getMsg(String lang, String key) {
        return getMsg(lang, key, key);
    }

    public String getMsg(String lang, String key, String dft) {
        Map<String, String> map = i18ns.get(lang);
        if (null != map)
            return Strings.sBlank(map.get(key), dft);
        return dft;
    }

    public Map<String, String> getLang(String lang) {
        return i18ns.get(lang);
    }

    /**
     * 重新加载 i18n 设定
     */
    public void reload() {
        i18ns.clear();
        WnObj oMsgHome = io.fetch(null, path);
        if (null != oMsgHome) {
            List<WnObj> list = io.getChildren(oMsgHome, null);
            for (WnObj oMsg : list) {
                String key = oMsg.name();
                Reader r = io.getReader(oMsg, 0);
                try {
                    PropertiesProxy pp = new PropertiesProxy(r);
                    i18ns.put(key, pp);
                }
                finally {
                    Streams.safeClose(r);
                }
            }
        }
    }
}
