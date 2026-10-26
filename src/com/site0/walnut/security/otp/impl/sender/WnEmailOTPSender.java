package com.site0.walnut.security.otp.impl.sender;

import com.site0.walnut.security.otp.WnOTPSender;
import com.site0.walnut.security.otp.bean.WnOTPBean;

public abstract class WnEmailOTPSender implements WnOTPSender {
    
    /**
     * @return Email 的标题, 支持模板
     */
    protected abstract String getSubject();
    

    @Override
    public void send(WnOTPBean otp) {
        // TODO Auto-generated method stub

    }

}
