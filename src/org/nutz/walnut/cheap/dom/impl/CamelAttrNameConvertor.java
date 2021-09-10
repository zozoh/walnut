package org.nutz.walnut.cheap.dom.impl;

import org.nutz.walnut.cheap.dom.CheapAttrNameConvertor;
import org.nutz.walnut.util.Ws;

public class CamelAttrNameConvertor implements CheapAttrNameConvertor {

    @Override
    public String covert(String attrName) {
        return Ws.camelCase(attrName);
    }

}
