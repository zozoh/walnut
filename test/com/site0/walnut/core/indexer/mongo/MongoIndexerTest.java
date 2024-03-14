package com.site0.walnut.core.indexer.mongo;

import org.junit.After;
import org.junit.Before;
import com.site0.walnut.core.AbstractWnIoIndexerTest;

public class MongoIndexerTest extends AbstractWnIoIndexerTest {

    @Before
    public void setUp() throws Exception {
        this.setup.cleanAllData();
        indexer = this.setup.getGlobalIndexer();
    }

    @After
    public void tearDown() throws Exception {}

}
