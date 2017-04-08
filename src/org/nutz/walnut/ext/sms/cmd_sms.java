package org.nutz.walnut.ext.sms;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.tmpl.Tmpl;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.sms.provider.YunPianSmsProvider;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

/**
 * 发送短信
 * 
 * @author wendal
 *
 */
public class cmd_sms extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        SmsCtx sc = new SmsCtx();
        ZParams params = ZParams.parse(args, "^(debug)$");
        sc.debug = params.is("debug");
        sc.provider = params.get("provider", "Yunpian");
        sc.mobiles = params.get("r");
        sc.header = params.has("header") ? params.get("header") : "";
        sc.conf = params.get("config");

        if (Strings.isBlank(sc.mobiles)) {
            throw Er.create("e.cmd.sms.nophone");
        }

        if (!sc.provider.equals("Yunpian")) {
            throw Er.create("e.cmd.sms.provider.unsupport", sc.provider);
        }

        // 得到配置主目录
        WnObj oSmsHome = Wn.checkObj(sys, "~/.sms");

        // 从参数里读取
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

        // 看看是否是模板消息
        if (params.has("t")) {
            WnObj oTmpl = sys.io.check(oSmsHome, params.get("t"));
            String str = sys.io.readText(oTmpl);
            NutMap map = Lang.map(sc.msg);
            sc.msg = Tmpl.exec(str, map, false);
        }

        // 默认配置文件
        WnObj oConf = sys.io.check(oSmsHome, Strings.sBlank(sc.conf, "config_" + sc.provider));
        NutMap conf = sys.io.readJson(oConf, NutMap.class);

        // 强制设置header，覆盖默认配置中的
        if (!Strings.isBlank(sc.header)) {
            conf.setv("header", sc.header);
        }

        // 检查header是否带有前后缀
        String hstr = conf.getString("header");
        if (!hstr.startsWith("【")) {
            hstr = "【" + hstr;
        }
        if (!hstr.endsWith("】")) {
            hstr = hstr + "】";
        }
        conf.setv("header", hstr);

        // TODO 适应各种提供商
        SmsProvider provider = new YunPianSmsProvider();
        for (String mobile : Strings.splitIgnoreBlank(sc.mobiles, ",")) {
            String re = provider.send(conf, sc.msg, mobile);
            if (sc.debug) {
                sys.out.printf("%s %s\n", mobile, re);
            }
        }
    }
}
