package org.nutz.walnut.api.io;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.lang.Lang;
import org.nutz.lang.Mirror;
import org.nutz.walnut.impl.WnIoImpl;

public class WnIoTest extends AbstractWnStoreTest {

    @Test
    public void test_simple_create_rw_delete() {
        WnObj o = io.create(null, "/a/b/c", WnRace.FILE);

        assertEquals("/a/b/c", o.path());

        String str = "hello";
        io.writeText(o, str);
        String str2 = io.readText(o);

        assertEquals(str, str2);
        assertEquals(str.length(), o.len());
        assertEquals(Lang.sha1(str), o.sha1());

        // 验证一下数据库里记录的正确性
        o = io.fetch(null, "/a/b/c");
        assertEquals("/a/b/c", o.path());
        assertEquals(str, str2);
        assertEquals(str.length(), o.len());
        assertEquals(Lang.sha1(str), o.sha1());

        o = io.get(o.id());
        assertEquals(str.length(), o.len());
        assertEquals(Lang.sha1(str), o.sha1());

        // 必然有一条历史
        List<WnHistory> list = io.getHistoryList(o, -1);
        assertEquals(1, list.size());

        WnHistory his = list.get(0);
        assertEquals(Lang.sha1(str), his.sha1());
        assertEquals(str.length(), his.len());
        assertEquals(o.nanoStamp(), his.nanoStamp());

        // 删除
        io.delete(o);

        assertNull(io.fetch(null, "/a/b/c"));

        // 没历史了
        assertNull(io.getHistory(o, -1));
        assertEquals(0, io.getHistoryList(o, -1).size());

        // 但是之前的目录还在
        WnObj a = io.fetch(null, "/a");
        WnObj b = io.fetch(a, "b");

        assertEquals(a.id(), b.parentId());
        assertEquals("/a", a.path());
        assertEquals("/a/b", b.path());
    }

    @Override
    protected String getTreeMount() {
        return pp.check("mnt-mongo-a");
    }

    protected WnIo io;

    protected void on_before(PropertiesProxy pp) {
        super.on_before(pp);

        io = new WnIoImpl();
        Mirror.me(io).setValue(io, "tree", tree);
        Mirror.me(io).setValue(io, "indexer", indexer);
        Mirror.me(io).setValue(io, "stores", storeFactory);
    }

}
