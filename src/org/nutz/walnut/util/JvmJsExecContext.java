package org.nutz.walnut.util;

import java.io.InputStream;
import java.io.OutputStream;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.usr.WnSession;
import org.nutz.walnut.impl.box.JvmBoxInput;
import org.nutz.walnut.impl.box.JvmBoxOutput;
import org.nutz.walnut.impl.box.WnSystem;

/**
 * 给 JsExec 用的运行时接口
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class JvmJsExecContext implements JsExecContext {

    private WnSystem sys;

    public JvmBoxOutput out;

    public JvmBoxOutput err;

    public WnSession se;

    public JvmBoxInput in;

    public JvmJsExecContext(WnSystem sys, JvmBoxOutput out) {
        this.sys = sys;
        this.in = sys.in;
        this.out = out;
        this.err = sys.err;
        this.se = sys.se;
    }

    public JvmJsExecContext(WnSystem sys, StringBuilder sb) {
        this(sys, new JvmBoxOutput(Lang.ops(sb)));
    }

    // 提供一下高级帮助方法

    /**
     * 
     * 便捷根据路径查找对象，如果不存在，返回 null
     * 
     * @param ph
     *            路径，支持环境变量和 ~ 符号
     * @return 对象
     */
    @Override
    public WnObj fetch(String ph) {
        String aph = Wn.normalizeFullPath(ph, se);
        return io().fetch(null, aph);
    }

    /**
     * 便捷根据路径查找对象，如果不存在，抛出哦
     * 
     * @param ph
     *            路径，支持环境变量和 ~ 符号
     * @return 对象
     */
    @Override
    public WnObj check(String ph) {
        String aph = Wn.normalizeFullPath(ph, se);
        return sys.io.check(null, aph);
    }

    // 提供低阶 IO 接口
    @Override
    public WnIo io() {
        return sys.io;
    }

    // 这些方法直接委托了 WnSystem
    @Override
    public void exec(String cmdText) {
        sys.exec(cmdText);
    }

    @Override
    public void execf(String fmt, Object... args) {
        sys.execf(fmt, args);
    }

    @Override
    public void exec(String cmdText, OutputStream stdOut, OutputStream stdErr, InputStream stdIn) {
        sys.exec(cmdText, stdOut, stdErr, stdIn);
    }

    @Override
    public void exec(String cmdText,
                     StringBuilder stdOut,
                     StringBuilder stdErr,
                     CharSequence stdIn) {
        sys.exec(cmdText, stdOut, stdErr, stdIn);
    }

    @Override
    public String exec2(String cmdText) {
        return sys.exec2(cmdText);
    }

    @Override
    public String exec2f(String fmt, Object... args) {
        return sys.exec2f(fmt, args);
    }

    @Override
    public String exec2(String cmdText, CharSequence input) {
        return sys.exec2(cmdText, input);
    }

    @Override
    public String json(Object obj) {
        return Json.toJson(obj, JsonFormat.compact().setQuoteName(true).setIgnoreNull(false));
    }

    @Override
    public Object exec2map(String cmdText) {
        try {
            return Json.fromJson(NutMap.class, exec2(cmdText));
        }
        catch (Exception e) {
            return null;
        }
    }

    @Override
    public Object exec2list(String cmdText) {
        try {
            return Json.fromJsonAsList(NutMap.class, exec2(cmdText));
        }
        catch (Exception e) {
            return null;
        }
    }

    @Override
    public String path(String path) {
        return Wn.normalizeFullPath(path, se);
    }
}
