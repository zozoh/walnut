package org.nutz.walnut.web;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.nutz.mvc.ModuleScanner;
import org.nutz.resource.Scans;

public class WnModuleScanner implements ModuleScanner {

    private String[] pkgs;

    @Override
    public Collection<Class<?>> scan() {
        Set<Class<?>> set = new HashSet<Class<?>>();
        if (null != pkgs) {
            for (String pkg : pkgs) {
                List<Class<?>> list = Scans.me().scanPackage(pkg);
                for (Class<?> klass : list) {
                    set.add(klass);
                }
            }
        }
        return set;
    }

}
