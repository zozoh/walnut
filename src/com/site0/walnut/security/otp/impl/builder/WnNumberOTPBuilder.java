package com.site0.walnut.security.otp.impl.builder;

import com.site0.walnut.security.otp.WnOTPBuilder;
import com.site0.walnut.security.otp.bean.WnOTPBean;
import com.site0.walnut.security.otp.bean.WnOTPType;
import com.site0.walnut.util.Wuu;

public class WnNumberOTPBuilder implements WnOTPBuilder {

    /**
     * 要生成几位数字
     */
    protected int n;

    /**
     * 指定密码的有效时长（秒）
     */
    protected int duration;

    public WnNumberOTPBuilder(int n, int duration) {
        this.n = n;
        this.duration = duration;

    }

    @Override
    public WnOTPBean make(WnOTPType type, String account) {
        String val = Wuu.captchaNumber(n);
        return new WnOTPBean(duration, type, account, val);
    }

}
