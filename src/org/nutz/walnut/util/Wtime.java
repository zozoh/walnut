package org.nutz.walnut.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;

/**
 * 日期时间相关的帮助函数
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public abstract class Wtime {

    private static Pattern _P_TIME = Pattern.compile("^((\\d{2,4})([/\\\\-])?(\\d{1,2})([/\\\\-])?(\\d{1,2}))?"
                                                     + "(([ T])?"
                                                     + "(\\d{1,2})(:)(\\d{1,2})((:)(\\d{1,2}))?"
                                                     + "(([.])"
                                                     + "(\\d{1,}))?)?"
                                                     + "(([+-])(\\d{1,2})(:\\d{1,2})?)?"
                                                     + "$");

    private static Pattern _P_TIME_LONG = Pattern.compile("^[0-9]+(L)?$");

    public String formatDate(Date d) {
        return formatDate(d, "yyyy-MM-dd'T'HH:mm:ss");
    }

    public String formatDate(Date d, String fmt) {
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
        Matcher m = _P_TIME.matcher(ds);
        if (m.find()) {
            int yy = _int(m, 2, 1970);
            int MM = _int(m, 4, 1);
            int dd = _int(m, 6, 1);

            int HH = _int(m, 9, 0);
            int mm = _int(m, 11, 0);
            int ss = _int(m, 14, 0);

            int ms = _int(m, 17, 0);

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
            if (null == tz && !Strings.isBlank(m.group(18))) {
                tz = TimeZone.getTimeZone(String.format("GMT%s%s:00", m.group(19), m.group(20)));
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
                throw Lang.wrapThrow(e);
            }
        } else if (_P_TIME_LONG.matcher(ds).find()) {
            if (ds.endsWith("L")) {
                ds.substring(0, ds.length() - 1);
            }
            long ams = Long.parseLong(ds);
            return new Date(ams);
        }
        throw Lang.makeThrow("Unexpect date format '%s'", ds);
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

    private static int _int(Matcher m, int index, int dft) {
        String s = m.group(index);
        if (Strings.isBlank(s)) {
            return dft;
        }
        return Integer.parseInt(s);
    }
}
