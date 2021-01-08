package org.nutz.walnut.alg.ds.buf;

/**
 * 一个简单的字符串缓冲，是对于字符数组的包裹，用来提供一些高级读取操作，譬如:
 * 
 * <ul>
 * <li>按行读取
 * <li>根据一个自动机匹配
 * <li>读取到指定字符
 * <li>等等
 * </ul>
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnCharArray {

    /**
     * 字符数据
     */
    private char[] data;
    
    /**
     * 开始位置下标（包含）
     */
    private int offset;

    /**
     * 最后一个位置的下标（不包含）
     */
    private int limit;

    /**
     * 当前位置游标
     */
    private int cursor;

    public WnCharArray(String str) {
        this(str.toCharArray());
    }

    public WnCharArray(char[] data) {
        this(data, 0, data.length);
    }

    public WnCharArray(char[] data, int offset, int len) {
        this.data = data;
        this.offset = offset;
        this.cursor = offset;
        this.limit = offset + len;
    }

    public int findIndex(char c) {
        for (int i = cursor; i < limit; i++) {
            if (data[i] == c) {
                return i;
            }
        }
        return -1;
    }

    public int findIndex(char[] cs) {
        for (int i = cursor; i < limit; i++) {
            boolean matched = true;
            for (int x = 0; x < cs.length; x++) {
                if (data[i + x] != cs[x]) {
                    matched = false;
                    break;
                }
            }
            if (matched) {
                return i;
            }
        }
        return -1;
    }

    public String getString(int fromIndex, int toIndex) {
        int len = toIndex - fromIndex;
        return new String(data, fromIndex, len);
    }
    
    public char[] getCharArray(int fromIndex, int toIndex) {
        int len = toIndex - fromIndex;
        char[] cs = new char[len];
        System.arraycopy(data, fromIndex, cs, 0, len);
        return cs;
    }
    
    /**
     * 返回当前位置下一行的内容。并将光标移动到下一行开头
     * 
     * @return 一行的内容（去掉行尾的换行符）。 null 表示没有更多内容了。
     */
    public char[] nextLineChars() {
        int ni = this.findIndex('\n');
        if (ni == cursor) {
            cursor++;
            return new char[0];
        }
        if (ni > cursor) {
            int endI = ni;
            if (data[endI - 1] == '\r') {
                endI--;
            }
            char[] s = this.getCharArray(cursor, endI);
            cursor = ni + 1;
            return s;
        }
        if (cursor < limit) {
            char[] s = this.getCharArray(cursor, limit);
            cursor = limit;
            return s;
        }
        return null;
    }

    /**
     * 返回当前位置下一行的内容。并将光标移动到下一行开头
     * 
     * @return 一行的内容（去掉行尾的换行符）。null 表示没有更多内容了。
     */
    public String nextLine() {
        int ni = this.findIndex('\n');
        if (ni == cursor) {
            cursor++;
            return "";
        }
        if (ni > cursor) {
            int endI = ni;
            if (data[endI - 1] == '\r') {
                endI--;
            }
            String s = this.getString(cursor, endI);
            cursor = ni + 1;
            return s;
        }
        if (cursor < limit) {
            String s = this.getString(cursor, limit);
            cursor = limit;
            return s;
        }
        return null;
    }

    /**
     * 返回当前字符，并后移一位光标。
     * 
     * @return 字符。 <code>0</code> 表示没有更多字符了。
     */
    public char nextChar() {
        if (cursor >= limit) {
            return 0;
        }
        return data[cursor++];
    }

    public boolean hasNextChar() {
        return cursor < limit;
    }

    /**
     * 前移一位光标，并返回当前字符。
     * 
     * @return 字符。 <code>0</code> 表示没有更多字符了。
     */
    public char prevChar() {
        if (cursor <= offset) {
            return 0;
        }
        return data[--cursor];
    }

    public boolean hasPrevChar() {
        return cursor > offset;
    }

}
