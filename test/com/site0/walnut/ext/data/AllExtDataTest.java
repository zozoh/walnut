package com.site0.walnut.ext.data;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import com.site0.walnut.ext.data.archive.util.MimeSpyTest;
import com.site0.walnut.ext.data.fake.AllFakeTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({MimeSpyTest.class, AllFakeTest.class,})
public class AllExtDataTest {}
