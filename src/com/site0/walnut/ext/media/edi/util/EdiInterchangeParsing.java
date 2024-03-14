package com.site0.walnut.ext.media.edi.util;

import java.util.LinkedList;

import org.nutz.lang.util.LinkedCharArray;
import com.site0.walnut.ext.media.edi.bean.EdiAdvice;
import com.site0.walnut.ext.media.edi.bean.EdiComponent;
import com.site0.walnut.ext.media.edi.bean.EdiElement;
import com.site0.walnut.ext.media.edi.bean.EdiSegment;

public class EdiInterchangeParsing {

    private EdiAdvice A;

    private char[] cs;

    private int I;

    private LinkedList<EdiComponent> components;

    private LinkedList<EdiElement> elements;

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

    public EdiSegment nextSegment() {
        EdiSegment seg = null;
        int N = this.cs.length;
        while (this.I < N) {
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

                seg = new EdiSegment(A, this.components);
                this.components = new LinkedList<>();
                // 行结束后，向后寻找第一个不是换行符的位置，这样可以兼容无换行符，以及有换行符的两种报文形式
                for (; I < N; I++) {
                    char next = cs[I];
                    if ('\r' == next || '\n' == next) {
                        continue;
                    }
                    break;
                }
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
        EdiComponent com = new EdiComponent(A, this.elements);
        this.elements = new LinkedList<>();
        this.components.add(com);
    }

    private void closeElement() {
        String str = stack.popAll();
        elements.add(new EdiElement(str));
        this.topC = 0;
    }

    public EdiInterchangeParsing(EdiAdvice a, char[] cs) {
        this.A = a;
        this.cs = cs;
        this.I = 0;
        this.elements = new LinkedList<>();
        this.components = new LinkedList<>();
        this.stack = new LinkedCharArray();
        this.topC = 0;
    }
}
