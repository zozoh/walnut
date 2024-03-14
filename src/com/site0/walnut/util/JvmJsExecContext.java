package com.site0.walnut.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Encoding;
import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.lang.random.R;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import com.site0.walnut.api.WnAuthExecutable;
import com.site0.walnut.api.WnOutputable;
import com.site0.walnut.api.auth.WnAuthSession;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.core.WnIoHandle;
import com.site0.walnut.core.WnIoHandleMutexException;
import com.site0.walnut.impl.box.JvmBoxInput;
import com.site0.walnut.impl.box.JvmBoxOutput;
import com.site0.walnut.impl.box.WnSystem;

/**
 * 给 JsExec 用的运行时接口
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class JvmJsExecContext implements JsExecContext {

    private WnIo io;

    private WnAuthExecutable sys;

    public WnOutputable out;

    public WnOutputable err;

    public WnAuthSession se;

    public JvmBoxInput in;

    private static final Log _log = Wlog.getCMD();

    public JvmJsExecContext(WnSystem sys) {
        this(sys, sys.out, sys.err);
    }

    public JvmJsExecContext(WnSystem sys, WnOutputable out) {
        this(sys, out, out);
    }

    public JvmJsExecContext(WnSystem sys, WnOutputable out, WnOutputable err) {
        this(sys.io, sys.session, sys, sys.in, out, err);
    }

    public JvmJsExecContext(WnSystem sys, StringBuilder sb) {
        this(sys, new JvmBoxOutput(Lang.ops(sb)));
    }

    public JvmJsExecContext(WnSystem sys, StringBuilder sbOut, StringBuilder sbErr) {
        this(sys, new JvmBoxOutput(Lang.ops(sbOut)), new JvmBoxOutput(Lang.ops(sbErr)));
    }

    public JvmJsExecContext(WnIo io,
                            WnAuthSession session,
                            WnAuthExecutable runner,
                            JvmBoxInput in,
                            WnOutputable out,
                            WnOutputable err) {
        this.io = io;
        this.se = session;
        this.sys = runner;
        this.in = in;
        this.out = WnJsOutput.WRAP(out);
        this.err = WnJsOutput.WRAP(err);
    }

    public JvmJsExecContext(WnIo io,
                            WnAuthSession session,
                            WnAuthExecutable runner,
                            String input,
                            StringBuilder sbOut,
                            StringBuilder sbErr) {
        this(io,
             session,
             runner,
             new JvmBoxInput(Lang.ins(input)),
             new JvmBoxOutput(Lang.ops(sbOut)),
             new JvmBoxOutput(Lang.ops(sbErr)));
    }

    public JvmJsExecContext(WnIo io,
                            WnAuthSession session,
                            WnAuthExecutable runner,
                            StringBuilder sbOut,
                            StringBuilder sbErr) {
        this(io, session, runner, null, sbOut, sbErr);
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
        return io.check(null, aph);
    }

    // 提供低阶 IO 接口
    @Override
    public WnIo io() {
        return this.io;
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
    public void exec(String cmdText, CharSequence input) {
        sys.exec(cmdText, input);
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

    /**
     * 抹除单引号,双引号,换行,变量
     * 
     * @param val
     * @return
     */
    public String safe(Object val) {
        if (val == null)
            return "null";
        return WnStr.safeTrim(val.toString(), new char[]{'\r', '\n', ';', '$', '\'', '"'}, null);
    }

    /**
     * 获取底层log对象,通过这个Log对象写入的日志,会写到文件系统
     */
    public Log nlog() {
        return _log;
    }

    public void writeText(String path, String data) {
        WnObj o = Wn.checkObj(io, se, path);
        io.writeText(o, data);
    }

    public String readText(String path) {
        WnObj o = Wn.checkObj(io, se, path);
        return io.readText(o);
    }

    public String readTextAt(String path, int off, int len) {
        byte[] bs = readBytes(path, off, len);
        if (null == bs)
            return null;
        if (bs.length == 0)
            return "";

        return new String(bs, Encoding.CHARSET_UTF8);
    }

    public byte[] readBytes(String path, int off, int len) {
        WnObj o = Wn.checkObj(io, se, path);
        WnIoHandle h = null;
        try {
            h = io.openHandle(o, Wn.Io.RW);
            if (off > 0)
                h.seek(off);
            byte[] buf = new byte[len];
            int sz = h.read(buf);
            if (sz == len) {
                return buf;
            }
            byte[] reb = new byte[sz];
            System.arraycopy(buf, 0, reb, 0, sz);
            return reb;
        }
        catch (WnIoHandleMutexException e) {
            throw Lang.wrapThrow(e);
        }
        catch (IOException e) {
            throw Lang.wrapThrow(e);
        }
        finally {
            Streams.safeClose(h);
        }
    }

    public String uu32() {
        return R.UU32();
    }
}
