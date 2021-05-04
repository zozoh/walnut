package org.nutz.walnut.cheap.dom;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.nutz.walnut.cheap.dom.match.AllMatchTest;
import org.nutz.walnut.cheap.dom.mutation.CheapDomOperationTest;
import org.nutz.walnut.cheap.dom.selector.AllSelectorTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({AllMatchTest.class,
                     AllSelectorTest.class,
                     CheapDomOperationTest.class,
                     CheapNodeTest.class,
                     CheapTextTest.class})
public class AllDomTest {}
