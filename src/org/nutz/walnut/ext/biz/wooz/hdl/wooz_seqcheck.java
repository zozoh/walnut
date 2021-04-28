package org.nutz.walnut.ext.biz.wooz.hdl;

import java.util.LinkedList;
import java.util.Map;

import org.nutz.lang.ContinueLoop;
import org.nutz.lang.Each;
import org.nutz.lang.ExitLoop;
import org.nutz.lang.LoopException;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;

@JvmHdlParamArgs(regex="clean")
public class wooz_seqcheck implements JvmHdl {

    @SuppressWarnings("unchecked")
    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        WnQuery query = new WnQuery();
        String compId = hc.params.val(0); // 赛事id
        WnObj comp = sys.io.checkById(compId);
        if (comp.containsKey("sp_comp_id")) {
            comp = sys.io.checkById(comp.getString("sp_comp_id"));
            compId = comp.id();
        }
        // 是否限定的赛项
        if (hc.params.vals.length > 1) {
            query.setv("u_pj", hc.params.val(1));
        }
        
        String path = "/home/" + comp.creator() + "/comp/data/" + comp.id() + "/signup";
        WnObj signup_dir = sys.io.check(null, path);

        query.setv("pid", signup_dir.id());
        
        
        NutMap map = new NutMap();
        sys.io.each(query, new Each<WnObj>() {
            public void invoke(int index, WnObj ele, int length) throws ExitLoop, ContinueLoop, LoopException {
                String u_code = ele.getString("u_code");
                if (u_code != null) {
                    map.addv2(u_code, ele);
                }
            }
        });
        boolean clean = hc.params.is("clean");
        NutMap metas = new NutMap("!u_code", "");
        
        for (Map.Entry<String, Object> en : map.entrySet()) {
            LinkedList<WnObj> values = (LinkedList<WnObj>) en.getValue();
            if (values.size() < 2)
                continue;
            String key = en.getKey();
            sys.out.printf("%-6s :", key);
            for (WnObj wobj : values) {
                sys.out.print(wobj.name() + ",");
                if (clean) {
                    sys.io.appendMeta(wobj, metas);
                }
            }
            sys.out.println();
        }
    }

}
