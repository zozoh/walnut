package org.nutz.walnut.ext.xapi;

import org.nutz.lang.util.NutBean;

/**
 * 第三方接口配置信息加载器
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public interface ThirdXConfigLoader {

    /**
     * @param appName
     *            第三方接口中某个应用的名称
     * @return 配置具体内容
     */
    NutBean load(String appName);

}
