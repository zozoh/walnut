package org.nutz.walnut.core.mapping;

import org.junit.After;
import org.junit.Before;
import org.nutz.walnut.core.AbstractWnIoMappingTest;

public class GlobalIoMappingTest extends AbstractWnIoMappingTest {

    @Before
    public void setUp() throws Exception {
        this.setup.cleanAllData();
        im = this.setup.getGlobalIoMapping();
    }

    @After
    public void tearDown() throws Exception {}

}