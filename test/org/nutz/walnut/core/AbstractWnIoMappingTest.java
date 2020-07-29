package org.nutz.walnut.core;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.util.Wn;

public abstract class AbstractWnIoMappingTest extends IoCoreTest {

    /**
     * 子类需要设置这个实例
     */
    protected WnIoMapping im;

    @Test
    public void test_00() throws IOException, WnIoHandleMutexException {
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
