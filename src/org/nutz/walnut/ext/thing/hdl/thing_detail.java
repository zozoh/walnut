package org.nutz.walnut.ext.thing.hdl;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.thing.Things;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.Wn;

@JvmHdlParamArgs(value = "cnqihbslVNHQ", regex = "^(drop)$")
public class thing_detail implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        // 得到对应对 Thing
        WnObj oT = Things.checkThing(hc.oHome);

        // 得到 Detail 文件
        WnObj oDetail = sys.io.fetch(oT, "detail");

        // 清除详情
        if (hc.params.is("drop")) {
            if (null != oDetail) {
                sys.io.delete(oDetail);
            }
        }
        // 创建或者修改
        else if (hc.params.has("content")) {
            // 确保创建
            if (null == oDetail) {
                oDetail = sys.io.createIfNoExists(oT, "detail", WnRace.FILE);
            }

            // 修改类型
            if (!oDetail.isType("^(txt|html|md)$")
                || (hc.params.has("tp") && !oDetail.isType(hc.params.get("tp")))) {
                Wn.set_type(sys.io.mimes(), oDetail, hc.params.get("tp", "txt"));
                sys.io.set(oDetail, "^(tp|mime)$");
            }

            // 写入内容
            String content = Cmds.getParamOrPipe(sys, hc.params, "content", false);
            sys.io.writeText(oDetail, content);
        }
        // 那就是获取内容咯，啥也不用做就好了

        // 最后返回 detail 对象
        hc.output = oDetail;
    }

}
