package org.nutz.walnut.util;

public abstract class Wnum {

    public static double precise(double n, int p) {
        if (p > 0) {
            double y = Math.pow(10, p);
            return Math.round(n * y) / y;
        }
        return n;
    }

    public static double precise(double n) {
        return precise(n, 2);
    }

}
