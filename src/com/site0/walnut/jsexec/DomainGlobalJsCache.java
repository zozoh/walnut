package com.site0.walnut.jsexec;

import java.util.HashMap;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.util.JsExecContext;

public class DomainGlobalJsCache {

    /**
     * 键就是 `/home/{domain}/jsbin/_g.js`
     */
    private HashMap<String, Object> cache;

    public DomainGlobalJsCache() {
        cache = new HashMap<>();
    }

    public Object getGlobalJs(ScriptEngine engine, JsExecContext jsc) {
        String aph = jsc.path("~/jsbin/_g.js");
        Object re = cache.get(aph);
        if (null == re) {
            synchronized (this) {
                re = cache.get(aph);
                if (null == re) {
                    WnObj oJs = jsc.fetch(aph);
                    String jss;
                    if (null == oJs) {
                        jss = "(function(){})";
                    } else {
                        jss = jsc.readText(aph);
                    }
                    Bindings bindings = engine.createBindings();
                    try {
                        re = ((Compilable) engine).compile(jss).eval(bindings);
                        cache.put(aph, re);
                    }
                    catch (ScriptException e) {
                        throw Er.wrap(e);
                    }
                }
            }
        }
        return re;
    }

    public void clearGlobalJs(JsExecContext jsc) {
        String aph = jsc.path("~/jsbin/_g.js");
        Object re = cache.get(aph);
        if (null != re) {
            synchronized (this) {
                cache.remove(aph);
            }
        }
    }
}
