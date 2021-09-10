package org.nutz.walnut.cheap.dom.impl;

import org.nutz.walnut.cheap.dom.CheapAttrNameConvertor;
import org.nutz.walnut.util.Ws;

public class SnakeAttrNameConvertor implements CheapAttrNameConvertor {

    @Override
    public String covert(String attrName) {
        return Ws.snakeCase(attrName);
    }

}
