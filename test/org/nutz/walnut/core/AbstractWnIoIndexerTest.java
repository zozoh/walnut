package org.nutz.walnut.core;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.nutz.json.Json;
import org.nutz.lang.Each;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.util.Wn;
import org.nutz.web.WebException;

public abstract class AbstractWnIoIndexerTest extends IoCoreTest {

    /**
     * 子类需要设置这个实例
     */
    protected WnIoIndexer indexer;

    /**
     * 特殊文件名
     */
    @Test
    public void test_00() {
        WnObj dC = indexer.create(null, "/a/b/c", WnRace.DIR);
        WnObj fX = indexer.create(dC, "x(1).txt", WnRace.FILE);

        WnObj dB = indexer.check(null, "/a/b");
        assertTrue(dB.isDIR());
        assertEquals("/a/b/", dB.getRegularPath());

        WnObj X2 = indexer.fetch(dB, "c/x(1).txt");

        // 对象树
        assertEquals(fX.id(), X2.id());
        assertEquals(dC.id(), X2.parentId());
        assertEquals("x(1).txt", X2.name());
        assertTrue(X2.isFILE());

        assertEquals("/a/b/c/x(1).txt", X2.path());

        // 权限
        assertEquals("root", X2.creator());
        assertEquals("root", X2.mender());
        assertEquals("root", X2.group());
        assertEquals(dB.mode(), X2.mode());

        // 内容相关
        assertEquals("txt", X2.type());
        assertEquals("text/plain", X2.mime());
        assertNull(X2.sha1());
        assertEquals(0, X2.len());

        // 特殊元数据
        assertEquals("a", X2.d0());
        assertEquals("b", X2.d1());

        // 时间戳
        assertTrue(X2.createTime() > 5000);
        assertTrue(X2.lastModified() > 5000);

    }

    /**
     * 移动测试
     */
    @Test
    public void test_01() {
        WnObj a = indexer.create(null, "a", WnRace.DIR);
        WnObj b = indexer.create(a, "b", WnRace.DIR);

        indexer.move(b, "/a/b");

        WnObj b2 = indexer.get(b.id());
        assertEquals("b", b2.name());
        assertEquals(a.id(), b2.parentId());

        b.setv("pid", b.id());
        try {
            indexer.set(b, "^pid$");
            fail();
        }
        catch (WebException e) {
            assertEquals("e.io.mv.parentToChild", e.getKey());
        }

        WnObj b3 = indexer.get(b.id());
        assertEquals("b", b3.name());
        assertEquals(a.id(), b3.parentId());
    }

    /**
     * 按条件批量修改元数据
     */
    @Test
    public void test_02() {
        WnObj a = indexer.create(null, "/a.txt", WnRace.FILE);
        WnObj b = indexer.create(null, "/b.txt", WnRace.FILE);
        WnObj c = indexer.create(null, "/c.txt", WnRace.FILE);

        // 设置条件
        indexer.setBy(a.id(), Lang.map("age:10, weight:10"), false);
        indexer.setBy(b.id(), Lang.map("age:12, weight:14"), false);
        indexer.setBy(c.id(), Lang.map("age:12, weight:10"), false);

        // 修改的字段
        // 修改一个，没条件应该是 null
        WnObj o = indexer.setBy(Wn.Q.map("{}"), Lang.map("realname:'xiaobai', nb:24"), false);
        assertNull(o);

        // 修改 "{age:12}" 的，只有一个修改了
        o = indexer.setBy(Wn.Q.map("{age:12}"), Lang.map("realname:'xiaobai', nb:24"), false);
        assertTrue(b.isSameId(o) || c.isSameId(o));
        assertEquals("xiaobai", indexer.getString(o.id(), "realname", null));
        assertEquals(24, indexer.getInt(o.id(), "nb", 0));

        // 修改 "{age:12, weight:11}" 的返回 null
        o = indexer.setBy(Wn.Q.map("{age:12, weight:11}"), Lang.map("brief:'AAA', x:3001"), false);
        assertNull(o);

        // 修改 "{age:12 weight:14}" 的返回只有一个被修改了
        o = indexer.setBy(Wn.Q.map("{age:12, weight:14}"), Lang.map("brief:'AAA', x:3001"), true);
        assertTrue(b.isSameId(o));
        assertEquals("AAA", indexer.getString(o.id(), "brief", null));
        assertEquals(3001, indexer.getInt(o.id(), "x", 0));
        assertEquals("AAA", o.getString("brief"));
        assertEquals(3001, o.getInt("x"));
    }

