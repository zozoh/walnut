package com.site0.walnut.ext.net.mailx.hdl;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Files;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.ext.net.mailx.MailxContext;
import com.site0.walnut.ext.net.mailx.MailxFilter;
import com.site0.walnut.ext.net.mailx.bean.WnImapMail;
import com.site0.walnut.ext.net.mailx.bean.WnImapPkcs12Mail;
import com.site0.walnut.ext.net.mailx.bean.WnMailPart;
import com.site0.walnut.ext.net.mailx.bean.WnMailSecurity;
import com.site0.walnut.ext.net.mailx.provider.MailStoreProvider;
import com.site0.walnut.ext.net.mailx.provider.MailStoreProviders;
import com.site0.walnut.ext.net.mailx.util.Mailx;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;
import com.site0.walnut.util.tmpl.WnTmplX;
import org.simplejavamail.api.mailer.config.Pkcs12Config;
import org.simplejavamail.utils.mail.smime.SmimeKey;
import org.simplejavamail.utils.mail.smime.SmimeKeyStore;

import com.sun.mail.imap.IMAPFolder;

import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.Store;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.search.SearchTerm;
import jakarta.mail.search.FlagTerm;
import jakarta.mail.search.OrTerm;
import jakarta.mail.search.AndTerm;

public class mailx_imap extends MailxFilter {

