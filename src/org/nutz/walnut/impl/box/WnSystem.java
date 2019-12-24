package org.nutz.walnut.impl.box;

import java.io.InputStream;
import java.io.OutputStream;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.Callback;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.log.impl.AbstractLog;
import org.nutz.trans.Atom;
import org.nutz.trans.Proton;
import org.nutz.walnut.api.WnExecutable;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.auth.WnAuthService;
import org.nutz.walnut.api.auth.WnAuthSession;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.io.WnEvalLink;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnContext;
import org.nutz.walnut.util.WnSysConf;
import org.nutz.walnut.util.ZParams;
import org.nutz.walnut.web.util.WalnutLog;

public class WnSystem implements WnExecutable {

    private static final Log log = Logs.get();

    public String boxId;

    public int pipeId;

    public int nextId;

    public boolean isOutRedirect;

    public String cmdOriginal;

    // public WnUsr me;
    //
    // public WnSession se;

    public JvmBoxInput in;

    public JvmBoxOutput out;

    public JvmBoxOutput err;

    public WnIo io;

    // public WnSessionService sessionService;
    //
    // public WnUsrService usrService;

    public WnAuthService auth;

    public WnAuthSession session;

    public JvmExecutorFactory jef;

    JvmAtomRunner _runner;

    public WnAccount getMe() {
        if (null != this.session) {
            return this.session.getMe();
        }
        return null;
    }

    public String getMyId() {
        WnAccount me = this.getMe();
        if (null != me) {
            return me.getId();
        }
        return null;
    }

    public String getMyName() {
        WnAccount me = this.getMe();
        if (null != me) {
            return me.getName();
        }
        return null;
    }

    public String getMyGroup() {
        WnAccount me = this.getMe();
        if (null != me) {
            return me.getGroupName();
        }
        return null;
    }

    /**
     * 获取当前用户的 HOME 对象
     *
     * @return HOME 对象
     */
    public WnObj getHome() {
        String home = this.session.getVars().getString("HOME");
        String path = Wn.normalizePath(home, this);
        return this.io.check(null, path);
    }

    /**
     * @param dft
     *            默认语言
     * @return 当前的语言
     */
    public String getLang(String dft) {
        return this.session.getVars().getString("LANG", dft);
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
        String pwd = this.session.getVars().getString("PWD");
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
     * 将当前的系统沙箱和线程上下文切换成某指定用户，并为这个用户创建一个会话。
     * 这样本沙箱运行的所有环境就变成了一个新用户。函数退出前，会恢复原始的会话信息。新会话也会被注销
     * <p>
     * ！！！ 注意，只有 root 用户组的成员以上权限才有资格执行这个操作
     * 
     * @param newUsr
     *            新用户
     * @param callback
     *            回调，参数为当前 WnSystem
     */
    public void switchUser(WnAccount newUsr, Callback<WnSystem> callback) {
        final WnSystem sys = this;
        // 检查权限
        if (!this.auth.isMemberOfGroup(this.getMe(), "root")) {
            throw Er.create("e.sys.switchUser.nopvg");
        }

        // 创建新会话
        WnAuthSession newSession = this.auth.createSession(this.getMe(), true);

        // 记录旧的 Session
        WnAuthSession old_se = this.session;
        this.session = newSession;
        WnContext wc = Wn.WC();
        try {
            // 切换沙箱的的会话
            this._runner.bc.session = newSession;
            // 切换 session
            wc.setSession(newSession);
            wc.su(newUsr, new Atom() {
                public void run() {
                    callback.invoke(sys);
                }
            });
        }
        // 释放 session
        finally {
            // 切换沙箱的的会话
            this._runner.bc.session = old_se;
            // 切换 session
            this.session = old_se;
            wc.setSession(old_se);
            this.auth.removeSession(newSession, 0);
        }
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
    public WnSysConf getSysConf() {
        return Wn.getSysConf(io);
    }
}
