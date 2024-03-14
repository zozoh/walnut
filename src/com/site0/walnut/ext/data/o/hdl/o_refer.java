package com.site0.walnut.ext.data.o.hdl;

import org.nutz.mapl.Mapl;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.data.o.OContext;
import com.site0.walnut.ext.data.o.OFilter;
import com.site0.walnut.ext.data.o.impl.WnReferLoader;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wobj;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;
import com.site0.walnut.util.each.WnEachIteratee;
import com.site0.walnut.util.validate.WnMatch;

public class o_refer extends OFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(id)$");
    }

    @Override
    protected void process(WnSystem sys, OContext fc, ZParams params) {
        String refKey = params.val(0);

        String getPath = params.getString("get", null);

        // 防守一下
        if (Ws.isBlank(refKey))
            return;

        // 准备加载器
        WnReferLoader loader = new WnReferLoader(sys.io);
        loader.setReferKey(refKey);
        loader.setStoreKey(params.val(1, "${key}_obj"));
        if (params.has("keys")) {
            String keys = params.getString("keys");
            WnMatch ma = Wobj.explainObjKeyMatcher(keys);
            loader.setKeyMatch(ma);
        } else {
            WnMatch ma = Wobj.explainObjKeyMatcher("%SHA1");
            loader.setKeyMatch(ma);
        }

        // 依次处理对象
        for (WnObj obj : fc.list) {
            // 对象的某个属性
            if (null != getPath) {
                Object val = Mapl.cell(obj, getPath);
                Wlang.each(val, new WnEachIteratee<WnObj>() {
                    public void invoke(int index, WnObj ele, Object src) {
                        loader.loadRefer(ele);
                    }
                });
            }
            // 直接针对上下文对象
            else {
                loader.loadRefer(obj);
            }
        }
    }

}
