package com.site0.walnut.util.time;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.lang.Strings;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.util.Wtime;

public class WnDayTime {

    private static final Pattern _p_tm = Pattern.compile("^([0-9]{1,2}):([0-9]{1,2})(:([0-9]{1,2})([.,]([0-9]{1,3}))?)?$");

    private int value;
    private int valueInMillisecond;
    private int hour;
    private int minute;
    private int second;
    private int millisecond;

    public WnDayTime() {
        this(System.currentTimeMillis());
    }

    public WnDayTime(long ams) {
        this.parseByMs(ams);
    }

    public WnDayTime(int sec) {
        this.parseBySec(sec);
    }

    public WnDayTime(String input) {
        this.parse(input);
    }

    /**
     * 根据一个毫秒数解析时间。
     * 
     * @param ams
     *            毫秒数。可以是绝对毫秒数（来自 Date.getTime()），也可以是一天中的毫秒数
     */
    public void parseByMs(long ams) {
        // 绝对毫秒数
        if (ams > 86400000L) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(ams);
            Wtime.setDayStart(c);
            ams = ams - c.getTimeInMillis();
        }
        // 解析
        this.valueInMillisecond = (int) ams;
        this.__eval_by_valueInMilliSecond();
    }

    /**
     * 根据一个秒数解析时间。
     * 
     * @param sec
     *            一天中的绝对秒数，<code>[0-86400)</code>
     */
    public void parseBySec(int sec) {
        if (sec >= 86400 || sec <= -86400) {
            sec = sec % 86400;
        }
        if (sec < 0) {
            sec = 86400 + sec;
        }
        this.valueInMillisecond = sec * 1000;
        this.__eval_by_valueInMilliSecond();
    }

    /**
     * 将一个时间字符串，转换成一个一天中的绝对时间对象
     * 
     * @param input
     *            时间字符串，符合格式
     *            <ul>
     *            <li>"HH:mm:ss"
     *            <li>"HH:mm"
     *            <li>"HH:mm:ss.SSS"
     *            <li>"HH:mm:ss,SSS"
     *            </ul>
     */
    public void parse(String input) {
        Matcher m = _p_tm.matcher(input);

        if (m.find()) {
            // 仅仅到分钟
            if (null == m.group(3)) {
                this.hour = Integer.parseInt(m.group(1));
                this.minute = Integer.parseInt(m.group(2));
                this.second = 0;
                this.millisecond = 0;
            }
            // 到秒
            else if (null == m.group(5)) {
                this.hour = Integer.parseInt(m.group(1));
                this.minute = Integer.parseInt(m.group(2));
                this.second = Integer.parseInt(m.group(4));
                this.millisecond = 0;
            }
            // 到毫秒
            else {
                this.hour = Integer.parseInt(m.group(1));
                this.minute = Integer.parseInt(m.group(2));
                this.second = Integer.parseInt(m.group(4));
                this.millisecond = Integer.parseInt(m.group(6));
            }
            // 计算其他的值
            this.__eval_value();
            return;
        }
        throw Er.create("e.time.invalid_format", input);
    }

    public void offset(int sec) {
        this.valueInMillisecond += sec * 1000;
        this.__eval_by_valueInMilliSecond();
    }

    public void offsetInMillisecond(int ms) {
        this.valueInMillisecond += ms;
        this.__eval_by_valueInMilliSecond();
    }

    private void __eval_by_valueInMilliSecond() {
        // 确保毫秒数在一天之内，即 [0, 86399000]
        if (this.valueInMillisecond >= 86400000) {
            this.valueInMillisecond = this.valueInMillisecond % 86400000;
        }
        // 负数表示后退
        else if (this.valueInMillisecond < 0) {
            this.valueInMillisecond = this.valueInMillisecond % 86400000;
            if (this.valueInMillisecond < 0) {
                this.valueInMillisecond = 86400000 + this.valueInMillisecond;
            }
        }
        // 计算其他值
        this.value = this.valueInMillisecond / 1000;
        this.millisecond = this.valueInMillisecond - this.value * 1000;
        this.hour = Math.min(23, this.value / 3600);
        this.minute = Math.min(59, (this.value - (this.hour * 3600)) / 60);
        this.second = Math.min(59, this.value - (this.hour * 3600) - (this.minute * 60));
    }

    private void __eval_value() {
        this.value = this.hour * 3600 + this.minute * 60 + this.second;
        this.valueInMillisecond = this.value * 1000 + this.millisecond;
    }

    @Override
    public String toString() {
        String fmt = "HH:mm";
        // 到毫秒
        if (0 != this.millisecond) {
            fmt += ":ss.SSS";
        }
        // 到秒
        else if (0 != this.second) {
            fmt += ":ss";
        }
        return toString(fmt);
    }

    private static Pattern _p_tmfmt = Pattern.compile("a|[HhKkms]{1,2}|S(SS)?");

    /**
     * <pre>
     * a    Am/pm marker (AM/PM)
     * H   Hour in day (0-23)
     * k   Hour in day (1-24)
     * K   Hour in am/pm (0-11)
     * h   Hour in am/pm (1-12)
     * m   Minute in hour
     * s   Second in minute
     * S   Millisecond Number
     * HH  补零的小时(0-23)
     * kk  补零的小时(1-24)
     * KK  补零的半天小时(0-11)
     * hh  补零的半天小时(1-12)
     * mm  补零的分钟
     * ss  补零的秒
     * SSS 补零的毫秒
     * </pre>
     * 
     * @param fmt
     *            格式化字符串类似 <code>"HH:mm:ss,SSS"</code>
     * @return 格式化后的时间
     */
    public String toString(String fmt) {
        StringBuilder sb = new StringBuilder();
        fmt = Strings.sBlank(fmt, "HH:mm:ss");
        Matcher m = _p_tmfmt.matcher(fmt);
        int pos = 0;
        while (m.find()) {
            int l = m.start();
            // 记录之前
            if (l > pos) {
                sb.append(fmt.substring(pos, l));
            }
            // 偏移
            pos = m.end();

            // 替换
            String s = m.group(0);
            if ("a".equals(s)) {
                sb.append(this.value > 43200 ? "PM" : "AM");
            }
            // H Hour in day (0-23)
            else if ("H".equals(s)) {
                sb.append(this.hour);
            }
            // k Hour in day (1-24)
            else if ("k".equals(s)) {
                sb.append(this.hour + 1);
            }
            // K Hour in am/pm (0-11)
            else if ("K".equals(s)) {
                sb.append(this.hour % 12);
            }
            // h Hour in am/pm (1-12)
            else if ("h".equals(s)) {
                sb.append((this.hour % 12) + 1);
            }
            // m Minute in hour
            else if ("m".equals(s)) {
                sb.append(this.minute);
            }
            // s Second in minute
            else if ("s".equals(s)) {
                sb.append(this.second);
            }
            // S Millisecond Number
            else if ("S".equals(s)) {
                sb.append(this.millisecond);
            }
            // HH 补零的小时(0-23)
            else if ("HH".equals(s)) {
                sb.append(String.format("%02d", this.hour));
            }
            // kk 补零的小时(1-24)
            else if ("kk".equals(s)) {
                sb.append(String.format("%02d", this.hour + 1));
            }
            // KK 补零的半天小时(0-11)
            else if ("KK".equals(s)) {
                sb.append(String.format("%02d", this.hour % 12));
            }
            // hh 补零的半天小时(1-12)
            else if ("hh".equals(s)) {
                sb.append(String.format("%02d", (this.hour % 12) + 1));
            }
            // mm 补零的分钟
            else if ("mm".equals(s)) {
                sb.append(String.format("%02d", this.minute));
            }
            // ss 补零的秒
            else if ("ss".equals(s)) {
                sb.append(String.format("%02d", this.second));
            }
            // SSS 补零的毫秒
            else if ("SSS".equals(s)) {
                sb.append(String.format("%03d", this.millisecond));
            }
            // 不认识
            else {
                sb.append(s);
            }
        }
        // 结尾
        if (pos < fmt.length()) {
            sb.append(fmt.substring(pos));
        }

        // 返回
        return sb.toString();
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getValueInMillisecond() {
        return valueInMillisecond;
    }

    public void setValueInMillisecond(int valueInMillisecond) {
        this.valueInMillisecond = valueInMillisecond;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public int getSecond() {
        return second;
    }

    public void setSecond(int second) {
        this.second = second;
    }

    public int getMillisecond() {
        return millisecond;
    }

    public void setMillisecond(int millisecond) {
        this.millisecond = millisecond;
    }

    public WnDayTime refreshValue() {
        this.__eval_value();
        return this;
    }

    public WnDayTime refreshFields() {
        this.__eval_by_valueInMilliSecond();
        return this;
    }
}
