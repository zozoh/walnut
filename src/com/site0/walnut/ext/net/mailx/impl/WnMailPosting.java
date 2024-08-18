package com.site0.walnut.ext.net.mailx.impl;

import java.io.InputStream;
import java.util.List;

import org.nutz.lang.Streams;
import org.nutz.lang.util.NutBean;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.net.mailx.bean.MailxSmtpConfig;
import com.site0.walnut.ext.net.mailx.bean.WnMailAttachment;
import com.site0.walnut.ext.net.mailx.bean.WnSmtpMail;
import com.site0.walnut.ext.net.mailx.util.Mailx;
import com.site0.walnut.ext.net.mailx.bean.WnMailSecurity;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.email.EmailPopulatingBuilder;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.api.mailer.config.Pkcs12Config;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;

public class WnMailPosting {

    private WnIo io;

    private NutBean vars;

    public WnMailPosting(WnSystem sys) {
        this(sys.io, sys.session.getVars());

    }

    public WnMailPosting(WnIo io, NutBean sessionVars) {
        this.io = io;
        this.vars = sessionVars;
    }

    public void send(MailxSmtpConfig smtp, WnSmtpMail mail) {
        // 小防守一把
        if (!smtp.hasAccount()) {
            throw Er.create("e.mailx.smtp.WithoutAccount");
        }
        if (!mail.hasTo()) {
            throw Er.create("e.mailx.smtp.WithoutToAddress");
        }
        //
        // 准备构造邮件
        EmailPopulatingBuilder builder;

        //
        // 设置发件人
        //
        if (smtp.hasAlias()) {
            builder = EmailBuilder.startingBlank().from(smtp.getAccount());
        }
        // 仅有地址
        else {
            builder = EmailBuilder.startingBlank().from(smtp.getAccount());
        }

        // to/cc/bcc
        builder.to(mail.getTo().trim());
        if (mail.hasCc()) {
            builder.cc(mail.getCc().trim());
        }
        if (mail.hasBcc()) {
            builder.bcc(mail.getBcc().trim());
        }

        // subject
        builder.withSubject(mail.getSubject());

        // content
        if (mail.hasContent()) {
            if (mail.isAsHtml()) {
                builder.withHTMLText(mail.getContent());
            } else {
                builder.withPlainText(mail.getContent());
            }
        }

        // attachment
        if (mail.hasAttachments()) {
            List<String> atPaths = mail.getAttachmentPaths();

            // 根据路径读取
            if (null != atPaths) {
                for (String atPath : atPaths) {
                    String aph = Wn.normalizeFullPath(atPath, vars);
                    WnObj ato = io.check(null, aph);
                    String name = ato.name();
                    String mime = ato.mime();
                    byte[] bs = io.readBytes(ato);
                    builder.withAttachment(name, bs, mime);
                }
            }

            // 设置的固定
            List<WnMailAttachment> ats = mail.getAttachment();
            if (null != ats) {
                for (WnMailAttachment at : ats) {
                    String name = at.getName();
                    String mime = at.getMime();
                    byte[] bs = at.getContent();
                    builder.withAttachment(name, bs, mime);
                }
            }
        }

        // 固定 attachment
        if (mail.hasAttachments()) {

        }

        // security
        if (mail.hasSecurity()) {
            WnMailSecurity secu = mail.getSecurity();
            // SMIMI
            if (secu.isSMIME()) {
                // 签名设置
                if (secu.hasSign()) {
                    Pkcs12Config pkcs12 = Mailx.createPkcs12Config(io, vars, secu);
                    builder.signWithSmime(pkcs12);
                }

                // 证书路径
                if (secu.hasEncryptCertFile()) {
                    String ecf = secu.getEncryptCertFile();
                    WnObj oCert = Wn.checkObj(io, vars, ecf);
                    InputStream ins = io.getInputStream(oCert, 0);
                    try {
                        builder.encryptWithSmime(ins);
                    }
                    finally {
                        Streams.safeClose(ins);
                    }
                }
            }
            // 报警吧
            else {
                throw Er.create("e.mailx.security.UnknownType", secu.getType());
            }
        }

        //
        // 搞定，发送
        //
        Mailer mailer = MailerBuilder.withSMTPServer(smtp.getHost(),
                                                     smtp.getPort(),
                                                     smtp.getAccount(),
                                                     smtp.getPassword())
                                     .withTransportStrategy(smtp.getStrategy())
                                     .buildMailer();
        Email mo = builder.buildEmail();
        mailer.sendMail(mo);
    }

}
