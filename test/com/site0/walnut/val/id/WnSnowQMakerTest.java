package com.site0.walnut.val.id;

import static org.junit.Assert.*;

import java.util.Date;

import com.site0.walnut.val.ValueMaker;

import org.junit.Test;

public class WnSnowQMakerTest {

    @Test
    public void test() {
        ValueMaker vm = new WnSnowQMaker("F", 3);
        Date now = new Date();
        Object val = vm.make(now, null);
        String vs = val.toString();
        assertTrue(vs.startsWith("F"));
        assertEquals(12, vs.length());
    }

}
