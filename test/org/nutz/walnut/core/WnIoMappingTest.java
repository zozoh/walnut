package org.nutz.walnut.core;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.util.Wn;

public abstract class WnIoMappingTest {

    /**
     * 子类需要设置这个实例
     */
    protected WnIoMapping im;

    @Test
    public void test() throws IOException {
        WnObj o = im.create(null, "/a", WnRace.FILE);
        WnIoHandle h = im.open(o, Wn.S.W);
        byte[] buf1 = "Hello".getBytes();
        h.write(buf1);
        h.close();

        h = im.open(o, Wn.S.R);
        byte[] buf2 = new byte[100];
        h.read(buf2);
        h.close();

        String s1 = new String(buf1);
        String s2 = new String(buf2);
        assertEquals(s1, s2);
    }

}
