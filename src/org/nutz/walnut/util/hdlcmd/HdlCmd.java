package org.nutz.walnut.util.hdlcmd;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nutz.lang.Mirror;
import org.nutz.resource.Scans;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;

public abstract class HdlCmd extends JvmExecutor {

    /**
     * 子命令前缀
     * 
     * @return
     */
    public abstract String prefix();

    /**
     * 手动设置ctx内容
     * 
     * @param sys
     * @param ctx
     */
    public abstract void setCtx(WnSystem sys, HdlCtx ctx);

    private Map<String, IHdl> hdls;

    public HdlCmd() {
        loadHdls();
    }

    private void loadHdls() {
        hdls = new HashMap<String, IHdl>();
        // 扫描本包下的所有类
        List<Class<?>> list = Scans.me().scanPackage(getClass());
        for (Class<?> klass : list) {
            // 跳过抽象类
            int mod = klass.getModifiers();
            if (Modifier.isAbstract(mod))
                continue;

            // 跳过非公共的类
            if (!Modifier.isPublic(klass.getModifiers()))
                continue;

            // 跳过内部类
            if (klass.getName().contains("$"))
                continue;

            // 如果是 WnCmdHdl 的实现类 ...
            Mirror<?> mi = Mirror.me(klass);
            if (mi.isOf(IHdl.class)) {
                // 获取 Key
                String nm = klass.getSimpleName();
                if (!nm.startsWith(prefix())) {
                    throw Er.create("e.cmd.hdl.wrongName", klass.getName());
                }
                String key = nm.substring(prefix().length()).toLowerCase();
                IHdl hdl = (IHdl) mi.born();
                hdls.put(key, hdl);
            }
        }
    }

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        if (hdls == null) {
            throw Er.create("e.cmd.not.use.loadHdls");
        }

        if (args == null || args.length == 0) {
            sys.err.print("this is hdl-cmd, you shoud input hdl name");
            return;
        }

        // 准备上下文
        HdlCtx ctx = new HdlCtx();
        ctx.sys = sys;

        // 找到控制器名称，以及关联的任务
        int pos = 0;

        // 首先看第一个参数是不是能找到一个控制器
        ctx.hdlName = args[pos];
        IHdl hdl = hdls.get(ctx.hdlName);

        // 如果找不到
        if (null == hdl) {
            throw Er.create("e.cmd.unknown.hdl", ctx.hdlName);
        }

        // 剩下的参数就是控制器的参数
        ctx.args = Arrays.copyOfRange(args, pos + 1, args.length);

        // 找到主目录与当前目录
        ctx.oCurrent = this.getCurrentObj(sys);
        ctx.oHome = this.getHome(sys);

        // 手动设置ctx
        setCtx(sys, ctx);

        // 调用执行器
        hdl.invoke(sys, ctx);
    }

}
