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
    
    public EdiMsgSegment valueOf(String input, EdiMsgAdvice setup) {
        return this;
    }

    public List<EdiMsgComponent> getComponents() {
        return components;
    }

    public void setComponents(List<EdiMsgComponent> components) {
        this.components = components;
    }

}
