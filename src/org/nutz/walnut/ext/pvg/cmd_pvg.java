package org.nutz.walnut.ext.pvg;

import java.util.Arrays;
import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

public class cmd_pvg extends JvmHdlExecutor {

    public static void setPvgService(JvmHdlContext hc, NutMap matrix) {
        hc.put("biz-pvg-service", new BizPvgService(matrix));
    }

    public static BizPvgService getPvgService(JvmHdlContext hc) {
        return hc.getAs("biz-pvg-service", BizPvgService.class);
    }

    @Override
    protected void _find_hdl_name(WnSystem sys, JvmHdlContext hc) {
        int pos;
        // 没有参数
        if (hc.args.length == 0) {
            throw Er.create("e.cmd.thing.lackArgs", hc.args);
        }
        // 第一个参数就是 hdl，那么权限矩阵内容来自管道
        else if (null != this.getHdl(hc.args[0])) {
            hc.hdlName = hc.args[0];
            String matrixJson = sys.in.readAll();
            NutMap matrix = Lang.map(matrixJson);
            setPvgService(hc, matrix);
            pos = 1;
        }
        // 否则第一个参数就是权限矩阵所在的文件名
        else if (hc.args.length >= 2) {
            hc.oRefer = Wn.checkObj(sys, hc.args[0]);
            hc.hdlName = hc.args[1];
            NutMap matrix = sys.io.readJson(hc.oRefer, NutMap.class);
            setPvgService(hc, matrix);
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

}
