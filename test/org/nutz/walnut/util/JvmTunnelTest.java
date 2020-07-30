package org.nutz.walnut.util;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.trans.Atom;
import org.nutz.walnut.api.box.WnTunnel;

public class JvmTunnelTest {

    private WnTunnel tnl;

    @Before
    public void setUp() {
        tnl = new JvmTunnel(3);
    }

    @Test
    public void test_in_two_threads_in_8192() throws InterruptedException {
        tnl = new JvmTunnel(8192);
        String src = Strings.dup('B', 99999999);
        final byte[] sbs = src.getBytes();
        // System.out.printf("gen %d bytes ~ %d Mbytes\n", sbs.length,
        // sbs.length / 1000 / 1000);
        final Object lock = new Object();
        final byte[] bs = new byte[sbs.length];

        Atom a = new Atom() {
            public void run() {
                synchronized (tnl) {
                    tnl.write(sbs);
                }
                // System.out.println("a quite");
            }
        };
        Atom b = new Atom() {
            public void run() {
                int re = -1;
                while (-1 == re) {
                    synchronized (tnl) {
                        re = tnl.read(bs);
                    }
                }
                // System.out.println("b quite");
            }
        };
        Thread aT = new Thread(a);
        Thread bT = new Thread(b);

        bT.start();
        aT.start();

        // 等待结果
        while (aT.isAlive() || bT.isAlive()) {
            Lang.wait(lock, 100);
            // System.out.printf("aT:%b, bT:%b\n", aT.isAlive(), bT.isAlive());
        }

        // 检查结果
        assertEquals(src, new String(bs));
    }

    @Test
    public void test_in_same_thread3() {
        String src = "AB";
        byte[] sbs = src.getBytes();

        tnl.write(sbs);

        byte[] bs = new byte[3];
        int re = tnl.read(bs);
        assertEquals(2, re);

        assertEquals("AB", new String(bs, 0, re));
    }

    @Test
    public void test_in_same_thread2() {
        String src = "ABCDEFG";
        byte[] sbs = src.getBytes();

        tnl.write(sbs);

        byte[] bs = new byte[3];
        tnl.read(bs);

        assertEquals("ABC", new String(bs));
    }

    @Test
    public void test_in_same_thread() {
        String src = "ABCDEFG";
        byte[] sbs = src.getBytes();

        tnl.write(sbs);

        byte[] bs = new byte[sbs.length];
        tnl.read(bs);

        assertEquals(src, new String(bs));
    }

}
