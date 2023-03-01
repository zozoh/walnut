package org.nutz.walnut.ext.net.email;

import java.util.List;

/**
 * 邮件实体
 * 
 * @author pw
 *
 */
public class MailEntity {

    public String subject;

    public String msg;

    public String htmlMsg;

    public List<MailAttachment> attachments;

}
