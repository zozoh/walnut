package org.nutz.walnut.core.indexer.mongo;

import org.junit.After;
import org.junit.Before;
import org.nutz.walnut.core.AbstractWnIoIndexerTest;

public class MongoIndexerTest extends AbstractWnIoIndexerTest {

    @Before
    public void setUp() throws Exception {
        this.setup.cleanAllData();
        indexer = this.setup.getGlobalIndexer();
    }

    @After
    public void tearDown() throws Exception {}

}