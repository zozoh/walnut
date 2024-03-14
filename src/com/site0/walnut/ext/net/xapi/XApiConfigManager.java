package com.site0.walnut.ext.net.xapi;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.ext.net.xapi.bean.XApiRequest;

/**
 * 第三方接口配置信息加载器
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public interface XApiConfigManager {

    /**
     * @param apiName
     *            接口平台名称
     * @param account
     *            平台的账号
     * @return 当前接口账户是否具备一个有效的动态密钥。<br>
     *         如果接口不许呀动态密钥，永远返回 <code>true</code>
     */
    boolean hasValidAccessKey(String apiName, String account);
    
    /**
     * 针对请求对象建立一个缓存接口
     * 
     * @param req 请求对象（必须执行过prepare）
     * @return 缓存接口
     */
    XApiCacheObj loadReqCache(XApiRequest req);

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
     * 
     * 相当于用 <code> NutMap</code>来读取配置
     * 
     * @param apiName
     *            第三方接口中某个应用的名称
     * @param account
     *            应用的账户名称
     * @return 配置
     * @see #loadConfig(String, String, Class)
     */
    NutMap loadConfig(String apiName, String account);

    /**
     * 实现类应该做好密钥缓存工作
     * 
     * @param apiName
     *            第三方接口中某个应用的名称
     * @param account
     *            应用的账户名称
     * @param vars
     *            上下文变量。有些 API，譬如 FB，需要一个短期的 AK 来交换一个长期的 AK。 而这个短期的 AK 必须由登录框来的。
     * @param force
     *            不看过期时间，直接强制加载一个新的访问凭证
     * 
     * @return 密钥
     */
    String loadAccessKey(String apiName, String accoun, NutBean vars, boolean force);

}
