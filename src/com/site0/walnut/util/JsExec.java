package com.site0.walnut.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.nashorn.NashornObjectWrapper;

public class JsExec {

    private static final Log log = Wlog.getCMD();

    public static String dft_engine_nm = "nashorn";

    private ScriptEngineManager engineManager;

    protected Map<String, ScriptEngine> engines;

    private static JsExec _me = new JsExec();
    protected Map<String, Object> globalVars = new HashMap<>();

    private JsExec() {
        engineManager = new ScriptEngineManager();
        engines = new HashMap<>();
        try {
            globalVars.put("_",
                           pre_eval(getEngine("nashorn"),
                                    "classpath:com/site0/walnut/impl/box/cmd/jsc/lodash.min.js",
                                    "_"));
            globalVars.put("__wn",
                           pre_eval(getEngine("nashorn"),
                                    "classpath:com/site0/walnut/impl/box/cmd/jsc/jsc_walnut.js",
                                    "$wn"));
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

    public Object exec(JsExecContext jsc, String engineName, NutMap vars, String jsStr)
            throws Exception {
        // 首先，确保有引擎名称
        if (Strings.isBlank(engineName)) {
            throw Er.create("e.jsc.engin.blank");
        }

        // 试图从缓存中获取引擎
        ScriptEngine engine = getEngine(engineName);
        boolean groovyMode = "groovy".equals(engineName);

        // 准备运行，首先设置上下文
        Bindings bindings = engine.createBindings();
        // 全局变量
        if (!groovyMode) {
            for (Entry<String, Object> en : globalVars.entrySet()) {
                String k = en.getKey();
                Object v = en.getValue();
                bindings.put(k, v);
            }
        }
        // 变量
        if (vars != null) {
            NashornObjectWrapper objWrapper = null;
            if ("nashorn".equals(engineName)) {
                objWrapper = new NashornObjectWrapper(engine);
            }

            for (Entry<String, Object> en : vars.entrySet()) {
                String k = en.getKey();
                Object v = en.getValue();
                // 对于 nashorns 的特殊包裹
                if (null != objWrapper
                    && null != v
                    && ((v instanceof Map)
                        || (v instanceof Collection)
                        || v.getClass().isArray())) {
                    v = objWrapper.deepConvert(v);
                }
                bindings.put(k, v);
            }
        }
        bindings.put("wc", Wn.WC());
        bindings.put("sys", jsc);
        bindings.put("_TRACE_ID", R.UU32());
        if (!bindings.containsKey("log")) {
            bindings.put("log", log);
        }

        // 生成动态访问的 $wn
        if (!groovyMode)
            jsStr = "var $wn = _.extend({}, __wn); $wn.sys = sys; $wn.toJsonStr = function(jsobj) { if (typeof jsobj == 'string') {return jsobj;} return JSON.stringify(jsobj, null, '');}\n"
                    + jsStr;

        // 执行
        Object obj = ((Compilable) engine).compile(jsStr).eval(bindings);

        // 返回结果
        return obj;
    }

    /**
     * 加载一个资源,然后返回某个变量
     * 
     * @param engine
     *            脚本引擎
     * @param from
     *            加载来源,通常是classpath:.........
     * @param name
     *            需要返回的变量名
     * @return 你需要的那个对象...
     * @throws ScriptException
     *             加载失败的时候抛出
     */
    public static Object pre_eval(ScriptEngine engine, String from, String name)
            throws ScriptException {
        String jsStr = String.format("load(\"%s\");function _eget(){return %s;};_eget();",
                                     from,
                                     name);
        return ((Compilable) engine).compile(jsStr).eval(engine.createBindings());
    }

    public ScriptEngine getEngine(String engineName) {
        ScriptEngine engine = engines.get(engineName);
        if (engine == null) {
            // 真正的生成引擎
            if ("nashorn".equals(engineName)) {
                try {
                    engine = getNashornEngineSafe((name) -> {
                        if (name.equals("java.lang.String"))
                            return true;
                        // 不允许加载java.lang下的类,尤其是Runtime
                        if (name.startsWith("java.lang"))
                            return false;
                        // 不允许使用io库
                        if (name.startsWith("java.io"))
                            return false;
                        return true;
                    });
                }
                catch (Exception e) {}
            }
            if (engine == null)
                engine = engineManager.getEngineByName(engineName);
            if (engine == null) {
                throw Er.create("e.jsc.engin.noexists", engineName);
            }
            // 记录这个引擎
            engines.put(engineName, engine);
        }
        return engine;
    }

    @SuppressWarnings("unchecked")
    public ScriptEngine getNashornEngineSafe(Function<String, Boolean> func) throws Exception {
        for (ScriptEngineFactory factory : getEngineFactories()) {
            if (factory.getClass()
                       .getName()
                       .equals("jdk.nashorn.api.scripting.NashornScriptEngineFactory")) {
                Class<Object> klass = (Class<Object>) Class.forName("jdk.nashorn.api.scripting.ClassFilter");
                Object proxy = Proxy.newProxyInstance(getClass().getClassLoader(),
                                                      new Class<?>[]{klass},
                                                      new InvocationHandler() {
                                                          @Override
                                                          public Object invoke(Object proxy,
                                                                               Method method,
                                                                               Object[] args)
                                                                  throws Throwable {
                                                              return func == null
                                                                     || func.apply((String) args[0]);
                                                          }
                                                      });
                return (ScriptEngine) factory.getClass()
                                             .getMethod("getScriptEngine", klass)
                                             .invoke(factory, proxy);
            }
        }
        return null;
    }

    public void addGlobalVar(String key, Object value) {
        globalVars.put(key, value);
    }

    public void removeGlobalVar(String key) {
        globalVars.remove(key);
    }
}
