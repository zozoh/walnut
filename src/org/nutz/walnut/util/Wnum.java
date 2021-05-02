package org.nutz.walnut.util;

public abstract class Wnum {

    public static int clamp(int n, int min, int max) {
        n = Math.max(n, min);
        n = Math.min(n, max);
        return n;
    }

    public static long clamp(long n, long min, long max) {
        n = Math.max(n, min);
        n = Math.min(n, max);
        return n;
    }

    public static double clamp(double n, double min, double max) {
        n = Math.max(n, min);
        n = Math.min(n, max);
        return n;
    }

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
