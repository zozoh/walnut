package com.site0.walnut.ext.net.aliyun;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.nutz.json.Json;
import com.site0.walnut.util.Wlang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.net.aliyun.sdk.WnAliyuns;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.Wn;

public abstract class JvmAliyunExecutor extends JvmHdlExecutor {

    /**
     * @param sys
     *            不解释
     * @param hc
     *            不解释
     * @param type
     *            命令类型，譬如 "vod" 或者 "oss"
     * @param confClass
     *            配置文件类型
     */
    protected void _findHdlNameBy(WnSystem sys, JvmHdlContext hc, String type, Class<?> confClass) {
        // 必须为有类型
        if (Strings.isBlank(type)) {
            throw Er.create("e.cmd." + this.getMyName() + ".nilType", hc.args);
        }
        // 如果第一个参数就是处理器，那么，HOME 则自动寻找
        if (hc.args.length < 1) {
            throw Er.create("e.cmd." + this.getMyName() + ".lackArgs", hc.args);
        }

        // 要 Copy 参数的起始位置
        int pos;

        // 第一个参数就是 hdl，那么就用 default.json
        if (null != this.getHdl(hc.args[0])) {
            hc.hdlName = hc.args[0];
            hc.oRefer = Wn.checkObj(sys, "~/.aliyun/" + type + "/default.json");
            pos = 1;
        }
        // 第一个参数为类型
        else {
            if (hc.args.length < 2) {
                throw Er.create("e.cmd." + this.getMyName() + ".lackArgs", hc.args);
            }
            // 得到配置信息名称
            String confName = hc.args[0];
            hc.oRefer = Wn.checkObj(sys, "~/.aliyun/" + type + "/" + confName + ".json");

            // 获得处理器名称
            hc.hdlName = hc.args[1];

            // 处理参数的位置
            pos = 2;
        }

        // 解析参数
        hc.args = Arrays.copyOfRange(hc.args, pos, hc.args.length);

        // 分析配置文件
        WnAliyuns.setConf(sys.io, hc, confClass);
    }

    /**
     * 命令退出前，看看是不是要输出一下内容
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void _before_quit(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 木有啥需要输出的
        if (null == hc.output)
            return;

        // 如果是字符串，不要强制输出换行
        if (hc.output instanceof CharSequence) {
            sys.out.print(hc.output.toString());
        }
        // 如果是 WnObj ..
        else if (hc.output instanceof WnObj) {
            Cmds.output_objs(sys, hc.params, hc.pager, Wlang.list((WnObj) hc.output), false);
        }
        // 如果是 NutBean
        else if (hc.output instanceof NutBean) {
            Cmds.output_beans(sys, hc.params, hc.pager, Wlang.list((NutBean) hc.output));
        }
        // 如果就是普通 Map
        else if (hc.output instanceof Map) {
            NutMap map = NutMap.WRAP((Map<String, Object>) hc.output);
            Cmds.output_beans(sys, hc.params, hc.pager, Wlang.list(map));
        }
        // 如果是数组或者列表，直接搞
        else if (hc.output.getClass().isArray() || hc.output instanceof List) {
            Object oFirst = Wlang.firstInAny(hc.output);

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
                Cmds.output_beans(sys, hc.params, hc.pager, (List<? extends NutBean>) hc.output);
            }
            // 其他集合只能简单的 toJson
            else {
                sys.out.println(Json.toJson(hc.output, hc.jfmt));
            }
        }
        // 其他的情况，就直接 toJSON 输出咯
        else {
            String json = Json.toJson(hc.output, hc.jfmt);
            sys.out.println(json);
        }
    }

}
