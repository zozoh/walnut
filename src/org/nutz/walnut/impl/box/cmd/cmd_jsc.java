package org.nutz.walnut.impl.box.cmd;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.nutz.json.Json;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;

/**
 * 实验性质执行js文件或直接读取
 * @author wendal(wendal1985@gmail.com)
 *
 */
public class cmd_jsc extends JvmExecutor {
    
    public static ScriptEngineManager engineManager =  new ScriptEngineManager();

    public void exec(WnSystem sys, String[] args) throws Exception {
        boolean debug = false;
        String jsStr = null;
        String jsFile = null;
        String engineName = "nashorn";
        NutMap vars = new NutMap();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            switch (arg) {
            case "-debug":
                debug = true;
                break;
            case "-f":
                jsFile = args[++i];
                break;
            case "-engine":
                engineName = args[++i];
                break;
            case "vars":
                vars = Json.fromJson(NutMap.class, args[++i]);
                break;
            default:
                jsStr = arg;
                break;
            }
        }
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
        engine.put("_walnut_call", this);
        
        NutMap walnut = new NutMap();
        walnut.put("vars", vars);
        walnut.put("sys", sys);
        engine.put("walnut", walnut);
        String js = jsStr.replaceAll("walnut\\.call\\(", "_walnut_call['call(WnSystem, String[])'](walnut.sys,");
        if (debug)
            sys.out.println("js=" + js + "\n");
        Object obj = engine.eval(js);
        if (debug) {
            sys.out.println("re=" + obj);
        }
    }

    public Object call(WnSystem sys, String...args) {
        sys.exec(Strings.join(" ", args));
        return "";
    }
}
