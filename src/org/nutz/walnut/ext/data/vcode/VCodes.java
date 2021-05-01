package org.nutz.walnut.ext.data.vcode;

import org.nutz.lang.Strings;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.util.Wn;

/**
 * 验证码用到的通用方法
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public abstract class VCodes {

    public static String getPathBy(String domain, String scene, String accountName) {
        // 危险字符串
        if (domain.indexOf('/') >= 0 || domain.indexOf("..") >= 0) {
            throw Er.create("e.vocde.invalid.domain", domain);
        }
        if (accountName.indexOf('/') >= 0 || accountName.indexOf("..") >= 0) {
            throw Er.create("e.vocde.invalid.accountName", accountName);
        }
        // 来吧
        return Wn.appendPath("/var/vcode", Strings.sBlank(domain, "walnut"), scene, accountName);
    }

    public static String getBookingPath(String domain, String accountName) {
        return getPathBy(domain, "booking", accountName);
    }

    public static String getSignupPath(String domain, String accountName) {
        return getPathBy(domain, "signup", accountName);
    }

    public static String getPasswdBackPath(String domain, String accountName) {
        return getPathBy(domain, "passwdback", accountName);
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
