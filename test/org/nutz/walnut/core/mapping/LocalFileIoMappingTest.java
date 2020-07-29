package org.nutz.walnut.core.mapping;

import org.junit.After;
import org.junit.Before;
import org.nutz.walnut.core.AbstractWnIoMappingTest;

public class LocalFileIoMappingTest extends AbstractWnIoMappingTest {

    @Before
    public void setUp() throws Exception {
        this.setup.cleanAllData();
        im = this.setup.getLocalFileMapping();
    }

    @After
    public void tearDown() throws Exception {}

}
