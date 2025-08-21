package com.site0.walnut.core.indexer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Each;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.api.io.WalkMode;
import com.site0.walnut.api.io.WnIoIndexer;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnQuery;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.core.IoCoreTest;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Wobj;

public abstract class WnVirtualFsIndexerTest extends IoCoreTest {

    protected WnIoIndexer indexer;

    protected String VID(String path) {
        return indexer.getRootId() + ":" + _IID(path);
    }

    protected String _IID(String path) {
        return Wobj.encodePathToBase64(path);
    }

    protected abstract WnIoIndexer _get_indexer();

    @Before
    public void setup() throws Exception {
        indexer = _get_indexer();
    }

    @Test
    public void test_z08() {
        indexer.create(null, "a.js", WnRace.FILE);
        indexer.create(null, "b.js", WnRace.FILE);
        WnObj root = indexer.getRoot();
        List<WnObj> children = new ArrayList<>(2);
        WnQuery q = Wn.Q.pid(root);
        indexer.each(q, new Each<WnObj>() {
            public void invoke(int index, WnObj ele, int length) {
                children.add(ele);
            }
        });
        assertEquals(2, children.size());
        assertEquals("a.js", children.get(0).name());
        assertEquals("b.js", children.get(1).name());
    }

    @Test
    public void test_z07() {
        indexer.create(null, "a.js", WnRace.FILE);
        indexer.create(null, "b.js", WnRace.FILE);
        WnObj root = indexer.getRoot();
        List<WnObj> children = indexer.getChildren(root, null);
        assertEquals(2, children.size());
        assertEquals("a.js", children.get(0).name());
        assertEquals("b.js", children.get(1).name());
    }

    @Test
    public void test_z06() {
        WnObj o = indexer.create(null, "/a/b/x.js", WnRace.FILE);
        assertEquals("x.js", o.name());
        assertEquals(VID("a/b/x.js"), o.id());
        assertEquals(VID("a/b/"), o.parentId());
    }

    @Test
    /**
     * 测试一下 walk
     */
    public void test_z05() {
        indexer.create(null, "a/b/c.txt", WnRace.FILE);
        indexer.create(null, "a/b/d.txt", WnRace.FILE);
        indexer.create(null, "a/b/e.txt", WnRace.FILE);

        List<WnObj> list = new ArrayList<>(3);
        indexer.walk(null, (WnObj obj) -> {
            list.add(obj);
        }, WalkMode.DEPTH_NODE_FIRST, null);

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
    public void test_z04() {
        indexer.create(null, "a/b/c.txt", WnRace.FILE);
        indexer.create(null, "a/b/d.txt", WnRace.FILE);
        indexer.create(null, "a/b/e.txt", WnRace.FILE);
        assertTrue(indexer.existsId(_IID("a/")));
        assertTrue(indexer.existsId(_IID("a/b/")));
        assertTrue(indexer.existsId(_IID("a/b/c.txt")));
        assertTrue(indexer.existsId(_IID("a/b/d.txt")));
        assertTrue(indexer.existsId(_IID("a/b/e.txt")));

        assertFalse(indexer.existsId(_IID("a/b/f.txt")));
        assertFalse(indexer.existsId(_IID("x/")));
        assertFalse(indexer.existsId(_IID("x/y")));
    }

    /**
     * 测试文件的获取
     */
    @Test
    public void test_z03() {
        indexer.create(null, "a/b/c.txt", WnRace.FILE);
        indexer.create(null, "a/b/d.txt", WnRace.FILE);
        indexer.create(null, "a/b/e.txt", WnRace.FILE);
        //
        // 目录
        //
        WnObj o = indexer.fetch(null, "a/b/c.txt");

        assertEquals(VID("a/b/c.txt"), o.id());
        assertEquals("c.txt", o.name());
        assertEquals("txt", o.type());
        assertEquals("text/plain", o.mime());
        assertTrue(o.isFILE());
        assertFalse(o.isDIR());

        // 试试转换成 JSON
        String json = Json.toJson(o, JsonFormat.full());
        NutMap map0 = Json.fromJson(NutMap.class, json);
        NutMap map1 = o.toMap(null);
        boolean is_equal = Wlang.isEqualDeeply(map0, map1);
        assertTrue(is_equal);

        //
        // 目录
        //
        o = indexer.fetch(null, "a/b/");

        assertEquals(VID("a/b/"), o.id());
        assertEquals("b", o.name());
        assertNull(o.type());
        assertNull(o.mime());
        assertFalse(o.isFILE());
        assertTrue(o.isDIR());
        // 试试转换成 JSON
        json = Json.toJson(o, JsonFormat.full());
        map0 = Json.fromJson(NutMap.class, json);
        map1 = o.toMap(null);
        is_equal = Wlang.isEqualDeeply(map0, map1);
        assertTrue(is_equal);
    }

    /**
     * 获取子
     */
    @Test
    public void test_z02() {
        indexer.create(null, "a/b/c.txt", WnRace.FILE);
        indexer.create(null, "a/b/d.txt", WnRace.FILE);
        indexer.create(null, "a/b/e.txt", WnRace.FILE);
        //
        // 全部
        //
        WnObj p = indexer.fetch(null, "a/b/");
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
        // 获取某个文件
        //
        list = indexer.getChildren(p, "c.txt");
        assertEquals(1, list.size());
        assertEquals("c.txt", list.get(0).name());
    }

    /**
     * 删除
     */
    @Test
    public void test_z01() {
        indexer.create(null, "a/b/c.txt", WnRace.FILE);
        indexer.create(null, "a/b/d.txt", WnRace.FILE);
        indexer.create(null, "a/b/e.txt", WnRace.FILE);
        //
        // 全部
        //
        WnObj p = indexer.fetch(null, "a/b/");
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

    @Test
    public void test_z00() {
        // 创建
        WnObj o = indexer.create(null, "foo/bar.txt", WnRace.FILE);

        // 读取
        WnObj o2 = indexer.fetch(null, "foo/bar.txt");

        assertTrue(o2.isFILE());
        assertEquals(o.id(), o2.id());
    }
}
