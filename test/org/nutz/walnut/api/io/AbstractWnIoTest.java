package org.nutz.walnut.api.io;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.nutz.lang.Lang;
import org.nutz.lang.util.Callback;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.BaseIoTest;
import org.nutz.walnut.util.Wn;

public abstract class AbstractWnIoTest extends BaseIoTest {

    protected abstract String getAnotherTreeMount();

    // -------------------------------------------------------------
    // 测试类
    static class WalkTest implements Callback<WnObj> {
        StringBuilder sb;

        public void invoke(WnObj obj) {
            sb.append(obj.name());
        }

        public WalkTest reset() {
            sb = new StringBuilder();
            return this;
        }

        public String toString() {
            return sb.toString();
        }
    }

    // -------------------------------------------------------------

    @Test
    public void test_walk() {
        io.create(null, "/a/b/c", WnRace.FILE);
        io.create(null, "/a/b/d", WnRace.FILE);
        io.create(null, "/a/b/e", WnRace.FILE);
        io.create(null, "/a/x/y", WnRace.FILE);
        io.create(null, "/a/x/z", WnRace.FILE);

        WalkTest wt = new WalkTest();

        io.walk(null, wt.reset(), WalkMode.BREADTH_FIRST);
        assertEquals("abxcdeyz", wt.toString());

        io.walk(null, wt.reset(), WalkMode.DEPTH_NODE_FIRST);
        assertEquals("abcdexyz", wt.toString());

        io.walk(null, wt.reset(), WalkMode.DEPTH_LEAF_FIRST);
        assertEquals("cdebyzxa", wt.toString());

        io.walk(null, wt.reset(), WalkMode.LEAF_ONLY);
        assertEquals("cdeyz", wt.toString());
    }

    @Test
    public void test_move_between_tree() {
        WnObj home = io.create(null, "/a", WnRace.DIR);
        assertFalse(home.isMount(home.tree()));

        WnObj o = io.create(home, "b/c", WnRace.FILE);

        // 写入内容
        io.writeText(o, "I am great!");

        // 挂载到另外一棵树
        WnObj m = io.create(home, "data", WnRace.DIR);
        String mnt = getAnotherTreeMount();
        io.setMount(m, mnt);
        assertEquals("/a/data", m.path());
        assertEquals(mnt, m.mount());
        assertTrue(m.isMount(home.tree()));

        // 清除数据
        WnTree tree = treeFactory.check(m);
        tree._clean_for_unit_test();

        // 创建目标
        WnObj ta = io.create(m, "ta", WnRace.DIR);
        assertTrue(ta.tree().equals(tree));
        assertEquals("/a/data/ta", ta.path());

        ta = io.get(ta.id());
        assertEquals("/a/data/ta", ta.path());

        // 移动
        WnObj m2 = io.move(o, "/a/data/ta");
        assertEquals(o.len(), m2.len());
        assertEquals("I am great!", io.readText(m2));
        assertTrue(m2.tree().equals(tree));

        // 原节点不在了
        assertNull(io.fetch(null, "/a/b/c"));

        // 目标节点存在
        WnObj m3 = io.fetch(null, "/a/data/ta/c");
        assertEquals(o.len(), m3.len());
        assertEquals("I am great!", io.readText(m3));

        // 目标节点属于第二棵树
        assertTrue(m3.tree().equals(tree));
    }

    @Test
    public void test_read_meta() {
        WnObj o = io.create(null, "/a", WnRace.FILE);
        io.appendMeta(o, "x:100, y:99");

        o = io.create(null, Wn.metaPath("/a"), WnRace.FILE);
        System.out.println(io.readText(o));
        NutMap map = io.readJson(o, NutMap.class);

        assertEquals(100, map.getInt("x"));
        assertEquals(99, map.getInt("y"));
    }

    @Test
    public void test_append_meta() {
        WnObj o = io.create(null, "/a", WnRace.FILE);
        io.appendMeta(o, "x:100, y:99");
        assertEquals(100, o.getInt("x"));
        assertEquals(99, o.getInt("y"));

        WnObj o2 = io.fetch(o, "/a");
        assertEquals(o.type(), o2.type());
        assertEquals(o.name(), o2.name());
        assertEquals(o.path(), o2.path());
        assertEquals(100, o2.getInt("x"));
        assertEquals(99, o2.getInt("y"));

        io.appendMeta(o, "{z:888}");
        assertEquals(100, o.getInt("x"));
        assertEquals(99, o.getInt("y"));
        assertEquals(888, o.getInt("z"));

        o2 = io.fetch(o, "/a");
        assertEquals(o.type(), o2.type());
        assertEquals(o.name(), o2.name());
        assertEquals(o.path(), o2.path());
        assertEquals(100, o2.getInt("x"));
        assertEquals(99, o2.getInt("y"));
        assertEquals(888, o2.getInt("z"));
    }

    @Test
    public void test_write_meta() {
        WnObj o = io.create(null, "/a", WnRace.FILE);
        o.setv("x", 100).setv("y", 99);
        io.writeMeta(o, "^x|y$");
        assertEquals(100, o.getInt("x"));
        assertEquals(99, o.getInt("y"));

        WnObj o2 = io.fetch(o, "/a");
        assertEquals(o.type(), o2.type());
        assertEquals(o.name(), o2.name());
        assertEquals(o.path(), o2.path());
        assertEquals(100, o2.getInt("x"));
        assertEquals(99, o2.getInt("y"));
    }

    @Test
    public void test_write_meta2() {
        io.create(null, "/a", WnRace.FILE);
        WnObj o = io.fetch(null, Wn.metaPath("/a"));
        io.writeMeta(o, "x:100,y:99");
        assertEquals(100, o.getInt("x"));
        assertEquals(99, o.getInt("y"));

        WnObj o2 = io.fetch(o, "/a");
        assertEquals(o.type(), o2.type());
        assertEquals(o.name(), o2.name());
        assertEquals(o.path(), o2.path());
        assertEquals(100, o2.getInt("x"));
        assertEquals(99, o2.getInt("y"));

        io.writeMeta(o, "{z:888}");
        assertEquals(-1, o.getInt("x"));
        assertEquals(-1, o.getInt("y"));
        assertEquals(888, o.getInt("z"));

        o2 = io.fetch(o, "/a");
        assertEquals(o.type(), o2.type());
        assertEquals(o.name(), o2.name());
        assertEquals(o.path(), o2.path());
        assertEquals(-1, o2.getInt("x"));
        assertEquals(-1, o2.getInt("y"));
        assertEquals(888, o2.getInt("z"));
    }

    @Test
    public void test_move() {
        WnObj o = io.create(null, "/a/b/c", WnRace.FILE);
        assertEquals("/a/b/c", o.path());
        String oid = o.id();

        io.create(null, "/x/y", WnRace.DIR);
        io.move(o, "/x/y/z");

        WnObj o2 = io.fetch(null, "/x/y/z");
        assertEquals(oid, o2.id());
    }

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

}
