package com.site0.walnut.ext.net.mailx.impl;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Properties;

import com.site0.walnut.api.err.Er;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

public class WnMailRawRecieving extends WnMailRecieving {

    public WnMailRawRecieving() {
        Properties props = new Properties();
        this.session = Session.getDefaultInstance(props, null);
    }

    /**
     * 邮件消息
     */
    public String mimeText;

    public Date recieveDate;

    private MimeMessage _msg;

    @Override
    protected Message getMailMessage() {
        // 防空
        if (null == this.mimeText) {
            return null;
        }

        if (null == _msg) {
            // 将 MIME 文本转换为输入流
            ByteArrayInputStream inputStream = new ByteArrayInputStream(mimeText
                .getBytes(StandardCharsets.UTF_8));

            try {
                _msg = new MimeMessage(session, inputStream) {
                    public Date getReceivedDate() throws MessagingException {
                        Date d = super.getReceivedDate();
                        if (null == d) {
                            d = recieveDate;
                        }
                        if (null == d) {
                            d = new Date();
                        }
                        return d;
                    }
                };
            }
            catch (MessagingException e) {
                throw Er.create(e, "Fail to build MimeMessage, mimeText=%s", mimeText);
            }
        }
        return _msg;
    }

    @Override
    protected String dump_mail_msg() {
        return mimeText;
    }

}
