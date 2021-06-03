package org.nutz.walnut.ext.net.xapi;

import java.net.Proxy;

import org.nutz.lang.util.NutBean;
import org.nutz.walnut.ext.net.xapi.bean.ThirdXRequest;

/**
 * 第三方接口的抽象接口
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public interface ThirdXApi {

    ThirdXConfigManager getConfigManager();

    ThirdXExpertManager getExpertManager();

    void setProxy(Proxy proxy);

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
     * 准备一个第三方平台的接口请求对象，其中参数和请求头已经根据传入的变量上下文展开。<br>
     * 调用者可以根据需要继续设置 body 等属性，或者特殊的调整参数表和请求头。<br>
     * 之后，再通过 send 函数发送请求，获得平台接口的返回结果
     * <p>
     * 注意，这里展开参数表和头时，上下文变量会被自动插入一个固定的键：<code>@AK="xxxx"</code>，<br>
     * 但是这对调用者应该是不用关心的问题。因为相关逻辑都被 <code>ThirdXAkManager</code> 以及
     * <code>ThirdXConfigLoader</code> 封装好了。，对调用者完全透明。
     * 
     * @param apiName
     *            接口平台名称
     * @param account
     *            平台的账号
     * @param path
     *            请求路径
     * @param vars
     *            参数需要的上下文
     * @return 第三方接口请求对象
     */
    ThirdXRequest prepare(String apiName, String account, String path, NutBean vars);

    /**
     * 发送请求，并将请求内容转换成用户需要的数据类型
     * 
     * @param <T>
     *            泛型参数
     * @param req
     *            请求对象
     * @param classOfT
     *            要转换的目标数据类型
     * @return 请求结果
     * @throws ThirdXException
     *             当请求造成服务器端返回非 200 的响应码时
     */
    <T> T send(ThirdXRequest req, Class<T> classOfT) throws ThirdXException;
}
