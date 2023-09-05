package org.nutz.walnut.util;

public abstract class Wnum {
    
    /***
     *
     * ```
     * scrollIndex( 3, 5) => 3
     * scrollIndex( 0, 5) => 0
     * scrollIndex( 4, 5) => 4
     * scrollIndex( 5, 5) => 0
     * scrollIndex( 6, 5) => 1
     * scrollIndex(-1, 5) => 4
     * scrollIndex(-5, 5) => 0
     * scrollIndex(-6, 5) => 4
     * scrollIndex(-5, 5) => 0
     * ```
     */
    public static int scrollIndex(int index, int len) {
      if (len > 0) {
        int md = index % len;
        return md >= 0 ? md : len + md;
      }
      return -1;
    }

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

    public static int sum(int... inputs) {
        int re = 0;
        for (int n : inputs) {
            re += n;
        }
        return re;
    }

    public static long sum(long... inputs) {
        long re = 0;
        for (long n : inputs) {
            re += n;
        }
        return re;
    }

    public static double sum(double... inputs) {
        double re = 0;
        for (double n : inputs) {
            re += n;
        }
        return re;
    }

}
