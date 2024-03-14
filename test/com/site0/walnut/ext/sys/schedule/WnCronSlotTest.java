package com.site0.walnut.ext.sys.schedule;

import static org.junit.Assert.*;

import org.junit.Test;
import org.nutz.lang.Times;

public class WnCronSlotTest {

    @Test
    public void test_simple() {
        WnCronSlot slot = new WnCronSlot();
        slot.fromString("20210913-0003-FakeId:zozoh:0 0 0 * * ?");
        assertEquals("2021-09-13", Times.format("yyyy-MM-dd", slot.getDate()));
        assertEquals(3, slot.getSlot());
        assertEquals("FakeId", slot.getTask());
        assertEquals("zozoh", slot.getUser());
        assertEquals("0 0 0 * * ?", slot.getCron());
    }

}
