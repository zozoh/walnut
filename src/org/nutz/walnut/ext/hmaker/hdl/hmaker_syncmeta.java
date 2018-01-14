package org.nutz.walnut.ext.hmaker.hdl;

import java.util.LinkedList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.hmaker.util.HmContext;
import org.nutz.walnut.ext.hmaker.util.Hms;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

@JvmHdlParamArgs("cqnl")
public class hmaker_syncmeta implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 上下文
        final HmContext hpc = new HmContext(sys.io, sys.se.group());
        hpc.oConfHome = Wn.checkObj(sys, "~/.hmaker");
        hpc.oApiHome = Wn.getObj(sys, "~/.regapi/api");

        // 得到处理的页面
        List<WnObj> list = new LinkedList<>();

        // 开始循环处理页面
        for (String pgnm : hc.args) {
            WnObj oPage = sys.io.check(hc.oRefer, pgnm);
            String content = sys.io.readText(oPage);
            Hms.syncPageMeta(hpc, sys, oPage, content);
            list.add(oPage);
        }

        // 输出结果
        if (!hc.params.is("l") && list.size() == 1) {
            sys.out.println(Json.toJson(list.get(0), hc.jfmt));
        }
        // 仅仅输出一个对象
        else {
            sys.out.println(Json.toJson(list, hc.jfmt));
        }
    }

}
