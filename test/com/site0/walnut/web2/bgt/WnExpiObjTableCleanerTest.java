package com.site0.walnut.web2.bgt;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import com.site0.walnut.api.io.WnExpiObj;
import com.site0.walnut.api.io.WnExpiObjTable;

/**
 * 回归测试：清理线程必须真的跑起来（takeover 被调用）并能被 interrupt 停下。
 * 背景：RM-NUTZ-15 删掉旧 web/clean 后，这条后台线程一度无人接盘；
 * 用本测试守住"线程存在且可停"这一最低契约，完整的"对象到时消失"由 ATEST 集成用例覆盖。
 */
public class WnExpiObjTableCleanerTest {

    static class StubTable implements WnExpiObjTable {

        final AtomicInteger takeovers = new AtomicInteger();

        final AtomicInteger cleans = new AtomicInteger();

        @Override
        public void insertOrUpdate(WnExpiObj o) {}

        @Override
        public void insertOrUpdate(String id, long expi) {}

        @Override
        public boolean remove(String id) {
            return false;
        }

        @Override
        public List<WnExpiObj> takeover(String owner, long duInMs, int limit) {
            takeovers.incrementAndGet();
            return new ArrayList<>();
        }

        @Override
        public int clean(String owner, long hold) {
            cleans.incrementAndGet();
            return 0;
        }
    }

    @Test
    public void test_thread_runs_and_stops() throws Exception {
        StubTable table = new StubTable();
        WnExpiObjTableCleaner cleaner = new WnExpiObjTableCleaner(table, null, 50, 10, 50);
        cleaner.start();

        long deadline = System.currentTimeMillis() + 3000;
        while (table.takeovers.get() == 0 && System.currentTimeMillis() < deadline) {
            Thread.sleep(10);
        }
        assertTrue("cleaner should call takeover()", table.takeovers.get() > 0);

        cleaner.interrupt();
        cleaner.join(2000);
        assertFalse("cleaner should stop on interrupt", cleaner.isAlive());
    }

}
