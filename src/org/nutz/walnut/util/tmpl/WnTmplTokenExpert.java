package org.nutz.walnut.util.tmpl;

import java.lang.reflect.Array;
import java.util.Arrays;

import org.nutz.walnut.alg.stack.WnCharStack;

public class WnTmplTokenExpert {

    /**
     * 解析时推入的字符缓冲
     * 
     * <pre>
     * [ 0 0 $ {]
     *         ^
     *         |---- 永远推入最后一个位置，每次推入，字符顺序前移动
     * </pre>
     */
    private char[] buf;

    private int bufLastI;

    /**
     * 符合这个特征的字符将会逃逸输出为一个字符（最后一个字符） <br>
     * 因此这样的字符也不会被激活
     */
    private char[] escapes;

    /**
     * 符合这个特征的字符将会自动进入激活状态， 最后一个字符将作为解析栈的初始字符
     */
    private char[] starts;

    private char pusher;

    /**
     * 结束字符，这个字符会停止激活状态
     */
    private char stop;

    public WnTmplTokenExpert(String escapes, String starts, char stop) {
        this(escapes.toCharArray(), starts.toCharArray(), stop);
    }

    public WnTmplTokenExpert(char[] escapes, char[] starts, char stop) {
        this(escapes, starts, starts[starts.length - 1], stop);
    }

    public WnTmplTokenExpert(char[] escapes, char[] starts, char pusher, char stop) {
        this.escapes = escapes;
        this.starts = starts;
        this.pusher = pusher;
        this.stop = stop;
        int N = Math.max(this.escapes.length, this.starts.length);
        this.buf = new char[N];
        this.bufLastI = this.buf.length - 1;
    }

    public boolean isEscape() {
        return __is_match(buf, escapes);
    }

    public boolean isStarts() {
        return __is_match(buf, starts);
    }

    public char[] escapeBuf() {
        return new char[]{buf[bufLastI]};
    }

    public void joinBufToString(StringBuilder sb) {
        for (char c : buf) {
            if (c > 0) {
                sb.append(c);
            }
        }
    }

    public void clearBuf() {
        Arrays.fill(this.buf, (char) 0);
    }

    public char pushToBuf(char c) {
        char re = this.buf[0];
        // 顺移缓冲字符
        for (int i = 0; i < bufLastI; i++) {
            this.buf[i] = buf[i + 1];
        }
        // 计入最后一个字符
        this.buf[bufLastI] = c;
        return re;
    }

    private static boolean __is_match(char[] buf, char[] cs) {
        int offInBuf = cs.length - buf.length;
        for (int i = 0; i < cs.length; i++) {
            if (cs[i] != buf[i + offInBuf]) {
                return false;
            }
        }
        return true;
    }

    public WnCharStack createCharStack() {
        return new WnCharStack(pusher, stop);
    }

}
