package org.nutz.walnut.impl.box;

import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.Nums;
import org.nutz.lang.Strings;
import org.nutz.walnut.api.err.Er;

public class Jvms {

    public static String[] split(String str, boolean keepQuote, char... seps) {
        List<String> list = new LinkedList<String>();
        char[] cs = str.toCharArray();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cs.length; i++) {
            char c = cs[i];
            // 遇到分隔符号
            if (Nums.isin(seps, c)) {
                if (!Strings.isBlank(sb)) {
                    list.add(Strings.trim(sb));
                    sb = new StringBuilder();
                }
            }
            // 字符串
            else if (c == '\'' || c == '"' || c == '`') {
                if (keepQuote)
                    sb.append(c);
                while (++i < cs.length) {
                    char c2 = cs[i];
                    // 如果是转义字符
                    if (c2 == '\\') {
                        c2 = cs[++i];
                        switch (c2) {
                        case 'n':
                            sb.append('\n');
                            break;
                        case 'r':
                            sb.append('\r');
                            break;
                        case 't':
                            sb.append('\t');
                            break;
                        case 'b':
                            sb.append('\b');
                            break;
                        case '\'':
                        case '"':
                        case '\\':
                            sb.append(c2);
                            break;
                        default:
                            throw Er.create("e.cmd.escape.invald", c2);
                        }
                    }
                    // 退出字符串
                    else if (c2 == c) {
                        if (keepQuote)
                            sb.append(c2);
                        break;
                    }
                    // 其他附加
                    else {
                        sb.append(c2);
                    }
                }
            }
            // 其他，计入
            else {
                sb.append(c);
            }
        }

        // 添加最后一个
        if (!Strings.isBlank(sb)) {
            list.add(Strings.trim(sb));
        }

        // 返回拆分后的数组
        return list.toArray(new String[list.size()]);
    }

}
