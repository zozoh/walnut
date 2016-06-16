package org.nutz.walnut.ext.thing.hdl;

import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.thing.Things;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.Wn;

@JvmHdlParamArgs(value = "cnqihbslVNHQ", regex = "^(drop|quiet)$")
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
                // 删除 detail
                sys.io.delete(oDetail);

                // 设置 thing 元数据
                oT.setv("th_detail_tp", null);
                oT.setv("th_detail_sz", 0);
                sys.io.set(oT, "^th_detail_.+$");

                // 输出被删除 detail 对象
                hc.output = oDetail;
            }
            // 看看有没有必要报错
            else if (!hc.params.is("quiet")) {
                throw Er.create("e.cmd.thing.detail.blank", oT.id());
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

            // 设置 thing 元数据
            oT.setv("th_detail_tp", oDetail.type());
            oT.setv("th_detail_sz", oDetail.len());
            sys.io.set(oT, "^th_detail_.+$");

            // 输出 detail 对象
            hc.output = oDetail;
        }
        // 那就是获取内容咯，检查一下空内容要不要抛错
        else if (null == oDetail && !hc.params.is("quiet")) {
            throw Er.create("e.cmd.thing.detail.blank", oT.id());
        }
        // 输出 detail 对象的内容
        else {
            hc.output = sys.io.readText(oDetail);
        }
    }

}
