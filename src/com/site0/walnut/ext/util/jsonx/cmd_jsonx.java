package com.site0.walnut.ext.util.jsonx;

import org.nutz.json.Json;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.impl.box.JvmFilterExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;

public class cmd_jsonx extends JvmFilterExecutor<JsonXContext, JsonXFilter> {

    public cmd_jsonx() {
        super(JsonXContext.class, JsonXFilter.class);
    }

    @Override
    protected JsonXContext newContext() {
        return new JsonXContext();
    }

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "cqn", "^(trim)$");
    }

    @Override
    protected void prepare(WnSystem sys, JsonXContext fc) {
        if (fc.params.vals.length > 0) {
            fc.input = fc.params.val(0);
        }
        // 来自标准输入
        else {
            fc.input = sys.in.readAll();
            if (fc.params.is("trim")) {
                fc.input = Ws.trim(fc.input);
            }
        }

        // 包裹为对象，这样输入为任意字符串都可以
        String wrapKey = fc.params.getString("wrap");
        if (!Ws.isBlank(wrapKey)) {
            fc.obj = Wlang.map(wrapKey, fc.input);
        }
        // 输入必然是 JSON
        else if (!Ws.isBlank(fc.input)) {
            if (Ws.isQuoteBy(fc.input, '[', ']')
                || Ws.isQuoteBy(fc.input, '{', '}')) {
                fc.obj = Json.fromJson(fc.input);
            }
        }

        // 确保上下文不为空
        if (null == fc.obj) {
            fc.obj = new NutMap();
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
