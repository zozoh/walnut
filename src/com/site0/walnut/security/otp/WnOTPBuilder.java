package com.site0.walnut.security.otp;

import com.site0.walnut.security.otp.bean.WnOTPBean;
import com.site0.walnut.security.otp.bean.WnOTPType;

public interface WnOTPBuilder {

    WnOTPBean make(WnOTPType type, String account);

}
