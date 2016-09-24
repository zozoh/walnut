package org.nutz.walnut.impl.hook;

import java.io.ByteArrayOutputStream;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.nutz.lang.stream.VoidInputStream;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.walnut.api.hook.WnHookContext;
import org.nutz.walnut.api.io.WnObj;

public class JsCommandHook extends AbstractWnHook {

    private static final Log log = Logs.get();

    private static ScriptEngineManager engineManager = new ScriptEngineManager();

    private static String engineName = "nashorn";

    protected String text;

    protected void _init(String text) {
        this.text = text;
    }

    @Override
    public String getType() {
        return "js";
    }

    @Override
    public void invoke(WnHookContext hc, WnObj o) {

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        ScriptEngine engine = engineManager.getEngineByName(engineName);

        // 分析输入变量
        engine.put("obj", o);
        engine.put("hc", hc);
        engine.put("stdin", new VoidInputStream());
        engine.put("stdout", out);
        engine.put("stderr", out);

        try {
            engine.eval(text);
        }
        catch (ScriptException e) {
            log.debug("js hook error", e);
        }
    }

}
