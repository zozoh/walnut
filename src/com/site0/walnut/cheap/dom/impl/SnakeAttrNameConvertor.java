package com.site0.walnut.cheap.dom.impl;

import com.site0.walnut.cheap.dom.CheapAttrNameConvertor;
import com.site0.walnut.util.Ws;

public class SnakeAttrNameConvertor implements CheapAttrNameConvertor {

    @Override
    public String covert(String attrName) {
        return Ws.snakeCase(attrName);
    }

}
