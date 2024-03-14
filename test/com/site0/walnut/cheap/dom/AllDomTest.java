package com.site0.walnut.cheap.dom;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import com.site0.walnut.cheap.dom.match.AllMatchTest;
import com.site0.walnut.cheap.dom.mutation.CheapDomOperationTest;
import com.site0.walnut.cheap.dom.selector.AllSelectorTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({AllMatchTest.class,
                     AllSelectorTest.class,
                     CheapDomOperationTest.class,
                     CheapNodeTest.class,
                     CheapTextTest.class})
public class AllDomTest {}
