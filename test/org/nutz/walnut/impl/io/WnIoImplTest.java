package org.nutz.walnut.impl.io;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.nutz.json.Json;
import org.nutz.lang.Each;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.lang.Xmls;
import org.nutz.lang.util.Callback;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.BaseIoTest;
import org.nutz.walnut.api.io.WalkMode;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.util.Wn;
import org.nutz.web.WebException;
import org.w3c.dom.Document;

public class WnIoImplTest extends BaseIoTest {

    @Test
    public void test_pid_to_self() {
        WnObj a = io.create(null, "a", WnRace.DIR);
        WnObj b = io.create(a, "b", WnRace.DIR);

        io.move(b, "/a/b");

        WnObj b2 = io.get(b.id());
        assertEquals("b", b2.name());
        assertEquals(a.id(), b2.parentId());

        b.setv("pid", b.id());
        try {
            io.set(b, "^pid$");
            fail();
        }
        catch (WebException e) {
            assertEquals("e.io.mv.parentToChild", e.getKey());
        }

        WnObj b3 = io.get(b.id());
        assertEquals("b", b3.name());
        assertEquals(a.id(), b3.parentId());

    }

    @Test
    public void test_setBy_query() {
        WnObj a = io.create(null, "/a.txt", WnRace.FILE);
        WnObj b = io.create(null, "/b.txt", WnRace.FILE);
        WnObj c = io.create(null, "/c.txt", WnRace.FILE);

        // 设置条件
        io.appendMeta(a, Lang.map("age:10, weight:10"));
        io.appendMeta(b, Lang.map("age:12, weight:14"));
        io.appendMeta(c, Lang.map("age:12, weight:10"));

        // 修改的字段
        // 修改一个，没条件应该是 null
        WnObj o = io.setBy(Wn.Q.map("{}"), Lang.map("realname:'xiaobai', nb:24"), false);
        assertNull(o);

        // 修改 "{age:12}" 的，只有一个修改了
        o = io.setBy(Wn.Q.map("{age:12}"), Lang.map("realname:'xiaobai', nb:24"), false);
        assertTrue(b.isSameId(o) || c.isSameId(o));
        assertEquals("xiaobai", io.getString(o.id(), "realname", null));
        assertEquals(24, io.getInt(o.id(), "nb", 0));

        // 修改 "{age:12, weight:11}" 的返回 null
        o = io.setBy(Wn.Q.map("{age:12, weight:11}"), Lang.map("brief:'AAA', x:3001"), false);
        assertNull(o);

        // 修改 "{age:12 weight:14}" 的返回只有一个被修改了
        o = io.setBy(Wn.Q.map("{age:12, weight:14}"), Lang.map("brief:'AAA', x:3001"), true);
        assertTrue(b.isSameId(o));
        assertEquals("AAA", io.getString(o.id(), "brief", null));
        assertEquals(3001, io.getInt(o.id(), "x", 0));
        assertEquals("AAA", o.getString("brief"));
        assertEquals(3001, o.getInt("x"));

    }

    @Test
    public void test_setBy() {
        WnObj a = io.create(null, "/a.txt", WnRace.FILE);

        WnObj a1 = io.setBy(a.id(), Lang.map("x:100,y:80"), false);
        assertEquals(a.id(), a1.id());
        assertEquals(-1, a1.getInt("x"));
        assertEquals(-1, a1.getInt("y"));

        WnObj a2 = io.get(a.id());
        assertEquals(a.id(), a2.id());
        assertEquals(100, a2.getInt("x"));
        assertEquals(80, a2.getInt("y"));
    }

    @Test
    public void test_copyData() {
        WnObj a = io.create(null, "/a.txt", WnRace.FILE);
        WnObj b = io.create(null, "/b.txt", WnRace.FILE);

        io.writeText(a, "haha");
        io.copyData(a, b);
        assertEquals(4, b.len());
        assertEquals(4, io.get(b.id()).len());
        assertTrue(a.isSameSha1(b.sha1()));
        assertTrue(a.isSameData(b.data()));
        assertTrue(io.get(a.id()).isSameSha1(io.get(b.id()).sha1()));
        assertTrue(io.get(a.id()).isSameData(io.get(b.id()).data()));

        io.writeText(a, "xyzmmm");
        io.copyData(a, b);
        assertEquals(6, b.len());
        assertEquals(6, io.get(b.id()).len());
        assertTrue(a.isSameSha1(b.sha1()));
        assertTrue(a.isSameData(b.data()));
        assertTrue(io.get(a.id()).isSameSha1(io.get(b.id()).sha1()));
        assertTrue(io.get(a.id()).isSameData(io.get(b.id()).data()));

    }

