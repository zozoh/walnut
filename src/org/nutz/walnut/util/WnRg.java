package org.nutz.walnut.util;

public abstract class WnRg {

    private static String L = "([\\[(])";
    private static String R = "([\\])])";
    private static String seg = "([ \t]*,[ \t]*)";
    private static String FMT = "%5$s%1$s(%3$s|(%3$s%4$s%3$s)|(%3$s%4$s)|(%4$s%3$s))%2$s%6$s";

    public static String regex(String v) {
        return regex(v, "^");
    }

    public static String regex(String v, String prefix) {
        return String.format(FMT, L, R, v, seg, prefix, "$");
    }

    public static String intRegion() {
        return regex("([0-9]{1,9})");
    }

    public static String longRegion() {
        return regex("([0-9]{9,})");
    }

    public static String floatRegion() {
        return regex("([0-9]*[.][0-9]+)");
    }

    public static String dateRegion(String prefix) {
        return regex("([0-9T/: -]{10,})", prefix);
    }

}
