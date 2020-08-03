package org.nutz.walnut.core.indexer.localfile;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.util.Callback;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WalkMode;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.core.IoCoreTest;

public class LocalFileIndexerTest extends IoCoreTest {

    private LocalFileIndexer indexer;

    private File dHome;

    @Before
    public void setUp() throws Exception {
        this.setup.cleanAllData();
        indexer = this.setup.getLocalFileIndexer();
    }

    @After
    public void tearDown() throws Exception {
        Files.clearDir(dHome);
    }

    @Test
    /**
     * 测试一下 walk
     */
    public void test_walk() {
        indexer.create(null, "a/b/c.txt", WnRace.FILE);
        indexer.create(null, "a/b/d.txt", WnRace.FILE);
        indexer.create(null, "a/b/e.txt", WnRace.FILE);

        List<WnObj> list = new ArrayList<>(3);
        indexer.walk(null, new Callback<WnObj>() {
            public void invoke(WnObj obj) {
                list.add(obj);
            }
        }, WalkMode.DEPTH_NODE_FIRST);

        assertEquals(5, list.size());
        assertEquals("a", list.get(0).name());
        assertEquals("b", list.get(1).name());
        assertEquals("c.txt", list.get(2).name());
        assertEquals("d.txt", list.get(3).name());
        assertEquals("e.txt", list.get(4).name());
    }

    /**
     * 判断是否存在
     */
    @Test
    public void test_00() {
        indexer.create(null, "a/b/c.txt", WnRace.FILE);
        indexer.create(null, "a/b/d.txt", WnRace.FILE);
        indexer.create(null, "a/b/e.txt", WnRace.FILE);
        assertTrue(indexer.existsId("a"));
        assertTrue(indexer.existsId("a/b"));
        assertTrue(indexer.existsId("a/b/c.txt"));
        assertTrue(indexer.existsId("a/b/d.txt"));
        assertTrue(indexer.existsId("a/b/e.txt"));

        assertFalse(indexer.existsId("a/b/f.txt"));
        assertFalse(indexer.existsId("x"));
        assertFalse(indexer.existsId("x/y"));
    }

    /**
     * 测试文件的获取
     */
    @Test
    public void test_01() {
        indexer.create(null, "a/b/c.txt", WnRace.FILE);
        indexer.create(null, "a/b/d.txt", WnRace.FILE);
        indexer.create(null, "a/b/e.txt", WnRace.FILE);
        //
        // 目录
        //
        WnObj o = indexer.fetch(null, "a/b/c.txt");

        assertEquals("@WnRoot:a/b/c.txt", o.id());
        assertEquals("c.txt", o.name());
        assertEquals("txt", o.type());
        assertEquals("text/plain", o.mime());
        assertTrue(o.isFILE());
        assertFalse(o.isDIR());

        // 试试转换成 JSON
        String json = Json.toJson(o, JsonFormat.full());
        NutMap map0 = Json.fromJson(NutMap.class, json);
        NutMap map1 = o.toMap(null);
        assertTrue(Lang.equals(map0, map1));

        //
        // 目录
        //
        o = indexer.fetch(null, "a/b");

        assertEquals("@WnRoot:a/b/", o.id());
        assertEquals("b", o.name());
        assertNull(o.type());
        assertNull(o.mime());
        assertFalse(o.isFILE());
        assertTrue(o.isDIR());
        // 试试转换成 JSON
        json = Json.toJson(o, JsonFormat.full());
        map0 = Json.fromJson(NutMap.class, json);
        map1 = o.toMap(null);
        assertTrue(Lang.equals(map0, map1));
    }

    /**
     * 获取子
     */
    @Test
    public void test_02() {
        indexer.create(null, "a/b/c.txt", WnRace.FILE);
        indexer.create(null, "a/b/d.txt", WnRace.FILE);
        indexer.create(null, "a/b/e.txt", WnRace.FILE);
        //
        // 全部
        //
        WnObj p = indexer.fetch(null, "a/b");
        List<WnObj> list = indexer.getChildren(p, null);
        assertEquals(3, list.size());
        // 确保一致排序
        list.sort(new Comparator<WnObj>() {
            public int compare(WnObj o1, WnObj o2) {
                return o1.name().compareTo(o2.name());
            }
        });
        // 校验
        assertEquals("c.txt", list.get(0).name());
        assertEquals("d.txt", list.get(1).name());
        assertEquals("e.txt", list.get(2).name());

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

    /**
     * 删除
     */
    @Test
    public void test_03() {
        indexer.create(null, "a/b/c.txt", WnRace.FILE);
        indexer.create(null, "a/b/d.txt", WnRace.FILE);
        indexer.create(null, "a/b/e.txt", WnRace.FILE);
        //
        // 全部
        //
        WnObj p = indexer.fetch(null, "a/b");
        WnObj o = indexer.fetchByName(p, "e.txt");
        indexer.delete(o);
        List<WnObj> list = indexer.getChildren(p, null);
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
        // 部分x2
        //
        o = indexer.fetchByName(p, "c.txt");
        indexer.delete(o);
        list = indexer.getChildren(p, null);
        assertEquals(1, list.size());
        // 校验
        assertEquals("d.txt", list.get(0).name());

        //
        // 部分x1
        //
        o = indexer.fetchByName(p, "d.txt");
        indexer.delete(o);
        list = indexer.getChildren(p, null);
        assertEquals(0, list.size());
    }

}