    @Test
    public void test_write_empty_file_by_ins() {
        WnObj o = io.create(null, "/a.txt", WnRace.FILE);

        io.writeText(o, "haha");
        assertEquals("haha", io.readText(o));
        assertEquals(4, o.len());
        assertEquals(4, io.get(o.id()).len());

        OutputStream ops = io.getOutputStream(o, 0);
        Streams.writeAndClose(ops, Lang.ins(""));
        assertEquals("", io.readText(o));
        assertEquals(0, o.len());
        assertEquals(0, io.get(o.id()).len());
    }

    @Test
    public void test_write_empty_file_by_text() {
        WnObj o = io.create(null, "/a.txt", WnRace.FILE);

        io.writeText(o, "haha");
        assertEquals("haha", io.readText(o));
        assertEquals(4, o.len());
        assertEquals(4, io.get(o.id()).len());

        io.writeText(o, "");
        assertEquals("", io.readText(o));
        assertEquals(0, o.len());
        assertEquals(0, io.get(o.id()).len());
    }

    @Test
    public void test_fetch_link_mnt_obj() {
        // 创建临时文件夹，并写入两个文件
        File d = Files.createDirIfNoExists("~/tmp/walnut/ua");
        Files.write(d.getAbsolutePath() + "/aa", "AAA");
        Files.write(d.getAbsolutePath() + "/bb", "BBB");

        Wn.WC().setSecurity(new WnEvalLink(io));
        try {

            // 创建一个映射文件一个链接文件
            WnObj oM = io.create(null, "/test/m", WnRace.DIR);
            WnObj o = io.create(null, "/mydir/a", WnRace.DIR);

            io.setMount(oM, "file://~/tmp/walnut/ua");

            o.link("/test/m");
            io.set(o, "^ln$");

            // 试着读取一下
            WnObj o_a = io.check(null, "/mydir/a/aa");
            assertEquals("/mydir/a/aa", o_a.path());
            assertEquals("AAA", io.readText(o_a));

            WnObj o_b = io.check(null, "/mydir/a/bb");
            assertEquals("/mydir/a/bb", o_b.path());
            assertEquals("BBB", io.readText(o_b));

        }
        finally {
            Wn.WC().setSecurity(null);
            Files.deleteDir(d);
        }
    }

    @Test
    public void test_create_delete_ln_obj() {
        Wn.WC().setSecurity(new WnEvalLink(io));
        try {
            WnObj c = io.create(null, "/a/b/c", WnRace.DIR);
            WnObj o = io.create(null, "/m", WnRace.FILE);
            o.link("/a");
            io.set(o, "^ln$");

            o = io.fetch(null, "/m");

            assertTrue(o.isLink());

            WnObj c2 = io.fetch(o, "b/c");
            assertEquals(c.id(), c2.id());

            io.delete(o);
            assertNull(io.fetch(null, "/m"));

        }
        finally {
            Wn.WC().setSecurity(null);
        }

    }

    @Test
    public void test_write_json_obj() {
        WnObj o = io.create(null, "/a", WnRace.FILE);
        NutMap map = Lang.map("x:100,y:10");
        io.writeJson(o, map, null);

        NutMap map2 = io.readJson(o, NutMap.class);
        assertTrue(Lang.equals(map, map2));
    }

    @Test
    public void test_fetch_by_special_path() {
        WnObj o = io.create(null, "/a/b/c/d", WnRace.FILE);

        WnObj f = io.fetch(null, "/a/b/c/d/.");
        assertEquals(o.id(), f.id());

        f = io.fetch(null, "/a/b/./c/./d");
        assertEquals(o.id(), f.id());

        f = io.fetch(null, "/a/b/../b/c/d");
        assertEquals(o.id(), f.id());
    }

    @Test
    public void test_quey_not() {
        WnObj a = io.create(null, "/a", WnRace.FILE);
        io.appendMeta(a, "x:100");
        WnObj b = io.create(null, "/b", WnRace.FILE);
        io.appendMeta(b, "x:99");
        WnObj c = io.create(null, "/c", WnRace.FILE);
        io.appendMeta(c, "x:98");

        WnQuery q;
        List<WnObj> list;

        q = new WnQuery();
        q.setv("x", Lang.map("$ne:100")).asc("nm");
        list = io.query(q);
        assertEquals(2, list.size());
        assertEquals(b.id(), list.get(0).id());
        assertEquals(c.id(), list.get(1).id());
    }

