package com.site0.walnut.ext.data.fake.impl;

import static org.junit.Assert.*;

import org.junit.Test;

public class WnIntTmplFakerTest {

    @Test
    public void test_00() {
        WnIntTmplFaker tf = new WnIntTmplFaker("192.168.{0-9}.{10-19}");
        String regex = "^(192.168.[0-9].1[0-9])$";

        for (int i = 0; i < 100; i++) {
            String s = tf.next();
            assertTrue(s.matches(regex));
        }

    }

}
