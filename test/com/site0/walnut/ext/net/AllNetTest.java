package com.site0.walnut.ext.net;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import com.site0.walnut.ext.net.http.bean.HttpContentDispositionTest;
import com.site0.walnut.ext.net.http.bean.HttpUrlTest;
import com.site0.walnut.ext.net.http.upload.AllUploadTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({HttpUrlTest.class,
                     HttpContentDispositionTest.class,
                     AllUploadTest.class})
public class AllNetTest {}
