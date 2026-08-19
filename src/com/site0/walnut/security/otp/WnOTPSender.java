package com.site0.walnut.security.otp;

import com.site0.walnut.security.otp.bean.WnOTPBean;

public interface WnOTPSender {

    void send(WnOTPBean otp);

}