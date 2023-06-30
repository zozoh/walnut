package org.nutz.walnut.ext.media.edi.bean;

import java.util.List;

public class EdiMsgComponent {

    private List<EdiMsgElement> elements;

    public EdiMsgComponent() {}

    public EdiMsgComponent(List<EdiMsgElement> elements) {
        this.elements = elements;
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
