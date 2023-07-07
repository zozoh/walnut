package org.nutz.walnut.ext.media.edi.bean;

import java.util.List;

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

    public List<EdiMsgComponent> getComponents() {
        return components;
    }

    public void setComponents(List<EdiMsgComponent> components) {
        this.components = components;
    }

}
