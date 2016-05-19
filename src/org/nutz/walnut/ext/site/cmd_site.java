package org.nutz.walnut.ext.site;

import java.io.Reader;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nutz.lang.Mirror;
import org.nutz.lang.Strings;
import org.nutz.resource.Scans;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;

public class cmd_site extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        // 准备上下文
        ShCtx sc = new ShCtx();

        // 找到 -home 和 handle 的名称
        List<String> _args = new ArrayList<String>(args.length);
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            // 用户自定义了 homePath
            if ("-home".equals(arg)) {
                sc.homePath = args[++i];
            }
            // 第一个参数是处理器名称
            else if (null == sc.hdlName) {
                sc.hdlName = arg;
            }
            // 后面的是处理器参数
            else {
                _args.add(arg);
            }
        }
        // 啥都木有，打印一下帮助
        if (null == sc.hdlName) {
            sys.out.println(this.getManual());
            return;
        }

        // 找到控制器
        SiteHdl hdl = hdls.get(sc.hdlName);
        if (null == hdl) {
            throw Er.create("e.cmd.site.nohdl", hdl);
        }

        // 找到主目录
        sc.oCurrent = this.getCurrentObj(sys);
        sc.oHome = sc.oCurrent;
        if (null != sc.homePath)
            sc.oHome = sys.io.check(sc.oHome, sc.homePath);

        sc.oConf = sys.io.fetch(sc.oHome, "site.conf");
        while (null == sc.oConf && sc.oHome.hasParent()) {
            sc.oHome = sc.oHome.parent();
        }

        if (null == sc.oConf) {
            throw Er.create("e.cmd.site.nosite", Strings.sNull(sc.homePath, "."));
        }

        // 生成上下文
        Reader reader = sys.io.getReader(sc.oConf, 0);
        sc.conf = new SiteConf(reader);
        sc.args = _args.toArray(new String[_args.size()]);
        sc.sys = sys;

        // 调用执行器
        hdl.invoke(sys, sc);

    }

    private Map<String, SiteHdl> hdls;

    public cmd_site() {
        hdls = new HashMap<String, SiteHdl>();
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

            // 如果是 SiteHdl 的实现类 ...
            Mirror<?> mi = Mirror.me(klass);
            if (mi.isOf(SiteHdl.class)) {
                // 获取 Key
                String nm = klass.getSimpleName();
                if (!nm.startsWith("Sh_")) {
                    throw Er.create("e.cmd.site.wrongName", klass.getName());
                }
                String key = nm.substring(2).toLowerCase();
                SiteHdl hdl = (SiteHdl) mi.born();
                hdls.put(key, hdl);
            }
        }
    }
}
