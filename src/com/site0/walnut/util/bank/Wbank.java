package com.site0.walnut.util.bank;

import java.util.ArrayList;

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
        boolean decimalFixed = true;

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
            if (null != options.decimalFixed) {
                decimalFixed = options.decimalFixed.booleanValue();
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

        ArrayList<String> parts = new ArrayList<>(2);
        String v = partitionInt(partInt, width, sep, to);
        if (prefix.length() > 0) {
            parts.add(prefix + v);
        } else {
            parts.add(v);
        }

        // 对于小数部分，对齐精度
        if (decimalPlaces > 0) {
            if (partFra.length() > decimalPlaces) {
                String dp = partFra.substring(0, decimalPlaces)
                            + "."
                            + partFra.substring(decimalPlaces);
                long fr = Math.round(Double.valueOf(dp));
                parts.add(Long.toString(fr));
            }
            // 强制对其精度
            else if(decimalFixed){
                parts.add(Ws.padEnd(partFra, decimalPlaces, '0'));
            }
            // 直接填入
            else if (!Strings.isBlank(partFra)) {
                parts.add(partFra);
            }
        }
        // 默认填入小数部分
        else if (!Strings.isBlank(partFra)) {
            parts.add(partFra);
        }

        // 搞定
        return Ws.join(parts, ".");
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
