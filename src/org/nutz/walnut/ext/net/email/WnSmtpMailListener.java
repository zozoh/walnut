package org.nutz.walnut.ext.net.email;

import java.io.InputStream;
import java.util.Properties;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.apache.commons.mail.util.MimeMessageParser;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.random.R;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.util.WnRun;
import org.subethamail.smtp.helper.SimpleMessageListener;

@IocBean
public class WnSmtpMailListener implements SimpleMessageListener {
    
    private static final Log log = Logs.get();
    
    @Inject
    protected WnRun wnRun;

    public boolean accept(String from, String recipient) {
        return true;
    }

    public void deliver(String from, String recipient, InputStream data) {
        try {
            log.debugf("from <%s> to <%s>", from, recipient);
            
            WnObj wobj = wnRun.io().createIfNoExists(null, "/sys/mail/income/" + R.UU32(), WnRace.FILE);
            wnRun.io().writeAndClose(wobj, data);

            NutMap meta = new NutMap();
            meta.put("mail-from", from);
            meta.put("mail-recipient", recipient);
            try (InputStream ins = wnRun.io().getInputStream(wobj, 0)) {
                MimeMessage msg = new MimeMessage(getSession(), ins);
                //log.debug("msg subject = " + msg.getSubject());
                //log.debug("msg content-type = " + msg.getContentType());
                //log.debug("msg class = " + msg.getClass().getName());
                meta.put("mail-subject", msg.getSubject());
                meta.put("msg-content-type", msg.getContentType());
                MimeMessageParser parser = new MimeMessageParser(msg);
                parser.parse();
                if (parser.hasHtmlContent() && log.isDebugEnabled()) {
                    log.debugf("mail html content=%s", parser.getHtmlContent());
                }
                if (parser.hasPlainContent() && log.isDebugEnabled()) {
                    log.debugf("mail plain content=%s", parser.getPlainContent());
                }
            }
            finally {
                wnRun.io().appendMeta(wobj, meta);
            }
        }
        catch (Exception e) {
            log.warn("rec mail fail", e);
        }
    }

    /**
     * Creates the JavaMail Session object for use in WiserMessage
     */
    protected Session getSession()
    {
        return Session.getDefaultInstance(new Properties());
    }
}