    /**
     * 简单设置元数据
     */
    @Test
    public void test_03() {
        WnObj a = indexer.create(null, "/a.txt", WnRace.FILE);

        WnObj a1 = indexer.setBy(a.id(), Lang.map("x:100,y:80"), false);
        assertEquals(a.id(), a1.id());
        assertEquals(-1, a1.getInt("x"));
        assertEquals(-1, a1.getInt("y"));

        WnObj a2 = indexer.get(a.id());
        assertEquals(a.id(), a2.id());
        assertEquals(100, a2.getInt("x"));
        assertEquals(80, a2.getInt("y"));
    }

    /**
     * 路径符号 . 和 ..
     */
    @Test
    public void test_04() {
        WnObj o = indexer.create(null, "/a/b/c/d", WnRace.FILE);

        WnObj f = indexer.fetch(null, "/a/b/c/d/.");
        assertEquals(o.id(), f.id());

        f = indexer.fetch(null, "/a/b/./c/./d");
        assertEquals(o.id(), f.id());

        f = indexer.fetch(null, "/a/b/../b/c/d");
        assertEquals(o.id(), f.id());
    }

    /**
     * 查询+排序
     */
    @Test
    public void test_05() {
        WnObj a = indexer.create(null, "/HT/a", WnRace.FILE);
        indexer.setBy(a.id(), Lang.map("x:100"), false);
        WnObj b = indexer.create(null, "/HT/b", WnRace.FILE);
        indexer.setBy(b.id(), Lang.map("x:99"), false);
        WnObj c = indexer.create(null, "/HT/c", WnRace.FILE);
        indexer.setBy(c.id(), Lang.map("x:98"), false);

        WnObj oHT = indexer.check(null, "/HT");

        WnQuery q = Wn.Q.pid(oHT);
        q.setv("x", Lang.map("$ne:100")).asc("nm");
        List<WnObj> list = indexer.query(q);
        assertEquals(2, list.size());
        assertEquals(b.id(), list.get(0).id());
        assertEquals(c.id(), list.get(1).id());
    }

    /**
     * 基础查询
     */
    @Test
    public void test_06() {
        WnObj a = indexer.create(null, "/HT/a", WnRace.FILE);
        indexer.setBy(a.id(), Lang.map("alias:'aaa'"), false);
        WnObj b = indexer.create(null, "/HT/b", WnRace.FILE);
        indexer.setBy(b.id(), Lang.map("alias:null"), false);
        WnObj c = indexer.create(null, "/HT/c", WnRace.FILE);

        WnObj oHT = indexer.check(null, "/HT");

        WnQuery q = Wn.Q.pid(oHT);
        q.setv("alias", Lang.map("$ne:null"));
        List<WnObj> list = indexer.query(q);
        assertEquals(1, list.size());
        assertEquals(a.id(), list.get(0).id());

        q = Wn.Q.pid(oHT);
        q.setv("alias", null).asc("nm");
        list = indexer.query(q);
        assertEquals(2, list.size());
        assertEquals(b.id(), list.get(0).id());
        assertEquals(c.id(), list.get(1).id());

        test_06_exists(a, b, c, oHT);
    }

