package com.site0.walnut.alg.ds;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import com.site0.walnut.alg.ds.adj.AdjacencyListTest;
import com.site0.walnut.alg.ds.buf.WnCharArrayTest;
import com.site0.walnut.alg.ds.buf.WnLinkedArrayListTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({WnLinkedArrayListTest.class,
                     WnCharArrayTest.class,
                     AdjacencyListTest.class,
                     WnCharOpTableTest.class})
public class AllAlgDsTest {}
