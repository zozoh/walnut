package org.nutz.walnut.ext.o;

import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmFilterExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

public class cmd_o extends JvmFilterExecutor<OContext, OFilter> {

    public cmd_o() {
        super(OContext.class, OFilter.class);
    }

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "cqn");
    }

    @Override
    protected OContext newContext() {
        return new OContext();
    }

    @Override
    protected void prepare(WnSystem sys, OContext fc) {
        for (String ph : fc.params.vals) {
            WnObj o = Wn.checkObj(sys, ph);
            fc.add(o);
        }
    }

    @Override
    protected void output(WnSystem sys, OContext fc) {
        if (!fc.alreadyOutputed) {
            Object reo = fc.list;
            // 木有翻页信息，看看是否需要自动拆包
            if (null == fc.pager) {
                if (fc.list.size() == 1) {
                    reo = fc.list.get(0);
                }
            }
            // 输出 Pager
            else {
                reo = Lang.map("list", fc.list).setv("pager", fc.pager);
            }

            // 输出
            String json = Json.toJson(reo, fc.jfmt);
            sys.out.print(json);
        }
    }

}
