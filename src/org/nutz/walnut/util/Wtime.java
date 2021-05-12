package org.nutz.walnut.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.walnut.api.err.Er;

/**
 * 日期时间相关的帮助函数
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public abstract class Wtime {

    static String REG = "^(\\d{2,4})([^\\d]*)" // 年 1,2
                        + "(\\d{1,2})([^\\d]*)" // 月 3,4
                        + "(\\d{1,2})([^\\d]*)" // 日 5,6
                        + "(\\d{1,2})?([^\\d]*)?" // 时 7,8
                        + "(\\d{1,2})?([^\\d]*)?" // 分 9,10
                        + "(\\d{1,2})?([^\\d+-]*)?" // 秒 11,12
                        + "(\\d{1,3})?([^\\d+-]*)?" // 毫秒 13,14
                        + "(([+-]?)(\\d{1,2}).*)?" // 时区 15,16,17
                        + "$";
    private static Pattern _P_TIME = Pattern.compile(REG);

    private static Pattern _P_TIME_LONG = Pattern.compile("^[0-9]+(L)?$");

    public static String formatDateTime(Date d) {
        return format(d, "yyyy-MM-dd'T'HH:mm:ss");
    }

    public static String formatDate(Date d) {
        return format(d, "yyyy-MM-dd");
    }

    public static String formatTime(Date d) {
        return format(d, "HH:mm:ss");
    }

    public static String format(Date d, String fmt) {
        return new SimpleDateFormat(fmt, Locale.ENGLISH).format(d);
    }

    public static long parseAMS(String ds) {
        return parseAMS(ds, null);
    }

    public static long parseAMS(String ds, TimeZone tz) {
        Calendar c = Calendar.getInstance();
        Date t = parseDate(ds, tz);
        c.setTime(t);
        return c.getTimeInMillis();
    }

    public static Calendar parseCalendar(String ds) {
        return parseCalendar(ds, null);
    }

    public static Calendar parseCalendar(String ds, TimeZone tz) {
        Calendar c = Calendar.getInstance();
        Date t = parseDate(ds, tz);
        c.setTime(t);
        return c;
    }

    /**
     * @see #parseDate(String, TimeZone)
     */
    public static Date parseDate(String ds) {
        return parseDate(ds, null);
    }

    /**
     * 根据字符串得到相对于 "UTC 1970-01-01 00:00:00" 的绝对毫秒数。
     * 本函数假想给定的时间字符串是本地时间。所以计算出来结果后，还需要减去时差
     * 
     * 支持的时间格式字符串为:
     * 
     * <pre>
     * yyyy-MM-dd HH:mm:ss
     * yyyy-MM-dd HH:mm:ss.SSS
     * yy-MM-dd HH:mm:ss;
     * yy-MM-dd HH:mm:ss.SSS;
     * yyyy-MM-dd;
     * yy-MM-dd;
     * HH:mm:ss;
     * HH:mm:ss.SSS;
     * </pre>
     * 
     * 时间字符串后面可以跟 +8 或者 +8:00 表示 GMT+8:00 时区。 同理 -9 或者 -9:00 表示 GMT-9:00 时区
     * 
     * @param ds
     *            时间字符串
     * @param tz
     *            你给定的时间字符串是属于哪个时区的
     * @return 日期时间对象
     * @see #_P_TIME
     */
    public static Date parseDate(String ds, TimeZone tz) {
        // 防守
        if (null == ds) {
            return null;
        }
        // 绝对毫秒数
        Matcher m = _P_TIME_LONG.matcher(ds);
        if (m.find()) {
            long ams = Long.parseLong(ds);
            return new Date(ams);
        }

        // 按字符串格式解析
        m = _P_TIME.matcher(ds);
        /**
         * <pre>
         0/18  Regin:0/18
         0:[  0, 18) `2020年4月12日12点3分20秒`
         1:[  0,  4) `2020`
         2:[  4,  5) `年`
         3:[  5,  6) `4`
         4:[  6,  7) `月`
         5:[  7,  9) `12`
         6:[  9, 10) `日`
         7:[ 10, 12) `12`
         8:[ 12, 13) `点`
         9:[ 13, 14) `3`
         10:[ 14, 15) `分`
         11:[ 15, 17) `20`
         12:[ 17, 18) `秒`
         13:[ 18, 21) `189`
         14:[ 21, 23) `毫秒`
         15:[ 24, 26) `+8`
         16:[ 24, 25) `+`
         17:[ 25, 26) `8`
         * </pre>
         */
        if (m.find()) {
            int yy = _int(m, 1, 1970);
            int MM = _int(m, 3, 1);
            int dd = _int(m, 5, 1);

            int HH = _int(m, 7, 0);
            int mm = _int(m, 9, 0);
            int ss = _int(m, 11, 0);

            int ms = _int(m, 13, 0);

            /*
             * zozoh: 先干掉，还是用 SimpleDateFormat 吧，"1980-05-01 15:17:23" 之前的日子
             * 得出的时间竟然总是多 30 分钟 long day = (long) D1970(yy, MM, dd); long MS =
             * day * 86400000L; MS += (((long) HH) * 3600L + ((long) mm) * 60L +
             * ss) * 1000L; MS += (long) ms;
             * 
             * // 如果没有指定时区 ... if (null == tz) { // 那么用字符串中带有的时区信息， if
             * (!Strings.isBlank(m.group(17))) { tz =
             * TimeZone.getTimeZone(String.format("GMT%s%s:00", m.group(18),
             * m.group(19))); // tzOffset = Long.parseLong(m.group(19)) // *
             * 3600000L // * (m.group(18).charAt(0) == '-' ? -1 : 1);
             * 
             * } // 如果依然木有，则用系统默认时区 else { tz = TimeZone.getDefault(); } }
             * 
             * // 计算 return MS - tz.getRawOffset() - tz.getDSTSavings();
             */
            String str = String.format("%04d-%02d-%02d %02d:%02d:%02d.%03d",
                                       yy,
                                       MM,
                                       dd,
                                       HH,
                                       mm,
                                       ss,
                                       ms);
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            // 那么用字符串中带有的时区信息 ...
            if (null == tz && !Ws.isBlank(m.group(15))) {
                tz = TimeZone.getTimeZone(String.format("GMT%s%s:00", m.group(16), m.group(17)));
            }
            // 指定时区 ...
            if (null != tz) {
                df.setTimeZone(tz);
            }
            // 解析返回
            try {
                return df.parse(str);
            }
            catch (ParseException e) {
                throw Wlang.wrapThrow(e);
            }
        } else if (_P_TIME_LONG.matcher(ds).find()) {
            if (ds.endsWith("L")) {
                ds.substring(0, ds.length() - 1);
            }
            long ams = Long.parseLong(ds);
            return new Date(ams);
        }
        throw Er.createf("e.time.invalid.format", "Unexpect date format '%s'", ds);
    }

    public static long parseAnyAMS(Object input) {
        if (null == input) {
            return 0;
        }
        // 日期
        if (input instanceof Calendar) {
            return ((Calendar) input).getTimeInMillis();
        }
        if (input instanceof Date) {
            return ((Date) input).getTime();
        }
        // 数字
        if (input instanceof Number) {
            return ((Number) input).longValue();
        }
        // 解析
        String s = input.toString();
        return parseAMS(s);
    }

    public static Calendar parseAnyCalendar(Object input) {
        if (null == input) {
            return null;
        }
        // 日期
        if (input instanceof Calendar) {
            return ((Calendar) input);
        }
        if (input instanceof Date) {
            Calendar c = Calendar.getInstance();
            c.setTime(((Date) input));
            return c;
        }
        // 数字
        if (input instanceof Number) {
            Calendar c = Calendar.getInstance();
            long ams = ((Number) input).longValue();
            c.setTimeInMillis(ams);
            return c;
        }
        // 解析
        String s = input.toString();
        return parseCalendar(s);
    }

    public static Date parseAnyDate(Object input) {
        if (null == input) {
            return null;
        }
        // 日期
        if (input instanceof Calendar) {
            return ((Calendar) input).getTime();
        }
        if (input instanceof Date) {
            return ((Date) input);
        }
        // 数字
        if (input instanceof Number) {
            long ams = ((Number) input).longValue();
            return new Date(ams);
        }
        // 解析
        String s = input.toString();
        return parseDate(s);
    }

    /**
     * @param offset
     *            <code>0</code> 表示当月第一日。
     *            <p>
     *            如果小于零，则表示从后面数 <code>-1</code> 表示当月最后一日
     * @return 日期对象
     */
    public static Calendar monthDay(int offset) {
        Calendar c = today();
        c.set(Calendar.DAY_OF_MONTH, 1);
        if (offset < 0) {
            int m = c.get(Calendar.MONTH);
            c.set(Calendar.MONTH, m + 1);
            int d = c.get(Calendar.DAY_OF_MONTH);
            d += offset;
            c.set(Calendar.DAY_OF_MONTH, d);
        }
        return c;
    }

    public static Date monthDayDate(int offset) {
        return monthDay(offset).getTime();
    }

    public static long monthDayInMs(int offset) {
        return monthDay(offset).getTimeInMillis();
    }

    /**
     * 获取本周的日期（00:00:00）
     * 
     * @param offset，
     *            <ul>
     *            <li><code>0</code> 周日
     *            <li><code>1</code> 周一
     *            <li><code>2</code> 周二
     *            <li><code>3</code> 周三
     *            <li><code>4</code> 周四
     *            <li><code>5</code> 周五
     *            <li><code>6</code> 周六
     *            </ul>
     *            如果超过了 <code>0-6</code>，则滚动
     * @return 日期对象
     */
    public static Calendar weekDay(int offset) {
        // 找到本周日
        Calendar c = today();
        c.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);

        // 偏移
        int d = c.get(Calendar.DAY_OF_MONTH);
        d += offset;

        c.set(Calendar.DAY_OF_MONTH, d);
        // 搞定
        return c;
    }

    public static Date weekDayDate(int offset) {
        return weekDay(offset).getTime();
    }

    public static long weekDayInMs(int offset) {
        return weekDay(offset).getTimeInMillis();
    }

    public static Calendar today() {
        Calendar c = Calendar.getInstance();
        setDayStart(c);
        return c;
    }

    public static Date todayDate() {
        Calendar c = Calendar.getInstance();
        setDayStart(c);
        return c.getTime();
    }

    public static long todayInMs() {
        Calendar c = Calendar.getInstance();
        setDayStart(c);
        return c.getTimeInMillis();
    }

    public static Calendar fromToday(int offset) {
        Calendar day = today();
        if (offset != 0) {
            int d = day.get(Calendar.DAY_OF_MONTH);
            day.set(Calendar.DAY_OF_MONTH, d + offset);
        }
        return day;
    }

    public static Date fromTodayDate(int offset) {
        return fromToday(offset).getTime();
    }

    public static long fromTodayInMs(int offset) {
        return fromToday(offset).getTimeInMillis();
    }

    /**
     * 将给定的日期对象时间设置为<code>00:00:00.000</code>
     * 
     * @param c
     *            日期
     */
    public static void setDayStart(Calendar c) {
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
    }

    /**
     * 将给定的日期对象时间设置为<code>23:59:59.999</code>
     * 
     * @param c
     *            日期
     */
    public static void setDayEnd(Calendar c) {
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        c.set(Calendar.MILLISECOND, 999);
    }

    private static int _int(Matcher m, int index, int dft) {
        String s = m.group(index);
        if (Ws.isBlank(s)) {
            return dft;
        }
        return Integer.parseInt(s);
    }
}
