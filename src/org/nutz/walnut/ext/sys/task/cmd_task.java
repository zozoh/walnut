package org.nutz.walnut.ext.sys.task;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nutz.lang.Mirror;
import org.nutz.resource.Scans;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

public class cmd_task extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        // 准备上下文
        TaskCtx sc = new TaskCtx();
        sc.sys = sys;

        // 找到控制器名称，以及关联的任务
        int pos = 0;

        // 首先看第一个参数是不是能找到一个控制器
        sc.hdlName = args[pos];
        TaskHdl hdl = hdls.get(sc.hdlName);

        // 如果找不到
        if (null == hdl) {
            // 那么第一个参数必须是一个任务 ID
            sc.oTask = sys.io.checkById(sc.hdlName);
            if (!sc.oTask.isType("task"))
                throw Er.create("e.cmd.task.notask", sc.hdlName);

            // 并且第二个参数必须是一个控制器
            sc.hdlName = args[++pos];
            hdl = hdls.get(sc.hdlName);
            if (null == hdl) {
                throw Er.create("e.cmd.task.unknown.hdl", sc.hdlName);
            }
        }
        // 剩下的参数就是控制器的参数
        sc.args = Arrays.copyOfRange(args, pos + 1, args.length);

        // 找到主目录
        sc.oCurrent = sys.getCurrentObj();
        sc.oTaskHome = sys.io.createIfNoExists(null,
                                               Wn.normalizeFullPath("~/.task", sys),
                                               WnRace.DIR);

        // 调用执行器
        hdl.invoke(sys, sc);

    }

    private Map<String, TaskHdl> hdls;

    public cmd_task() {
        hdls = new HashMap<String, TaskHdl>();
        // 扫描本包下的所有类
        List<Class<?>> list = Scans.me().scanPackage(this.getClass());
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

            // 如果是 TaskHdl 的实现类 ...
            Mirror<?> mi = Mirror.me(klass);
            if (mi.isOf(TaskHdl.class)) {
                // 获取 Key
                String nm = klass.getSimpleName();
                if (!nm.startsWith("Th_")) {
                    throw Er.create("e.cmd.site.wrongName", klass.getName());
                }
                String key = nm.substring(3).toLowerCase();
                TaskHdl hdl = (TaskHdl) mi.born();
                hdls.put(key, hdl);
            }
        }
    }
}
