package org.nutz.walnut.impl.box.cmd;

import java.net.URLDecoder;

import org.nutz.json.Json;
import org.nutz.lang.Strings;
import org.nutz.lang.tmpl.Tmpl;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

public class cmd_httpparam extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        ZParams params = ZParams.parse(args, null);

        String str;

        // 文件里
        if (params.has("in")) {
            WnObj o = Wn.checkObj(sys.io, params.check("in"));
            str = sys.io.readText(o);
        }
        // 管道里
        else if (sys.pipeId > 0) {
            str = sys.in.readAll();
        }
        // 奇怪了
        else {
            throw Er.create("e.cmd.http_param.noinput");
        }

        // 解析
        String[] ss = Strings.splitIgnoreBlank(str, "&");
        NutMap c = new NutMap();

        for (String s : ss) {
            String des = URLDecoder.decode(s, "UTF-8");
            int pos = des.indexOf('=');
            if (pos > 0) {
                String key = des.substring(0, pos);
                String val = des.substring(pos + 1);
                c.setv(key, val);
            } else {
                c.setv(des, "");
            }
        }

        // 输出
        String out = params.get("out");
        if (!Strings.isBlank(out)) {
            sys.out.println(Tmpl.exec(out, c));
        }
        // 否则就全部输出一个 JSON
        else {
            sys.out.println(Json.toJson(c));
        }

    }

}
