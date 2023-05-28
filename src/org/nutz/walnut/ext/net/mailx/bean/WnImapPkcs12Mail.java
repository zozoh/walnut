package org.nutz.walnut.ext.net.mailx.bean;

import java.io.IOException;
import java.util.ArrayList;

import org.nutz.walnut.api.err.Er;
import org.simplejavamail.utils.mail.smime.SmimeKey;
import org.simplejavamail.utils.mail.smime.SmimeUtil;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;

public class WnImapPkcs12Mail extends WnImapMail {

    private Session session;
    private SmimeKey smimeKey;

    public WnImapPkcs12Mail(Session session, SmimeKey smimeKey) {
        this.session = session;
        this.smimeKey = smimeKey;
    }

    protected void loadBody(Message msg, String asContent) {
        MimeMessage mmsg = (MimeMessage) msg;
        // 首先解密
        MimeMessage d_mmsg = SmimeUtil.decrypt(session, mmsg, smimeKey);

        try {
            MimeBodyPart body = SmimeUtil.getSignedContent(d_mmsg);
            this.bodyParts = new ArrayList<>(1);
            WnMailPart mp = new WnMailPart(body, asContent);

            this.bodyParts.add(mp);
        }
        catch (MessagingException | IOException e) {
            throw Er.wrap(e);
        }

    }
}
