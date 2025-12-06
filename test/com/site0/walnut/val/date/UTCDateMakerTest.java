package com.site0.walnut.val.date;

import static org.junit.Assert.*;
import com.site0.walnut.val.ValueMaker;

import java.util.Date;

import org.junit.Test;

import com.site0.walnut.util.Wtime;

public class UTCDateMakerTest {

    @Test
    public void test00() {
        String ds = "2025-08-11 22:02:24.129";
        Date d = Wtime.parseAnyDateUTC(ds);
        ValueMaker vm;
        vm = new UTCDateMaker(null);
        Object val = vm.make(d, null);
        assertEquals(ds, val);

    }

    @Test
    public void test01() {
        String ds = "2025-08-11 22:02:24.129";
        Date d = Wtime.parseAnyDateUTC(ds);
        ValueMaker vm;
        vm = new UTCDateMaker("+1d");
        Object val = vm.make(d, null);
        assertEquals("2025-08-12 22:02:24.129", val);

    }

}
