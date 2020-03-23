package org.nutz.walnut.ext.bizhook;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.nutz.json.Json;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

public class cmd_bizhook extends JvmHdlExecutor {

    @Override
    protected void _find_hdl_name(WnSystem sys, JvmHdlContext hc) {
        // 记录从哪里 copy args 的位置
        int pos;

        // 没有参数
        if (hc.args.length == 0) {
            throw Er.create("e.cmd.bizhook.lackArgs", hc.args);
        }
        // 第一个参数表示一个 bizhook 的配置文件路径
        if (hc.args.length >= 2) {
            String confPath = hc.args[0];
            hc.oRefer = Wn.checkObj(sys, confPath);
            hc.hdlName = hc.args[1];
            pos = 2;
        }
        // 否则还是缺参数
        else {
            throw Er.create("e.cmd.thing.lackArgs", hc.args);
        }

        // Copy 剩余参数
        hc.args = Arrays.copyOfRange(hc.args, pos, hc.args.length);

    }

    @SuppressWarnings("unchecked")
    @Override
    protected void _before_invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 读取配置
        NutMap conf = sys.io.readJson(hc.oRefer, NutMap.class);
        BizHookGroup bhg = new BizHookGroup(conf);
        hc.put("hooks", bhg);

        // 分析输入参数，变成一个对象集合
        List<NutBean> beans = new LinkedList<>();

        // 管道输入
        String inputS = sys.in.readAll();
        if (!Strings.isBlank(inputS)) {
            Object input = Json.fromJson(inputS);
            // 多个对象集合
            if (input instanceof Collection<?>) {
                Collection<?> coll = (Collection<?>) input;
                for (Object obj : coll) {
                    NutMap map = NutMap.WRAP((Map<String, Object>) obj);
                    beans.add(map);
                }
            }
            // 单个对象
            else if (input instanceof Map<?, ?>) {
                NutMap map = NutMap.WRAP((Map<String, Object>) input);
                beans.add(map);
            }
        }

        // 参数输入
        for (String ph : hc.params.vals) {
            // 直接就是对象
            if (Strings.isQuoteBy(ph, '{', '}')) {
                NutMap o = Json.fromJson(NutMap.class, ph);
                beans.add(o);
            }
            // WnObj 的 id
            else {
                WnObj o = Wn.checkObj(sys, ph);
                beans.add(o);
            }
        }
        hc.put("beans", beans);
    }

}
