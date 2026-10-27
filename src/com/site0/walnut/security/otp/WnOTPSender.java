package com.site0.walnut.security.otp;

import org.nutz.web.ajax.AjaxReturn;

import com.site0.walnut.security.otp.bean.WnOTPBean;
import com.site0.walnut.security.otp.bean.WnOTPType;

public interface WnOTPSender {

    /**
     * @return 这个发送器送达的动态密码类型
     */
    WnOTPType getType();

    AjaxReturn send(WnOTPBean otp);

}
