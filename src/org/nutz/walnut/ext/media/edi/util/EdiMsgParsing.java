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

    public EdiMsgSegment nextSegment() {
        EdiMsgSegment seg = null;
        while (this.I < this.cs.length) {
            char c = this.cs[this.I++];
            // 逃逸字符
            if (topC == A.escaper) {
                stack.push(c);
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

                seg = new EdiMsgSegment(this.components);
            }
        }
        return seg;
    }

    private void closeComponent() {
        EdiMsgComponent com = new EdiMsgComponent(this.elements);
        this.elements = new LinkedList<>();
        this.components.add(com);
    }

    private void closeElement() {
        String str = stack.popAll();
        elements.add(new EdiMsgElement(str));
    }

    public void reset() {
        this.elements.clear();
        this.components.clear();
        this.stack.clear();
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
