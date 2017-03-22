package org.nutz.walnut.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.impl.box.cmd.cmd_jsc_api;

public class JsExec {

    private ScriptEngineManager engineManager;

    protected Map<String, ScriptEngine> engines;

    private static JsExec _me = new JsExec();
    protected Map<String, Object> globalVars = new HashMap<>();

    private JsExec() {
        engineManager = new ScriptEngineManager();
        engines = new HashMap<>();
        try {
            globalVars.put("_", pre_eval(getEngine("nashorn"), "classpath:org/nutz/walnut/impl/box/cmd/jsc/lodash.min.js", "_"));
        }
        catch (ScriptException e) {
            throw new RuntimeException(e);
        }
    }

    public static JsExec me() {
        return _me;
    }

    public List<ScriptEngineFactory> getEngineFactories() {
        return engineManager.getEngineFactories();
    }

    public Object exec(WnSystem sys, String engineName, NutMap vars, String jsStr)
            throws Exception {
        // 首先，确保有引擎名称
        if (Strings.isBlank(engineName)) {
            throw Er.create("e.jsc.engin.blank");
        }

        // 试图从缓存中获取引擎
        ScriptEngine engine = getEngine(engineName);


        // 准备运行，首先设置上下文
        Bindings bindings = engine.createBindings();
        // 全局变量
        for (Entry<String, Object> en : globalVars.entrySet()) {
            bindings.put(en.getKey(), en.getValue());
        }
        // 变量
        if (vars != null) {
            for (Entry<String, Object> en : vars.entrySet()) {
                bindings.put(en.getKey(), en.getValue());
            }
        }
        bindings.put("wc", Wn.WC());
        bindings.put("sys", new cmd_jsc_api(sys));
        // bindings.put("args", params.vals);
        // bindings.put("log", log);
        bindings.put("walnut_js", "classpath:org/nutz/walnut/impl/box/cmd/jsc/jsc_walnut.js");
        bindings.put("lodash_js", "classpath:org/nutz/walnut/impl/box/cmd/jsc/lodash.core.min.js");
        // 默认加载的几个js
        // TODO 需要测试加载默认js的时间，是否影响性能等问题
        String jsPreload = "";
        jsPreload += "load(walnut_js);\n";
        // jsPreload += "load(lodash_js);\n";
        if (!Strings.isBlank(jsPreload)) {
            jsStr = jsPreload + jsStr;
        }

        // 执行
        Object obj = ((Compilable) engine).compile(jsStr).eval(bindings);

        // 返回结果
        return obj;
    }

    /**
     * 加载一个资源,然后返回某个变量
     * @param engine 脚本引擎
     * @param from 加载来源,通常是classpath:.........
     * @param name 需要返回的变量名
     * @return 你需要的那个对象...
     * @throws ScriptException 加载失败的时候抛出
     */
    public static Object pre_eval(ScriptEngine engine, String from, String name) throws ScriptException {
        String jsStr = String.format("load(\"%s\");function _eget(){return %s;};_eget();", from, name);
        return ((Compilable) engine).compile(jsStr).eval(engine.createBindings());
    }
    
    public ScriptEngine getEngine(String engineName) {
        ScriptEngine engine = engines.get(engineName);
        if (engine == null) {
            // 真正的生成引擎
            engine = engineManager.getEngineByName(engineName);
            if (engine == null) {
                throw Er.create("e.jsc.engin.noexists", engineName);
            }
            // 记录这个引擎
            engines.put(engineName, engine);
        }
        return engine;
    }
}
