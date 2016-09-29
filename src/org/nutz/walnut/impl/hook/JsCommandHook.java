package org.nutz.walnut.impl.hook;

import java.io.ByteArrayOutputStream;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.nutz.lang.Stopwatch;
import org.nutz.lang.stream.VoidInputStream;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.walnut.api.hook.WnHookContext;
import org.nutz.walnut.api.io.WnObj;

public class JsCommandHook extends AbstractWnHook {

    private static final Log log = Logs.get();

    private static ScriptEngineManager engineManager = new ScriptEngineManager();

    private static String engineName = "nashorn";
    ScriptEngine engine = engineManager.getEngineByName(engineName);
    protected CompiledScript function;

    protected void _init(String text) {
        Compilable compilable = (Compilable) engine;
        try {
            function = compilable.compile(text);
        }
        catch (ScriptException e) {
           log.debug("bad js hook", e); 
        }
    }

    @Override
    public String getType() {
        return "js";
    }

    @Override
    public void invoke(WnHookContext hc, WnObj o) {
        if (function == null) {
            log.debug("bad js hook, skip");
            return;
        }
        Stopwatch sw = Stopwatch.begin();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Bindings bindings = engine.createBindings();
        bindings.put("obj", o);
        bindings.put("hc", hc);
        bindings.put("stdin", new VoidInputStream());
        bindings.put("stdout", out);
        bindings.put("stderr", out);
        try {
            function.eval(bindings);
        }
        catch (ScriptException e) {
            log.debug("js hook error", e);
        }
        sw.stop();
        log.debug("js hook time = " + sw);
    }
}
