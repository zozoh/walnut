package com.site0.walnut.ext.sys.lock;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import com.site0.walnut.impl.box.JvmFilterExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.ZParams;

public class cmd_lock extends JvmFilterExecutor<LockContext, LockFilter> {

    public cmd_lock() {
        super(LockContext.class, LockFilter.class);
    }

    @Override
    protected LockContext newContext() {
        return new LockContext();
    }

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "cqnl");
    }

    @Override
    protected void prepare(WnSystem sys, LockContext fc) {
        fc.api = sys.services.getLockApi();
    }

    @Override
    protected void output(WnSystem sys, LockContext fc) {
        if (fc.locks.isEmpty()) {
            if (fc.params.is("l")) {
                sys.out.println("[]");
            } else {
                sys.out.println("null");
            }
            return;
        }
        // 获取格式化
        JsonFormat jfmt = Cmds.gen_json_format(fc.params);
        // 一个对象
        if (fc.locks.size() == 1 && !fc.params.is("l")) {
            String json = Json.toJson(fc.locks.get(0), jfmt);
            sys.out.println(json);
        }
        // 多个对象
        else {
            String json = Json.toJson(fc.locks, jfmt);
            sys.out.println(json);
        }
    }

}
