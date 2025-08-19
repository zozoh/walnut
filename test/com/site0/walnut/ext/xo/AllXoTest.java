package com.site0.walnut.ext.xo;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import com.site0.walnut.ext.xo.impl.CosXoServiceTest;
import com.site0.walnut.ext.xo.impl.S3XoServiceTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({S3XoServiceTest.class, CosXoServiceTest.class})
public class AllXoTest {}
