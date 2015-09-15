package org.nutz.walnut.impl.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.nutz.lang.Lang;
import org.nutz.lang.util.Callback;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.BaseIoTest;
import org.nutz.walnut.api.io.WalkMode;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.util.Wn;
import org.nutz.web.WebException;

public class WnIoImplTest extends BaseIoTest {

    @Test
    public void test_inc() {
        WnObj o = io.create(null, "/a/b/c", WnRace.FILE);
        o.setv("nb", 10);
        io.set(o, "nb");

        assertEquals(10, io.inc(o.id(), "nb", -1));
        assertEquals(9, io.inc(o.id(), "nb", 3));
        assertEquals(12, io.inc(o.id(), "nb", -2));
    }

    @Test
    public void test_write_empty() {
        String path = "/a/b/c";
        WnObj o = io.create(null, path, WnRace.FILE);
        io.writeText(o, "");
        WnObj o2 = io.check(null, "/a/b/c");
        assertNull(o2.get("_write_handle"));
    }

    /**
     * for issue #29
     */
    @Test
    public void test_query_and_read() {
        WnObj o = io.create(null, "/a/b/c", WnRace.FILE);
        io.writeText(o, "hello");

        WnQuery q = new WnQuery();
        q.setv("nm", "c");

        o = io.query(q).get(0);
        String txt = io.readText(o);

        assertEquals("hello", txt);
    }

    @Test
    public void test_short_id() {
        WnObj p = io.create(null, "/tt", WnRace.DIR);
        WnObj a = io.createById(p, "a003vgv123c", "a", WnRace.DIR);
        WnObj b = io.createById(p, "a029hd83219", "b", WnRace.DIR);

        try {
            io.get("a0");
            fail();
        }
        catch (WebException e) {
            assertEquals("e.io.obj.get.shortid : a0", e.toString());
        }

        WnObj a2 = io.checkById("a00");
        assertEquals(a.id(), a2.id());
        assertEquals(a.path(), a2.path());
        WnObj b2 = io.checkById("a02");
        assertEquals(b.id(), b2.id());
        assertEquals(b.path(), b2.path());
    }

    @Test
    public void test_create_by_id() {
        WnObj a = io.createById(null, "id0", "a", WnRace.DIR);
        WnObj b = io.createById(a, "id1", "b", WnRace.DIR);
        WnObj c = io.createById(b, "id2", "c", WnRace.FILE);

        assertEquals("id0", a.id());
        assertEquals("a", a.name());
        assertEquals("id1", b.id());
        assertEquals("b", b.name());
        assertEquals("id2", c.id());
        assertEquals("c", c.name());

        a = io.check(null, "/a");
        b = io.check(null, "/a/b");
        c = io.check(null, "/a/b/c");

        assertEquals("id0", a.id());
        assertEquals("a", a.name());
        assertEquals("id1", b.id());
        assertEquals("b", b.name());
        assertEquals("id2", c.id());
        assertEquals("c", c.name());

        a = io.checkById("id0");
        b = io.checkById("id1");
        c = io.checkById("id2");

        assertEquals("id0", a.id());
        assertEquals("a", a.name());
        assertEquals("id1", b.id());
        assertEquals("b", b.name());
        assertEquals("id2", c.id());
        assertEquals("c", c.name());
    }

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

        List<WnObj> l0 = new ArrayList<WnObj>(2);
        List<WnObj> l1 = new ArrayList<WnObj>(2);

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

        // 等几毫秒 ...
        Thread.sleep(5);

        // 修改子节点，同步时间改变了
        io.rename(c, "c");
        st = io.check(null, "/a/b").syncTime();
        assertTrue(st > last_st);
        last_st = st;

        // 等几毫秒 ...
        Thread.sleep(5);

        // 修改孙文件，同步时间改变了
        io.writeText(d, "hello world");
        st = io.check(null, "/a/b").syncTime();
        assertTrue(st > last_st);
        last_st = st;

        // 等几毫秒 ...
        Thread.sleep(5);

        // 孙文件没改动，同步时间不改变
        io.writeText(d, "hello world");
        st = io.check(null, "/a/b").syncTime();
        assertTrue(st == last_st);

