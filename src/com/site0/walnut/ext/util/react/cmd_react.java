package com.site0.walnut.ext.util.react;

import org.nutz.json.Json;
import com.site0.walnut.impl.box.JvmFilterExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

public class cmd_react extends JvmFilterExecutor<ReactContext, ReactFilter> {

    public cmd_react() {
        super(ReactContext.class, ReactFilter.class);
    }

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "cqn", "^(quiet)$");
    }

    @Override
    protected ReactContext newContext() {
        return new ReactContext();
    }

    @Override
    protected void prepare(WnSystem sys, ReactContext fc) {}

    @Override
    protected void output(WnSystem sys, ReactContext fc) {
        if (!fc.params.is("quiet")) {
            String json = Json.toJson(fc.result, fc.jfmt);
            sys.out.println(json);
        }
    }

}
