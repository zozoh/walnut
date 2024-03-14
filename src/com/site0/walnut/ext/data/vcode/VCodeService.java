package com.site0.walnut.ext.data.vcode;

import com.site0.walnut.api.io.WnObj;

/**
 * 提供验证码服务的接口。
 * <p>
 * 所有的接口都要接受一个 <code>vcodePath</code> 作为参数，通常这个参数格式为
 * 
 * <pre>
 * 类别/域名/应用
 * 比如:
 * phone/site0/register
 * </pre>
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public interface VCodeService {

    /**
     * 保存一个验证码
     * 
     * @param vcodePath
     *            验证码路径
     * @param code
     *            要保存的验证码
     * @param duInMin
     *            验证码有效时间（分钟）
     * @param maxRetry
     *            最大重试次数
     * @return 保存成功后的验证码对象。
     */
    WnObj save(String vcodePath, String code, int duInMin, int maxRetry);

    /**
     * 验证一个验证码，即使验证成功，也不会移除
     * 
     * @param vcodePath
     *            验证码路径
     * @param code
     *            要验证的验证码
     * @return true 验证成功，false 验证失败
     */
    boolean check(String vcodePath, String code);

    /**
     * 验证一个验证码，如果验证成功，则会移除
     * 
     * @param vcodePath
     *            验证码路径
     * @param code
     *            要验证的验证码
     * @param method
     *            验证方法，比如 "MD5", "SHA1" 等，null 表示直接验证。具体由子类来理解
     * @return true 验证成功，false 验证失败
     */
    boolean checkAndRemove(String vcodePath, String code);

}
