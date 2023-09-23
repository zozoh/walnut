package org.nutz.walnut.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.lang.Strings;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.ext.sys.datex.bean.WnHolidays;

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

    private static Pattern _P_TIME_LONG = Pattern.compile("^(-?[0-9]+)(L)?$");

    private static Pattern _P_OFFSET = Pattern.compile("^=(\\d{2,})[+](\\d+)$");

    public static String formatDateTime(Date d) {
        return format(d, "yyyy-MM-dd'T'HH:mm:ss");
    }

    public static String formatDate(Date d) {
        return format(d, "yyyy-MM-dd");
    }

    public static String formatTime(Date d) {
        return format(d, "HH:mm:ss");
    }

    public static String format(Calendar c, String fmt) {
        return format(c.getTime(), fmt);
    }

    public static String format(Date d, String fmt) {
        WnContext wc = Wn.WC();
        TimeZone tz = wc.getTimeZone();
        SimpleDateFormat formater = new SimpleDateFormat(fmt, Locale.ENGLISH);
        if (null != tz) {
            formater.setTimeZone(tz);
        }
        return formater.format(d);
    }

    public static long parseAMS(String ds) {
        return parseAMS(ds, null);
    }

    public static long parseAMS(String ds, TimeZone tz) {
        // Calendar c = Calendar.getInstance();
        Date t = parseDate(ds, tz);
        // c.setTime(t);
        return t.getTime();
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
     * =1900+44504
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

        // 采用默认时区
        if (null == tz) {
            WnContext wc = Wn.WC();
            tz = wc.getTimeZone();
        }

        // 绝对毫秒数
        Matcher m = _P_TIME_LONG.matcher(ds);
        if (m.find()) {
            long ams = Long.parseLong(m.group(1));
            return new Date(ams);
        }

        // 偏移量 "=1900+44504"
        m = _P_OFFSET.matcher(ds);
        if (m.find()) {
            int year = Integer.parseInt(m.group(1));
            int offd = Integer.parseInt(m.group(2));
            Calendar c = Wtime.today();
            c.set(Calendar.YEAR, year);
            c.set(Calendar.MONTH, 0);
            c.set(Calendar.DAY_OF_MONTH, 1);
            c.set(Calendar.DAY_OF_YEAR, offd - 1);
            return c.getTime();
        }

        String str = null;
        // 一个8位的数字，譬如 20201202
        if (ds.length() == 8 && ds.matches("^\\d{8}$")) {
            str = String.format("%s-%s-%s 00:00:00.000",
                                ds.substring(0, 4),
                                ds.substring(4, 6),
                                ds.substring(6, 8));
        }

        // 看来给的字符串需要认真解析一下 ...
        if (null == str) {
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
                 * 得出的时间竟然总是多 30 分钟 long day = (long) D1970(yy, MM, dd); long MS
                 * = day * 86400000L; MS += (((long) HH) * 3600L + ((long) mm) *
                 * 60L + ss) * 1000L; MS += (long) ms;
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
                String fmt = "%04d-%02d-%02d %02d:%02d:%02d.%03d";
                str = String.format(fmt, yy, MM, dd, HH, mm, ss, ms);
                if (null == tz && !Ws.isBlank(m.group(15))) {
                    tz = TimeZone.getTimeZone(String.format("GMT%s%s:00",
                                                            m.group(16),
                                                            m.group(17)));
                }
            }
        }
        //
        // 采用标准格式化器来解析
        //
        if (null != str) {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
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

    public static Calendar monthDayEnd(int offset) {
        Calendar c = monthDay(offset);
        Wtime.setDayEnd(c);
        return c;
    }

    public static Date monthDayEndDate(int offset) {
        Calendar c = monthDayEnd(offset);
        return c.getTime();
    }

    public static long monthDayEndInMs(int offset) {
        Calendar c = monthDayEnd(offset);
        return c.getTimeInMillis();
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

    public static Calendar weekDayEnd(int offset) {
        Calendar c = weekDay(offset);
        Wtime.setDayEnd(c);
        return c;
    }

    public static Date weekDayEndDate(int offset) {
        Calendar c = weekDayEnd(offset);
        return c.getTime();
    }

    public static long weekDayEndInMs(int offset) {
        Calendar c = weekDayEnd(offset);
        return c.getTimeInMillis();
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

    public static Calendar from(Calendar c, int offset, WnHolidays holidays) {
        // 不偏移
        if (0 == offset) {
            return c;
        }
        // 未指定节假日
        if (null == holidays) {
            int d2 = c.get(Calendar.DAY_OF_MONTH);
            d2 += offset;
            c.set(Calendar.DAY_OF_MONTH, d2);
        }
        // 偏移需要考虑节假日: 向后偏移
        else if (offset > 0) {
            int n = offset;
            while (n > 0) {
                int d2 = c.get(Calendar.DAY_OF_MONTH);
                d2++;
                c.set(Calendar.DAY_OF_MONTH, d2);
                if (holidays.isWordDay(c)) {
                    n--;
                }
            }
        }
        // 偏移需要考虑节假日: 向前
        else {
            int n = Math.abs(offset);
            while (n > 0) {
                int d2 = c.get(Calendar.DAY_OF_MONTH);
                d2--;
                c.set(Calendar.DAY_OF_MONTH, d2);
                if (holidays.isWordDay(c)) {
                    n--;
                }
            }
        }
        // 返回
        return c;
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

    private static final String TM_REG = "^[(]?(now"
                                         + "|today|Mon|Tue|Wed|Thu|Fri|Sat|Sun"
                                         + "|monthBegin|monthEnd"
                                         + "|\\d{4}[^+)]{4,}"
                                         + ")\\s*[)]?\\s*"
                                         + "("
                                         + "([+-])"
                                         + "([0-9]+[smhdMy]?)"
                                         + ")?$";
    private static final Pattern P_TM_MACRO = Pattern.compile(TM_REG);

    /**
     * 解析一个表示日期时间绝对毫秒数的字符串，格式为：
     * 
     * <pre>
     * [开始时间][+-][偏移量]
     * </pre>
     * 
     * 其中，开始时间可能的值是：
     * 
     * <ul>
     * <li><code>now</code>: 系统当前毫秒数
     * <li><code>today</code>: 今天开始的 00:00:00 的绝对毫秒数
     * <li><code>Mon</code>: 本周一的 00:00:00 的绝对毫秒数
     * <li><code>Tue</code>: 本周一的 00:00:00 的绝对毫秒数
     * <li><code>Tue</code>: 本周一的 00:00:00 的绝对毫秒数
     * <li><code>Wed</code>: 本周一的 00:00:00 的绝对毫秒数
     * <li><code>Thu</code>: 本周一的 00:00:00 的绝对毫秒数
     * <li><code>Fri</code>: 本周一的 00:00:00 的绝对毫秒数
     * <li><code>Sat</code>: 本周一的 00:00:00 的绝对毫秒数
     * <li><code>Sun</code>: 本周一的 00:00:00 的绝对毫秒数
     * <li><code>monthBegin</code>: 本月第一日的 00:00:00 的绝对毫秒数
     * <li><code>monthEnd</code>: 本月最后一日的 23:59:59 的绝对毫秒数
     * <li><code>2021-09-23</code>: 一个绝对日期字符串
     * <li><code>2021-09-23T12:10:18</code>: 一个绝对时间字符串
     * <li><code>1636387320000</code>: 一个绝对时间毫秒数
     * </ul>
     * 
     * 偏移量可能的值是：
     * 
     * <ul>
     * <li><code>10s</code> 表示10秒
     * <li><code>20m</code> 表示20分钟
     * <li><code>1h</code> 表示1小时
     * <li><code>1d</code> 表示一天
     * <li><code>1w</code> 表示一周
     * <li><code>1M</code> 表示一月
     * <li><code>1y</code> 表示一年
     * <li><code>100</code> 表示 100毫秒</li>
     * </ul>
     * 
     * <b>注意</b>,如果你指定一个日期，譬如<code>2021-09-21</code>，并且你还想写偏移量，
     * 你的语法就变成<code>2021-09-21-3d</code>，这显然不是一个清晰的日期。<br>
     * 你可以用半角括弧帮助我理解这个表达式，譬如你可以写成这样:
     * 
     * <pre>
     * (2021 - 09 - 21) - 3d
     * </pre>
     * 
     * @param str
     *            输入字符串，可能的值：
     * 
     * @return 绝对毫秒数
     */
    public static long valueOf(String str) {
        long ms = -1;

        // 判断到操作符
        Matcher m = P_TM_MACRO.matcher(str);

        /**
         * <pre>
         0/22  Regin:0/22
        0:[  0, 22) 2020-12-02 12:23:32-4d
        1:[  0, 19) 2020-12-02 12:23:32
        2:[ 19, 22) -4d
        3:[ 19, 20) -
        4:[ 20, 22) 4d
         * </pre>
         */

        // 当前时间
        if (m.find()) {
            // 分析表达式
            String current = m.group(1);
            String offset = m.group(2); // -4d
            String sign = m.group(3); // - or +
            String dus = m.group(4); // 4d or 4s ...
            // 类似 now+4d
            if ("now".equals(current)) {
                ms = Wn.now();
            }
            // 类似 today+1d
            else if ("today".equals(current)) {
                ms = todayInMs();
            }
            // 类似 monthBegin+1d
            else if ("monthBegin".equals(current)) {
                ms = monthDayInMs(0);
            }
            // 类似 monthEnd+1d
            else if ("monthEnd".equals(current)) {
                ms = monthDayEndInMs(-1);
            }
            // 类似 Sun+1d
            else if ("Sun".equals(current)) {
                ms = weekDayInMs(0);
            }
            // 类似 Mon+1d
            else if ("Mon".equals(current)) {
                ms = weekDayInMs(1);
            }
            // 类似 Tue+1d
            else if ("Tue".equals(current)) {
                ms = weekDayInMs(2);
            }
            // 类似 Wed+1d
            else if ("Wed".equals(current)) {
                ms = weekDayInMs(3);
            }
            // 类似 Thu+1d
            else if ("Thu".equals(current)) {
                ms = weekDayInMs(4);
            }
            // 类似 Fri+1d
            else if ("Fri".equals(current)) {
                ms = weekDayInMs(5);
            }
            // 类似 Sat+1d
            else if ("Sat".equals(current)) {
                ms = weekDayInMs(6);
            }
            // 类似 2020-12-05T00:12:32
            // 或者 1636387320000
            else {
                ms = parseAMS(current);
            }
            //
            // 嗯要加点偏移量
            //
            if (!Strings.isBlank(offset)) {
                // 偏移年/月，不能直接用毫秒数
                m = P_YM_STR.matcher(dus);
                if (m.find()) {
                    int n = Integer.parseInt(m.group(1));
                    if ("-".equals(sign)) {
                        n = n * -1;
                    }
                    String unit = m.group(2);
                    Calendar c = Calendar.getInstance();
                    c.setTimeInMillis(ms);
                    // 偏移年:y
                    if ("y".equals(unit)) {
                        c.add(Calendar.YEAR, n);
                        ms = c.getTimeInMillis();
                    }
                    // 偏移月: M
                    else {
                        c.add(Calendar.MONTH, n);
                        ms = c.getTimeInMillis();
                    }
                }
                // 直接可以偏移毫秒: s/m/h/d/w
                else {
                    long off = Wtime.millisecond(dus);
                    // 看是加还是减
                    if ("-".equals(sign)) {
                        off = off * -1L;
                    }
                    // 偏移
                    ms += off;
                }
            }
        }

        // 搞定返回
        return ms;
    }

    private static final Pattern P_MS_STR = Pattern.compile("^([-]?[0-9]+)([smhdw])?$");
    private static final Pattern P_YM_STR = Pattern.compile("^([-]?[0-9]+)([yM])?$");

    /**
     * 将一个字符串变成毫秒数，如果就是数字，那么表示毫秒
     * 
     * <ul>
     * <li><code>10s</code> 表示10秒
     * <li><code>20m</code> 表示20分钟
     * <li><code>1h</code> 表示1小时
     * <li><code>1d</code> 表示一天
     * <li><code>1w</code> 表示一周
     * <li><code>100</code> 表示 100毫秒</li>
     * </ul>
     * 
     * @param str
     *            描述时间的字符串
     * @return 字符串表示的毫秒数
     */
    public static long millisecond(String str) {
        Matcher m = P_MS_STR.matcher(str);
        if (!m.find())
            throw Er.create("e.ms.invalid", str);
        long ms = Long.parseLong(m.group(1));
        String unit = m.group(2);
        return millisecond(ms, unit);
    }

    /**
     * 将一个时间值变成毫秒数，那么表示毫秒
     * 
     * <ul>
     * <li><code>"s"</code> 表示秒
     * <li><code>"m"</code> 表示分钟
     * <li><code>"h"</code> 表示小时
     * <li><code>"d"</code> 表示天
     * <li><code>"w"</code> 表示周
     * <li><code>null</code> 默认表示毫秒</li>
     * </ul>
     * 
     * @param time
     *            时间值
     * @param unit
     *            时间单位
     * @return 毫秒数
     */
    public static long millisecond(long time, String unit) {
        // s 秒
        if ("s".equals(unit)) {
            return time * 1000L;
        }
        // m 分
        else if ("m".equals(unit)) {
            return time * 60000L;
        }
        // h 小时
        else if ("h".equals(unit)) {
            return time * 3600000L;
        }
        // d 天
        else if ("d".equals(unit)) {
            return time * 86400000L;
        }
        // w 周
        else if ("w".equals(unit)) {
            return time * 86400000L * 7;
        }
        // 默认就是毫秒
        return time;
    }
}
