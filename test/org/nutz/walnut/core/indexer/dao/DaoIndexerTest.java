package org.nutz.walnut.core.indexer.dao;

import org.junit.After;
import org.junit.Before;
import org.nutz.walnut.core.AbstractWnIoIndexerTest;

public class DaoIndexerTest extends AbstractWnIoIndexerTest {

    @Before
    public void setUp() throws Exception {
        this.setup.cleanAllData();
        indexer = this.setup.getDaoIndexer();
    }

    @After
    public void tearDown() throws Exception {}

}
