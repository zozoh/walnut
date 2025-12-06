package com.site0.walnut.core.mapping.support;

import static org.junit.Assert.*;

import org.junit.Test;

public class WnVofsOptionsTest {

    @Test
    public void test_00() {
        String str = "s3:/home/demo#test";
        WnVofsOptions opt = new WnVofsOptions(str);
        assertEquals("s3", opt.osType);
        assertEquals("/home/demo", opt.domainHomePath);
        assertEquals("test", opt.configName);
        assertEquals(str, opt.toString());
    }

    @Test
    public void test_01() {
        try {
            new WnVofsOptions("myos:/home/demo#test");
            fail();
        }
        catch (RuntimeException e) {}
    }

}
