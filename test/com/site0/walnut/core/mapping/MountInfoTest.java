package com.site0.walnut.core.mapping;

import static org.junit.Assert.*;

import org.junit.Test;

public class MountInfoTest {

    @Test
    public void test_0() {
        MountInfo mi = new MountInfo("dao(abc/t_news)");
        assertTrue(mi.hasIndexer());
        assertFalse(mi.hasBM());
        assertEquals("dao", mi.ix.type);
        assertEquals("abc/t_news", mi.ix.arg);
    }

    @Test
    public void test_1() {
        MountInfo mi = new MountInfo("dao(abc/t_news)://lbm(Abc)");
        assertTrue(mi.hasIndexer());
        assertTrue(mi.hasBM());
        assertEquals("dao", mi.ix.type);
        assertEquals("abc/t_news", mi.ix.arg);
        assertEquals("lbm", mi.bm.type);
        assertEquals("Abc", mi.bm.arg);
    }

    @Test
    public void test_2() {
        MountInfo mi = new MountInfo("dao(abc/t_news)://aliyunoss");
        assertTrue(mi.hasIndexer());
        assertTrue(mi.hasBM());
        assertEquals("dao", mi.ix.type);
        assertEquals("abc/t_news", mi.ix.arg);
        assertEquals("aliyunoss", mi.bm.type);
        assertNull(mi.bm.arg);
    }

    @Test
    public void test_3() {
        MountInfo mi = new MountInfo("dao(abc/t_news)://aliyunoss(news-data)");
        assertTrue(mi.hasIndexer());
        assertTrue(mi.hasBM());
        assertEquals("dao", mi.ix.type);
        assertEquals("abc/t_news", mi.ix.arg);
        assertEquals("aliyunoss", mi.bm.type);
        assertEquals("news-data", mi.bm.arg);
    }

    @Test
    public void test_4() {
        MountInfo mi = new MountInfo("mem");
        assertTrue(mi.hasIndexer());
        assertFalse(mi.hasBM());
        assertEquals("mem", mi.ix.type);
        assertNull(mi.ix.arg);
    }

    @Test
    public void test_5() {
        MountInfo mi = new MountInfo("mem://lbm(Abc)");
        assertTrue(mi.hasIndexer());
        assertTrue(mi.hasBM());
        assertEquals("mem", mi.ix.type);
        assertNull(mi.ix.arg);
        assertEquals("lbm", mi.bm.type);
        assertEquals("Abc", mi.bm.arg);
    }

    @Test
    public void test_6() {
        MountInfo mi = new MountInfo("file://C:/data/demo/");
        assertTrue(mi.hasIndexer());
        assertTrue(mi.hasBM());
        assertEquals("file", mi.ix.type);
        assertEquals("C:/data/demo/", mi.ix.arg);
        assertEquals("file", mi.bm.type);
        assertEquals("C:/data/demo/", mi.bm.arg);

        mi = new MountInfo("filew://C:/data/demo/");
        assertTrue(mi.hasIndexer());
        assertTrue(mi.hasBM());
        assertEquals("filew", mi.ix.type);
        assertEquals("C:/data/demo/", mi.ix.arg);
        assertEquals("filew", mi.bm.type);
        assertEquals("C:/data/demo/", mi.bm.arg);
    }

    @Test
    public void test_7() {
        MountInfo mi = new MountInfo(null);
        assertFalse(mi.hasIndexer());
        assertFalse(mi.hasBM());

        mi = new MountInfo("");
        assertFalse(mi.hasIndexer());
        assertFalse(mi.hasBM());

        mi = new MountInfo("\r\n\t  ");
        assertFalse(mi.hasIndexer());
        assertFalse(mi.hasBM());
    }

}