    @Test
    public void test_query_no_null() {
        WnObj a = io.create(null, "/a", WnRace.FILE);
        io.appendMeta(a, "alias:'aaa'");
        WnObj b = io.create(null, "/b", WnRace.FILE);
        io.appendMeta(b, "alias:null");
        WnObj c = io.create(null, "/c", WnRace.FILE);

        WnQuery q;
        List<WnObj> list;

        q = new WnQuery();
        q.setv("alias", Lang.map("$ne:null"));
        list = io.query(q);
        assertEquals(1, list.size());
        assertEquals(a.id(), list.get(0).id());

        q = new WnQuery();
        q.setv("alias", null).asc("nm");
        list = io.query(q);
        assertEquals(2, list.size());
        assertEquals(b.id(), list.get(0).id());
        assertEquals(c.id(), list.get(1).id());

        q = new WnQuery();
        q.setv("$and", Json.fromJson("[{alias:null}, {alias:{$exists:true}}]"));
        list = io.query(q);
        assertEquals(1, list.size());
        assertEquals(b.id(), list.get(0).id());

        q = new WnQuery();
        q.setv("alias", Lang.map("$exists:true")).asc("nm");
        list = io.query(q);
        assertEquals(2, list.size());
        assertEquals(a.id(), list.get(0).id());
        assertEquals(b.id(), list.get(1).id());

        q = new WnQuery();
        q.setv("alias", Lang.map("$exists:false"));
        list = io.query(q);
        assertEquals(1, list.size());
        assertEquals(c.id(), list.get(0).id());

    }

    @Test
    public void test_get_meta_directly() {
        WnObj o = io.create(null, "/abc", WnRace.FILE);
        o.setv("x", 100);
        o.setv("y", 4000);
        o.setv("map", "{txt:'haha'}");
        io.set(o, "^(x|y|map)$");

        String id = o.id();

        assertEquals(100, io.getInt(id, "x", -1));
        assertEquals(4000, io.getLong(id, "y", -1));
        assertEquals("abc", io.getString(id, "nm", null));
        NutMap map = io.getAs(id, "map", NutMap.class, null);
        assertEquals(1, map.size());
        assertEquals("haha", map.get("txt"));
    }

    @Test
    public void test_write_empty_text() {
        WnObj o = io.create(null, "/abc", WnRace.FILE);
        io.writeText(o, "AA");
        assertEquals(2, o.len());
        assertEquals(2, io.check(null, "/abc").len());

        io.writeText(o, "");
        assertEquals(0, o.len());
        assertEquals(0, io.check(null, "/abc").len());
    }

    @Test
    public void test_read_text() {
        WnObj o = io.create(null, "/a.xml", WnRace.FILE);
        String path = "org/nutz/walnut/impl/io/test.xml";
        String xml = Files.read(path);
        io.writeText(o, xml);

        String exp = Files.read(path);

        InputStream ins = io.getInputStream(o, 0);
        String str = Streams.readAndClose(Streams.utf8r(ins));

        assertEquals(exp, str);
    }

    @Test
    public void test_read_xml() {
        WnObj o = io.create(null, "/a.xml", WnRace.FILE);
        String path = "org/nutz/walnut/impl/io/test.xml";
        String xml = Files.read(path);
        io.writeText(o, xml);

        InputStream ins = io.getInputStream(o, 0);
        Document doc = Xmls.xml(ins);
        assertEquals("a", doc.getDocumentElement().getTagName());
    }

