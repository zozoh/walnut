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
        while (this.I < this.cs.length>) {
            char c = this.cs[this.I++];
            // 逃逸字符
            if (topC == A.escaper) {
                stack.push(c);
            }
            // 元素结束
            else if (c == A.element) {
                String str = stack.popAll();
                elements.add(new EdiMsgElement(str));
            }
            // 组件结束
            else if(c == A.component) {
                this.tryCloseElement();
                // 尝试清空字符缓冲或许它是另外一个元素
                if(!stack.isEmpty()) {
                    String str = stack.popAll();
                    elements.add(new EdiMsgElement(str));
                }
                // 组装一个组件
                EdiMsgComponent com = new EdiMsgComponent(this.elements);
                this.elements = new LinkedList<>();
                this.components.add(com);
            }
            // 行结束
            else if(c == A.segment) {
                
            }
        }
    }
    
    private void tryCloseElement() {
        if(!stack.isEmpty()) {
            String str = stack.popAll();
            elements.add(new EdiMsgElement(str));
        }
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
