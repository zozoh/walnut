package org.nutz.walnut.ext.o.hdl;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.o.OContext;
import org.nutz.walnut.ext.o.OFilter;
import org.nutz.walnut.ext.o.impl.WnReferLoader;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wobj;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.ZParams;
import org.nutz.walnut.validate.WnMatch;

public class o_refer extends OFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(id)$");
    }

    @Override
    protected void process(WnSystem sys, OContext fc, ZParams params) {
        String refKey = params.val(0);

        // 防守一下
        if (Ws.isBlank(refKey))
            return;

        // 准备加载器
        WnReferLoader loader = new WnReferLoader(sys.io);
        loader.setReferKey(refKey);
        loader.setStoreKey(params.val(1, refKey + "_obj"));
        loader.setAsId(params.is("id"));
        if (params.has("keys")) {
            String keys = params.getString("keys");
            WnMatch ma = Wobj.explainObjKeyMatcher(keys);
            loader.setKeyMatch(ma);
        } else {
            WnMatch ma = Wobj.explainObjKeyMatcher("%SHA1");
            loader.setKeyMatch(ma);
        }

        // 依次处理对象
        for (WnObj o : fc.list) {
            loader.loadRefer(o);
        }
    }

}