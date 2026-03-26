package com.site0.walnut.util.bank;

import org.nutz.lang.Strings;

import com.site0.walnut.util.Ws;

public abstract class Wbank {

    public static String toBankText(Object val, PartitionOptions options) {
        // 防空：与 TS 逻辑一致，空值或空白字符串直接返回
        if (null == val) {
            return null;
        }
        String raw = String.valueOf(val);
        if (Ws.isBlank(raw)) {
            return raw;
        }

        // 默认参数
        int width = 3;
        String sep = ",";
        HDirecton to = HDirecton.left;
        int decimalPlaces = 2;

        if (null != options) {
            if (options.width > 0) {
                width = options.width;
            }
            if (!Strings.isBlank(options.sep)) {
                sep = options.sep;
            }
            if (null != options.to) {
                to = options.to;
            }
            if (options.decimalPlaces >= 0) {
                decimalPlaces = options.decimalPlaces;
            }
        }

        String str = raw.trim();

        // 先拿到前缀 +/-
        String prefix = "";
        if (!str.isEmpty()) {
            char c0 = str.charAt(0);
            if ('+' == c0 || '-' == c0) {
                prefix = String.valueOf(c0);
                str = str.substring(1);
            }
        }

        // 过滤掉非数字和小数点字符
        String s = str.replaceAll("[^0-9.]", "");

        // 分成整数和小数两部分
        String partInt = "";
        String partFra = "";
        int pos = s.indexOf('.');
        if (0 == pos) {
            partInt = "0";
            partFra = s.substring(1).trim();
        } else if (pos < 0) {
            partInt = s;
        } else {
            partInt = s.substring(0, pos).trim();
            partFra = s.substring(pos + 1).trim();
        }

        StringBuilder sb = new StringBuilder();
        sb.append(prefix);
        sb.append(partitionInt(partInt, width, sep, to));

        if (decimalPlaces > 0) {
            String fra = partFra;
            if (fra.length() < decimalPlaces) {
                StringBuilder fsb = new StringBuilder(fra);
                while (fsb.length() < decimalPlaces) {
                    fsb.append('0');
                }
                fra = fsb.toString();
            }
            sb.append('.').append(fra);
        } else if (!Strings.isBlank(partFra)) {
            sb.append('.').append(partFra);
        }

        return sb.toString();
    }

    private static String partitionInt(String input,
                                       int width,
                                       String sep,
                                       HDirecton to) {
        if (Strings.isBlank(input)) {
            return "";
        }
        if (width <= 0) {
            return input;
        }

        String s = input.trim();
        StringBuilder sb = new StringBuilder();

        // left: 从右向左分隔（金额常用）
        if (HDirecton.left == to) {
            int len = s.length();
            int first = len % width;
            int i = 0;
            if (first > 0) {
                sb.append(s, 0, first);
                i = first;
            }
            while (i < len) {
                if (sb.length() > 0) {
                    sb.append(sep);
                }
                int end = Math.min(i + width, len);
                sb.append(s, i, end);
                i = end;
            }
            return sb.toString();
        }

        // right: 从左向右分隔（账号/激活码常用）
        int i = 0;
        int len = s.length();
        while (i < len) {
            if (sb.length() > 0) {
                sb.append(sep);
            }
            int end = Math.min(i + width, len);
            sb.append(s, i, end);
            i = end;
        }
        return sb.toString();
    }

}
