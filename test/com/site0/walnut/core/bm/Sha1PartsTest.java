package com.site0.walnut.core.bm;

import static org.junit.Assert.*;

import org.junit.Test;

public class Sha1PartsTest {

    @Test
    public void test_0() {
        Sha1Parts sp = new Sha1Parts("22");
        String sha1 = "b10d47941e27dad21b63fb76443e1669195328f2";
        String path = sp.toPath(sha1);
        assertEquals("b1/0d/47941e27dad21b63fb76443e1669195328f2", path);
        assertEquals(sha1, sp.fromPath(path));
    }

    @Test
    public void test_1() {
        Sha1Parts sp = new Sha1Parts("");
        String sha1 = "b10d47941e27dad21b63fb76443e1669195328f2";
        String path = sp.toPath(sha1);
        assertEquals(sha1, path);
        assertEquals(sha1, sp.fromPath(path));
    }

    @Test
    public void test_2() {
        Sha1Parts sp = new Sha1Parts("234");
        String sha1 = "b10d47941e27dad21b63fb76443e1669195328f2";
        String path = sp.toPath(sha1);
        assertEquals("b1/0d4/7941/e27dad21b63fb76443e1669195328f2", path);
        assertEquals(sha1, sp.fromPath(path));
    }

}
