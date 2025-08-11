package com.site0.walnut.val.date;

import static org.junit.Assert.*;

import java.util.Date;

import org.junit.Test;

import com.site0.walnut.util.Wtime;
import com.site0.walnut.val.ValueMaker;

public class UTCTimestampMakerTest {

    @Test
    public void test00() {
        Date d = Wtime.parseAnyDate("2025-08-11 22:02:24.129");
        ValueMaker vm;
        vm = new UTCTimestampMaker(null);
        Object val = vm.make(d, null);
        assertEquals(d.getTime(), val);

    }

    @Test
    public void test01() {
        Date d = Wtime.parseAnyDate("2025-08-11 22:02:24.129");
        ValueMaker vm;
        vm = new UTCTimestampMaker("+1d");
        Object val = vm.make(d, null);
        assertEquals(d.getTime() + 86400000L, val);

    }

}
