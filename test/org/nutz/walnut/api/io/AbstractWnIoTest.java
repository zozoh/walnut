package org.nutz.walnut.api.io;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.util.Callback;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.BaseIoTest;
import org.nutz.walnut.impl.io.WnEvalLink;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnContext;

public abstract class AbstractWnIoTest extends BaseIoTest {

    protected abstract String getAnotherTreeMount();

    @Test
    public void test_move_chtype() {
        io.create(null, "/m/n", WnRace.DIR);
        WnObj c = io.create(null, "/a/b/c", WnRace.FILE);
        WnObj o = io.move(c, "/m/n/xyz.js");

        assertEquals("xyz.js", o.name());
        assertEquals("/m/n/xyz.js", o.path());
        assertEquals("js", o.type());
        assertEquals(mimes.getMime("js"), o.mime());
    }

    @Test
    public void test_rename_chtype() {
        WnObj c = io.create(null, "/a/b/c", WnRace.FILE);
        WnObj o = io.rename(c, "xyz.js");

        assertEquals("xyz.js", o.name());
        assertEquals("/a/b/xyz.js", o.path());
        assertEquals("js", o.type());
        assertEquals(mimes.getMime("js"), o.mime());
    }

    @Test
    public void test_rename() {
        WnObj c = io.create(null, "/a/b/c", WnRace.FILE);
        WnObj o = io.rename(c, "xyz");

        assertEquals("xyz", o.name());
        assertEquals("/a/b/xyz", o.path());
    }

    @Test
    public void test_load_parents_twice() {
        WnObj c = io.create(null, "/a/b/c", WnRace.FILE);

        List<WnNode> l0 = new ArrayList<WnNode>(2);
        List<WnNode> l1 = new ArrayList<WnNode>(2);

        c.loadParents(l0, false);
        assertEquals(2, l0.size());
        assertEquals("a", l0.get(0).name());
        assertEquals("b", l0.get(1).name());

        c.loadParents(l1, false);
        assertEquals(2, l1.size());
        assertEquals("a", l1.get(0).name());
        assertEquals("b", l1.get(1).name());
    }

    @Test
    public void test_sync_time() throws InterruptedException {
        io.create(null, "/a/b/c/d", WnRace.FILE);
        WnObj b = io.check(null, "/a/b");

        // 同步时间不存在
        long st = b.syncTime();
        assertTrue(st <= 0);

        // 修改自身，同步时间还是不存在
        // io.changeType(b, "abc");
        io.appendMeta(b, "tp:'abc'");
        assertTrue(io.check(null, "/a/b").syncTime() <= 0);

        // 修改子节点，同步时间不存在
        WnObj c = io.check(b, "c");
        io.rename(c, "haha");
        assertTrue(io.check(null, "/a/b").syncTime() <= 0);

        // 修改孙文件，同步时间不存在
        WnObj d = io.check(c, "d");
        io.writeText(d, "hello");
        assertTrue(io.check(null, "/a/b").syncTime() <= 0);

        // 添加同步时间描述
        long last_st = System.currentTimeMillis();
        io.appendMeta(b, "st:" + last_st);

        Thread.sleep(5);

        // 修改子节点，同步时间改变了
        Thread.sleep(5);
        io.rename(c, "c");
        st = io.check(null, "/a/b").syncTime();
        assertTrue(st > last_st);
        last_st = st;

        // 修改孙文件，同步时间改变了
        Thread.sleep(5);
        io.writeText(d, "hello world");
        st = io.check(null, "/a/b").syncTime();
        assertTrue(st > last_st);
        last_st = st;

        // 孙文件没改动，同步时间不改变
        Thread.sleep(5);
        io.writeText(d, "hello world");
        st = io.check(null, "/a/b").syncTime();
        assertTrue(st == last_st);

        // 添加一个子节点，同步时间改变
        Thread.sleep(5);
        WnObj oNew = io.create(null, "/a/b/c/newfile", WnRace.FILE);
        st = io.check(null, "/a/b").syncTime();
        assertTrue(st > last_st);
        last_st = st;

        // 删除一个子节点，同步时间改变
        Thread.sleep(5);
        io.delete(oNew);
        st = io.check(null, "/a/b").syncTime();
        assertTrue(st > last_st);
        last_st = st;

    }

    /**
     * mount 一个节点后，unmount 它，会回到原来的 mount
     */
    @Test
    public void test_mount_unmount() {
        WnObj b = io.create(null, "/a/b", WnRace.DIR);

        String oldmnt = b.mount();

        io.setMount(b, "file://~/noexists/dir");
        assertEquals("file://~/noexists/dir", b.mount());

        b = io.checkById(b.id());
        assertEquals("file://~/noexists/dir", b.mount());

        io.setMount(b, null);
        assertEquals(oldmnt, b.mount());

        b = io.checkById(b.id());
        assertEquals(oldmnt, b.mount());
    }

    /**
     * for issue #17
     */
    @Test
    public void test_mount_local_auto_use_root_cgm() {
        WnObj b = io.create(null, "/a/b", WnRace.DIR);

        // 创建临时目录
        try {
            Files.createFileIfNoExists2("~/.walnut/tmp/dir/x/y.txt");

            // 挂载目录
            WnContext wc = Wn.WC();
            String me = wc.checkMe();
            String grp = wc.checkGroup();
            wc.me("nobody", "nogrp");
            try {
                io.setMount(b, "file://~/.walnut/tmp/dir");

                // 获取
                WnObj o = io.check(null, "/a/b/x/y.txt");
                assertEquals("y.txt", o.name());
                assertTrue(o.isFILE());
                assertEquals(me, o.creator());
                assertEquals(me, o.mender());
                assertEquals(grp, o.group());

                o = io.check(null, "/a/b/x");
                assertEquals("x", o.name());
                assertTrue(o.isDIR());
                assertEquals(me, o.creator());
                assertEquals(me, o.mender());
                assertEquals(grp, o.group());

                o = io.check(null, "/a/b");
                assertEquals("b", o.name());
                assertTrue(o.isDIR());
                assertEquals(me, o.creator());
                assertEquals(me, o.mender());
                assertEquals(grp, o.group());

            }
            finally {
                wc.me(me, grp);
            }
        }
        // 删除临时目录
        finally {
            Files.deleteDir(Files.findFile("~/.walnut/tmp"));
        }
    }

    @Test
    public void test_simple_link() {
        Wn.WC().setSecurity(new WnEvalLink(io));
        try {
            io.create(null, "/a/b/c/e/f", WnRace.FILE);
            WnObj z = io.create(null, "/x/y/z", WnRace.DIR);

            io.appendMeta(z, "ln:'/a/b'");

            WnObj o;

            o = io.fetch(null, "/x/y/z");
            assertEquals("z", o.name());
            assertEquals("/x/y/z", o.path());

            o = io.fetch(null, "/x/y/z/c");
            assertEquals("c", o.name());
            assertEquals("/x/y/z/c", o.path());

        }
        finally {
            Wn.WC().setSecurity(null);
        }

    }

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
    public void test_simple_create_right() {
        WnObj o = io.create(null, "/a", WnRace.DIR);
        String g = Wn.WC().checkGroup();
        String c = Wn.WC().checkMe();

        assertEquals(g, o.group());
        assertEquals(c, o.mender());
        assertEquals(c, o.creator());
        assertEquals(0750, o.mode());
    }

    @Test
    public void test_get_top_dir() {
        io.create(null, "/a", WnRace.DIR);
        WnObj o = io.check(null, "/a");
        assertEquals("a", o.name());
    }

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
