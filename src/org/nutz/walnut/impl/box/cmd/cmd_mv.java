package org.nutz.walnut.impl.box.cmd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

public class cmd_mv extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) {

        ZParams params = ZParams.parse(args, "STvocnql");

        // 参数错误
        if (params.vals.length < 2) {
            throw Er.create("e.cmd.mv.lackargs");
        }

        // 得到源
        String[] srcPaths = Arrays.copyOfRange(params.vals, 0, params.vals.length - 1);
        List<WnObj> list = new LinkedList<WnObj>();
        WnObj p = evalCandidateObjs(sys, srcPaths, list, 0);

        // 检查是否候选对象列表为空
        checkCandidateObjsNoEmpty(args, list);

        // 检查目标
        String dest = params.vals[params.vals.length - 1];
        String destPath = Wn.normalizeFullPath(dest, sys);
        WnObj oDest = sys.io.fetch(p, destPath);

        // 如果移动的是多个，那么目标必须是一个目录
        if (list.size() > 1) {
            if (null == oDest || oDest.isFILE()) {
                throw Er.create("e.cmd.mv.multidest.notdir", dest);
            }
        }

        // 计算移动模式
        int mode = 0;
        if (!params.is("T"))
            mode |= Wn.MV.TP;
        if (!params.is("S"))
            mode |= Wn.MV.SYNC;

        // 逐个移动
        List<WnObj> outs = new ArrayList<WnObj>(list.size());
        for (WnObj o : list) {
            String oldName = o.name();
            sys.io.move(o, destPath, mode);
            // 显示
            if (params.is("v")) {
                // 换目录了
                if (dest.contains("/")) {
                    sys.out.printlnf("%s -> %s/%s", oldName, dest, o.name());
                }
                // 仅仅是改名
                else {
                    sys.out.printlnf("%s -> %s", oldName, o.name());
                }
            }
            // 记录准备输出
            else if (params.is("o")) {
                outs.add(o);
            }
        }

        // 准备输出
        if (outs.size() > 0) {
            JsonFormat fmt = this.gen_json_format(params);
            // 输出单个
            if (outs.size() == 1) {
                sys.out.println(Json.toJson(outs.get(0), fmt));
            }
            // 输出列表
            else if (outs.size() > 1) {
                sys.out.println(Json.toJson(outs, fmt));
            }
        }
    }
}
