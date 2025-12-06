package com.site0.walnut.core.mapping.support;

import static org.junit.Assert.*;

import org.junit.Test;

public class MountInfoTest {

    @Test
    public void test_10() {
        MountInfo mi = new MountInfo("vofs://s3:/home/demo#test");
        assertTrue(mi.hasIndexer());
        assertTrue(mi.hasBM());
        assertEquals("vofs", mi.ix.type);
        assertEquals("s3:/home/demo#test", mi.ix.arg);
        assertEquals("vofs", mi.bm.type);
        assertEquals("s3:/home/demo#test", mi.bm.arg);

        mi = new MountInfo("vofs://cos:/home/demo#test");
        assertTrue(mi.hasIndexer());
        assertTrue(mi.hasBM());
        assertEquals("vofs", mi.ix.type);
        assertEquals("cos:/home/demo#test", mi.ix.arg);
        assertEquals("vofs", mi.bm.type);
        assertEquals("cos:/home/demo#test", mi.bm.arg);
    }

    @Test
    public void test_09() {
        MountInfo mi = new MountInfo("::s3(sha1:22:/home/demo#test)");
        assertFalse(mi.hasIndexer());
        assertTrue(mi.hasBM());
        assertEquals("s3", mi.bm.type);
        assertEquals("sha1:22:/home/demo#test", mi.bm.arg);
    }

    @Test
    public void test_08() {
        MountInfo mi = new MountInfo("sql(:pet)::sql(:pet)");
        assertTrue(mi.hasIndexer());
        assertTrue(mi.hasBM());
        assertEquals("sql", mi.ix.type);
        assertEquals(":pet", mi.ix.arg);
        assertEquals("sql", mi.bm.type);
        assertEquals(":pet", mi.bm.arg);
    }

    @Test
    public void test_07() {
        MountInfo mi = new MountInfo("dao(abc/t_news)");
        assertTrue(mi.hasIndexer());
        assertFalse(mi.hasBM());
        assertEquals("dao", mi.ix.type);
        assertEquals("abc/t_news", mi.ix.arg);
    }

    @Test
    public void test_06() {
        MountInfo mi = new MountInfo("dao(abc/t_news)://lbm(Abc)");
        assertTrue(mi.hasIndexer());
        assertTrue(mi.hasBM());
        assertEquals("dao", mi.ix.type);
        assertEquals("abc/t_news", mi.ix.arg);
        assertEquals("lbm", mi.bm.type);
        assertEquals("Abc", mi.bm.arg);
    }

    @Test
    public void test_05() {
        MountInfo mi = new MountInfo("dao(abc/t_news)://aliyunoss");
        assertTrue(mi.hasIndexer());
        assertTrue(mi.hasBM());
        assertEquals("dao", mi.ix.type);
        assertEquals("abc/t_news", mi.ix.arg);
        assertEquals("aliyunoss", mi.bm.type);
        assertNull(mi.bm.arg);
    }

    @Test
    public void test_04() {
        MountInfo mi = new MountInfo("dao(abc/t_news)://aliyunoss(news-data)");
        assertTrue(mi.hasIndexer());
        assertTrue(mi.hasBM());
        assertEquals("dao", mi.ix.type);
        assertEquals("abc/t_news", mi.ix.arg);
        assertEquals("aliyunoss", mi.bm.type);
        assertEquals("news-data", mi.bm.arg);
    }

    @Test
    public void test_03() {
        MountInfo mi = new MountInfo("mem");
        assertTrue(mi.hasIndexer());
        assertFalse(mi.hasBM());
        assertEquals("mem", mi.ix.type);
        assertNull(mi.ix.arg);
    }

    @Test
    public void test_02() {
        MountInfo mi = new MountInfo("mem://lbm(Abc)");
        assertTrue(mi.hasIndexer());
        assertTrue(mi.hasBM());
        assertEquals("mem", mi.ix.type);
        assertNull(mi.ix.arg);
        assertEquals("lbm", mi.bm.type);
        assertEquals("Abc", mi.bm.arg);
    }

    @Test
    public void test_01() {
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
    public void test_00() {
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
