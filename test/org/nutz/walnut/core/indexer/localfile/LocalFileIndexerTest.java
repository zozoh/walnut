package org.nutz.walnut.core.indexer.localfile;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nutz.json.Json;
import org.nutz.lang.Files;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.core.AbstractIoCoreTest;

public class LocalFileIndexerTest extends AbstractIoCoreTest {

    private LocalFileIndexer indexer;

    private File dHome;

    @Before
    public void setUp() throws Exception {
        indexer = this.setup.getLocalFileIndexer();
        dHome = indexer.getFileHome();

        Files.createFileIfNoExists(Files.getFile(dHome, "a/b/c.txt"));
        Files.createFileIfNoExists(Files.getFile(dHome, "a/b/d.txt"));
        Files.createFileIfNoExists(Files.getFile(dHome, "a/b/e.txt"));
    }

    @After
    public void tearDown() throws Exception {
        Files.clearDir(dHome);
    }

    /**
     * 判断是否存在
     */
    @Test
    public void test_00() {
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
        String json = Json.toJson(o);
        NutMap map0 = Json.fromJson(NutMap.class, json);
        NutMap map1 = o.toMap(null);
        assertTrue(map0.equals(map1));

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
        json = Json.toJson(o);
        map0 = Json.fromJson(NutMap.class, json);
        map1 = o.toMap(null);
        assertTrue(map0.equals(map1));
    }

}
