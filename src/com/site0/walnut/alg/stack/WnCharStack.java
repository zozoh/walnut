package com.site0.walnut.alg.stack;

import java.util.LinkedList;
import java.util.List;
import org.nutz.lang.util.LinkedCharArray;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.util.Wchar.EscapeTable;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Ws;

/**
 * 这是一个简单的堆栈，主要用来做字符串解析。
 * 
 * <h2>场景</h2>
 * 
 * <ul>
 * <li><code>"abcd"</code> : 简单字符串解析 => <code>abcd</code>
 * <li><code>"ab\"cd"</code> : 带逃逸字符串解析 => <code>ab"cd</code>
 * <li><code>"a'b'cd"</code> : 字符串嵌套 => <code>a'b'cd</code>
 * <li><code>{{x:100}}</code> : 复杂的对象 => <code>{x:100}</code>
 * </ul>
 * 
 * <h2>基本思路</h2> 本类需要有两个字符，压栈符和退栈符。譬如 '"' 于 '"' 或者 '{' 与 '}' 之后，它维护了一个界定字符的堆栈：
 * 
 * <pre>
 * 譬如界定字符为 '{' 与 '}' ，逃逸标识符为 '\'
 * 
 * [ \ ]  <-- 栈顶表示逃逸，下一个接收的字符将会逃逸并压入下一层缓冲，逃逸失败则抛错
 * [ { ]  <-- 每个压栈符，将堆栈升高 --> 并对应一个字符缓冲 [...]
 * [ { ]  <-- 如果上层堆栈弹出，则将字符（包括压/退栈符）都存入本层缓冲
 * [ { ]  <-- 最后一层缓冲弹出，并不包括压/退栈符
 * </pre>
 * 
 * 当初始状态，即并未有压栈符进入堆栈，则会一直跳过输入的字符串，直到遇到第一个压栈符才开始正式的分析操做<br>
 * 当退栈符导致堆栈被清空，则进入贤者时间，除非执行过 getContentAndReset 否则不再接受字符
 * 
 * 因此本堆栈有下面几种状态
 * 
 * <ul>
 * <li><code>S0</code> 休眠态： 未曾压栈，对于输入默认是 REJECT
 * <li><code>S1</code> 激活态： 已经压栈，对于输入默认是 ACCEPT
 * <li><code>S9</code> 完成态： 已经清栈，对于输入默认是 DONE
 * </ul>
 * 
 * <pre>
 * REJECT          ACCEPT           DONE
 *   ^               ^                ^
 * +----+          +----+          +----+
 * | S0 |--- { --> | S1 |--- } --> | S9 | 
 * +----+          +----+          +----+
 *   ^                                |
 *   |                                |
 *   +---- getContentAndReset --------+
 * </pre>
 * 
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnCharStack {

    /**
     * 内部状态
     */
    enum Status {
        S0, S1, S9
    }

    /**
     * 压栈符
     */
    private char cPush;

    /**
     * 候选压栈符：主要是动态决定 " 还是 ' 作为压栈符 <br>
     * 如果有候选压栈符，则重置时，也需要将 cPush 标记为 0
     */
    private char[] canPush;

    /**
     * 退栈符，0 表示退栈符，与压栈符成对出现
     */
    private char cPop;

    /**
     * 逃逸字符
     */
    private char cEscaper;

    /**
     * 可逃逸字符表
     */
    private EscapeTable escTable;

    private char topC;

    private StringBuilder topBuf;

    private LinkedCharArray stackC;

    private LinkedList<StringBuilder> stackBuf;

    private Status status;

    public WnCharStack(char cPush, char cPop) {
        this(cPush, cPop, '\\', Ws.STR_UNESC_TAB);
    }

    public WnCharStack(String canPushs) {
        this((char) 0, (char) 0, '\\', Ws.STR_UNESC_TAB);
        this.setCanPush(canPushs.toCharArray());
    }

    public WnCharStack(char cPush, char cPop, char cEscaper, EscapeTable escTable) {
        this.cPush = cPush;
        this.canPush = null;
        this.cPop = cPop;
        this.cEscaper = cEscaper;
        this.escTable = escTable;
        reset();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder('<').append(status).append("> :: ");
        sb.append('\'').append(cPush).append(cPop);
        sb.append(cEscaper).append('\'');
        if (Status.S0 != this.status) {
            // 栈顶
            sb.append(String.format("\n[ %s ] >> (\"%s\")", this.topC, this.topBuf));
            // 后续栈
            if (stackBuf.size() > 0) {
                StringBuilder[] sbs = stackBuf.toArray(new StringBuilder[stackBuf.size()]);
                int N = stackC.size();
                for (int i = 0; i < N; i++) {
                    char c = stackC.get(N - i - 1);
                    int IS = sbs.length - i - 1;

                    if (IS >= 0) {
                        sb.append(String.format("\n[ %s ] >> (\"%s\")", c, sbs[IS]));
                    } else {
                        sb.append(String.format("\n[ %s ]", c));
                    }
                }
            }
        }
        return sb.toString();
    }

    private void reset() {
        this.status = Status.S0;
        this.topC = 0;
        this.topBuf = null;
        this.stackC = null;
        this.stackBuf = null;
        if (null != this.canPush) {
            this.cPush = (char) 0;
        }
    }

    public WnStackPushResult push(char c) {
        // 休眠态
        if (Status.S0 == this.status) {
            // 没有压栈付，从候选压栈符里选择
            if (this.cPush == 0 && null != this.canPush) {
                for (char ca : this.canPush) {
                    // 确定了本次压栈符
                    if (ca == c) {
                        this.cPush = ca;
                        break;
                    }
                }
            }
            // 只有压栈付才能接受
            if (this.cPush != c) {
                return WnStackPushResult.REJECT;
            }
            // 接受压栈符
            this.topC = c;
            this.topBuf = new StringBuilder();
            this.stackC = new LinkedCharArray();
            this.stackBuf = new LinkedList<>();
            this.status = Status.S1;
            return WnStackPushResult.ACCEPT;
        }
        // 激活态
        if (Status.S1 == this.status) {
            // 逃逸字符
            if (this.cEscaper == this.topC) {
                char c2 = this.escTable.get(c);
                if (0 == c2) {
                    throw Er.create("e.char.stack.InvalidEscapeChar", c);
                }
                topBuf.append(c2);
                this.topC = this.stackC.popLast();
                return WnStackPushResult.ACCEPT;
            }
            // 激活逃逸
            if (this.cEscaper == c) {
                this.stackC.push(this.topC);
                this.topC = c;
                return WnStackPushResult.ACCEPT;
            }
            // 退栈符
            if (this.cPop == c || (this.cPop == 0 && this.topC == c)) {
                // 最底层栈了
                if (this.stackC.isEmpty()) {
                    this.status = Status.S9;
                    return WnStackPushResult.DONE;
                }
                // 弹出一层
                StringBuilder sb = this.stackBuf.pop();
                sb.append(this.topC).append(this.topBuf).append(c);
                this.topBuf = sb;
                this.topC = this.stackC.popLast();
                return WnStackPushResult.ACCEPT;
            }
            // 压栈符
            if (cPush == c) {
                this.stackC.push(this.topC);
                this.stackBuf.push(topBuf);
                this.topC = c;
                this.topBuf = new StringBuilder();
                return WnStackPushResult.ACCEPT;
            }
            // 其他字符默认计入缓冲
            this.topBuf.append(c);
            return WnStackPushResult.ACCEPT;
        }
        // 完成态
        if (Status.S9 == this.status) {
            return WnStackPushResult.DONE;
        }
        throw Wlang.impossible();
    }

    public String getContentAndReset() {
        // 小防守一下，S0 是 null 的
        if (null == this.topBuf) {
            return null;
        }
        String s = this.topBuf.toString();
        this.reset();
        return s;
    }

    public String process(String input) {
        if (null != input) {
            char[] cs = input.toCharArray();
            for (var i = 0; i < cs.length; i++) {
                char c = cs[i];
                WnStackPushResult re = this.push(c);
                // System.out.println(this.toString());
                if (WnStackPushResult.DONE == re) {
                    return this.getContentAndReset();
                }
            }
        }
        return null;
    }

    public List<String> processAsList(String input) {
        List<String> list = new LinkedList<>();
        if (null != input) {
            char[] cs = input.toCharArray();
            for (var i = 0; i < cs.length; i++) {
                char c = cs[i];
                WnStackPushResult re = this.push(c);
                if (WnStackPushResult.DONE == re) {
                    String s = this.getContentAndReset();
                    list.add(s);
                }
            }
        }
        return list;
    }

    public String[] processAsArray(String input) {
        List<String> list = this.processAsList(input);
        int n = list.size();
        if (n == 0) {
            return new String[0];
        }
        return list.toArray(new String[n]);
    }

    public char getcPush() {
        return cPush;
    }

    public void setcPush(char cPush) {
        this.cPush = cPush;
    }

    public char[] getCanPush() {
        return canPush;
    }

    public void setCanPush(char[] canPush) {
        this.canPush = canPush;
    }

    public char getcPop() {
        return cPop;
    }

    public void setcPop(char cPop) {
        this.cPop = cPop;
    }

    public char getcEscaper() {
        return cEscaper;
    }

    public void setcEscaper(char cEscaper) {
        this.cEscaper = cEscaper;
    }

    public EscapeTable getEscTable() {
        return escTable;
    }

    public void setEscTable(EscapeTable escTable) {
        this.escTable = escTable;
    }

    public LinkedCharArray getStackC() {
        return stackC;
    }

    public void setStackC(LinkedCharArray cstack) {
        this.stackC = cstack;
    }

}
