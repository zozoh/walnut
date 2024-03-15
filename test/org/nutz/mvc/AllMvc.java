package org.nutz.mvc;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.nutz.mvc.adaptor.JsonAdaptorTest;
import org.nutz.mvc.adaptor.injector.AllInjector;
import org.nutz.mvc.impl.MappingNodeTest;
import org.nutz.mvc.impl.ViewProcessorTest;
import org.nutz.mvc.init.AllInit;
import org.nutz.mvc.view.AllView;
import org.nutz.mvc.view.DefaultViewMakerTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({MappingNodeTest.class,
                     JsonAdaptorTest.class,
                     DefaultViewMakerTest.class,
                     ViewProcessorTest.class,
                     AllInit.class,
                     AllInjector.class,
                     AllView.class})
public class AllMvc {}
