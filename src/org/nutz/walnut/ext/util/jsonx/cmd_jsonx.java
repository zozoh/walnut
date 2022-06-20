package org.nutz.walnut.ext.util.jsonx;

import org.nutz.json.Json;
import org.nutz.lang.Strings;
import org.nutz.walnut.impl.box.JvmFilterExecutor;
import org.nutz.walnut.impl.box.WnSystem;

public class cmd_jsonx extends JvmFilterExecutor<JsonXContext, JsonXFilter> {

    public cmd_jsonx() {
        super(JsonXContext.class, JsonXFilter.class);
    }

    @Override
    protected JsonXContext newContext() {
        return new JsonXContext();
    }

    @Override
    protected void prepare(WnSystem sys, JsonXContext ctx) {
        String json;
        if (ctx.params.vals.length > 0) {
            json = ctx.params.val(0);
        }
        // 来自标准输入
        else {
            json = sys.in.readAll();
        }

        if (!Strings.isBlank(json)) {
            ctx.obj = Json.fromJson(json);
        }
    }

    @Override
    protected void output(WnSystem sys, JsonXContext ctx) {
        if (!ctx.quite) {
            String output = Json.toJson(ctx.obj, ctx.jfmt);
            sys.out.println(output);
        }
    }

}
