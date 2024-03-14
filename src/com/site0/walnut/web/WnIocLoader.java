package com.site0.walnut.web;

import org.nutz.ioc.IocLoader;
import org.nutz.ioc.IocLoading;
import org.nutz.ioc.ObjectLoadException;
import org.nutz.ioc.loader.annotation.AnnotationIocLoader;
import org.nutz.ioc.meta.IocObject;

public class WnIocLoader implements IocLoader {

    private AnnotationIocLoader __loader;

    public WnIocLoader(WnConfig conf) {
        __loader = new AnnotationIocLoader(conf.getWebIocPkgs());
    }

    public String[] getName() {
        return __loader.getName();
    }

    public boolean has(String name) {
        return __loader.has(name);
    }

    public IocObject load(IocLoading loading, String name) throws ObjectLoadException {
        return __loader.load(loading, name);
    }

}
