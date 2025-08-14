package com.site0.walnut.ext.net.mailx.impl;

import jakarta.mail.Message;

public class WnMailIMAPRecieving extends WnMailRecieving {

    /**
     * 邮件消息
     */
    public Message mail_msg;

    @Override
    protected Message getMailMessage() {
        return mail_msg;
    }

    @Override
    protected String dump_mail_msg() {
        if (null == this.mail_msg) {
            return "-nil-";
        }
        return this.mail_msg.toString();
    }

}
