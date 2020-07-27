package org.nutz.walnut.core.mapping;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.lang.Strings;
import org.nutz.lang.util.Regex;
import org.nutz.walnut.api.err.Er;

public class MountInfo {

    static class Item {
        // 类型
        String type;
        // 参数
        String arg;
    }

    /**
     * 索引管理器
     */
    Item ix;

    /**
     * 桶管理器类型
     */
    Item bm;

    private static final String regex = "^([^(]+)(\\(([^)]+)\\))?$";
    private static final Pattern _P = Regex.getPattern(regex);

    MountInfo(String str) {
        // 为空！不科学啊
        if (Strings.isBlank(str)) {
            throw Er.create("e.io.mapping.BlankMount");
        }
        // 保险一点，抹去前后空白吧
        str = str.trim();
        int pos = str.indexOf("://");
        // 木有的话，那么说明，桶管理器是用全局的，给定这个仅仅是索引管理器
        if (pos < 0) {
            ix = parseItem(str);
        }
        // 第一个，那么表示索引用全局的，桶管理器是指定的
        else if (0 == pos) {
            bm = parseItem(str.substring(3));
        }
        // 两个都指定了
        else {
            ix = parseItem(str.substring(0, pos));
            bm = parseItem(str.substring(pos + 3));

            // 这个特殊规则用来兼容之前的 "file:///" 和 "file://C:/" 映射
            // 即，如果只有 bmArg 且 bmType 为空，那么 bmType 相当于 file
            if ("file".equals(ix.type) || "filew".equals(ix.type)) {
                if (null == bm.arg && null != bm.type) {
                    bm.arg = bm.type;
                    bm.type = ix.type;
                }
                // 最后，索引管理器的参数也来一个咯
                ix.arg = bm.arg;
            }
        }
    }

    Item parseItem(String str) {
        Matcher m = _P.matcher(str.trim());
        // 靠，什么妖物？！
        if (!m.find()) {
            throw Er.create("e.io.mount.invalid", str);
        }
        Item it = new Item();
        it.type = Strings.trim(m.group(1));
        it.arg = Strings.trim(m.group(3));
        return it;
    }

    boolean hasIndexer() {
        return null != ix;
    }

    boolean hasBM() {
        return null != bm;
    }
}
