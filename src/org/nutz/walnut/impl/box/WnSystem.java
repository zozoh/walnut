package org.nutz.walnut.impl.box;

import java.io.InputStream;
import java.io.OutputStream;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.log.impl.AbstractLog;
import org.nutz.trans.Atom;
import org.nutz.trans.Proton;
import org.nutz.walnut.api.WnExecutable;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.usr.WnSession;
import org.nutz.walnut.api.usr.WnSessionService;
import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.api.usr.WnUsrService;
import org.nutz.walnut.impl.io.WnEvalLink;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;
import org.nutz.walnut.web.util.WalnutLog;

public class WnSystem implements WnExecutable{

    private static final Log log = Logs.get();

    public String boxId;

    public int pipeId;

    public int nextId;

    public boolean isOutRedirect;

    public String cmdOriginal;

    public WnUsr me;

    public WnSession se;

    public JvmBoxInput in;

    public JvmBoxOutput out;

    public JvmBoxOutput err;

    public WnIo io;

    public WnSessionService sessionService;

    public WnUsrService usrService;

    public JvmExecutorFactory jef;

    JvmAtomRunner _runner;

    /**
     * 获取当前用户的 HOME 对象
     *
     * @return HOME 对象
     */
    public WnObj getHome() {
        String pwd = this.se.vars().getString("HOME");
        String path = Wn.normalizePath(pwd, this);
        return this.io.check(null, path);
    }

    /**
     * @param dft
     *            默认语言
     * @return 当前的语言
     */
    public String getLang(String dft) {
        String lang = this.se.varString("LANG");
        return Strings.sBlank(lang, dft);
    }

    /**
     * @return 当前的语言（默认zh-cn）
     * @see #getLang(String)
     */
    public String getLang() {
        return getLang("zh-cn");
    }

    /**
     * 获取当前用户的当前所在路径对象
     * 
     * @return 对象
     */
    public WnObj getCurrentObj() {
        String pwd = this.se.vars().getString("PWD");
        String path = Wn.normalizePath(pwd, this);
        WnObj re = this.io.check(null, path);
        return Wn.WC().whenEnter(re, false);
    }

    public NutMap attrs() {
        return _runner.bc.attrs;
    }

    public void exec(String cmdText) {
        exec(cmdText, out.getOutputStream(), err.getOutputStream(), in.getInputStream());
    }

    public void execf(String fmt, Object... args) {
        String cmdText = String.format(fmt, args);
        exec(cmdText);
    }

    public void exec(String cmdText, OutputStream stdOut, OutputStream stdErr, InputStream stdIn) {
        String[] cmdLines = Cmds.splitCmdLine(cmdText);
        _runner.out = new EscapeCloseOutputStream(null == stdOut ? out.getOutputStream() : stdOut);
        _runner.err = new EscapeCloseOutputStream(null == stdErr ? err.getOutputStream() : stdErr);
        _runner.in = new EscapeCloseInputStream(null == stdIn ? in.getInputStream() : stdIn);

        if (log.isInfoEnabled())
            log.info(" > sys.exec: " + cmdText);

        for (String cmdLine : cmdLines) {
            _runner.run(cmdLine);
            _runner.wait_for_idle();
        }
        _runner.__free();
    }

    public void exec(String cmdText,
                     StringBuilder stdOut,
                     StringBuilder stdErr,
                     CharSequence stdIn) {
        InputStream ins = null == stdIn ? in.getInputStream() : Lang.ins(stdIn);
        OutputStream out = null == stdOut ? null : Lang.ops(stdOut);
        OutputStream err = null == stdErr ? null : Lang.ops(stdErr);

        exec(cmdText, out, err, ins);
    }

    public void exec(String cmdText, CharSequence input) {
        exec(cmdText, null, null, input);
    }

    public String exec2(String cmdText) {
        return exec2(cmdText, null);
    }

    public String exec2f(String fmt, Object... args) {
        String cmdText = String.format(fmt, args);
        return exec2(cmdText);
    }

    public String exec2(String cmdText, CharSequence input) {
        StringBuilder sb = new StringBuilder();
        exec(cmdText, sb, sb, input);
        return sb.toString();
    }

    public Log getLog(ZParams params) {
        String level = null;

        // 设置级别（默认info)
        if (params.is("error")) {
            level = "E";
        } else if (params.is("warn")) {
            level = "W";
        } else if (params.is("debug")) {
            level = "D";
        } else if (params.is("v") || params.is("trace")) {
            level = "T";
        } else {
            level = "I";
        }

        return getLog(level, params.get("logtmpl"));
    }

    /**
     * @param level
     *            级别，可以是 fatal|error|warn|info|debug|trace 或者是 F|E|W|I|D|T
     * @param tmpl
     *            日志输出模板，格式类似
     *            <code>@{P} @{d<date:dd'T'HH:mm:ss.SSS>}: @{m}</code>
     * @return 日志对象
     */
    public Log getLog(String level, String tmpl) {
        int l = AbstractLog.level(level);

        WalnutLog log = new WalnutLog(this, l);
        if (!Strings.isBlank(tmpl))
            log.setTmpl(tmpl);

        return log;
    }

    /**
     * 进入内核态执行操作
     * 
     * @param atom
     *            操作
     */
    public void nosecurity(Atom atom) {
        Wn.WC().nosecurity(io, atom);
    }

    /**
     * 进入内核态执行带返回的操作
     * 
     * @param protom
     *            操作
     * 
     * @return 返回结果
     */
    public <T> T nosecurity(Proton<T> proton) {
        return Wn.WC().nosecurity(io, proton);
    }

    /**
     * 进入超级内核态（不执行钩子）执行操作
     * 
     * @param synctimeOff
     *            是否关闭文件同步时间戳的更新逻辑（关闭的话，速度会更快，就像偷偷的把数据改掉一样）
     * @param atom
     *            操作
     */
    public void core(boolean synctimeOff, Atom atom) {
        Wn.WC().core(new WnEvalLink(io), synctimeOff, null, atom);
    }

    /**
     * 进入超级内核态（不执行钩子）执行带返回操作
     * 
     * @param synctimeOff
     *            是否关闭文件同步时间戳的更新逻辑（关闭的话，速度会更快，就像偷偷的把数据改掉一样）
     * @param atom
     *            操作
     * 
     * @return 返回结果
     */
    public <T> T core(boolean synctimeOff, Proton<T> proton) {
        return Wn.WC().core(new WnEvalLink(io), synctimeOff, null, proton);
    }

    /**
     * @return 系统配置对象
     */
    public NutMap getSysConf() {
        return Wn.getSysConf(io);
    }
}
