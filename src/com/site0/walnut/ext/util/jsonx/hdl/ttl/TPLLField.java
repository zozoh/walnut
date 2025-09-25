package com.site0.walnut.ext.util.jsonx.hdl.ttl;

import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.Wtime;

public class TPLLField {

    private String key;

    private TPLLFieldType type;

    /**
     * 字段开始位置（1base）
     */
    private int start;

    /**
     * 字段长度
     */
    private int len;

    private String comments;

    private String timezone;

    public boolean isFiller() {
        return "Filler".equalsIgnoreCase(this.key);
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public boolean isNumeric() {
        return TPLLFieldType.Numeric == this.type;
    }

    public boolean isCharacter() {
        return TPLLFieldType.Character == this.type;
    }

    public boolean isDts20() {
        return TPLLFieldType.Dts20 == this.type;
    }

    private static Pattern PDts20 = Pattern
        .compile("^([0-9]{4})([0-9]{2})([0-9]{2})"
                 + "([0-9]{2})([0-9]{2})([0-9]{2})"
                 + "([0-9]{3})([0-9]{3})$");

    private static Date Y3000 = Wtime.parseDate("3000-01-01",
                                                TimeZone.getTimeZone("UTC"));

    public Date parseAsDts20(TimeZone dftTz, String val) {
        if (Ws.isBlank(val))
            return null;
        TimeZone tz = this.getTimezone(dftTz);
        Matcher m = PDts20.matcher(val);
        if (!m.find()) {
            throw Er.create("e.tpll.dst20.InvalidFormat", val);
        }
        String s = String.format("%s-%s-%s %s:%s:%s.%s",
                                 m.group(1),
                                 m.group(2),
                                 m.group(3),
                                 m.group(4),
                                 m.group(5),
                                 m.group(6),
                                 m.group(7));
        return Wtime.parseDate(s, tz);
    }

    public boolean isDcymd8() {
        return TPLLFieldType.Dcymd8 == this.type;
    }

    public boolean isDcymd16() {
        return TPLLFieldType.Dcymd16 == this.type;
    }

    private static Pattern PDcymd8 = Pattern
        .compile("^([0-9]{4})([0-9]{2})([0-9]{2})$");

    private static Pattern PDcymd16 = Pattern
        .compile("^([0-9]{4})([0-9]{2})([0-9]{2})([0-9]{2})([0-9]{2})([0-9]{2})$");

    public Date parseAsDcymd8(TimeZone dftTz, String val) {
        if (Ws.isBlank(val))
            return null;
        // ICS 的码表，默认认为 00010101 表示没有结束日期
        // 为了统一，全部干到1千年后
        if ("00010101".equals(val)) {
            return Y3000;
        }
        TimeZone tz = this.getTimezone(dftTz);
        Matcher m = PDcymd8.matcher(val);
        if (!m.find()) {
            throw Er.create("e.tpll.dcymd8.InvalidFormat", val);
        }
        String s = String
            .format("%s-%s-%s", m.group(1), m.group(2), m.group(3));
        return Wtime.parseDate(s, tz);
    }

    public Date parseAsDcymd16(TimeZone dftTz, String val) {
        if (Ws.isBlank(val))
            return null;
        // ICS 的码表，默认认为 00010101000000 表示没有结束日期
        // 为了统一，全部干到1千年后
        if (val.startsWith("00010101000000")) {
            return Y3000;
        }
        TimeZone tz = this.getTimezone(dftTz);
        Matcher m = PDcymd16.matcher(val);
        if (!m.find()) {
            throw Er.create("e.tpll.dcymd8.InvalidFormat", val);
        }
        String s = String
            .format("%s-%s-%s", m.group(1), m.group(2), m.group(3));
        return Wtime.parseDate(s, tz);
    }

    public TPLLFieldType getType() {
        return type;
    }

    public void setType(TPLLFieldType type) {
        this.type = type;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getLen() {
        return len;
    }

    public void setLen(int len) {
        this.len = len;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public TimeZone getTimezone(TimeZone dftTz) {
        if (Ws.isBlank(timezone)) {
            return dftTz;
        }
        return TimeZone.getTimeZone(timezone);
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

}
