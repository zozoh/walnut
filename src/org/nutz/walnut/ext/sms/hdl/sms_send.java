package org.nutz.walnut.ext.sms.hdl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.tmpl.Tmpl;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.sms.SmsProvider;
import org.nutz.walnut.ext.sms.SmsSend;
import org.nutz.walnut.ext.sms.cmd_sms;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

@JvmHdlParamArgs("^debug$")
public class sms_send implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws IOException {
        ZParams params = hc.params;
        // boolean isDebug = params.is("debug");
        String mobiles = params.get("r");
        String header = params.has("header") ? params.get("header") : "";
        String lang = params.get("lang");
        // ............................................
        NutMap vars = hc.attrs().getAs(cmd_sms.KEY_VARS, NutMap.class);
        // ............................................
        // 检查一下参数
        if (Strings.isBlank(mobiles)) {
            throw Er.create("e.cmd.sms.nophone");
        }
        // ............................................
        // 得到配置信息
        WnObj oSmsHome = hc.oRefer;
        NutMap conf = hc.attrs().getAs(cmd_sms.KEY_CONFIG, NutMap.class);
        // ............................................
        // 强制设置header，覆盖默认配置中的
        if (!Strings.isBlank(header)) {
            conf.setv("header", header);
        }
        // ............................................
        // 确定语言
        if (Strings.isBlank(lang)) {
            lang = conf.getString("lang", "zh-cn");
        }
        // ............................................
        // 分析消息: 从参数里读取
        String msg = null;
        if (params.vals.length > 0) {
            msg = Lang.concat(" ", params.vals).toString();
        }
        // 从管道里读取
        else if (null != sys.in) {
            msg = sys.in.readAll();
        }

        if (Strings.isBlank(msg)) {
            throw Er.create("e.cmd.sms.nomsg");
        }
        // ............................................
        // 处理模板消息
        if (params.has("t")) {
            String tmpl = params.get("t");
            WnObj oTmpl;
            // 多国语言
            if (tmpl.startsWith("i18n:")) {
                String tmplKey = Strings.trim(tmpl.substring("i18n:".length()));
                oTmpl = sys.io.check(oSmsHome, "i18n/" + lang + "/" + tmplKey);
            }
            // 普通模板
            else {
                oTmpl = sys.io.check(oSmsHome, tmpl);
            }
            // 渲染消息
            String str = sys.io.readText(oTmpl);
            NutMap map = Lang.map(msg);
            msg = Tmpl.exec(str, map, false);
        }
        // ............................................
        // 检查header是否带有前后缀
        String hstr = conf.getString("header");
        if (!hstr.startsWith("【")) {
            hstr = "【" + hstr;
        }
        if (!hstr.endsWith("】")) {
            hstr = hstr + "】";
        }
        conf.setv("header", hstr);
        // ............................................
        // 执行发送
        SmsProvider provider = hc.attrs().getAs(cmd_sms.KEY_PROVIDER, SmsProvider.class);
        SmsSend s = new SmsSend();
        s.vars = vars;
        s.message = msg;
        String[] tmp = Strings.splitIgnoreBlank(mobiles, ",");
        Map<String, Object> res = new HashMap<>();
        for (String mobile : tmp) {
            s.receiver = mobile;
            String re = provider.send(conf, s);
            if (re == null)
                res.put(mobile, null);
            try {
                res.put(mobile, Json.fromJson(re));
            }
            catch (Throwable e) {
                res.put(mobile, re);
            }
        }
        sys.out.writeJson(res, JsonFormat.full());
    }

}