    @Test
    public void test_inc() {
        WnObj o = io.create(null, "/a/b/c", WnRace.FILE);
        o.setv("nb", 10);
        io.set(o, "nb");

        assertEquals(10, io.inc(o.id(), "nb", -1, false));
        assertEquals(9, io.inc(o.id(), "nb", 3, false));
        assertEquals(12, io.inc(o.id(), "nb", -2, false));

        assertEquals(9, io.inc(o.id(), "nb", -1, true));
        assertEquals(109, io.inc(o.id(), "nb", 100, true));
        assertEquals(-1, io.inc(o.id(), "nb", -110, true));

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
    public void test_update_dn() {
        WnObj o = io.create(null, "/a/b/c", WnRace.FILE);
        assertEquals("a", o.d0());
        assertEquals("b", o.d1());

        io.move(o, "/a");
        assertEquals("a", o.d0());
        assertEquals("c", o.d1());

        o.d0("H").d1("Y");
        io.set(o, "^(d0|d1)$");

        WnObj o2 = io.get(o.id());
        assertEquals("a", o2.d0());
        assertEquals("c", o2.d1());
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
    public void test_move_2() {
        io.create(null, "/abc", WnRace.DIR);

        WnObj x = io.create(null, "/x", WnRace.DIR);
        WnObj y = io.create(null, "/y", WnRace.DIR);
        WnObj z = io.create(null, "/z", WnRace.DIR);

        assertEquals("x", x.d0());
        assertEquals("y", y.d0());
        assertEquals("z", z.d0());
        assertNull(x.d1());
        assertNull(y.d1());
        assertNull(z.d1());

        io.move(z, "/abc/z");
        io.move(y, "/abc/y");
        io.move(x, "/abc/x");

        WnObj x2 = io.fetch(null, "/abc/x");
        WnObj y2 = io.fetch(null, "/abc/y");
        WnObj z2 = io.fetch(null, "/abc/z");

        assertEquals(x.id(), x2.id());
        assertEquals(y.id(), y2.id());
        assertEquals(z.id(), z2.id());
        assertEquals("abc", x2.d0());
        assertEquals("abc", y2.d0());
        assertEquals("abc", z2.d0());
        assertEquals("x", x2.d1());
        assertEquals("y", y2.d1());
        assertEquals("z", z2.d1());

        WnObj abc = io.check(null, "/abc");
        List<WnObj> list = io.query(Wn.Q.pid(abc).sortBy("nm", 1));

        assertEquals(3, list.size());
        assertEquals(x.id(), list.get(0).id());
        assertEquals(y.id(), list.get(1).id());
        assertEquals(z.id(), list.get(2).id());

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

    @Test
    public void test_mount_and_ln() {
        Wn.WC().setSecurity(new WnEvalLink(io));
        try {
            File f = Files.findFile("org/nutz/walnut");
            String mnt = "file://" + f.getAbsolutePath();

            WnObj b = io.create(null, "/a/b", WnRace.DIR);
            WnObj y = io.create(null, "/x/y", WnRace.DIR);

            // 挂载
            io.setMount(b, mnt);

            // 链接
            y.link("/a/b/impl/io");
            io.set(y, "^ln$");

            // 试图找找文件
            WnObj x = io.fetch(null, "/x");
            WnObj o = io.fetch(x, "y/WnIoImplTest.class");

            // 验证
            assertTrue(o.isMount());

        }
        finally {
            Wn.WC().setSecurity(null);
        }

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
    
    @Test
    public void test_array_push_pull() throws IOException {
        WnObj o = io.create(null, "/a/b/c", WnRace.FILE);
        String id = o.id();

        assertEquals("/a/b/c", o.path());

        // 新增
        io.push(id, "pets", "wendal", false);
        io.push(id, "pets", "zozoh", false);
        io.push(id, "pets", "pangwu", false);
        
        o = io.get(id);
        assertNotNull(o);
        assertNotNull(o.get("pets"));
        assertEquals(3, Lang.eleSize(o.get("pets")));
        Lang.each(o.get("pets"), new Each<Object>() {
            public void invoke(int index, Object ele, int length){
                assertTrue("wendal".equals(ele) || "zozoh".equals(ele) || "pangwu".equals(ele));
            }
        });
        
        // 减少
        io.pull(id, "pets", "zozoh", false);
        o = io.get(id);
        assertNotNull(o);
        assertNotNull(o.get("pets"));
        assertEquals(2, Lang.eleSize(o.get("pets")));
        Lang.each(o.get("pets"), new Each<Object>() {
            public void invoke(int index, Object ele, int length){
                assertTrue("wendal".equals(ele) || "pangwu".equals(ele));
            }
        });
        
        io.pull(id, "pets", "wendal", false);
        io.pull(id, "pets", "pangwu", false);

        o = io.get(id);
        assertNotNull(o);
        assertEquals(0, Lang.eleSize(o.get("pets")));
        
    }
    
    @Test
    public void test_array_push_pull_by_query() throws IOException {
        io.create(null, "/a/b/z", WnRace.FILE);
        WnObj o = io.create(null, "/a/b/c", WnRace.FILE);
        String id = o.id();
        String pid = o.parentId();

        assertEquals("/a/b/c", o.path());

        // 新增
        io.push(Wn.Q.id(id), "pets", "wendal");
        io.push(Wn.Q.pid(pid), "pets", "zozoh");
        io.push(Wn.Q.pid(pid), "pets", "pangwu");
        
        o = io.get(id);
        assertNotNull(o);
        assertNotNull(o.get("pets"));
        assertEquals(3, Lang.eleSize(o.get("pets")));
        Lang.each(o.get("pets"), new Each<Object>() {
            public void invoke(int index, Object ele, int length){
                assertTrue("wendal".equals(ele) || "zozoh".equals(ele) || "pangwu".equals(ele));
            }
        });
        
        // 减少
        io.pull(Wn.Q.pid(pid), "pets", "zozoh");
        o = io.get(id);
        assertNotNull(o);
        assertNotNull(o.get("pets"));
        assertEquals(2, Lang.eleSize(o.get("pets")));
        Lang.each(o.get("pets"), new Each<Object>() {
            public void invoke(int index, Object ele, int length){
                assertTrue("wendal".equals(ele) || "pangwu".equals(ele));
            }
        });
        
        io.pull(Wn.Q.pid(pid), "pets", "wendal");
        io.pull(Wn.Q.pid(pid), "pets", "pangwu");

        o = io.get(id);
        assertNotNull(o);
        assertEquals(0, Lang.eleSize(o.get("pets")));
        
    }
}