    // 某些子类不支持 exists
    protected void test_06_exists(WnObj a, WnObj b, WnObj c, WnObj oHT) {
        WnQuery q;
        List<WnObj> list;
        q = Wn.Q.pid(oHT);
        q.setv("$and", Json.fromJson("[{alias:null}, {alias:{$exists:true}}]"));
        list = indexer.query(q);
        assertEquals(1, list.size());
        assertEquals(b.id(), list.get(0).id());

        q = Wn.Q.pid(oHT);
        q.setv("alias", Lang.map("$exists:true")).asc("nm");
        list = indexer.query(q);
        assertEquals(2, list.size());
        assertEquals(a.id(), list.get(0).id());
        assertEquals(b.id(), list.get(1).id());

        q = Wn.Q.pid(oHT);
        q.setv("alias", Lang.map("$exists:false"));
        list = indexer.query(q);
        assertEquals(1, list.size());
        assertEquals(c.id(), list.get(0).id());
    }

    /**
     * 自定义元数据测试
     */
    @Test
    public void test_07() {
        WnObj o = indexer.create(null, "/abc", WnRace.FILE);
        o.setv("x", 100);
        o.setv("y", 4000);
        o.setv("map", Lang.map("{txt:'haha'}"));
        indexer.set(o, "^(x|y|map)$");

        String id = o.id();

        assertEquals(100, indexer.getInt(id, "x", -1));
        assertEquals(4000, indexer.getLong(id, "y", -1));
        assertEquals("abc", indexer.getString(id, "nm", null));
        NutMap map = indexer.getAs(id, "map", NutMap.class, null);
        assertEquals(1, map.size());
        assertEquals("haha", map.get("txt"));
    }

    /**
     * inc 函数测试
     */
    @Test
    public void test_08() {
        WnObj o = indexer.create(null, "/a/b/c", WnRace.FILE);
        o.setv("nb", 10);
        indexer.set(o, "nb");

        assertEquals(10, indexer.inc(o.id(), "nb", -1, false));
        assertEquals(9, indexer.inc(o.id(), "nb", 3, false));
        assertEquals(12, indexer.inc(o.id(), "nb", -2, false));

        assertEquals(9, indexer.inc(o.id(), "nb", -1, true));
        assertEquals(109, indexer.inc(o.id(), "nb", 100, true));
        assertEquals(-1, indexer.inc(o.id(), "nb", -110, true));
    }

    /**
     * 短ID
     */
    @Test
    public void test_09() {
        WnObj p = indexer.create(null, "/tt", WnRace.DIR);
        WnObj a = indexer.createById(p, "a003vgv123c", "a", WnRace.DIR);
        WnObj b = indexer.createById(p, "a029hd83219", "b", WnRace.DIR);

        try {
            indexer.get("a0");
            fail();
        }
        catch (WebException e) {
            assertEquals("e.io.obj.get.shortid : a0", e.toString());
        }

        WnObj a2 = indexer.checkById("a00");
        assertEquals(a.id(), a2.id());
        assertEquals(a.path(), a2.path());
        WnObj b2 = indexer.checkById("a02");
        assertEquals(b.id(), b2.id());
        assertEquals(b.path(), b2.path());
    }

    /**
     * 简单测试：createById
     */
    @Test
    public void test_10() {
        WnObj a = indexer.createById(null, "id0", "a", WnRace.DIR);
        WnObj b = indexer.createById(a, "id1", "b", WnRace.DIR);
        WnObj c = indexer.createById(b, "id2", "c", WnRace.FILE);

        assertEquals("id0", a.id());
        assertEquals("a", a.name());
        assertEquals("id1", b.id());
        assertEquals("b", b.name());
        assertEquals("id2", c.id());
        assertEquals("c", c.name());

        a = indexer.check(null, "/a");
        b = indexer.check(null, "/a/b");
        c = indexer.check(null, "/a/b/c");

        assertEquals("id0", a.id());
        assertEquals("a", a.name());
        assertEquals("id1", b.id());
        assertEquals("b", b.name());
        assertEquals("id2", c.id());
        assertEquals("c", c.name());

        a = indexer.checkById("id0");
        b = indexer.checkById("id1");
        c = indexer.checkById("id2");

        assertEquals("id0", a.id());
        assertEquals("a", a.name());
        assertEquals("id1", b.id());
        assertEquals("b", b.name());
        assertEquals("id2", c.id());
        assertEquals("c", c.name());
    }

