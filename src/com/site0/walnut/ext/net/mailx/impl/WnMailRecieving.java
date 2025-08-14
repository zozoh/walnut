package com.site0.walnut.ext.net.mailx.impl;

import static com.site0.walnut.ext.net.mailx.util.Mailx.LOG;

import java.io.ByteArrayInputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.nutz.json.Json;
import org.nutz.lang.Files;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.simplejavamail.api.mailer.config.Pkcs12Config;
import org.simplejavamail.utils.mail.smime.SmimeKey;
import org.simplejavamail.utils.mail.smime.SmimeKeyStore;

import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.ext.net.mailx.MailxContext;
import com.site0.walnut.ext.net.mailx.bean.WnMailPart;
import com.site0.walnut.ext.net.mailx.bean.WnMailSecurity;
import com.site0.walnut.ext.net.mailx.bean.WnMimeMail;
import com.site0.walnut.ext.net.mailx.bean.WnMimePkcs12Mail;
import com.site0.walnut.ext.net.mailx.util.Mailx;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.tmpl.WnTmplX;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;

public abstract class WnMailRecieving implements Runnable {

    private static final Log log = Wlog.getCMD();

    private static final String HR = Ws.repeat('#', 80);

    public boolean isAutoDecrypt;

    public WnSystem sys;

    public MailxContext fc;

    public boolean showHeader;

    public boolean debug;

    public int i;

    public int N;

    public WnTmplX taTmpl;

    public NutMap fixedMeta;

    public WnTmplX attachmentTmpl;

    public String after;

    public String asContent;

    public Vector<WnObj> outputs;

    public Session session;

    abstract protected Message getMailMessage();

    abstract protected String dump_mail_msg();

    @Override
    public void run() {
        try {
            run_with_error();
        }
        catch (MessagingException e) {
            String str = String.format("Error raised MailRecieving: i=%s, N=%s, msg=%s",
                                       i,
                                       N,
                                       dump_mail_msg());
            log.warn(str, e);
        }
    }

    protected void run_with_error() throws MessagingException {
        // 获取邮件消息对象
        Message msg = this.getMailMessage();
        
        // 解析为标准邮件对象
        WnMimeMail mail = buildMail(msg);
        
        // 输出到数据集
        if (null != taTmpl) {
            WnObj oMail = create_mail_obj(mail);
            // 计入结果
            outputs.add(oMail);
        }
        // 仅仅打印信息
        else {
            LOG(sys,
                debug,
                "%s\n# %d/%d) <%s>: %s\n%s\n%s",
                HR,
                i,
                N,
                mail.getSender(),
                mail.getSubject(),
                HR,
                mail.dumpString(showHeader));
        }
    }

    protected WnMimeMail buildMail(Message msg) throws MessagingException {
        WnMimeMail mail;

        // 获取邮件内容类型
        String contentType = Mailx.evalContentType(msg.getContentType(), null);

        // 加密邮件尝试解密
        if (isAutoDecrypt && "application/pkcs7-mime".equals(contentType)) {
            SmimeKey smimeKey = preparePkcs7SmimeKey();

            mail = new WnMimePkcs12Mail(session, smimeKey);
            mail.fromMessage(msg, asContent);
        }
        // 普通邮件
        else {
            mail = new WnMimeMail(msg, asContent);
        }
        return mail;
    }

    protected SmimeKey preparePkcs7SmimeKey() {
        WnMailSecurity secu = fc.mail.getSecurity();
        Pkcs12Config pkcs12 = Mailx.createPkcs12Config(sys, secu);
        ByteArrayInputStream storeIns = new ByteArrayInputStream(pkcs12.getPkcs12StoreData());
        char[] storePasswd = pkcs12.getStorePassword();
        SmimeKeyStore keyStore = new SmimeKeyStore(storeIns, storePasswd);
        String keyAlias = secu.getSign().getKeyAlias();
        String keyPasswd = secu.getSign().getKeyPassword();
        SmimeKey smimeKey = keyStore.getPrivateKey(keyAlias, keyPasswd.toCharArray());
        return smimeKey;
    }

