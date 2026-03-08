package org.nutz.lang.util;

public abstract class Ranges {

    public static RangeInfo toRangeInfo(NutMap map) {
        RangeInfo info = new RangeInfo();
        info.hasMinValue = map.getBoolean("hasMinValue");
        info.minValue = map.get("minValue");
        info.minValueIncluded = map.getBoolean("minValueIncluded");
        info.hasMaxValue = map.getBoolean("hasMaxValue");
        info.maxValue = map.get("maxValue");
        info.maxValueIncluded = map.getBoolean("maxValueIncluded");
        return info;
    }

    public static IntRange Int(String str) {
        return new IntRange(str);
    }

    public static IntRange Intf(String fmt, Object... args) {
        return new IntRange(String.format(fmt, args));
    }

    public static LongRange Long(String str) {
        return new LongRange(str);
    }

    public static LongRange Longf(String fmt, Object... args) {
        return new LongRange(String.format(fmt, args));
    }

    public static FloatRange Float(String str) {
        return new FloatRange(str);
    }

    public static FloatRange Floatf(String fmt, Object... args) {
        return new FloatRange(String.format(fmt, args));
    }

    public static DoubleRange Double(String str) {
        return new DoubleRange(str);
    }

    public static DoubleRange Doublef(String fmt, Object... args) {
        return new DoubleRange(String.format(fmt, args));
    }

    public static StrRange Str(String str) {
        return new StrRange(str);
    }

    public static StrRange Strf(String fmt, Object... args) {
        return new StrRange(String.format(fmt, args));
    }

    public static DateRange Date(String str) {
        return new DateRange(str);
    }

    public static DateRange Datef(String fmt, Object... args) {
        return new DateRange(String.format(fmt, args));
    }

    public static TimeRange Time(String str) {
        return new TimeRange(str);
    }

    public static TimeRange Timef(String fmt, Object... args) {
        return new TimeRange(String.format(fmt, args));
    }

}
