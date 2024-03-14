package com.site0.walnut.core.bm.redis;

import org.junit.After;
import org.junit.Before;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.core.AbstractWnIoBMTest;

public class RedisBMTest extends AbstractWnIoBMTest {

    @Before
    public void setUp() throws Exception {
        this.setup.cleanAllData();
        indexer = this.setup.getGlobalIndexer();
        bm = this.setup.getRedisBM();

        o = indexer.create(null, "/a/b.txt", WnRace.FILE);
    }

    @After
    public void tearDown() throws Exception {}

    @Override
    protected String getObjSha1ForTest(String sha1) {
        return o.sha1();
    }

}
