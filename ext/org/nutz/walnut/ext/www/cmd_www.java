package org.nutz.walnut.ext.www;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nutz.lang.Lang;
import org.nutz.lang.Mirror;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.resource.Scans;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

public class cmd_www extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        // 生成上下文
        WWWContext wwc = new WWWContext();
        wwc.params = ZParams.parse(args, null);

        // 得到输入的模板文件
        String str = wwc.params.check("in");
        WnObj o = Wn.checkObj(sys, str);
        wwc.input = sys.io.readText(o);
        wwc.type = o.type();
        wwc.fnm = o.name();
        wwc.oCurrent = o.parent();

        // 初始化上下文
        wwc.context = new NutMap();
        wwc.context.put("grp", sys.se.group());
        wwc.context.put("fnm", wwc.fnm);
        wwc.context.put("rs", "/gu/rs");

        // 指定了一个上下文的 JSON 字符串，合并
        if (wwc.params.has("c")) {
            String json = wwc.params.check("c");
            // 仅仅是声明，那么就从标准输入读一下
            if ("true".equals(json)) {
                json = sys.in.readAll();
            }
            // 不是 JSON 的格式，那么试图读读文件对象
            else if (!Strings.isQuoteBy(json, '{', '}')) {
                WnObj oJson = Wn.checkObj(sys.io, json);
                json = sys.io.readText(oJson);
            }
            // 解析并放入上下文
            wwc.context.putAll(Lang.map(json));
        }

        // 读取 type 和 name
        wwc.type = wwc.params.get("type", wwc.type);
        wwc.fnm = wwc.params.get("fnm", wwc.fnm);

        // 调用
        WWWHdl hdl = hdls.get(wwc.type);

        if (null == hdl) {
            throw Er.create("e.cmd.www.invalidHdl", wwc.type);
        }

        hdl.invoke(sys, wwc);

    }

    private Map<String, WWWHdl> hdls;

    public cmd_www() {
        hdls = new HashMap<String, WWWHdl>();
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

            // 如果是 HopeHdl 的实现类 ...
            Mirror<?> mi = Mirror.me(klass);
            if (mi.isOf(WWWHdl.class)) {
                // 获取 Key
                String nm = klass.getSimpleName();
                if (!nm.startsWith("www_")) {
                    throw Er.create("e.cmd.www.wrongName", klass.getName());
                }
                String key = nm.substring(4).toLowerCase();
                WWWHdl hdl = (WWWHdl) mi.born();
                hdls.put(key, hdl);
            }
        }
    }
}
