package org.nutz.walnut.impl.io;

import org.junit.After;
import org.junit.Before;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.io.AbsractWnIoTest;
import org.nutz.walnut.util.Wn;

public class WnIoImpl2Test extends AbsractWnIoTest {

    @Before
    public void setUp() throws Exception {
        this.setup.cleanAllData();
        this.io = this.setup.getIo();

        this.refers = this.setup.getWnReferApi();
        this.handles = this.setup.getWnIoHandleManager();

        WnAccount me = setup.genAccount("root");
        Wn.WC().setMe(me);
        Wn.WC().setSecurity(new WnEvalLink(io));
    }

    @After
    public void tearDown() throws Exception {}

}
