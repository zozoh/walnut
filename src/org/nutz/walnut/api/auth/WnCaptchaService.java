package org.nutz.walnut.api.auth;

import org.nutz.walnut.api.io.WnObj;

public interface WnCaptchaService {

    /**
     * 保存验证码，如果存在，替换
     * 
     * @param cap
     *            验证码对象
     * @return 验证码的数据对象
     */
    WnObj saveCaptcha(WnCaptcha cap);

    /**
     * 检查如果存在，就移除。如果不能匹配，则 retry+1, 如果匹配就移除并返回true
     * 
     * @param scene
     *            场景
     * @param account
     *            账号
     * @param code
     *            待验证的的验证码
     * @return 是否匹配成功
     */
    boolean removeCaptcha(String scene, String account, String code);

}