    private static Log log = Wlog.getCMD();

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "cqn", "^(decrypt|or|json|header)$");
    }

    @Override
    protected void process(WnSystem sys, MailxContext fc, ZParams params) {
        // 禁止发送
        fc.setQuiet(true);

        boolean isOr = params.is("or");
        boolean showHeader = params.is("header");
        boolean isAutoDecrypt = params.is("decrypt");
        boolean isJson = params.is("json");
        String asContent = params.getString("content");
        String after = params.getString("after", null);
        NutMap fixedMeta = params.getMap("meta");

        // 得到输出目标
        String taPath = params.getString("to");
        WnTmplX taTmpl = null;
        if (!Ws.isBlank(taPath)) {
            taTmpl = WnTmplX.parse(taPath);
        }

        // 附件的输出目标
        String attchmentPath = params.getString("at");
        WnTmplX attachmentTmpl = null;
        if (!Ws.isBlank(attchmentPath)) {
            attachmentTmpl = WnTmplX.parse(attchmentPath);
        }

        // boolean hasTarget = null != oTa;
        boolean debug = null == taTmpl;
        debug = params.is("debug", debug);

        // 分析搜索标签
        List<SearchTerm> terms = __eval_flags(params);

        // 防守
        if (null == fc.config.imap) {
            throw Er.create("e.mailx.imap.NilImapConfig");
        }

        Store store = null;
        String HR = Ws.repeat('#', 80);
        try {
            // 获取邮箱交互类
            MailStoreProvider provider;
            if (fc.config.imap.hasProvider()) {
                String providerName = fc.config.imap.getProvider().getName();
                provider = MailStoreProviders.me().createProvider(sys, providerName);
            } else {
                provider = MailStoreProviders.me().createDefaultProvider(sys);
            }
            Session session = provider.createSession(fc.config.imap);
            store = provider.createStrore(session, fc.config.imap);

            // 获取查询目录
            String folderName = Ws.sBlank(params.val(0, fc.config.imap.getInboxName()), "INBOX");
            IMAPFolder folder = (IMAPFolder) store.getFolder(folderName);
            LOG(sys, debug, "Get folder: [%s]", folderName);
            folder.open(Folder.READ_WRITE);
            LOG(sys, debug, "Open folder: [%s]", folder.toString());

            // 得到搜索条件
            SearchTerm term = __eval_term(isOr, terms);

            // 收取到消息
            Message[] messages = folder.search(term);
            int N = messages.length;
            LOG(sys, debug, "find %s messages", N);

            // 循环处理消息
            List<WnObj> outputs = new ArrayList<>(N); // 如果输出为数据集，输出目标记录在这里
            for (int i = 0; i < N; i++) {
                Message msg = messages[i];
                WnImapMail mail;
                String contentType = Mailx.evalContentType(msg.getContentType(), null);

                // 加密邮件尝试解密
                if (isAutoDecrypt && "application/pkcs7-mime".equals(contentType)) {
                    WnMailSecurity secu = fc.mail.getSecurity();
                    Pkcs12Config pkcs12 = Mailx.createPkcs12Config(sys, secu);
                    ByteArrayInputStream storeIns = new ByteArrayInputStream(pkcs12.getPkcs12StoreData());
                    char[] storePasswd = pkcs12.getStorePassword();
                    SmimeKeyStore keyStore = new SmimeKeyStore(storeIns, storePasswd);
                    String keyAlias = secu.getSign().getKeyAlias();
                    String keyPasswd = secu.getSign().getKeyPassword();
                    SmimeKey smimeKey = keyStore.getPrivateKey(keyAlias, keyPasswd.toCharArray());

                    mail = new WnImapPkcs12Mail(session, smimeKey);
                    mail.fromMessage(msg, asContent);
                }
                // 普通邮件
                else {
                    mail = new WnImapMail(msg, asContent);
                }
                //
                // 输出到数据集
                //
                if (null != taTmpl) {
                    WnObj oMail = __create_mail_obj(sys,
                                                    showHeader,
                                                    debug,
                                                    asContent,
                                                    N,
                                                    i,
                                                    fixedMeta,
                                                    mail,
                                                    taTmpl,
                                                    attachmentTmpl,
                                                    after);
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
                        Ws.join(msg.getFrom(), ","),
                        msg.getSubject(),
                        HR,
                        mail.dumpString(showHeader));
                }
            } // for (int i = 0; i < N; i++) {

            // 输出
            if (isJson) {
                JsonFormat jfmt = Cmds.gen_json_format(params);
                String json = Json.toJson(outputs, jfmt);
                sys.out.println(json);
            }

        }
        catch (Throwable e) {
            throw Er.wrap(e);
        }
        finally {
            if (null != store) {
                try {
                    store.close();
                }
                catch (MessagingException e2) {
                    throw Er.wrap(e2);
                }
            }
        }
    }

    protected WnObj __create_mail_obj(WnSystem sys,
                                      boolean showHeader,
                                      boolean debug,
                                      String asContent,
                                      int N,
                                      int i,
                                      NutMap fixedMeta,
                                      WnImapMail mail,
                                      WnTmplX taTmpl,
                                      WnTmplX attachmentTmpl,
                                      String after) {
        // 不是 JSON 输出，也要打印到控制台
        LOG(sys, debug, "%d/%d) %s", i, N, mail.toBrief());
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
        LOG(sys, debug, "     -> %s", oMail.toString());
        // 写入正文
        if (null != text) {
            sys.io.writeText(oMail, text);
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
                LOG(sys, debug, "     ++ %d attachments:", M);
                for (WnMailPart att : attachments) {
                    x++;
                    String fnm = att.getFileName();
                    NutMap ctx = new NutMap("attachment_file_name", fnm);
                    ctx.attach(oMail);
                    String atPath = attachmentTmpl.render(ctx);
                    String atAbsPath = Wn.normalizeFullPath(atPath, sys, false);
                    LOG(sys, debug, "     -> %d. %s => %s", x, fnm, atAbsPath);
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

    protected SearchTerm __eval_term(boolean isOr, List<SearchTerm> terms) {
        SearchTerm term;

        if (terms.size() == 1) {
            // term = terms.get(0);
            term = new FlagTerm(new Flags(Flags.Flag.SEEN), false);
        } else {
            SearchTerm[] tt = terms.toArray(new SearchTerm[terms.size()]);
            if (isOr) {
                term = new OrTerm(tt);
            } else {
                term = new AndTerm(tt);
            }
        }
        return term;
    }

    protected List<SearchTerm> __eval_flags(ZParams params) {
        // 分析查询 flag
        String[] ss = params.getAs("flags", String[].class);
        int n = null == ss ? 0 : ss.length;
        List<SearchTerm> terms = new ArrayList<>(n);
        // 默认标签
        if (n == 0) {
            terms.add(new FlagTerm(new Flags(Flags.Flag.RECENT), true));
        }
        // 分析标签
        else {
            for (String f : ss) {
                boolean set = true;
                // 预处理标记，支持 `!XXXX`
                if (f.startsWith("!")) {
                    f = f.substring(1);
                    set = false;
                }
                // 获取标记
                f = f.toUpperCase();
                Flags.Flag fg = MAIL_FLAGS.get(f);
                if (null == fg) {
                    throw Er.create("e.cmd.mailx.imap.InvalideFlag", f);
                }
                terms.add(new FlagTerm(new Flags(fg), set));
            }
        }
        return terms;
    }

    private void LOG(WnSystem sys, boolean showDebug, String fmt, Object... args) {
        if (showDebug) {
            String msg = String.format(fmt, args);
            sys.out.println(msg);

        }
        if (log.isInfoEnabled()) {
            log.infof(fmt, args);
        }
    }

    private static Map<String, Flags.Flag> MAIL_FLAGS = new HashMap<>();

    static {
        MAIL_FLAGS.put("ANSWERED", Flags.Flag.ANSWERED);
        MAIL_FLAGS.put("DELETED", Flags.Flag.DELETED);
        MAIL_FLAGS.put("DRAFT", Flags.Flag.DRAFT);
        MAIL_FLAGS.put("FLAGGED", Flags.Flag.FLAGGED);
        MAIL_FLAGS.put("RECENT", Flags.Flag.RECENT);
        MAIL_FLAGS.put("SEEN", Flags.Flag.SEEN);
        MAIL_FLAGS.put("USER", Flags.Flag.USER);
    }
}
