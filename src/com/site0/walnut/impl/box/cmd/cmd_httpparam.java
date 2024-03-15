package com.site0.walnut.impl.box.cmd;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import com.site0.walnut.util.Wlang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.impl.box.JvmExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.WnStr;
import com.site0.walnut.util.ZParams;

public class cmd_httpparam extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        ZParams params = ZParams.parse(args, "cqn");

        String str;
        WnObj oReq = null;
        // .......................................................
        // 读取内容
        // 文件里
        if (params.has("in")) {
            oReq = Wn.checkObj(sys, params.check("in"));
            str = sys.io.readText(oReq);
        }
        // 管道里
        else if (sys.pipeId > 0) {
            str = sys.in.readAll();
        }
        // 奇怪了
        else {
            throw Er.create("e.cmd.http_param.noinput");
        }
        // .......................................................
        // 解析
        NutMap c;
        // 按照 JSON 方式解析
        if (null != str && Strings.isQuoteBy(str, '{', '}')) {
            c = Json.fromJson(NutMap.class, str);
        }
        // 按照 form 表单方式解析
        else {
            c = WnStr.parseFormData(str);
        }
        // .......................................................
        // 如果是请求文件，那么也把 GET 参数也回复一下
        if (oReq != null) {
            for (String key : oReq.keySet()) {
                if (key.startsWith("http-qs-")) {
                    String val = oReq.getString(key);
                    c.setv(key.substring("http-qs-".length()), val);
                }
            }
        }
        // .......................................................
        // 将指定的参数根据分隔符拆分成数组
        if (params.has("varray")) {
            NutMap mapping = Wlang.map(params.getString("varray"));
            for (String nm : mapping.keySet()) {
                String sep = mapping.getString(nm);
                String val = c.getString(nm);
                String[] v2 = Strings.splitIgnoreBlank(val, sep);
                c.put(nm, v2);
            }
        }
        // .......................................................
        // 将指定的参数变成整数
        if (params.has("vint")) {
            String[] nms = Strings.splitIgnoreBlank(params.getString("vint"));
            for (String nm : nms) {
                int v2 = c.getInt(nm);
                c.put(nm, v2);
            }
        }
        // .......................................................
        // 将指定的参数变成布尔
        if (params.has("vbool")) {
            String[] nms = Strings.splitIgnoreBlank(params.getString("vbool"));
            for (String nm : nms) {
                boolean v2 = c.getBoolean(nm);
                c.put(nm, v2);
            }
        }
        // .......................................................
        // 将指定的参数变成浮点数
        if (params.has("vfloat")) {
            String[] nms = Strings.splitIgnoreBlank(params.getString("vfloat"));
            for (String nm : nms) {
                float v2 = c.getFloat(nm);
                c.put(nm, v2);
            }
        }
        // .......................................................
        // 收缩参数到一个 Map
        if (params.has("map")) {
            String mapKey = params.get("map");
            if ("true".equals(mapKey))
                mapKey = "params";
            c = Wlang.map(mapKey, c);
        }

        // .......................................................
        // 输出 @{xxx} 作为参数模板
        String out = params.get("out");
        if (!Strings.isBlank(out)) {
            sys.out.println(Cmds.out_by_tmpl(out, c));
        }
        // 否则就全部输出一个 JSON
        else {
            JsonFormat jfmt = Cmds.gen_json_format(params);
            sys.out.println(Json.toJson(c, jfmt));
        }

    }

}
