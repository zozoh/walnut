package com.site0.walnut.util.time;

import static org.junit.Assert.*;

import org.junit.Test;

public class WnDayTimeTest {

    @Test
    public void test() {
        WnDayTime t = new WnDayTime();

        t.parse("20:23");
        assertEquals("20:23:00.000", t.toString("HH:mm:ss.SSS"));
    }

}
