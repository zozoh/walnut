package org.nutz.walnut.impl.box.cmd;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

import org.nutz.lang.Lang;
import org.nutz.lang.Stopwatch;
import org.nutz.lang.Strings;
import org.nutz.lang.Times;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;
import org.nutz.walnut.web.WnConfig;

/**
 * 执行js文件
 * 
 * @author wendal(wendal1985@gmail.com)
 *
 */
public class cmd_jsc extends JvmExecutor {

    private static ScriptEngineManager engineManager = new ScriptEngineManager();

    private static String dft_engine_nm = null;

    protected Map<String, ScriptEngine> engines = new HashMap<>();

    private static final Log log = Logs.get();

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        log.info("init exec");
        // 分析参数
        ZParams params = ZParams.parse(args, "^debug$");
        boolean debug = params.is("debug");

        // 首先，确保得到默认的引擎名称
        if (null == dft_engine_nm) {
            synchronized (cmd_jsc.class) {
                if (null == dft_engine_nm) {
                    WnConfig conf = ioc.get(WnConfig.class, "conf");
                    dft_engine_nm = conf.get("jsc-dft-engine", "nashorn");
                }
            }
        }

        // 查看所有可用引擎
        if (params.has("lsengine")) {
            sys.out.printlnf("*%s", dft_engine_nm);
            for (ScriptEngineFactory ef : engineManager.getEngineFactories()) {
                sys.out.printlnf("%s(%s) %s(%s) : [%s]",
                                 ef.getEngineName(),
                                 ef.getEngineVersion(),
                                 ef.getLanguageName(),
                                 ef.getLanguageVersion(),
                                 Lang.concat(",", ef.getNames()));
            }
            return;
        }

        // 得到引擎
        String engineName = params.get("engine", dft_engine_nm);

        ScriptEngine engine = engines.get(engineName);
        if (engine == null) {
            engine = engineManager.getEngineByName(engineName);
            if (engine == null) {
                sys.err.println("no such engine name=" + engineName);
                return;
            } else {
                engines.put(engineName, engine);
            }
        }

        // 分析输入变量
        NutMap vars = new NutMap();

        // 用户定义了变量
        if (params.has("vars")) {
            String vstr = params.get("vars");
            // 从管道读取
            if (("~pipe".equals(vstr) || "true".equals(vstr)) && sys.pipeId > 0) {
                vstr = sys.in.readAll();
            }
            vars = Lang.map(vstr);
        }
        // 预防null
        if (vars == null) {
            vars = new NutMap();
        }

        String jsStr;
        // 用文件?
        if (params.has("f")) {
            String str = params.get("f");
            WnObj o = Wn.checkObj(sys, str);
            jsStr = sys.io.readText(o);
        } else if (params.has("cmd")) {
            jsStr = params.get("cmd");
        }
        // 用输入的参数吧
        else if (params.vals.length > 0) {
            String str = params.vals[0];

            // 看看这个字符串本身是不是一个路径或者 ID ...
            WnObj o = null;
            if (!str.contains(";")) {
                o = Wn.getObj(sys, str);
            }
            if (null != o) {
                jsStr = sys.io.readText(o);
            } else {
                jsStr = str;
            }
            if (params.vals.length > 1)
                params.vals = Arrays.copyOfRange(params.vals, 1, params.vals.length);
            else
                params.vals = new String[0];
        }
        // 看看是不是要从管道里读取
        else if (sys.pipeId > 0) {
            jsStr = sys.in.readAll();
        }
        // 总得输入点啥吧
        else {
            throw Er.create("e.cmd.jsc.noinput");
        }

        // 准备运行，首先设置上下文
        Bindings bindings = engine.createBindings();
        for (Entry<String, Object> en : vars.entrySet()) {
            bindings.put(en.getKey(), en.getValue());
        }
        bindings.put("wc", Wn.WC());
        bindings.put("sys", new cmd_jsc_api(sys));
        bindings.put("args", params.vals);
        bindings.put("log", log);
        bindings.put("walnut_js", "classpath:org/nutz/walnut/impl/box/cmd/jsc/jsc_walnut.js");
        bindings.put("lodash_js", "classpath:org/nutz/walnut/impl/box/cmd/jsc/lodash.core.min.js");

        // 默认加载的几个js
        // TODO 需要测试加载默认js的时间，是否影响性能等问题
        Stopwatch stopwatch = null;
        if (debug) {
            stopwatch = Stopwatch.begin();
        }
        String jsPreload = "";
        jsPreload += "load(walnut_js);\n";
        // jsPreload += "load(lodash_js);\n";
        if (!Strings.isBlank(jsPreload)) {
            jsStr = jsPreload + jsStr;
        }
        // 执行
        if (debug)
            sys.out.println("js=\n" + jsStr + "\n");
        Object obj = ((Compilable) engine).compile(jsStr).eval(bindings);
        if (debug) {
            stopwatch.stop();
            sys.out.printlnf("runTime=%dms(%s)",
                             stopwatch.getDuration(),
                             Times.fromMillis(stopwatch.getDuration()));
            sys.out.println("re=" + obj);
        }
    }
}
