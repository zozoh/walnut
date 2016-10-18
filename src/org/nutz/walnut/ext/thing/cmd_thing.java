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

public class cmd_thing extends JvmHdlExecutor {

    @Override
    protected void _find_hdl_name(WnSystem sys, JvmHdlContext hc) {
        // 记录从哪里 copy args 的位置
        int pos;

        // 没有参数
        if (hc.args.length == 0) {
            throw Er.create("e.cmd.thing.lackArgs", hc.args);
        }
        // 第一个参数就是 hdl，那么当前目录就作为 oHome
        // :> thing hdlName xxx
        else if (null != this.getHdl(hc.args[0])) {
            hc.oRefer = this.getCurrentObj(sys);
            hc.hdlName = hc.args[0];
            pos = 1;
        }
        // 第一个参数表示一个 TsID 并且有多余一个的参数
        // :> thing ID hdlName xxx
        else if(hc.args.length >= 2){
            hc.oRefer = Things.checkThingSet(sys.io, hc.args[0]);
            hc.hdlName = hc.args[1];
            pos = 2;
        }
        // 否则还是缺参数
        else {
            throw Er.create("e.cmd.thing.lackArgs", hc.args);
        }

        // 检查 oHome，如果又不是 ThingSet 又不是 Thing，抛错
        // if (!hc.oHome.isType("thing") && !hc.oHome.has("thing")) {
        // throw Er.create("e.cmd.thing.invalidHome", hc.oHome);
        // }

        // Copy 剩余参数
        hc.args = Arrays.copyOfRange(hc.args, pos, hc.args.length);

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

                    // 确保按照列表输出
                    hc.params.setv("l", true);

                    // WnObj 的集合
                    if (null == oFirst || oFirst instanceof WnObj) {
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
