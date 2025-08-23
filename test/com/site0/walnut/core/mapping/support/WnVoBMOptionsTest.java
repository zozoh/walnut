package com.site0.walnut.core.mapping.support;

import static org.junit.Assert.*;

import org.junit.Test;

public class WnVoBMOptionsTest {

    @Test
    public void test_00() {
        String str = "s3:sha1:22:/home/demo#test@redis";
        WnVoBMOptions opt = new WnVoBMOptions(str);
        assertEquals("s3", opt.osType);
        assertEquals("sha1", opt.signAlg);
        assertEquals("22", opt.parts);
        assertEquals("/home/demo", opt.domainHomePath);
        assertEquals("test", opt.configName);
        assertEquals("redis", opt.referSercieType);
        assertEquals(str, opt.toString());
    }

    @Test
    public void test_01() {
        String str = "cos:sha1:2245:/home/abc#mydata";
        WnVoBMOptions opt = new WnVoBMOptions(str);
        assertEquals("cos", opt.osType);
        assertEquals("sha1", opt.signAlg);
        assertEquals("2245", opt.parts);
        assertEquals("/home/abc", opt.domainHomePath);
        assertEquals("mydata", opt.configName);
        assertEquals("redis", opt.referSercieType);
        assertEquals(str+"@redis", opt.toString());
    }

    @Test
    public void test_02() {
        try {
            new WnVoBMOptions("myos:sha1:22:/home/demo#test");
            fail();
        }
        catch (RuntimeException e) {}

        try {
            new WnVoBMOptions("s3:md5:22:/home/demo#test");
            fail();
        }
        catch (RuntimeException e) {}

        try {
            new WnVoBMOptions("s3:sha1:y73:/home/demo#test");
            fail();
        }
        catch (RuntimeException e) {}

        try {
            new WnVoBMOptions("s3:sha1:222:/home/demo#test@sql");
            fail();
        }
        catch (RuntimeException e) {}

        try {
            new WnVoBMOptions("");
            fail();
        }
        catch (RuntimeException e) {}

        try {
            new WnVoBMOptions(null);
            fail();
        }
        catch (RuntimeException e) {}

    }

}
