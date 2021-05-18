package org.nutz.walnut.util;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.Date;
import org.junit.Test;
import org.nutz.walnut.util.time.WnDayTime;

public class WtimeTest {

    @Test
    public void test_parse_offset2() {
        long ams = Wtime.valueOf("(2021-05-13)-30d");

        Calendar c = Wtime.parseCalendar("2021-05-13");
        assertEquals(0, c.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, c.get(Calendar.MINUTE));
        assertEquals(0, c.get(Calendar.SECOND));
        assertEquals(0, c.get(Calendar.MILLISECOND));
        int d = c.get(Calendar.DAY_OF_MONTH);
        c.set(Calendar.DAY_OF_MONTH, d - 30);
        long ms2 = c.getTimeInMillis();

        assertEquals(ms2, ams);

        String ds = Wtime.format(new Date(ams), "yyyy-MM-dd");
        assertEquals("2021-04-13", ds);
    }

    @Test
    public void test_parse_offset() {
        long ams = Wtime.valueOf("(2021-05-19)-3d");

        Calendar c = Wtime.parseCalendar("2021-05-19");
        assertEquals(0, c.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, c.get(Calendar.MINUTE));
        assertEquals(0, c.get(Calendar.SECOND));
        assertEquals(0, c.get(Calendar.MILLISECOND));
        int d = c.get(Calendar.DAY_OF_MONTH);
        c.set(Calendar.DAY_OF_MONTH, d - 3);
        long ms2 = c.getTimeInMillis();

        assertEquals(ms2, ams);

        String ds = Wtime.format(new Date(ams), "yyyy-MM-dd");
        assertEquals("2021-05-16", ds);
    }

    @Test
    public void test_parse_ti_by_sec() {
        _assert_parseSec(-86400, 0, 0, 0, 0);
        _assert_parseSec(3612, 1, 0, 12, 0);
        _assert_parseSec(-1, 23, 59, 59, 0);
        _assert_parseSec(0, 0, 0, 0, 0);
        _assert_parseSec(86400, 0, 0, 0, 0);
        _assert_parseSec(86399, 23, 59, 59, 0);
        _assert_parseSec(86398, 23, 59, 58, 0);
    }

    @Test
    public void test_parse_str() {
        _assert_parseTime("12:59", 12, 59, 0, 0);
        _assert_parseTime("12:59:03", 12, 59, 3, 0);
        _assert_parseTime("12:59:03.120", 12, 59, 3, 120);
    }

    private void _assert_parseSec(int sec, int h, int m, int s, int ms) {
        WnDayTime ti = new WnDayTime(sec);
        assertEquals(h, ti.getHour());
        assertEquals(m, ti.getMinute());
        assertEquals(s, ti.getSecond());
        assertEquals(ms, ti.getMillisecond());
        assertEquals(h * 3600 + m * 60 + s, ti.getValue());
        assertEquals(h * 3600000 + m * 60000 + s * 1000 + ms, ti.getValueInMillisecond());
    }

    private void _assert_parseTime(String ts, int h, int m, int s, int ms) {
        WnDayTime ti = new WnDayTime(ts);
        assertEquals(h, ti.getHour());
        assertEquals(m, ti.getMinute());
        assertEquals(s, ti.getSecond());
        assertEquals(ms, ti.getMillisecond());
        assertEquals(h * 3600 + m * 60 + s, ti.getValue());
        assertEquals(h * 3600000 + m * 60000 + s * 1000 + ms, ti.getValueInMillisecond());
    }

    /**
     * For issue #524 again
     */
    @Test
    public void test_parse_not_second() {
        long ms0 = Wtime.parseAMS("2013-09-14 12:33");
        long ms1 = Wtime.parseAMS("2013-09-14T12:34");
        assertEquals(60 * 1000, ms1 - ms0);
    }

    /**
     * For issue #524
     */
    @Test
    public void test_with_timezone() {
        long ms0 = Wtime.parseAMS("2013-09-14T12:33:14+8");
        long ms1 = Wtime.parseAMS("2013-09-14T12:33:14-8");
        assertEquals(16 * 3600 * 1000, ms1 - ms0);

        ms0 = Wtime.parseAMS("2013-09-14T12:33:14Z+8:00");
        ms1 = Wtime.parseAMS("2013-09-14T12:33:14Z-8:00");
        assertEquals(16 * 3600 * 1000, ms1 - ms0);
    }

    /**
     * For issue #524
     */
    @Test
    public void test_sep_by_T() {
        Date d0 = Wtime.parseDate("2013-09-14 12:33:14");
        Date d1 = Wtime.parseDate("2013-09-14T12:33:14");
        assertEquals(d0.getTime(), d1.getTime());
    }

    @Test
    public void test_1940() {
        Date d = Wtime.parseDate("1940-8-15");
        assertEquals("1940-08-15", Wtime.formatDate(d));
    }

    @Test
    public void test_end_in_month() throws Exception {
        String s0430 = "2016-04-30T00:00:00";
        Date d0430 = Wtime.parseDate(s0430);
        String s0430_2 = Wtime.formatDateTime(d0430);
        assertEquals(s0430, s0430_2);
    }

    @Test
    public void test_start_in_month() throws Exception {
        String s0501 = "2016-05-01T00:00:00";
        Date d0501 = Wtime.parseDate(s0501);
        String s0501_2 = Wtime.formatDateTime(d0501);
        assertEquals(s0501, s0501_2);
    }

    @Test
    public void test_next_day2() {
        long t = Wtime.parseAMS("2019-01");
        System.out.println(new Date(t));
    }

}
