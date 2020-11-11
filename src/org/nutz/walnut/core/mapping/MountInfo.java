package org.nutz.walnut.core.mapping;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.lang.Strings;
import org.nutz.lang.util.Regex;
import org.nutz.walnut.api.err.Er;

public class MountInfo {

    static public class Item {
        // 模式
        boolean forBM;

        // 类型
        String type;

        // 参数
        String arg;

        @Override
        public String toString() {
            // 特殊规则，如果是文件映射
            if (type.startsWith("file")) {
                // 对于索引，只输出类型
                if (!this.forBM) {
                    return type;
                }
                // 对于桶，只输出 arg
                else {
                    return arg;
                }
            }
            // 只有类型
            if (Strings.isBlank(arg)) {
                return type;
            }
            // 混合输出
            return String.format("%s(%s)", type, arg);
        }
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

    public MountInfo() {}

    public MountInfo(String str) {
        set(str);
    }

    public void set(String str) {
        // 为空！那就是木有任何映射咯
        if (Strings.isBlank(str)) {
            return;
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
            bm.forBM = true;
        }
        // 两个都指定了
        else {
            ix = parseItem(str.substring(0, pos));
            bm = parseItem(str.substring(pos + 3));
            bm.forBM = true;

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

    static public Item parseItem(String str) {
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

    public boolean hasIndexer() {
        return null != ix;
    }

    public boolean hasBM() {
        return null != bm;
    }

    public boolean hasIndexerAndBM() {
        return null != ix && null != bm;
    }

    public boolean isIndexerType(String regex) {
        if (null == ix) {
            return false;
        }
        if (regex.startsWith("^")) {
            return ix.type.matches(regex);
        }
        return ix.type.equals(regex);
    }

    public Item getIndexer() {
        return ix;
    }

    public String getIndexerType() {
        return null == ix ? null : ix.type;
    }

    public String getIndexerArg() {
        return null == ix ? null : ix.arg;
    }

    public void setIndexer(String str) {
        if (Strings.isBlank(str)) {
            this.ix = null;
        } else {
            this.ix = parseItem(str);
        }
    }

    public boolean isBMType(String regex) {
        if (null == bm) {
            return false;
        }
        if (regex.startsWith("^")) {
            return bm.type.matches(regex);
        }
        return bm.type.equals(regex);
    }

    public Item getBM() {
        return bm;
    }

    public String getBMType() {
        return null == bm ? null : bm.type;
    }

    public String getBMArg() {
        return null == bm ? null : bm.arg;
    }

    public void setBM(String str) {
        if (Strings.isBlank(str)) {
            this.bm = null;
        } else {
            this.bm = parseItem(str);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (null != ix) {
            sb.append(ix.toString());
        }
        if (null != bm) {
            sb.append("://");
            sb.append(bm.toString());
        }
        return sb.toString();
    }
}