    /**
     * 移动并修改文件类型
     */
    @Test
    public void test_11() {
        indexer.create(null, "/m/n", WnRace.DIR);
        WnObj c = indexer.create(null, "/a/b/c", WnRace.FILE);
        WnObj o = indexer.move(c, "/m/n/xyz.js");

        assertEquals("xyz.js", o.name());
        assertEquals("/m/n/xyz.js", o.path());
        assertEquals("js", o.type());
        assertEquals(setup.getMimes().getMime("js"), o.mime());
    }

    /**
     * 移动测试2
     */
    @Test
    public void test_12() {
        indexer.create(null, "/abc", WnRace.DIR);

        WnObj x = indexer.create(null, "/x", WnRace.DIR);
        WnObj y = indexer.create(null, "/y", WnRace.DIR);
        WnObj z = indexer.create(null, "/z", WnRace.DIR);

        assertEquals("x", x.d0());
        assertEquals("y", y.d0());
        assertEquals("z", z.d0());
        assertNull(x.d1());
        assertNull(y.d1());
        assertNull(z.d1());

        indexer.move(z, "/abc/z");
        indexer.move(y, "/abc/y");
        indexer.move(x, "/abc/x");

        WnObj x2 = indexer.fetch(null, "/abc/x");
        WnObj y2 = indexer.fetch(null, "/abc/y");
        WnObj z2 = indexer.fetch(null, "/abc/z");

        assertEquals(x.id(), x2.id());
        assertEquals(y.id(), y2.id());
        assertEquals(z.id(), z2.id());
        assertEquals("abc", x2.d0());
        assertEquals("abc", y2.d0());
        assertEquals("abc", z2.d0());
        assertEquals("x", x2.d1());
        assertEquals("y", y2.d1());
        assertEquals("z", z2.d1());

        WnObj abc = indexer.check(null, "/abc");
        List<WnObj> list = indexer.query(Wn.Q.pid(abc).sortBy("nm", 1));

        assertEquals(3, list.size());
        assertEquals(x.id(), list.get(0).id());
        assertEquals(y.id(), list.get(1).id());
        assertEquals(z.id(), list.get(2).id());

    }

    /**
     * 重命名+改变类型
     */
    @Test
    public void test_13() {
        WnObj c = indexer.create(null, "/a/b/c", WnRace.FILE);
        WnObj o = indexer.rename(c, "xyz.js");

        assertEquals("xyz.js", o.name());
        assertEquals("/a/b/xyz.js", o.path());
        assertEquals("js", o.type());
        assertEquals(setup.getMimes().getMime("js"), o.mime());
    }

    /**
     * 简单重命名
     */
    @Test
    public void test_14() {
        WnObj c = indexer.create(null, "/a/b/c", WnRace.FILE);
        WnObj o = indexer.rename(c, "xyz");

        assertEquals("xyz", o.name());
        assertEquals("/a/b/xyz", o.path());
    }

