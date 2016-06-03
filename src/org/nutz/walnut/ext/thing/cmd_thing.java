package org.nutz.walnut.ext.thing;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.Wn;

public class cmd_thing extends JvmHdlExecutor {

    @Override
    protected void _find_hdl_name(WnSystem sys, JvmHdlContext hc) {
        // 记录从哪里 copy args 的位置
        int pos;

        // 没有参数
        if (hc.args.length == 0) {
            hc.hdlName = "get";
            hc.oHome = this.getCurrentObj(sys);
            pos = 0;
        }
        // 第一个参数就是 hdl，那么当前目录就作为 oHome
        else if (null != this.getHdl(hc.args[0])) {
            hc.hdlName = hc.args[0];
            hc.oHome = this.getCurrentObj(sys);
            pos = 1;
        }
        // 第一个参数表示一个 Thing|ThingSet
        else {
            hc.hdlName = hc.args.length > 1 ? hc.args[1] : "get";
            hc.oHome = Wn.checkObj(sys, hc.args[0]);
            pos = 2;
        }

        // 检查 oHome，如果又不是 ThingSet 又不是 Thing，抛错
        if (!hc.oHome.isType("thing") && !hc.oHome.has("thing")) {
            throw Er.create("e.cmd.thing.invalidHome", hc.oHome);
        }

        // 解析参数
        hc.args = Arrays.copyOfRange(hc.args, pos, hc.args.length);

        // 得到配置文件
        if (null != hc.oHome) {
            WnObj oConf = sys.io.check(hc.oHome, "wxconf");
            hc.setv("wxconf_obj", oConf);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void _before_quit(WnSystem sys, JvmHdlContext hc) {
        // 输出
        if (!hc.params.is("Q")) {
            // 输出内容
            if (null != hc.output) {
                // 如果是 WnObj ..
                if (hc.output instanceof WnObj) {
                    Cmds.output_objs(sys, hc.params, hc.pager, Lang.list((WnObj) hc.output), false);
                }
                // 如果是 NutBean
                else if (hc.output instanceof NutBean) {
                    Cmds.output_beans(sys, hc.params, hc.pager, Lang.list((NutBean) hc.output));
                }
                // 如果就是普通 Map
                else if (hc.output instanceof Map) {
                    NutMap map = NutMap.WRAP((Map<String, Object>) hc.output);
                    Cmds.output_beans(sys, hc.params, hc.pager, Lang.list(map));
                }
                // 如果是数组或者列表，直接搞
                else if (hc.output.getClass().isArray() || hc.output instanceof List) {
                    Object oFirst = Lang.first(hc.output);
                    // WnObj 的集合
                    if (oFirst instanceof WnObj) {
                        Cmds.output_objs(sys,
                                         hc.params,
                                         hc.pager,
                                         (List<? extends WnObj>) hc.output,
                                         false);
                    }
                    // WnBean 的集合
                    else if (oFirst instanceof NutBean) {
                        Cmds.output_beans(sys,
                                          hc.params,
                                          hc.pager,
                                          (List<? extends NutBean>) hc.output);
                    }
                    // 其他集合只能简单的 toJson
                    else {
                        sys.out.println(Json.toJson(hc.output, hc.jfmt));
                    }
                }
                // 其他的情况，就直接 toString 输出咯
                else {
                    sys.out.println(hc.output);
                }
            }
        }
    }

}
