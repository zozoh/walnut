package org.nutz.walnut.ext.vcode;

import org.nutz.lang.Strings;
import org.nutz.walnut.util.Wn;

/**
 * 验证码用到的通用方法
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public abstract class VCodes {

    public static String getPathBy(String domain, String scene, String accountName) {
        return Wn.appendPath("/var/vcode", Strings.sBlank(domain, "walnut"), scene, accountName);
    }

    public static String getSignupPath(String domain, String accountName) {
        return getPathBy(domain, "signup", accountName);
    }

    public static String getCaptchaPath(String domain, String accountName) {
        return getPathBy(domain, "captcha", accountName);
    }

    public static String getLoginPath(String domain, String accountName) {
        return getPathBy(domain, "login", accountName);
    }

    // 阻止实例化
    private VCodes() {}
}
