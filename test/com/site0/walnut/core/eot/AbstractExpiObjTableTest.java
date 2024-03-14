package com.site0.walnut.core.eot;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import com.site0.walnut.api.io.WnExpiObj;
import com.site0.walnut.api.io.WnExpiObjTable;
import com.site0.walnut.core.IoCoreTest;
import com.site0.walnut.util.Wn;

public abstract class AbstractExpiObjTableTest extends IoCoreTest {

    /**
     * 子类需要设置这个实例
     */
    protected WnExpiObjTable table;

    @Test
    public void test_remove() throws InterruptedException {
        table.insertOrUpdate("c", 3);
        table.insertOrUpdate("a", 1);
        table.insertOrUpdate("b", 2);

        // 接手了三个
        List<WnExpiObj> list = table.takeover("zozoh", 1, 100);
        assertEquals(3, list.size());
        // 一定是按照过期时间，从最老到最新排序的
        assertEquals("a", list.get(0).getId());
        assertEquals("b", list.get(1).getId());
        assertEquals("c", list.get(2).getId());

        // 清除自己接手的
        int n = table.clean("zozoh", list.get(0).getHoldTime());
        assertEquals(3, n);

        // 等 100ms 再接手还是什么也没有
        Thread.sleep(100);
        list = table.takeover("zozoh", 3000, 100);
        assertEquals(0, list.size());
    }

    @Test
    public void test_simple() throws InterruptedException {
        long now = Wn.now();
        table.insertOrUpdate("c", 3);
        table.insertOrUpdate("a", 1);
        table.insertOrUpdate("b", 2);

        // 这个未过期
        table.insertOrUpdate("d", now + 2000);

        // 接手了三个
        List<WnExpiObj> list = table.takeover("zozoh", 1000, 100);
        assertEquals(3, list.size());
        // 一定是按照过期时间，从最老到最新排序的
        assertEquals("a", list.get(0).getId());
        assertEquals("b", list.get(1).getId());
        assertEquals("c", list.get(2).getId());

        // 再接手，一个也木有了
        list = table.takeover("zozoh", 3000, 100);
        assertEquals(0, list.size());

        // 等 1秒后，那三个过期了，再接手两个
        Thread.sleep(1000);
        list = table.takeover("zozoh", 2000, 2);
        assertEquals(2, list.size());
        // 一定是按照过期时间，从最老到最新排序的
        assertEquals("a", list.get(0).getId());
        assertEquals("b", list.get(1).getId());

        // 再接手，还能接受一个
        list = table.takeover("zozoh", 3000, 100);
        assertEquals(1, list.size());
        assertEquals("c", list.get(0).getId());

        // 再接手，一个也木有了
        list = table.takeover("zozoh", 3000, 100);
        assertEquals(0, list.size());

        // 等 1秒后，d过期了，再接手，应该有一个
        Thread.sleep(1000);
        list = table.takeover("zozoh", 1000, 2);
        assertEquals(1, list.size());
        // 一定是按照过期时间，从最老到最新排序的
        assertEquals("d", list.get(0).getId());

        // 再接手，一个也木有了
        list = table.takeover("zozoh", 3000, 100);
        assertEquals(0, list.size());
    }

}
