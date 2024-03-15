package org.nutz.mvc.impl.contextCollector;

import com.site0.walnut.util.Wlang;
import org.nutz.lang.util.Context;
import org.nutz.mvc.Mvcs;
import org.nutz.mvc.ViewContextCollector;
import org.nutz.mvc.config.AtMap;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 路径入口收集
 */
public class AtCollector implements ViewContextCollector {
    @Override
    public Context collect(HttpServletRequest req, Object obj) {
        Map<String, String> u = new HashMap<String, String>();
        AtMap at = Mvcs.getAtMap();
        if (at != null) {
            for (Object o : at.keys()) {
                String key = (String) o;
                u.put(key, at.get(key));
            }
            return Wlang.context("u", u);
        }
        return null;
    }
}
