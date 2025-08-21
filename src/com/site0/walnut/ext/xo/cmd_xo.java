package com.site0.walnut.ext.xo;

import com.site0.walnut.impl.box.JvmFilterExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

public class cmd_xo extends JvmFilterExecutor<XoContext, XoFilter> {

    public cmd_xo() {
        super(XoContext.class, XoFilter.class);
    }

    //private static final Log log = Wlog.getCMD();

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "cqn", "^(quiet|ajax|json)$");
    }

    @Override
    protected XoContext newContext() {
        return new XoContext();
    }

    @Override
    protected void prepare(WnSystem sys, XoContext fc) {
        // 读取配置文件
        String ph = fc.params.val(0, "default");
        if (!ph.endsWith(".json")) {
            ph += ".json";
        }
        // String aph = Wn.appendPath("~/.mailx", ph);
        // WnObj oConf = Wn.checkObj(sys, aph);
        // String json = sys.io.readText(oConf);
        // fc.config = Json.fromJson(MailxConfig.class, json);
        //
        // // 准备 Email 构造器
        // fc.mail = new WnSmtpMail();
        // fc.vars = new NutMap();
        //
        // // 更多上下文设置
        // if (fc.params.has("lang")) {
        // fc.lang = fc.params.getString("lang", fc.config.smtp.getLang());
        // }

    }

    @Override
    protected void output(WnSystem sys, XoContext fc) {
        if (fc.quiet) {
            return;
        }
    }

}
