package com.site0.walnut.core.indexer.localfile;

import static org.junit.Assert.*;

import java.util.Comparator;
import java.util.List;

import org.junit.Test;
import com.site0.walnut.api.io.WnIoIndexer;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.core.bean.WnIoObj;
import com.site0.walnut.core.indexer.WnVirtualFsIndexerTest;

public class LocalFileIndexerTest extends WnVirtualFsIndexerTest {

    protected WnIoIndexer _get_indexer() {
        this.setup.cleanAllData();
        return this.setup.getLocalFileIndexer();

    }

    @Test
    public void test_children_by_regex() {
        indexer.create(null, "a/b/c.txt", WnRace.FILE);
        indexer.create(null, "a/b/d.txt", WnRace.FILE);
        indexer.create(null, "a/b/e.txt", WnRace.FILE);
        //
        // 全部
        //
        WnObj p = indexer.fetch(null, "a/b/");
        List<WnObj> list;

        //
        // 部分x2
        //
        list = indexer.getChildren(p, "^[cd]\\.txt$");
        assertEquals(2, list.size());
        // 确保一致排序
        list.sort(new Comparator<WnObj>() {
            public int compare(WnObj o1, WnObj o2) {
                return o1.name().compareTo(o2.name());
            }
        });
        // 校验
        assertEquals("c.txt", list.get(0).name());
        assertEquals("d.txt", list.get(1).name());

        //
        // 部分x1
        //
        list = indexer.getChildren(p, "!^[cd].txt$");
        assertEquals(1, list.size());
        // 校验
        assertEquals("e.txt", list.get(0).name());
    }

    @Test
    public void test_get_mount_parent() {
        WnObj p = new WnIoObj();
        p.id("@VirtualID");
        p.race(WnRace.DIR);
        p.path("/x/y");
        indexer.create(null, "a/b/c/d.txt", WnRace.FILE);

        WnObj b = indexer.fetch(null, "a/b");
        b.setParent(p);
        assertEquals("/x/y/b", b.path());

        WnObj d = indexer.fetch(b, "c/d.txt");

        assertEquals("/x/y/b/c/d.txt", d.path());

        WnObj c = d.parent();
        assertEquals("/x/y/b/c", c.path());

        WnObj b2 = c.parent();
        assertEquals("/x/y/b", b2.path());

        WnObj y = b2.parent();
        assertEquals("/x/y", y.path());
        assertTrue(y.isSameId(p));
    }

}
