package org.nutz.walnut.alg.ds;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.nutz.walnut.alg.ds.adj.AdjacencyListTest;
import org.nutz.walnut.alg.ds.buf.WnCharArrayTest;
import org.nutz.walnut.alg.ds.buf.WnLinkedArrayListTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({WnLinkedArrayListTest.class,
                     WnCharArrayTest.class,
                     AdjacencyListTest.class,
                     WnCharOpTableTest.class})
public class AllAlgDsTest {}
