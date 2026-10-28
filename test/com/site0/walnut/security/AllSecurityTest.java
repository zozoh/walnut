package com.site0.walnut.security;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import com.site0.walnut.security.captcha.impl.AllCaptchaTest;
import com.site0.walnut.security.otp.impl.AllOTPTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({AllOTPTest.class, AllCaptchaTest.class})
public class AllSecurityTest {}
