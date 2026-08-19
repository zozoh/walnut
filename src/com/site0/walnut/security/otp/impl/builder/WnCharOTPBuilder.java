package com.site0.walnut.security.otp.impl.builder;

import com.site0.walnut.security.otp.bean.WnOTPBean;
import com.site0.walnut.security.otp.bean.WnOTPType;
import com.site0.walnut.util.Wuu;

public class WnCharOTPBuilder extends WnNumberOTPBuilder {

    public WnCharOTPBuilder(int n, int duration) {
        super(n, duration);

    }

    @Override
    public WnOTPBean make(WnOTPType type, String account) {
        String val = Wuu.captchaChar(n);
        return new WnOTPBean(duration, type, account, val);
    }

}
