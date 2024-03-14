package com.site0.walnut.impl.box.cmd;

import java.util.LinkedList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Each;
import org.nutz.lang.Strings;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.impl.box.JvmExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.ZParams;

public class cmd_disk extends JvmExecutor {

    @Override
    public void exec(final WnSystem sys, String[] args) {
        // 分析参数
        final ZParams params = ZParams.parse(args, "A");

        // 计算要列出的目录并得到当前目录
        List<WnObj> list = new LinkedList<WnObj>();
        Cmds.evalCandidateObjs(sys, params.vals, list, Wn.Cmd.JOIN_CURRENT);

        // 检查是否候选对象列表为空
        Cmds.assertCandidateObjsNoEmpty(args, list);

        final boolean showHidden = params.is("A");
        final String tp = params.get("tp");
        // 先显示文件
        for (WnObj o : list) {
            if (o.isFILE()) {
                showObj(sys, o, showHidden, tp);
            }
        }
        // 再输出所有的目录
        for (WnObj o : list) {
            if (!o.isFILE()) {
                sys.io.each(Wn.Q.pid(o.id()), new Each<WnObj>() {
                    public void invoke(int index, WnObj child, int length) {
                        showObj(sys, child, showHidden, tp);
                    }
                });
            }
        }
    }

    private void showObj(WnSystem sys, WnObj obj, boolean showHidden, String tp) {
        // 隐藏
        if (obj.isHidden() && !showHidden) {
            return;
        }
        // 过滤文件类型
        if (!Strings.isBlank(tp) && !tp.equalsIgnoreCase(obj.type())) {
            return;
        }
        obj.path();
        sys.out.println(Json.toJson(obj, JsonFormat.compact()));
    }
}
