package org.nutz.walnut.core.indexer.dao;

import static org.junit.Assert.assertEquals;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nutz.walnut.api.io.WnIoIndexer;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.core.IoCoreTest;
import org.nutz.walnut.core.bean.WnIoObj;

public class DaoNoNameIndexerTest extends IoCoreTest {

    protected WnIoIndexer indexer;

    @Before
    public void setUp() throws Exception {
        this.setup.cleanAllData();
        indexer = this.setup.getDaoNoNameIndexer();
    }

    @After
    public void tearDown() throws Exception {}

    /**
     * 测试布尔字段
     */
    @Test
    public void test_create_tow_records() {
        WnObj p = indexer.getRoot();

        WnObj o0 = new WnIoObj();
        o0.setParent(p);
        o0.put("age", 10);
        o0.put("realname", "A");

        WnObj o1 = new WnIoObj();
        o1.setParent(p);
        o1.put("age", 12);
        o1.put("realname", "B");

        // 连续创建两个
        indexer.create(p, o0);
        indexer.create(p, o1);

        // 查询出来
        WnQuery q = new WnQuery();
        q.asc("age");
        List<WnObj> list = indexer.query(q);

        assertEquals(2, list.size());
        assertEquals(10, list.get(0).getInt("age"));
        assertEquals("A", list.get(0).getString("realname"));
        assertEquals(12, list.get(1).getInt("age"));
        assertEquals("B", list.get(1).getString("realname"));

        // 反序查询
        q.desc("age");
        list = indexer.query(q);

        assertEquals(2, list.size());
        assertEquals(12, list.get(0).getInt("age"));
        assertEquals("B", list.get(0).getString("realname"));
        assertEquals(10, list.get(1).getInt("age"));
        assertEquals("A", list.get(1).getString("realname"));
    }
}
