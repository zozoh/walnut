package org.nutz.walnut.impl.box.cmd;

import java.util.Map.Entry;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.nutz.json.Json;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

/**
 * 实验性质执行js文件或直接读取
 * @author wendal(wendal1985@gmail.com)
 *
 */
public class cmd_jsc extends JvmExecutor {
    
    public static ScriptEngineManager engineManager =  new ScriptEngineManager();

    public void exec(WnSystem sys, String[] args) throws Exception {
        ZParams params = ZParams.parse(args, "^debug$");
        boolean debug = params.is("debug");
        String jsStr = params.vals.length > 0 ? params.vals[0] : null;
        String jsFile = params.get("f");
        String engineName = params.get("engine", "nashorn");
        NutMap vars = Json.fromJson(NutMap.class, params.get("vars", "{}"));
        if (jsFile != null) {
            WnObj tmp = sys.io.check(null, jsFile);
            jsStr = sys.io.readText(tmp);
        }
        if (jsStr == null) {
            sys.err.println("need string or file");
            return;
        }
        ScriptEngine engine =  engineManager.getEngineByName(engineName);
        if (engine == null) {
            sys.err.println("no such engine name=" + engineName);
            return;
        }
        
        // 过渡一下
        for (Entry<String, Object> en : vars.entrySet()) {
            engine.put(en.getKey(), en.getValue());
        }
        engine.put("sys", sys);
        if (debug)
            sys.out.println("js=" + jsStr + "\n");
        Object obj = engine.eval(jsStr);
        if (debug) {
            sys.out.println("re=" + obj);
        }
    }
}
