package org.nutz.walnut.ext.sms;

import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
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

    public void exec(WnSystem sys, String[] args) throws Exception {
        SmsCtx sc = new SmsCtx();
        ZParams params = ZParams.parse(args, "^(debug)$");
        sc.debug = params.is("debug");
        sc.provider = params.get("provider", "Yunpian");
        sc.mobiles = params.get("r");
        sc.conf = params.get("config");
        if (params.vals.length == 0) {
            sys.err.println("need msg");
            return;
        }
        if (Strings.isBlank(sc.mobiles)) {
            sys.err.println("need mobiles!!");
            return;
        }
        sc.msg = params.vals[0];
        if(!sc.provider.equals("Yunpian")) {
            sys.err.println("当前仅支持云片网的SMS服务");
            return;
        }
        if (sc.conf == null) {
            sc.conf = Wn.normalizeFullPath("~/.sms/config_" + sc.provider, sys);
        }
        WnObj tmp = sys.io.check(null, sc.conf);
        NutMap conf = sys.io.readJson(tmp, NutMap.class);
        
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
