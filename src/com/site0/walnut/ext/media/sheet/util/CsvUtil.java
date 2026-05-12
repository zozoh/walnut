package com.site0.walnut.ext.media.sheet.util;

import java.util.ArrayList;
import java.util.List;

import com.site0.walnut.util.Ws;

public abstract class CsvUtil {

    public static String[] splitLineAsArray(String csvLine, char[] seps) {
        List<String> list = splitLine(csvLine, seps);
        return list.toArray(new String[list.size()]);
    }

    public static String[] splitLineAsArray(String csvLine, String seps) {
        List<String> list = splitLine(csvLine, seps);
        return list.toArray(new String[list.size()]);
    }

    public static List<String> splitLine(String csvLine, String seps) {
        seps = Ws.sBlank(seps, ",");
        return splitLine(csvLine, seps.toCharArray());
    }

    /**
     * 针对一个 CSV 文件的一行，对其按分隔符拆分。 符合(CSV 标准（RFC 4180）)
     * <p>
     * 本函数支持多个分隔符符，譬如 <code>seps=[' ',',',';']</code> 标识可以按空格，半角逗号，分号，拆分
     * <p>
     * 会自动去掉单元格的引号包裹，规则是:
     * <ol>
     * <li>单元格以双引号(<code>"</code>)开头，就要以双引号结尾，包裹范围忽略分隔符
     * <li>连续两个引号表示转义
     * </ol>
     * 因此输出的内容，需要考虑上面的情况，仅仅输出转义以后的内容，以及解开引号包裹以后的内容
     * 
     * @param csvLine
     *            CSV 文件行
     * @param seps
     *            分隔符表
     * @return 拆分后，单元格内容数组
     */
    public static List<String> splitLine(String csvLine, char[] seps) {
        List<String> re = new ArrayList<>();
        if (null == csvLine) {
            return re;
        }

        StringBuilder sb = new StringBuilder();
        boolean inQuote = false;

        for (int i = 0; i < csvLine.length(); i++) {
            char c = csvLine.charAt(i);

            if (inQuote) {
                if ('"' == c) {
                    // 双引号内部的连续两个引号代表一个转义引号
                    if (i + 1 < csvLine.length()
                        && '"' == csvLine.charAt(i + 1)) {
                        sb.append('"');
                        i++;
                    } else {
                        inQuote = false;
                    }
                } else {
                    sb.append(c);
                }
                continue;
            }

            if ('"' == c) {
                inQuote = true;
                continue;
            }

            if (is_sep(c, seps)) {
                re.add(sb.toString());
                sb.setLength(0);
                continue;
            }

            sb.append(c);
        }

        re.add(sb.toString());
        return re;
    }

    private static boolean is_sep(char c, char[] seps) {
        if (null == seps || seps.length == 0) {
            return false;
        }
        for (char sep : seps) {
            if (sep == c) {
                return true;
            }
        }
        return false;
    }

}
