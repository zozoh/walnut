package org.nutz.walnut.ext.sys.hookx.hdl;

import java.util.LinkedList;
import java.util.List;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.sys.hookx.HookXContext;
import org.nutz.walnut.ext.sys.hookx.HookXFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.ZParams;

public class hookx_keys extends HookXFilter {

    @Override
    protected void process(WnSystem sys, HookXContext fc, ZParams params) {
        List<String> keys = new LinkedList<>();
        for (String val : params.vals) {
            String[] ss = Ws.splitIgnoreBlank(val);
            for (String s : ss) {
                keys.add(s);
            }
        }
        if (!keys.isEmpty() && null != fc.objs) {
            for (WnObj o : fc.objs) {
                o.put("__meta_keys", keys);
            }
        }
    }

}
