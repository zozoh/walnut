package com.site0.walnut.core.indexer.dao;

import org.junit.After;
import org.junit.Before;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.core.AbstractWnIoIndexerTest;

public class DaoIndexerTest extends AbstractWnIoIndexerTest {

    @Before
    public void setUp() throws Exception {
        this.setup.cleanAllData();
        indexer = this.setup.getDaoIndexer();
    }

    @After
    public void tearDown() throws Exception {}

    @Override
    protected void test_06_exists(WnObj a, WnObj b, WnObj c, WnObj oHT) {
        // 跳过这部分测试， SQL 不需要考虑 $exists
    }

}
