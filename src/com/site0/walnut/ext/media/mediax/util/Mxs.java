package com.site0.walnut.ext.media.mediax.util;

import java.net.URI;

import org.nutz.http.Header;
import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.lang.Strings;

/**
 * 静态帮助方法集
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public abstract class Mxs {

    /**
     * 根据一个模板生成 HTTP 头 <br>
     * 模板名称现在支持
     * <ul>
     * <li><code>mac_chrome</code>
     * </ul>
     * 
     * @param name
     *            模板名称。如果为空，默认取 "mac_chrome"
     * @return 根据模板生成的头
     */
    public static Header genHeader(String name) {
        name = Strings.sBlank(name, "mac_chrome");
        String path = "com/site0/walnut/ext/mediax/util/httpheader/" + name + ".properties";
        PropertiesProxy pp = new PropertiesProxy(path);
        return Header.create(pp.toMap());
    }

    /**
     * @param objPath
     *            路径，可以是一个完整 URL 或者是一个路径
     * @return 完整的 URL
     */
    public static String normalizePath(URI uri, String ph) {
        if (null == ph)
            return uri.toString();

        if (ph.matches("^https?://.+$"))
            return ph;

        String url;
        url = uri.getScheme() + "://" + uri.getHost();
        int port = uri.getPort();
        if (port > 0) {
            url += ":" + port;
        }
        url += ph;

        return url;
    }

}
