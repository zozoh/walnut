package com.site0.walnut.ext.biz.wooz.hdl;

import java.util.List;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Each;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.data.thing.util.Things;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlParamArgs;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.Wn;
import org.nutz.web.WebException;
import org.nutz.web.ajax.AjaxReturn;

/**
 * 检查赛事各种数据是否完备
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
@JvmHdlParamArgs("cqn")
public class wooz_comp_validate implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        AjaxReturn re = new AjaxReturn();
        try {
            WnObj oComp = __do_check_comp(sys, hc);
            // 输出正常返回
            re.setOk(true);
            re.setData(oComp);
        }
        catch (Exception e) {
            WebException we = Er.wrap(e);
            re.setOk(false);
            re.setErrCode(we.getKey());
            re.setMsg(we.toString());
            re.setData(we.getReason());
        }

        // 输出
        JsonFormat jfmt = Cmds.gen_json_format(hc.params);
        sys.out.println(Json.toJson(re, jfmt));
    }

    private WnObj __do_check_comp(WnSystem sys, JvmHdlContext hc) {
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
        if (!oComp.name().toLowerCase().equals(oComp.name())) {
            throw Er.create("wooz.err.comp_name_must_lower");
        }

        // 自定义域名也不能大写
        if (oComp.has("cname")) {
            Lang.each(oComp.get("cname"), new Each<String>() {
                public void invoke(int index, String nm, int length) {
                    if (!nm.toLowerCase().equals(nm)) {
                        throw Er.create("wooz.err.comp_cname_must_lower");
                    }
                }
            });
        }

        // 检查赛事的四个时间
        this._check_date(oComp);

        // 依次检查每个赛项设置
        WnObj oData = Things.dirTsData(sys.io, oComp);
        WnObj oPjHome = sys.io.fetch(oData, oComp.id() + "/proj/");
        if (null == oPjHome) {
            throw Er.create("wooz.err.comp_no_proj");
        }
        List<WnObj> oPjList = sys.io.getChildren(oPjHome, null);
        if (oPjList.isEmpty()) {
            throw Er.create("wooz.err.comp_no_proj");
        }
        for (WnObj oPj : oPjList) {
            // 四个日期有木有
            this._check_date(oPj);
            // 检查价格策略 pj_price
            List<NutMap> ppList = oPj.getAsList("pj_price", NutMap.class);
            if (ppList.isEmpty()) {
                throw Er.create("wooz.err.proj_no_price", oPj.name());
            }
            // 必须有默认价格策略
            boolean hasDefaultPrice = false;
            for (NutMap pp : ppList) {
                if (pp.is("type", "dft")) {
                    hasDefaultPrice = true;
                    break;
                }
            }
            if (!hasDefaultPrice) {
                throw Er.create("wooz.err.proj_no_dft_price", oPj.name());
            }
        }

        return oComp;
    }

    private void _check_date(WnObj obj) {
        long d_apply = obj.getLong("d_apply", -1);
        long d_ready = obj.getLong("d_ready", -1);
        long d_start = obj.getLong("d_start", -1);
        long d_end = obj.getLong("d_end", -1);
        long today = Wn.now();
        String prefix = obj.has("cm_tp") ? "comp" : "proj";
        // d_ready 必须在 d_apply 以后，否则报名区间木有啊
        if (d_ready < 0)
            throw Er.create("wooz.err." + prefix + "_no_d_ready");
        if (d_apply < 0)
            throw Er.create("wooz.err." + prefix + "_no_d_apply");
        if (d_ready <= d_apply) {
            throw Er.create("wooz.err." + prefix + "_d_ready_gt");
        }
        // d_ready 必须在 today 以后，否则表示比赛已经开始，还审核啥
        if (d_ready <= today) {
            throw Er.create("wooz.err." + prefix + "_d_ready_passed");
        }
        // d_end 必须在 d_start 以后，否则没时间比赛
        if (d_start < 0)
            throw Er.create("wooz.err." + prefix + "_no_d_start");
        if (d_end < 0)
            throw Er.create("wooz.err." + prefix + "_no_d_end");
        if (d_start >= d_end) {
            throw Er.create("wooz.err." + prefix + "_d_start_gt");
        }
        // d_start 必须在 d_ready 以后，否则不科学
        if (d_start < d_ready) {
            throw Er.create("wooz.err." + prefix + "_d_start_lt");
        }
    }

}
