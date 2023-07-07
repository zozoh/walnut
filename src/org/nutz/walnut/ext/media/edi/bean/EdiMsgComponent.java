package org.nutz.walnut.ext.media.edi.bean;

import java.util.List;

public class EdiMsgComponent extends EdiMsgItem {

    private List<EdiMsgElement> elements;

    public EdiMsgComponent(EdiMsgAdvice advice) {
        super(advice);
    }

    public EdiMsgComponent(EdiMsgAdvice advice, List<EdiMsgElement> elements) {
        this(advice);
        this.elements = elements;
    }

    @Override
    public void joinString(StringBuilder sb) {
        if (null != elements) {
            int i = 0;
            for (EdiMsgElement ele : this.elements) {
                if ((i++) > 0) {
                    sb.append(advice.element);
                }
                ele.joinString(sb);
            }
        }
    }

    public EdiMsgElementType getFirstElementType() {
        if (null != elements && elements.size() > 0) {
            return elements.get(0).getType();
        }
        return null;
    }

    public boolean isFirstElement(String name) {
        if (elements.size() > 0) {
            EdiMsgElement ele0 = elements.get(0);
            return ele0.isTag(name);
        }
        return false;
    }

    public List<EdiMsgElement> getElements() {
        return elements;
    }

    public void setElements(List<EdiMsgElement> elements) {
        this.elements = elements;
    }

}
