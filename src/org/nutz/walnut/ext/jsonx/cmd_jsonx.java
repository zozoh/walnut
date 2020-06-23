package org.nutz.walnut.ext.jsonx;

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
        String json = sys.in.readAll();
        if (!Strings.isBlank(json)) {
            ctx.obj = Json.fromJson(json);
        }
    }

    @Override
    protected void output(WnSystem sys, JsonXContext ctx) {
        String output = Json.toJson(ctx.obj, ctx.jfmt);
        sys.out.println(output);
    }

}
