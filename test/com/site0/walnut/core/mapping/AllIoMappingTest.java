package com.site0.walnut.core.mapping;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import com.site0.walnut.core.mapping.support.AllMappingSupportTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({AllMappingSupportTest.class,
                     LocalFileIoMappingTest.class,
                     GlobalIoMappingTest.class,
                     GlobalRedisBMTest.class})
public class AllIoMappingTest {}
