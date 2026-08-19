package com.site0.walnut.security.otp;

import com.site0.walnut.security.otp.bean.WnOTPBean;
import com.site0.walnut.security.otp.bean.WnOTPType;

public interface WnOTPStore {

    boolean save(WnOTPBean otp);

    boolean remove(WnOTPBean otp);

    boolean remove(WnOTPType type, String account);

    WnOTPBean get(WnOTPType type, String account);

    WnOTPBean check(WnOTPType type, String account, String value);
}
