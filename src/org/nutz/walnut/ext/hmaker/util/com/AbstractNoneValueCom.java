package org.nutz.walnut.ext.hmaker.util.com;

import org.jsoup.nodes.Element;

public abstract class AbstractNoneValueCom extends AbstractSimpleCom {

    @Override
    public Object getValue(Element eleCom) {
        return null;
    }

}
