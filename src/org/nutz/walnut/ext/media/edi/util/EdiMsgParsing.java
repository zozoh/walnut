package org.nutz.walnut.ext.media.edi.util;

import java.util.LinkedList;

import org.nutz.lang.util.LinkedCharArray;
import org.nutz.walnut.ext.media.edi.bean.EdiMsgAdvice;
import org.nutz.walnut.ext.media.edi.bean.EdiMsgComponent;
import org.nutz.walnut.ext.media.edi.bean.EdiMsgElement;
import org.nutz.walnut.ext.media.edi.bean.EdiMsgSegment;

public class EdiMsgParsing {

    private EdiMsgAdvice A;

    private char[] cs;

    private int I;

    private LinkedList<EdiMsgComponent> components;

    private LinkedList<EdiMsgElement> elements;

    private LinkedCharArray stack;

    private char topC;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("I: %d => %c", this.I, this.topC));
        sb.append("\n<stack>: ").append(stack.toArray());
        dumpList(sb.append('\n'), "@components", this.components);
        dumpList(sb.append('\n'), "@elements", this.elements);
        sb.append("\n------------------------------");
        sb.append('\n').append("cs[]:");
        sb.append("\n------------------------------");
        sb.append('\n').append(cs);

        return sb.toString();
    }

    private void dumpList(StringBuilder sb, String name, LinkedList<?> list) {
        if (null == list || list.isEmpty()) {
            sb.append(name).append(": nil");
        } else {
            int N = list.size();
            sb.append(name).append(": ").append(N);
            for (int i = 0; i < N; i++) {
                Object com = list.get(i);
                sb.append(String.format("\n - %d/%d) %s", i, N, com.toString()));
            }
        }
    }

    public EdiMsgSegment nextSegment() {
        EdiMsgSegment seg = null;
        while (this.I < this.cs.length) {
            char c = this.cs[this.I++];
            // 逃逸字符
            if (topC == A.escaper) {
                stack.push(c);
                topC = 0; // 逃逸了一下，就不要将逃逸字符标记为栈顶字符了吧 ...
            }
            // 元素结束
            else if (c == A.element) {
                this.closeElement();
            }
            // 组件结束
            else if (c == A.component) {
                // 尝试清空字符缓冲或许它是另外一个元素
                if (!stack.isEmpty()) {
                    this.closeElement();
                }

                // 组装一个组件
                this.closeComponent();
            }
            // 行结束
            else if (c == A.segment) {
                // 尝试清空字符缓冲或许它是另外一个元素
                if (!stack.isEmpty()) {
                    this.closeElement();
                }

                // 组装一个组件
                if (!components.isEmpty()) {
                    this.closeComponent();
                }

                seg = new EdiMsgSegment(A, this.components);
                this.components = new LinkedList<>();
                this.I++;
                break;
            }
            // 普通字符
            else {
                stack.push(c);
                this.topC = c;
            }
        }
        return seg;
    }

    private void closeComponent() {
        EdiMsgComponent com = new EdiMsgComponent(A, this.elements);
        this.elements = new LinkedList<>();
        this.components.add(com);
    }

    private void closeElement() {
        String str = stack.popAll();
        elements.add(new EdiMsgElement(str));
        this.topC = 0;
    }

    public EdiMsgParsing(EdiMsgAdvice a, char[] cs) {
        this.A = a;
        this.cs = cs;
        this.I = 0;
        this.elements = new LinkedList<>();
        this.components = new LinkedList<>();
        this.stack = new LinkedCharArray();
        this.topC = 0;
    }
}