    /**
     * 两次读取祖先
     */
    @Test
    public void test_15() {
        WnObj c = indexer.create(null, "/a/b/c", WnRace.FILE);

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

    /**
     * 顶层目录获取
     */
    @Test
    public void test_16() {
        indexer.create(null, "/a", WnRace.DIR);
        WnObj o = indexer.check(null, "/a");
        assertEquals("a", o.name());
    }

    /**
     * 元数据数组 push/pull (ID)
     */
    @Test
    public void test_17() {
        WnObj o = indexer.create(null, "/a/b/c", WnRace.FILE);
        String id = o.id();

        assertEquals("/a/b/c", o.path());

        // 新增
        indexer.push(id, "pets", "wendal", false);
        indexer.push(id, "pets", "zozoh", false);
        indexer.push(id, "pets", "pangwu", false);

        o = indexer.get(id);
        assertNotNull(o);
        assertNotNull(o.get("pets"));
        assertEquals(3, Lang.eleSize(o.get("pets")));
        Lang.each(o.get("pets"), new Each<Object>() {
            public void invoke(int index, Object ele, int length) {
                assertTrue("wendal".equals(ele) || "zozoh".equals(ele) || "pangwu".equals(ele));
            }
        });

        // 减少
        indexer.pull(id, "pets", "zozoh", false);
        o = indexer.get(id);
        assertNotNull(o);
        assertNotNull(o.get("pets"));
        assertEquals(2, Lang.eleSize(o.get("pets")));
        Lang.each(o.get("pets"), new Each<Object>() {
            public void invoke(int index, Object ele, int length) {
                assertTrue("wendal".equals(ele) || "pangwu".equals(ele));
            }
        });

        indexer.pull(id, "pets", "wendal", false);
        indexer.pull(id, "pets", "pangwu", false);

        o = indexer.get(id);
        assertNotNull(o);
        assertEquals(0, Lang.eleSize(o.get("pets")));

    }

    /**
     * 元数据数组 push/pull (query)
     */
    @Test
    public void test_18() {
        indexer.create(null, "/a/b/z", WnRace.FILE);
        WnObj o = indexer.create(null, "/a/b/c", WnRace.FILE);
        String id = o.id();
        String pid = o.parentId();

        assertEquals("/a/b/c", o.path());

        // 新增
        indexer.push(Wn.Q.id(id), "pets", "wendal");
        indexer.push(Wn.Q.pid(pid), "pets", "zozoh");
        indexer.push(Wn.Q.pid(pid), "pets", "pangwu");

        o = indexer.get(id);
        assertNotNull(o);
        assertNotNull(o.get("pets"));
        assertEquals(3, Lang.eleSize(o.get("pets")));
        Lang.each(o.get("pets"), new Each<Object>() {
            public void invoke(int index, Object ele, int length) {
                assertTrue("wendal".equals(ele) || "zozoh".equals(ele) || "pangwu".equals(ele));
            }
        });

        // 减少
        indexer.pull(Wn.Q.pid(pid), "pets", "zozoh");
        o = indexer.get(id);
        assertNotNull(o);
        assertNotNull(o.get("pets"));
        assertEquals(2, Lang.eleSize(o.get("pets")));
        Lang.each(o.get("pets"), new Each<Object>() {
            public void invoke(int index, Object ele, int length) {
                assertTrue("wendal".equals(ele) || "pangwu".equals(ele));
            }
        });

        indexer.pull(Wn.Q.pid(pid), "pets", "wendal");
        indexer.pull(Wn.Q.pid(pid), "pets", "pangwu");

        o = indexer.get(id);
        assertNotNull(o);
        assertEquals(0, Lang.eleSize(o.get("pets")));

    }

    /**
     * 设置负责元数据
     */
    @Test
    public void test_19() {
        WnObj o = indexer.create(null, "/a", WnRace.FILE);
        File f = Files.findFile("org/nutz/walnut/core/req_meta.json");
        NutMap meta = Json.fromJsonFile(NutMap.class, f);

        WnObj o2 = indexer.setBy(o.id(), meta, true);
        assertEquals("demo", o2.getString("http-usr"));
        assertEquals("http://api.local.io:8080/api/demo/thing/list", o2.getString("http-url"));
        for (String key : meta.keySet()) {
            Object val = meta.get(key);
            Object v2 = o2.get(key);
            assertTrue(val.equals(v2));
        }
    }
}
