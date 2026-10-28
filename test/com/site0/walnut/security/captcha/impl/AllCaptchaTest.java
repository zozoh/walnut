package com.site0.walnut.security.captcha.impl;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import com.site0.walnut.security.captcha.impl.store.WnRedisCaptchaAnswerStoreTest;
import com.site0.walnut.security.captcha.impl.store.WnRedisCaptchaTicketStoreTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({WnRedisCaptchaAnswerStoreTest.class, WnRedisCaptchaTicketStoreTest.class})
public class AllCaptchaTest {}
