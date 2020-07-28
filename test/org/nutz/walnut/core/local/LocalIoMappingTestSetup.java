package org.nutz.walnut.core.local;

import org.junit.After;
import org.junit.Before;
import org.nutz.walnut.core.WnIoMapping;
import org.nutz.walnut.core.AbstractWnIoIndexerTest;

public class LocalIoMappingTestSetup extends AbstractWnIoIndexerTest {

    @Before
    public void setUp() throws Exception {
        System.out.println("setUp");
    }

    @After
    public void tearDown() throws Exception {
        System.out.println("tearDown");
    }

}