        // 等几毫秒 ...
        Thread.sleep(5);

        // 添加一个子节点，同步时间改变
        WnObj oNew = io.create(null, "/a/b/c/newfile", WnRace.FILE);
        st = io.check(null, "/a/b").syncTime();
        assertTrue(st > last_st);
        last_st = st;

        // 等几毫秒 ...
        Thread.sleep(5);

        // 删除一个子节点，同步时间改变
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

    @Test
    public void test_read_write_link_file() throws Exception {
        Wn.WC().setSecurity(new WnEvalLink(io));
        WnObj a = io.create(null, "/linktest/a", WnRace.FILE);
        WnObj b = io.create(null, "/linktest/b", WnRace.FILE);
        try {
            io.writeText(a, "haha");
            io.appendMeta(b, "ln:'/linktest/a'");

            assertEquals("haha", io.readText(a));
            assertEquals("haha", io.readText(b));

            io.writeText(b, "hehe");

            assertEquals("hehe", io.readText(a));
            assertEquals("hehe", io.readText(b));

        }
        finally {
            io.delete(a);
            io.delete(b);
            Wn.WC().setSecurity(null);
        }
    }

    @Test
    public void test_read_write_link_dir() throws Exception {
        Wn.WC().setSecurity(new WnEvalLink(io));
        WnObj a = io.create(null, "/linktest/a", WnRace.DIR);
        WnObj a1 = io.create(null, "/linktest/a/a1", WnRace.FILE);
        WnObj b = io.create(null, "/linktest/b", WnRace.DIR);
        try {
            io.writeText(a1, "haha");
            io.appendMeta(b, "ln:'/linktest/a'");

            assertEquals("haha", io.readText(io.fetch(null, "/linktest/a/a1")));
            assertEquals("haha", io.readText(io.fetch(null, "/linktest/b/a1")));

            io.writeText(io.fetch(null, "/linktest/b/a1"), "hehe");

            assertEquals("hehe", io.readText(io.fetch(null, "/linktest/a/a1")));
            assertEquals("hehe", io.readText(io.fetch(null, "/linktest/b/a1")));

        }
        finally {
            io.delete(a1);
            io.delete(a);
            io.delete(b);
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
        String oid = o.id();

        io.create(null, "/x/y", WnRace.DIR);
        io.move(o, "/x/y/z");
        assertEquals("z", o.name());
        assertEquals("/x/y/z", o.path());

        o = io.fetch(null, "/x/y/z");
        assertEquals(oid, o.id());
        assertEquals("z", o.name());
        assertEquals("/x/y/z", o.path());
    }

    @Test
    public void test_rewrite_content() {
        WnObj o = io.create(null, "/a/b/c", WnRace.FILE);

        io.writeText(o, "ABC");
        io.writeText(o, "A");

        assertEquals(1, o.len());
        assertEquals("A", io.readText(o));
        assertEquals(Lang.sha1("A"), o.sha1());
    }

    @Test
    public void test_simple_append() {
        WnObj o = io.create(null, "/a/b/c", WnRace.FILE);

        io.writeText(o, "A");
        io.appendText(o, "B");

        assertEquals(2, o.len());
        assertEquals(Lang.sha1("AB"), o.sha1());
        assertEquals("AB", io.readText(o));
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

        // 删除
        io.delete(o);

        assertNull(io.fetch(null, "/a/b/c"));

        // 但是之前的目录还在
        WnObj a = io.fetch(null, "/a");
        WnObj b = io.fetch(a, "b");

        assertEquals(a.id(), b.parentId());
        assertEquals("/a", a.path());
        assertEquals("/a/b", b.path());
    }

    @Test
    public void test_random_write() throws IOException {
        WnObj o = io.create(null, "/a/b/c", WnRace.FILE);

        assertEquals("/a/b/c", o.path());

        io.writeText(o, "abc");

        String hid = io.open(o, Wn.S.WM);
        io.seek(hid, 1);
        io.write(hid, "z".getBytes());
        io.close(hid);

        assertEquals("azc", io.readText(o));
    }
}