    protected WnObj create_mail_obj(WnMimeMail mail) {
        // 不是 JSON 输出，也要打印到控制台
        // LOG(sys, debug, "%d/%d) %s", i, N, mail.toBrief());
        LOG(sys, debug, "__create_mail_obj i=%d, N=%d, mailBrief=%s", i, N, mail.toBrief());
        NutMap meta = mail.toMeta(showHeader);
        meta.put("tp", "txt");
        meta.put("mime", "text/plain");

        if (null != fixedMeta) {
            meta.putAll(fixedMeta);
        }

        // 寻找正文
        List<WnMailPart> textCans = mail.findContentParts(asContent);
        String text = null;
        if (!textCans.isEmpty()) {
            // 采用最后一个文本
            WnMailPart textPart = textCans.get(textCans.size() - 1);
            String mime = textPart.getContentType();
            if ("text/html".equals(mime)) {
                meta.put("mime", "text/html");
                meta.put("tp", "html");
            } else {
                meta.put("mime", "text/plain");
                meta.put("tp", "txt");
            }

            if (textPart.hasFileName()) {
                meta.put("msg_content_name", textPart.getFileName());
            }
            text = textPart.getContent();
        }

        // 准备目标路径，并创建对象
        String mailPath = taTmpl.render(meta);
        String mailAbsPath = Wn.normalizeFullPath(mailPath, sys, false);
        String mailParentPath = Files.getParent(mailAbsPath);
        String mailFileName = Files.getName(mailAbsPath);
        WnObj oMailDir = sys.io.createIfNoExists(null, mailParentPath, WnRace.DIR);
        WnObj oMail = sys.io.create(oMailDir, mailFileName, WnRace.FILE);

        // 写入对象元数据
        sys.io.appendMeta(oMail, meta);
        LOG(sys, debug, "     IMAP appendMeta: %s", Json.toJson(meta));
        // 写入正文
        if (null != text) {
            sys.io.writeText(oMail, text);
            LOG(sys, debug, "     IMAP writeText: len=%s", text.length());
        } else {
            LOG(sys, debug, "     IMAP writeText: text is null");
        }

        //
        // 计入附件
        //
        if (null != attachmentTmpl) {
            List<WnMailPart> attachments = mail.findAttachmentParts(asContent);
            if (null != attachments) {
                int x = 0;
                int M = attachments.size();
                List<String> attachmentIds = new LinkedList<>();
                LOG(sys, debug, "     it will add attachments: M=%d", M);
                for (WnMailPart att : attachments) {
                    x++;
                    String fnm = att.getFileName();
                    NutMap ctx = new NutMap("attachment_file_name", fnm);
                    ctx.attach(oMail);
                    String atPath = attachmentTmpl.render(ctx);
                    String atAbsPath = Wn.normalizeFullPath(atPath, sys, false);
                    LOG(sys, debug, "     add attachment x=%d, fnm=%s => %s", x, fnm, atAbsPath);
                    String pPath = Files.getParent(atAbsPath);
                    String atName = Files.getName(atAbsPath);
                    WnObj oAtP = sys.io.createIfNoExists(null, pPath, WnRace.DIR);
                    WnObj oAtt = sys.io.create(oAtP, atName, WnRace.FILE);
                    byte[] data = att.getData();
                    sys.io.writeBytes(oAtt, data);
                    attachmentIds.add(oAtt.id());
                }
                if (attachmentIds.size() > 0) {
                    sys.io.appendMeta(oMail, new NutMap("msg_attachments", attachmentIds));
                }
            }
        }

        // 执行回调
        if (!Ws.isBlank(after)) {
            String input = after;
            // 命令来自一个文件
            if (after.startsWith("o:")) {
                String path = after.substring(2).trim();
                WnObj oAfter = Wn.checkObj(sys, path);
                input = sys.io.readText(oAfter);
            }
            // 渲染命令
            if (!Ws.isBlank(input)) {
                String cmdText = WnTmplX.exec(input, oMail);
                sys.exec(cmdText);
            }
        }
        return oMail;
    }
}