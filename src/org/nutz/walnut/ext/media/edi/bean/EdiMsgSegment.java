package org.nutz.walnut.ext.media.edi.bean;

import java.util.List;

import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.util.Wlang;

/**
 * 一个 EDI 报文行
 * 
 * @author zozoh
 *
 */
public class EdiMsgSegment extends EdiMsgItem {

    private List<EdiMsgComponent> components;

    public EdiMsgSegment(EdiMsgAdvice advice) {
        super(advice);
    }

    public EdiMsgSegment(EdiMsgAdvice advice, List<EdiMsgComponent> components) {
        this(advice);
        this.components = components;
    }

    @Override
    public void joinString(StringBuilder sb) {
        if (null != this.components) {
            int i = 0;
            for (EdiMsgComponent com : this.components) {
                if ((i++) > 0) {
                    sb.append(advice.component);
                }
                com.joinString(sb);
            }
        }
    }

    public boolean isTag(String name) {
        if (components.size() > 0) {
            EdiMsgComponent com = components.get(0);
            return com.isFirstElement(name);
        }
        return false;
    }

    public void setComponent(int index, String str) {
        EdiMsgElement ele = new EdiMsgElement(str);
        EdiMsgComponent com = new EdiMsgComponent(advice, Wlang.list(ele));
        this.setComponent(index, com);
    }

    public void setComponent(int index, Integer n) {
        EdiMsgElement ele = new EdiMsgElement(EdiMsgElementType.NUMBER, n);
        EdiMsgComponent com = new EdiMsgComponent(advice, Wlang.list(ele));
        this.setComponent(index, com);
    }

    public void setComponent(int index, EdiMsgComponent com) {
        if (null == this.components) {
            throw Er.create("e.edi.segment.componentsWithoutInit");
        }
        if (index < 0 || index >= components.size()) {
            throw Er.create("e.edi.segment.componentOutOfBound", index);
        }
        this.components.set(index, com);
    }

    public List<EdiMsgComponent> getComponents() {
        return components;
    }

    public void setComponents(List<EdiMsgComponent> components) {
        this.components = components;
    }

}
