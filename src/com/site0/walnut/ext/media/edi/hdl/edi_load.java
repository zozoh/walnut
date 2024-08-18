package com.site0.walnut.ext.media.edi.hdl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.json.Json;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.media.edi.EdiContext;
import com.site0.walnut.ext.media.edi.EdiFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;
import com.site0.walnut.util.tmpl.WnTmplX;

public class edi_load extends EdiFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, null);
    }

    @Override
    protected void process(WnSystem sys, EdiContext fc, ZParams params) {
        boolean _loaded_stdin = false;
        String str;

        // 获取报文文件路径
        String path = params.val(0);

        // 从标准输入读取内容
        if (Ws.isBlank(path)) {
            _loaded_stdin = true;
            str = sys.in.readAll();
        }
        // 从文件对象读取
        else {
            WnObj obj = Wn.checkObj(sys, path);
            str = sys.io.readText(obj);
        }

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
                if (_loaded_stdin) {
                    throw Er.create("e.cmd.edi.load.StdInUsed");
                }
                String input = sys.in.readAll();
                vars = Json.fromJson(NutMap.class, input);
            }
            //
            // 渲染
            WnTmplX tmpl = WnTmplX.parse(str);
            str = tmpl.render(vars);
        }

        // 计入原始输入
        fc.raw_input = str;

        // 寻找分隔符 :-------------------------
        // 分隔符后面的就是报文的原始渲染上下文，即一个 JSON
        Pattern p = Pattern.compile(":-{5,}\r?\n");
        Matcher m = p.matcher(str);
        if (m.find()) {
            int pos_begin = m.start();
            int pos_end = m.end();
            fc.message = str.substring(0, pos_begin).trim();
            fc.vars = str.substring(pos_end).trim();
        }
        // 计入到上下文
        else {
            fc.message = str;
        }

    }

}
