package com.site0.walnut.core;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;
import org.nutz.lang.util.LinkedByteBuffer;
import com.site0.walnut.api.io.WnIoIndexer;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.util.Wn;

public abstract class AbstractWnIoMappingTest extends IoCoreTest {

    /**
     * 子类需要设置这个实例
     */
    protected WnIoMapping im;

    @Test
    public void test_fetch_by_file() {
        WnIoIndexer indexer = im.getIndexer();
        WnObj ox = indexer.create(null, "/a/b/x.js", WnRace.FILE);
        WnObj oy = indexer.create(null, "/a/b/y.js", WnRace.FILE);

        assertEquals("/a/b/x.js", ox.path());
        assertEquals("/a/b/y.js", oy.path());

        WnObj y = indexer.fetch(ox, "y.js");
        assertEquals("y.js", y.name());
        assertEquals(oy.id(), y.id());
        assertEquals("/a/b/y.js", y.path());

        y = indexer.fetch(ox, "./y.js");
        assertEquals("y.js", y.name());
        assertEquals(oy.id(), y.id());
        assertEquals("/a/b/y.js", y.path());

        WnObj b = y.parent();
        assertEquals("b", b.name());
        assertEquals(ox.parentId(), b.id());
        assertEquals("/a/b/", b.getRegularPath());
    }

    @Test
    public void test_simple_copy() throws IOException, WnIoHandleMutexException {
        WnIoIndexer indexer = im.getIndexer();
        WnObj oa = indexer.create(null, "/a", WnRace.FILE);
        WnObj ob = indexer.create(null, "/b", WnRace.FILE);
        String str = "Hello";

        LinkedByteBuffer bytes;
        byte[] buf;
        int len;

        // 写入数据
        WnIoHandle h = im.open(oa, Wn.S.W);
        byte[] buf1 = str.getBytes();
        h.write(buf1);
        h.close();

        // Copy 数据
        im.copyData(oa, ob);
        assertEquals(oa.len(), ob.len());
        assertEquals(oa.sha1(), ob.sha1());

        // 读取内容
        bytes = new LinkedByteBuffer();
        h = im.open(ob, Wn.S.R);
        buf = new byte[20];
        while ((len = h.read(buf)) >= 0) {
            bytes.write(buf, 0, len);
        }
        assertEquals(str, bytes.toString());

        // 再次读取并验证
        ob = indexer.fetch(null, "/b");
        assertEquals(oa.len(), ob.len());
        assertEquals(oa.sha1(), ob.sha1());

        bytes = new LinkedByteBuffer();
        h = im.open(ob, Wn.S.R);
        buf = new byte[20];
        while ((len = h.read(buf)) >= 0) {
            bytes.write(buf, 0, len);
        }
        assertEquals(str, bytes.toString());

        // 删除原始
        im.delete(oa, true, null);

        // 再次读取并验证
        ob = indexer.fetch(null, "/b");
        assertEquals(str.length(), ob.len());
        assertEquals(oa.sha1(), ob.sha1());

        // 读取内容，还是存在的
        bytes = new LinkedByteBuffer();
        h = im.open(ob, Wn.S.R);
        buf = new byte[20];
        while ((len = h.read(buf)) >= 0) {
            bytes.write(buf, 0, len);
        }
        assertEquals(str, bytes.toString());

    }

    @Test
    public void test_open_write_read() throws IOException, WnIoHandleMutexException {
        WnIoIndexer indexer = im.getIndexer();
        WnObj o = indexer.create(null, "/a", WnRace.FILE);
        WnIoHandle h = im.open(o, Wn.S.W);
        byte[] buf1 = "Hello".getBytes();
        h.write(buf1);
        h.close();

        h = im.open(o, Wn.S.R);
        byte[] buf2 = new byte[100];
        int n = h.read(buf2);
        h.close();

        String s1 = new String(buf1);
        String s2 = new String(buf2, 0, n);
        assertEquals(s1, s2);
    }

}
