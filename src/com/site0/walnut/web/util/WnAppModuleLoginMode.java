package com.site0.walnut.web.util;

public enum WnAppModuleLoginMode {

    /**
     * 需要密码和 OTP 双重认证
     */
    PASSWD_AND_OTP,

    /**
     * 提供密码或者 OTP 均可
     */
    PASSWD_OR_OTP,

    /**
     * 仅用 OTP 认证
     */
    OTP_ONLY,

    /**
     * 仅用密码认证
     */
    PASSWD_ONLY
}
