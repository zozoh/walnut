package org.nutz.walnut.ext.mediax.util;

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
        String path = "org/nutz/walnut/ext/mediax/util/httpheader/" + name + ".properties";
        PropertiesProxy pp = new PropertiesProxy(path);
        return Header.create(pp.toMap());
    }

}
