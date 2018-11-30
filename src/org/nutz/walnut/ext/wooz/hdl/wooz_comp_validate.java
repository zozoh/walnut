package org.nutz.walnut.ext.wooz.hdl;

import org.nutz.lang.ContinueLoop;
import org.nutz.lang.Each;
import org.nutz.lang.ExitLoop;
import org.nutz.lang.Lang;
import org.nutz.lang.LoopException;
import org.nutz.lang.Strings;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;

/**
 * 检查赛事各种数据是否完备
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class wooz_comp_validate implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 得到关键参数
        String compId = hc.params.val_check(0);

        // 获取赛事对象并确保是主办方赛事
        WnObj oComp = sys.io.get(compId);
        if (oComp.has("sp_comp_id")) {
            oComp = sys.io.checkById(oComp.getString("sp_comp_id"));
        }

        // 域名不能为空
        if (Strings.isBlank(oComp.name())) {
            throw Er.create("wooz.err.comp_name_blank");
        }

        // 域名不能大写
        if (oComp.name().toLowerCase().equals(oComp.name())) {
            throw Er.create("wooz.err.comp_name_must_lower");
        }

        // 自定义域名也不能大写
        if(oComp.has("cname")) {
            Lang.each(oComp.get("cname"), (i, nm, len)->{
                
            });
        }

        // 检查赛事的四个时间
    }

}
