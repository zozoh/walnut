package com.site0.walnut.ext.util.jsonx;

import org.nutz.json.Json;
import org.nutz.json.JsonException;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.impl.box.JvmFilterExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wlang;
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
    protected void prepare(WnSystem sys, JsonXContext fc) {
        String input;
        if (fc.params.vals.length > 0) {
            input = fc.params.val(0);
        }
        // 来自标准输入
        else {
            input = sys.in.readAll();
        }

        // 包裹为对象，这样输入为任意字符串都可以
        String wrapKey = fc.params.getString("wrap");
        if (!Ws.isBlank(wrapKey)) {
            fc.obj = Wlang.map(wrapKey, input);
        }
        // 输入必然是 JSON
        else if (!Ws.isBlank(input)) {
            // 错误字符串，打印到错误输出流
            if (input.startsWith("e.")) {
                sys.err.print(input);
                return;
            }
            try {
                fc.obj = Json.fromJson(input);
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
