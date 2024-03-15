package org.nutz.mvc.impl;

import javax.servlet.ServletContext;

import org.nutz.ioc.IocMaking;
import org.nutz.ioc.ValueProxy;
import org.nutz.ioc.ValueProxyMaker;
import org.nutz.ioc.meta.IocValue;
import org.nutz.ioc.val.StaticValue;

import com.site0.walnut.util.Wlang;

public class ServletValueProxyMaker implements ValueProxyMaker {

    private ServletContext sc;

    public ServletValueProxyMaker(ServletContext sc) {
        this.sc = sc;
    }

    public String[] supportedTypes() {
        return Wlang.array("app");
    }

    public ValueProxy make(IocMaking ing, IocValue iv) {
        if (iv.getValue() == null)
            return null;
        String value = iv.getValue().toString();
        if ("app".equals(iv.getType())) {
            if ("$servlet".equalsIgnoreCase(value))
                return new StaticValue(sc);
            return new StaticValue(sc.getAttribute(value));
        }
        return null;
    }

}
