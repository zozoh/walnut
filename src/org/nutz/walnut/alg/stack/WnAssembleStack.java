package org.nutz.walnut.alg.stack;

import java.lang.reflect.Array;
import java.util.LinkedList;
import java.util.List;

import org.nutz.walnut.util.Wlang;

/**
 * 一个通用堆栈，主要用来复杂结构的解析，它可以将一组对象汇集为一个树形结构。<br>
 * 在 markdown/xml/html 等结构文本的解析过程中，可以起很大帮助。
 * <p>
 * 
 * 类似 <code>WnCharStack</code> 不过它的实现更加抽象。对于当前元素 是否为压/退栈元素，是否是逃逸，这些决定都会交给子类来处理了。
 * 
 * <pre>
 * [ E ]  <-- 栈顶表示逃逸，下一个接收的元素将会逃逸并压入下一层缓冲，逃逸失败则抛错
 * [ T ]  <-- 每个压栈元素，将堆栈升高 --> 并对应一个缓冲列表 [..T..]
 * [ T ]  <-- 如果上层堆栈弹出，则将字符（包括压/退栈元素）都存入本层缓冲
 * [ T ]  <-- 最后一层缓冲弹出，并不包括压/退栈元素
 * </pre>
 * 
 * 本堆栈有下面几种状态
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
 *   +------ getListAndReset ---------+
 * </pre>
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public abstract class WnAssembleStack<T> {

    /**
     * 内部状态
     */
    enum Status {
        S0, S1, S9
    }

    private T topC;

    private List<T> topBuf;

    private LinkedList<T> stackC;

    private LinkedList<List<T>> stackBuf;

    private Status status;

    public WnAssembleStack() {
        this.status = Status.S0;
    }

    protected abstract boolean hasCandidatePusher();

    protected abstract boolean hasPusher();

    protected abstract T setPusherByCandidatePushers(T obj);

    protected abstract boolean isPusher(T obj);

    protected abstract boolean isPoper(T obj);

    protected abstract boolean isEscaper(T obj);

    protected abstract T unescape(T obj);

    protected abstract void resetPusher();

    protected void reset() {
        this.status = Status.S0;
        this.topC = null;
        this.topBuf = null;
        this.stackC = null;
        this.stackBuf = null;
        if (this.hasCandidatePusher()) {
            this.resetPusher();
        }
    }

    public WnStackPushResult push(T obj) {
        // 休眠态
        if (Status.S0 == this.status) {
            // 没有压栈付，从候选压栈符里选择
            if (!this.hasPusher() && this.hasCandidatePusher()) {
                this.setPusherByCandidatePushers(obj);
            }
            // 只有压栈付才能接受
            if (!this.isPusher(obj)) {
                return WnStackPushResult.REJECT;
            }
            // 接受压栈符
            this.topC = obj;
            this.topBuf = new LinkedList<>();
            this.stackC = new LinkedList<>();
            this.stackBuf = new LinkedList<>();
            this.status = Status.S1;
            return WnStackPushResult.ACCEPT;
        }
        // 激活态
        if (Status.S1 == this.status) {
            // 逃逸字符
            if (this.isEscaper(topC)) {
                T o2 = this.unescape(obj);
                if (null != o2)
                    topBuf.add(o2);
                this.topC = this.stackC.removeLast();
                return WnStackPushResult.ACCEPT;
            }
            // 激活逃逸
            if (this.isEscaper(obj)) {
                this.stackC.push(this.topC);
                this.topC = obj;
                return WnStackPushResult.ACCEPT;
            }
            // 退栈元素
            if (this.isPoper(obj)) {
                // 最底层栈了
                if (this.stackC.isEmpty()) {
                    this.status = Status.S9;
                    return WnStackPushResult.DONE;
                }
                // 弹出一层
                List<T> buf = this.stackBuf.pop();
                buf.add(this.topC);
                buf.addAll(buf);
                buf.add(obj);
                this.topBuf = buf;
                this.topC = this.stackC.removeLast();
                return WnStackPushResult.ACCEPT;
            }
            // 压栈元素
            if (this.isPusher(obj)) {
                this.stackC.push(this.topC);
                this.stackBuf.push(topBuf);
                this.topC = obj;
                this.topBuf = new LinkedList<>();
                return WnStackPushResult.ACCEPT;
            }
            // 其他元素默认计入缓冲
            this.topBuf.add(obj);
            return WnStackPushResult.ACCEPT;
        }
        // 完成态
        if (Status.S9 == this.status) {
            return WnStackPushResult.DONE;
        }
        throw Wlang.impossible();
    }

    public List<T> getListAndReset() {
        List<T> list = this.topBuf;
        this.reset();
        return list;
    }

    @SuppressWarnings("unchecked")
    public T[] getArrayAndReset(Class<T> classOfT) {
        List<T> list = this.getListAndReset();
        if (null != list) {
            T[] re = (T[]) Array.newInstance(classOfT, list.size());
            list.toArray(re);
            return re;
        }
        return null;
    }

    public T getTopC() {
        return topC;
    }

    public void setTopC(T topC) {
        this.topC = topC;
    }

    public List<T> getTopBuf() {
        return topBuf;
    }

    public void setTopBuf(List<T> topBuf) {
        this.topBuf = topBuf;
    }

    public LinkedList<T> getStackC() {
        return stackC;
    }

    public void setStackC(LinkedList<T> stackC) {
        this.stackC = stackC;
    }

    public LinkedList<List<T>> getStackBuf() {
        return stackBuf;
    }

    public void setStackBuf(LinkedList<List<T>> stackBuf) {
        this.stackBuf = stackBuf;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

}
