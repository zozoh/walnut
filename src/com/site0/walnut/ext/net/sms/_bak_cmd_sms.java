package com.site0.walnut.ext.net.sms;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import com.site0.walnut.util.tmpl.WnTmpl;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.net.sms.provider.YunPianSmsProvider;
import com.site0.walnut.impl.box.JvmExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.ZParams;

/**
 * 发送短信
 * 
 * @author wendal
 *
 */
public class _bak_cmd_sms extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        SmsCtx sc = new SmsCtx();
        // ............................................
        // 分析参数
        ZParams params = ZParams.parse(args, "^(debug)$");
        sc.debug = params.is("debug");
        sc.provider = params.get("provider", "Yunpian");
        sc.mobiles = params.get("r");
        sc.header = params.has("header") ? params.get("header") : "";
        sc.conf = params.get("config");
        sc.lang = params.get("lang");
        // ............................................
        NutMap vars;
        if (params.has("vars")) {
            vars = params.getMap("vars");
        } else {
            vars = new NutMap();
        }
        // ............................................
        // 检查一下参数
        if (Strings.isBlank(sc.mobiles)) {
            throw Er.create("e.cmd.sms.nophone");
        }
        if (!sc.provider.equals("Yunpian")) {
            throw Er.create("e.cmd.sms.provider.unsupport", sc.provider);
        }
        // ............................................
        // 得到配置主目录
        WnObj oSmsHome = Wn.checkObj(sys, "~/.sms");
        // 默认配置文件
        WnObj oConf = sys.io.check(oSmsHome, Strings.sBlank(sc.conf, "config_" + sc.provider));
        NutMap conf = sys.io.readJson(oConf, NutMap.class);
        // ............................................
        // 强制设置header，覆盖默认配置中的
        if (!Strings.isBlank(sc.header)) {
            conf.setv("header", sc.header);
        }
        // ............................................
        // 确定语言
        if (Strings.isBlank(sc.lang)) {
            sc.lang = conf.getString("lang", "zh-cn");
        }
        // ............................................
        // 分析消息: 从参数里读取
        if (params.vals.length > 0) {
            sc.msg = Lang.concat(" ", params.vals).toString();
        }
        // 从管道里读取
        else if (null != sys.in) {
            sc.msg = sys.in.readAll();
        }

        if (Strings.isBlank(sc.msg)) {
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
                oTmpl = sys.io.check(oSmsHome, "i18n/" + sc.lang + "/" + tmplKey);
            }
            // 普通模板
            else {
                oTmpl = sys.io.check(oSmsHome, tmpl);
            }
            // 渲染消息
            String str = sys.io.readText(oTmpl);
            NutMap map = Lang.map(sc.msg);
            sc.msg = WnTmpl.exec(str, map, false);
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
        // TODO 适应各种提供商
        SmsProvider provider = new YunPianSmsProvider();
        SmsSend s = new SmsSend();
        s.vars = vars;
        s.message = sc.msg;
        for (String mobile : Strings.splitIgnoreBlank(sc.mobiles, ",")) {
            s.receiver = mobile;
            String re = provider.send(conf, s);
            if (sc.debug) {
                sys.out.printf("%s %s\n", mobile, re);
            }
        }
    }
}
