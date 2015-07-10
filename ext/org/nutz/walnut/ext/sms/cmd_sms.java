package org.nutz.walnut.ext.sms;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.ext.sms.provider.YunPianSmsProvider;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;

/**
 * 发送短信
 * 
 * @author wendal
 *
 */
public class cmd_sms extends JvmExecutor {

    public void exec(WnSystem sys, String[] args) throws Exception {
        String msg = null;
        SmsCtx sc = new SmsCtx();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            switch (arg) {
            case "-config":
                sc.conf = args[++i];
                break;
            case "-provider":
                sc.provider = args[++i];
                break;
            case "-r":
            case "-to":
                sc.mobiles.add(args[++i]);
                break;
            case "-debug":
                sc.debug = true;
            default:
                msg = arg;
                break;
            }
        }
        if (msg == null) {
            sys.err.println("need msg");
            return;
        }
        if (sc.provider == null) {
            sc.provider = "Yunpian";
        }else if(!sc.provider.equals("Yunpian")) {
            sys.err.println("当前仅支持云片网的SMS服务");
            return;
        }
        if (sc.conf == null) {
            sc.conf = userHome(sys.me) + "/.sms/config_" + sc.provider;
        }
        WnObj tmp = sys.io.check(null, sc.conf);
        NutMap conf = sys.io.readJson(tmp, NutMap.class);
        
        // TODO 适应各种提供商
        SmsProvider provider = new YunPianSmsProvider();
        for (String mobile : sc.mobiles) {
            String re = provider.send(conf, msg, mobile);
            if (sc.debug) {
                sys.out.printf("%s %s\n", mobile, re);
            }
        }
    }
    
    public String userHome(WnUsr u) {
        return "root".equals(u.name()) ? "/root" : "/home/" + u.name();
    }
}
