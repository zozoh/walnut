package org.nutz.walnut.ext.net.mailx;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.net.mailx.bean.MailxConfig;
import org.nutz.walnut.ext.net.mailx.bean.MailxSmtpConfig;
import org.nutz.walnut.impl.box.JvmFilterExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;

public class cmd_mailx extends JvmFilterExecutor<MailxContext, MailxFilter> {

    public cmd_mailx() {
        super(MailxContext.class, MailxFilter.class);
    }

    @Override
    protected MailxContext newContext() {
        return new MailxContext();
    }

    @Override
    protected void prepare(WnSystem sys, MailxContext fc) {
        // 读取配置文件
        String ph = fc.params.val(0, "default");
        if (!ph.endsWith(".json")) {
            ph += ".json";
        }
        String aph = Wn.appendPath("~/.mailx", ph);
        WnObj oConf = Wn.checkObj(sys, aph);
        String json = sys.io.readText(oConf);
        fc.config = Json.fromJson(MailxConfig.class, json);

        // 准备 Email 构造器
        fc.builder = EmailBuilder.startingBlank().from(fc.config.smtp.getAccount());
    }

    @Override
    protected void output(WnSystem sys, MailxContext fc) {
        sys.out.println(Json.toJson(fc.config, JsonFormat.nice()));
        MailxSmtpConfig smtp = fc.config.smtp;
        Mailer mailer = MailerBuilder.withSMTPServer(smtp.getHost(),
                                                     smtp.getPort(),
                                                     smtp.getAccount(),
                                                     smtp.getPassword())
                                     .withTransportStrategy(smtp.getStrategy())
                                     .buildMailer();
        Email mail = fc.builder.buildEmail();
        mailer.sendMail(mail);
    }

}
