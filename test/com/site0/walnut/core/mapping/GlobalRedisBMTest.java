package com.site0.walnut.core.mapping;

import org.junit.After;
import org.junit.Before;
import com.site0.walnut.core.AbstractWnIoMappingTest;

public class GlobalRedisBMTest extends AbstractWnIoMappingTest {

    @Before
    public void setUp() throws Exception {
        this.setup.cleanAllData();
        im = this.setup.getGlobalRedisBMMapping();
    }

    @After
    public void tearDown() throws Exception {}

}
