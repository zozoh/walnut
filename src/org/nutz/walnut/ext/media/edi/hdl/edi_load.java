package org.nutz.walnut.ext.media.edi.hdl;

import org.nutz.json.Json;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.media.edi.EdiContext;
import org.nutz.walnut.ext.media.edi.EdiFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.ZParams;
import org.nutz.walnut.util.tmpl.WnTmplX;

public class edi_load extends EdiFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, null);
    }

    @Override
    protected void process(WnSystem sys, EdiContext fc, ZParams params) {
        // 获取报文文件路径
        String path = params.val_check(0);

        // 读取内容
        WnObj obj = Wn.checkObj(sys, path);
        String str = sys.io.readText(obj);

        // 是否需要渲染呢？
        if (params.has("render")) {
            String vs = params.getString("render");
            // 准备变量
            NutMap vars;
            // 本身就是 JSON
            if (Ws.isQuoteBy(vs, '{', '}')) {
                vars = Json.fromJson(NutMap.class, vs);
            }
            // 读取变量文件
            else if (!Ws.isBlank(vs)) {
                WnObj oVars = Wn.checkObj(sys, vs);
                vars = sys.io.readJson(oVars, NutMap.class);
            }
            // 从管道读取
            else {
                String input = sys.in.readAll();
                vars = Json.fromJson(NutMap.class, input);
            }
            //
            // 渲染
            WnTmplX tmpl = WnTmplX.parse(str);
            str = tmpl.render(vars);
        }

        // 计入到上下文
        fc.message = str;
    }

}