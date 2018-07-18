package org.nutz.walnut.ext.voucher.hdl;

import java.util.List;
import java.util.Map;

import org.nutz.lang.Lang;
import org.nutz.lang.Times;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.WnPager;

@JvmHdlParamArgs("cqn")
public class voucher_list_promotion implements JvmHdl {

	@Override
	public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {

		WnQuery query = new WnQuery().setv("d0", "var").setv("d1", "voucher").setv("race", WnRace.DIR.toString());
		if (hc.params.has("match")) {
			NutMap match = Lang.map(hc.params.get("match"));
			for (Map.Entry<String, Object> en : match.entrySet()) {
				if ("voucher_startTime".equals(en.getKey())) {
					query.setv(en.getKey(), new NutMap("$gte", Times.ams(en.getValue().toString())));
				} else if ("voucher_endTime".equals(en.getKey())) {
					query.setv(en.getKey(), new NutMap("$lte", Times.ams(en.getValue().toString())));
				} else {
					query.setv(en.getKey(), en.getValue());
				}
			}
		}
		sys.nosecurity(() -> {
			WnObj wobj = sys.io.check(null, "/var/voucher/" + sys.me.name());
			query.setv("pid", wobj.id());
			WnPager pager = new WnPager(hc.params);
			pager.setupQuery(sys, query);
			List<WnObj> list = sys.io.query(query);
			// 强制输出列表
			hc.params.setv("l", true);
			Cmds.output_objs(sys, hc.params, pager, list, false);
		});
	}

}
