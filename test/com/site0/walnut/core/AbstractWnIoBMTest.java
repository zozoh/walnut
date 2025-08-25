package com.site0.walnut.core;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;
import org.nutz.lang.Encoding;
import com.site0.walnut.util.Wlang;
import org.nutz.lang.util.LinkedByteBuffer;
import com.site0.walnut.api.io.WnIoIndexer;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.util.Wn;

public abstract class AbstractWnIoBMTest extends IoCoreTest {

    /**
     * 子类需要设置这个实例
     */
    protected WnIoIndexer indexer;

    /**
     * 子类需要设置这个实例
     */
    protected WnIoBM bm;

    /**
     * 子类需要设置这个实例
     */
    protected WnObj o;

    protected abstract String getObjSha1ForTest(String sha1);

    @Test
    public void test_simple_trucate() throws IOException, WnIoHandleMutexException {
        WnIoHandle h = bm.open(o, Wn.S.W, indexer);
        String str = "hello";
        String sha1 = Wlang.sha1(str);

        // 写入
        byte[] buf = str.getBytes(Encoding.CHARSET_UTF8);
        h.write(buf);
        h.close();

        // 验证
        assertEquals(str.length(), o.len());
        assertEquals(sha1, getObjSha1ForTest(sha1));

        // 剪裁
        bm.truncate(o, 3, indexer);

        // 准备读回来
        h = bm.open(o, Wn.S.R, indexer);
        LinkedByteBuffer bytes = new LinkedByteBuffer();
        sha1 = Wlang.sha1("hel");

        // 读取
        buf = new byte[20];
        int len;

        while ((len = h.read(buf)) >= 0) {
            bytes.write(buf, 0, len);
        }
        h.close();

        // 验证
        String s2 = bytes.toString();
        assertEquals("hel", s2);
        assertEquals(sha1, bytes.sha1sum());
    }

    @Test
    public void test_simple_write_read() throws Exception {
        WnIoHandle h = bm.open(o, Wn.S.W, indexer);
        String str = "hello";
        String sha1 = Wlang.sha1(str);

        // 写入
        byte[] buf = str.getBytes(Encoding.CHARSET_UTF8);
        h.write(buf);
        h.close();

        // 验证
        assertEquals(str.length(), o.len());
        assertEquals(sha1, getObjSha1ForTest(sha1));

        // 准备读回来
        h = bm.open(o, Wn.S.R, indexer);
        LinkedByteBuffer bytes = new LinkedByteBuffer();

        // 读取
        buf = new byte[20];
        int len;

        while ((len = h.read(buf)) >= 0) {
            bytes.write(buf, 0, len);
        }
        h.close();

        // 验证
        String s2 = bytes.toString();
        assertEquals(str, s2);
        assertEquals(sha1, bytes.sha1sum());
    }

}
