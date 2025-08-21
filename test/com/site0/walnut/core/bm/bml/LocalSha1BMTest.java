package com.site0.walnut.core.bm.localbm;

import org.junit.After;
import org.junit.Before;
import com.site0.walnut.core.AbstractWnIoBMTest;
import com.site0.walnut.core.bean.WnIoObj;
import com.site0.walnut.util.Wn;

public class LocalIoBMTest extends AbstractWnIoBMTest {

    @Before
    public void setUp() throws Exception {
        this.setup.cleanAllData();
        indexer = this.setup.getGlobalIndexer();
        bm = this.setup.getGlobalIoBM();

        o = new WnIoObj();
        o.id(Wn.genId());
    }

    @After
    public void tearDown() throws Exception {}

    @Override
    protected String getObjSha1ForTest(String sha1) {
        return o.sha1();
    }

}
