package org.nutz.walnut.impl.box.cmd;

import java.io.InputStream;
import java.io.OutputStream;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.usr.WnSession;
import org.nutz.walnut.impl.box.JvmBoxInput;
import org.nutz.walnut.impl.box.JvmBoxOutput;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

/**
 * 给 cmd_jsc 用的 sys
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class cmd_jsc_api {

    private WnSystem sys;

    public JvmBoxOutput out;

    public JvmBoxOutput err;

    public WnSession se;

    public JvmBoxInput in;

    public cmd_jsc_api(WnSystem sys) {
        this.sys = sys;
        this.in = sys.in;
        this.out = sys.out;
        this.err = sys.err;
        this.se = sys.se;
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
    public WnObj fetch(String ph) {
        String aph = Wn.normalizeFullPath(ph, sys);
        return sys.io.fetch(null, aph);
    }

    /**
     * 便捷根据路径查找对象，如果不存在，抛出哦
     * 
     * @param ph
     *            路径，支持环境变量和 ~ 符号
     * @return 对象
     */
    public WnObj check(String ph) {
        String aph = Wn.normalizeFullPath(ph, sys);
        return sys.io.check(null, aph);
    }

    // 提供低阶 IO 接口
    public WnIo io() {
        return sys.io;
    }

    // 这些方法直接委托了 WnSystem
    public void exec(String cmdText) {
        sys.exec(cmdText);
    }

    public void execf(String fmt, Object... args) {
        sys.execf(fmt, args);
    }

    public void exec(String cmdText, OutputStream stdOut, OutputStream stdErr, InputStream stdIn) {
        sys.exec(cmdText, stdOut, stdErr, stdIn);
    }

    public void exec(String cmdText,
                     StringBuilder stdOut,
                     StringBuilder stdErr,
                     CharSequence stdIn) {
        sys.exec(cmdText, stdOut, stdErr, stdIn);
    }

    public String exec2(String cmdText) {
        return sys.exec2(cmdText);
    }

    public String exec2f(String fmt, Object... args) {
        return sys.exec2f(fmt, args);
    }

    public String exec2(String cmdText, CharSequence input) {
        return sys.exec2(cmdText, input);
    }

    public String json(Object obj) {
        return Json.toJson(obj, JsonFormat.compact().setQuoteName(true).setIgnoreNull(false));
    }

    public Object exec2map(String cmdText) {
        try {
            return Json.fromJson(NutMap.class, exec2(cmdText));
        }
        catch (Exception e) {
            return null;
        }
    }

    public Object exec2list(String cmdText) {
        try {
            return Json.fromJsonAsList(NutMap.class, exec2(cmdText));
        }
        catch (Exception e) {
            return null;
        }
    }

    public String path(String path) {
        return Wn.normalizeFullPath(path, sys);
    }
}
