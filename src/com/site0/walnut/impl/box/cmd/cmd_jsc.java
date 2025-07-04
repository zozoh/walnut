package com.site0.walnut.impl.box.cmd;

import java.util.Arrays;

import javax.script.ScriptEngineFactory;

import org.nutz.json.Json;
import com.site0.walnut.util.Wlang;
import org.nutz.lang.Stopwatch;
import org.nutz.lang.Times;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.impl.box.JvmExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.jsexec.JsExec;
import com.site0.walnut.util.JvmJsExecContext;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.ZParams;

/**
 * 执行js文件
 * 
 * @author wendal(wendal1985@gmail.com)
 *
 */
public class cmd_jsc extends JvmExecutor {

    private static final Log log = Wlog.getCMD();

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        if (log.isTraceEnabled())
            log.trace("init exec");

        // 得到运行器
        JsExec JE = JsExec.me();

        // 分析参数
        ZParams params = ZParams.parse(args, "^(debug|clear|merge)$");
        boolean debug = params.is("debug");

        // 查看所有可用引擎
        if (params.has("lsengine")) {
            sys.out.printlnf("*%s", JsExec.dft_engine_nm);
            for (ScriptEngineFactory ef : JE.getEngineFactories()) {
                sys.out.printlnf("%s(%s) %s(%s) : [%s]",
                                 ef.getEngineName(),
                                 ef.getEngineVersion(),
                                 ef.getLanguageName(),
                                 ef.getLanguageVersion(),
                                 Wlang.concat(",", ef.getNames()));
            }
            return;
        }

        // 清除缓存
        if (params.is("clear")) {
            JE.clearDomainGlobalJs(new JvmJsExecContext(sys));
            return;
        }

        // 得到引擎
        String engineName = params.get("engine", JsExec.dft_engine_nm);

        // 分析输入变量
        NutMap vars = new NutMap();

        // 用户定义了变量
        if (params.has("vars")) {
            String vstr = params.get("vars");
            // 从管道读取
            if ("~pipe".equals(vstr) || "true".equals(vstr)) {
                vstr = sys.in.readAll();
                vars = Wlang.map(vstr);
            }
            // 那么就认为是参数
            else {
                vars = Wlang.map(vstr);
            }
            // 融合模式，还是要从标准输入读取一遍
            if (params.is("merge")) {
                String input = sys.in.readAll();
                NutMap inputMap = Wlang.map(input);
                inputMap.putAll(vars);
                vars = inputMap;

            }
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
                if (o.name().endsWith("groovy"))
                    engineName = "groovy";
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

        // 添加固定上下文变量
        vars.put("args", params.vals);
        vars.put("_args_str", Json.toJson(params.vals));
        vars.put("log", log);
        vars.put("zparams", params);

        // 默认加载的几个js
        // TODO 需要测试加载默认js的时间，是否影响性能等问题
        Stopwatch stopwatch = null;
        if (debug) {
            stopwatch = Stopwatch.begin();
        }
        // 执行
        if (debug)
            sys.out.println("js=\n" + jsStr + "\n");
        Object obj = JE.exec(new JvmJsExecContext(sys), engineName, vars, jsStr);
        if (debug) {
            stopwatch.stop();
            sys.out.printlnf("runTime=%dms(%s)",
                             stopwatch.getDuration(),
                             Times.fromMillis(stopwatch.getDuration()));
            sys.out.println("re=" + obj);
        }
    }
}
