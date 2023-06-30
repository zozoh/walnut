package org.nutz.walnut.ext.media.edi.bean;

import java.util.List;

/**
 * 一个EDI 报文行
 * 
 * @author zozoh
 *
 */
public class EdiMsgSegment {

    private List<EdiMsgComponent> components;

    public EdiMsgSegment() {}

    public EdiMsgSegment(List<EdiMsgComponent> components) {
        this.components = components;
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
