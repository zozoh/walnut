package org.nutz.ioc;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.nutz.ioc.impl.PropertiesProxyTest;
import org.nutz.ioc.java.ChainParsingTest;
import org.nutz.ioc.json.AllJsonIoc;
import org.nutz.ioc.loader.AllLoader;
import org.nutz.ioc.val.AllVal;

@RunWith(Suite.class)
@Suite.SuiteClasses({ChainParsingTest.class,
                     AllJsonIoc.class,
                     AllLoader.class,
                     AllVal.class,
                     SimpleIocTest.class,
                     PropertiesProxyTest.class})
public class AllIoc {}
