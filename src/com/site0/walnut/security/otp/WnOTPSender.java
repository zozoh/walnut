package com.site0.walnut.security.otp;

import org.nutz.web.ajax.AjaxReturn;

import com.site0.walnut.security.otp.bean.WnOTPBean;

public interface WnOTPSender {

    AjaxReturn send(WnOTPBean otp);

}