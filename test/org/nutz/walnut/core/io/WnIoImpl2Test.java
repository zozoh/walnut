package org.nutz.walnut.core.io;

import org.junit.After;
import org.junit.Before;
import org.nutz.walnut.core.AbstractWnIoTest;

public class WnIoImpl2Test extends AbstractWnIoTest {

    @Before
    public void setUp() throws Exception {
        this.setup.cleanAllData();
        this.io = this.setup.getIo();
    }

    @After
    public void tearDown() throws Exception {}

}
