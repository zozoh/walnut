package org.nutz.walnut.ext.xapi;

/**
 * 第三方接口配置信息加载器
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public interface ThirdXConfigManager {

    /**
     * @param apiName
     *            第三方接口中某个应用的名称
     * @param account
     *            应用的账户名称
     * @param configType
     *            配置对象类型，如果没有自定义类型，可以传入 <code>NutMap</code>
     * 
     * @return 配置对象
     */
    <T> T loadConfig(String apiName, String account, Class<T> configType);

    /**
     * 实现类应该做好密钥缓存工作
     * 
     * @param apiName
     *            第三方接口中某个应用的名称
     * @param account
     *            应用的账户名称
     * 
     * @return 密钥
     */
    String loadAccessKey(String apiName, String account);

}
