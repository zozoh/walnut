package com.site0.walnut.core.bm.localfile;

import org.junit.After;
import org.junit.Before;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.core.AbstractWnIoBMTest;

public class LocalFileBMTest extends AbstractWnIoBMTest {

    @Before
    public void setUp() throws Exception {
        this.setup.cleanAllData();
        indexer = this.setup.getLocalFileIndexer();
        bm = this.setup.getLocalFileBM();

        o = indexer.create(null, "/a/b.txt", WnRace.FILE);
    }

    @After
    public void tearDown() throws Exception {}

    @Override
    protected String getObjSha1ForTest(String sha1) {
        return sha1;
    }

}
