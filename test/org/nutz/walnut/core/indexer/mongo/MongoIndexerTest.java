package org.nutz.walnut.core.indexer.mongo;

import org.junit.After;
import org.junit.Before;
import org.nutz.walnut.core.AbstractWnIoIndexerTest;

public class MongoIndexerTest extends AbstractWnIoIndexerTest {

    @Before
    public void setUp() throws Exception {
        indexer = this.setup.getMongoIndexer();
    }

    @After
    public void tearDown() throws Exception {}

}
