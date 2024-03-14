package com.site0.walnut.ext.util.jsonx;

import org.nutz.json.Json;
import org.nutz.json.JsonException;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.impl.box.JvmFilterExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Ws;

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

        if (!Ws.isBlank(json)) {
            // 错误字符串，打印到错误输出流
            if (json.startsWith("e.")) {
                sys.err.print(json);
                return;
            }
            try {
                ctx.obj = Json.fromJson(json);
            }
            catch (JsonException e) {
                throw Er.create("e.json.InvalidFormat", e.getMessage());
            }
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
