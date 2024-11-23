package com.site0.walnut.util.tmpl.ele;

import java.text.DecimalFormat;

public class StrFloatSubStrConvertor implements StrEleConvertor {

    /**
     * 首先用这个长度截一道, 如果小于等于 0 则书屋
     */
    private int maxLen;

    /**
     * 精度，如果 `>0` 则表示要四舍五入
     */
    private int precision;

    private double _r;

    public StrFloatSubStrConvertor(String input) {
        String[] ss = input.trim().split("[.,#-]");
        if (ss.length > 1) {
            maxLen = Integer.parseInt(ss[0]);
            precision = Integer.parseInt(ss[1]);
        }
        // 只有一个值
        else if (ss.length > 0) {
            maxLen = Integer.parseInt(ss[0]);
            precision = 0;
        }

        if (precision > 0) {
            _r = Math.pow(10, precision);
        }
    }

    private static final DecimalFormat DEC_FMT = new DecimalFormat();
    static {
        DEC_FMT.setMaximumFractionDigits(340);
    }

    @Override
    public String process(String str) {
        try {
            // 无效
            if (maxLen < 0) {
                return str;
            }

            // 确保是数字那么，科学计数法的数字也能被归一化处理
            double d_input = Double.parseDouble(str);
            String d_str = DEC_FMT.format(d_input).replaceAll(",", "");

            // 首先截取一下
            String s = d_str.substring(0, maxLen);

            // 如果结尾为 . 也截取
            if (s.endsWith(".")) {
                s.substring(0, maxLen - 1).trim();
            }

            // 不需要四舍五入
            if (precision <= 0) {
                return s;
            }

            // 转换为数字
            double d = Double.parseDouble(s);
            double n = Math.round(d * _r) / _r;

            // 处理 0 的情况
            if (n == 0.0 && d_input > 0.0) {
                n = 1.0 / _r;
            }

            String re = DEC_FMT.format(n).replaceAll(",", "");
            if (re.endsWith(".0")) {
                return re.substring(0, re.length() - 2);
            }
            return re;
        }
        catch (Throwable e) {
            return str;
        }
    }
}
