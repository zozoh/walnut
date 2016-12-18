package org.nutz.walnut.ext.sms;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
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

        // 默认配置文件
        if (sc.conf == null) {
            sc.conf = "~/.sms/config_" + sc.provider;
        }

        WnObj oConf = Wn.checkObj(sys, sc.conf);
        NutMap conf = sys.io.readJson(oConf, NutMap.class);

        // 强制设置header，覆盖默认配置中的
        if (!Strings.isBlank(sc.header)) {
            conf.setv("header", sc.header);
        }

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
