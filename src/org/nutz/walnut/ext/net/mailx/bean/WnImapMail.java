package org.nutz.walnut.ext.net.mailx.bean;

import java.util.Enumeration;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.util.Ws;

import jakarta.mail.Header;
import jakarta.mail.Message;
import jakarta.mail.Message.RecipientType;

public class WnImapMail extends WnMail {

    private NutMap headers;

    public WnImapMail() {
        headers = new NutMap();
    }

    public WnImapMail(Message msg) {
        this();
        this.fromMessage(msg);
    }

    public void fromMessage(Message msg) {
        if (null == msg)
            return;

        try {
            // 读取头
            Enumeration<Header> hs = msg.getAllHeaders();
            while (hs.hasMoreElements()) {
                Header h = hs.nextElement();
                headers.addv(h.getName(), h.getValue());
            }

            // 设置其他信息
            this.subject = msg.getSubject();
            this.to = Ws.join(msg.getRecipients(RecipientType.TO), ",");
            this.cc = Ws.join(msg.getRecipients(RecipientType.CC), ",");
            this.bcc = Ws.join(msg.getRecipients(RecipientType.BCC), ",");

            this.charset = "UTF-8";

            // if (mail.hasContent()) {
            // this.asHtml = msg.i
            // this.content = mail.content;
            // }
        }
        catch (Exception e) {
            throw Er.wrap(e);
        }
    }

    @Override
    protected String dumpHeaders() {
        return Json.toJson(headers, JsonFormat.nice());
    }

    @Override
    public boolean hasAttachments() {
        return false;
    }

    @Override
    protected String dumpAttachments() {
        return null;
    }

}
