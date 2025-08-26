package com.site0.walnut.ext.net.mailx.hdl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Stopwatch;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.net.mailx.MailxContext;
import com.site0.walnut.ext.net.mailx.MailxFilter;
import com.site0.walnut.ext.net.mailx.impl.WnMailIMAPRecieving;
import com.site0.walnut.ext.net.mailx.provider.MailStoreProvider;
import com.site0.walnut.ext.net.mailx.provider.MailStoreProviders;
import com.site0.walnut.ext.net.mailx.util.Mailx;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;
import com.site0.walnut.util.tmpl.WnTmplX;
import com.sun.mail.imap.IMAPFolder;
import static com.site0.walnut.ext.net.mailx.util.Mailx.LOG;

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

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "cqn", "^(decrypt|or|json|header)$");
    }

    @Override
    protected void process(WnSystem sys, MailxContext fc, ZParams params) {
        // 禁止发送
        fc.setQuiet(true);

        // 准备参数
        boolean isOr = params.is("or");
        boolean showHeader = params.is("header");
        boolean isAutoDecrypt = params.is("decrypt");
        boolean isJson = params.is("json");
        String asContent = params.getString("content");
        String after = params.getString("after", null);
        NutMap fixedMeta = params.getMap("meta");

        // 得到输出目标
        WnTmplX taTmpl = Mailx.getTmpl(params, "to");

        // 附件的输出目标
        WnTmplX attachmentTmpl = Mailx.getTmpl(params, "at");

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
        try {
            // 获取邮箱交互类
            MailStoreProvider provider;
            if (fc.config.imap.hasProvider()) {
                String providerName = fc.config.imap.getProvider().getName();
                provider = MailStoreProviders.me()
                    .createProvider(sys, providerName);
            } else {
                provider = MailStoreProviders.me().createDefaultProvider(sys);
            }
            Session session = provider.createSession(fc.config.imap);
            store = provider.createStrore(session, fc.config.imap);

            // 获取查询目录
            String folderName = Ws.sBlank(
                                          params.val(0,
                                                     fc.config.imap
                                                         .getInboxName()),
                                          "INBOX");
            IMAPFolder folder = (IMAPFolder) store.getFolder(folderName);
            LOG(sys,
                debug,
                "mailx_imap: Get folder: folderName=%s",
                folderName);
            folder.open(Folder.READ_WRITE);
            LOG(sys,
                debug,
                "mailx_imap: Open folder: folder open=%s",
                folder.toString());

            // 得到搜索条件
            SearchTerm term = __eval_term(isOr, terms);

            // 收取到消息
            Message[] messages = folder.search(term);
            int N = messages.length;
            LOG(sys, debug, "mailx_imap: find messages, N=%d", N);

            if (N <= 0) {
                return;
            }

            // 准备计时
            Stopwatch sw = Stopwatch.begin();

            // 循环处理消息
            Vector<WnObj> outputs = new Vector<>(N); // 如果输出为数据集，输出目标记录在这里
            for (int i = 0; i < N; i++) {
                WnMailIMAPRecieving rv = new WnMailIMAPRecieving();
                rv.mail_msg = messages[i];
                rv.isAutoDecrypt = isAutoDecrypt;
                rv.sys = sys;
                rv.fc = fc;
                rv.session = session;
                rv.asContent = asContent;
                rv.showHeader = showHeader;
                rv.debug = debug;
                rv.i = i;
                rv.N = N;
                rv.fixedMeta = fixedMeta;

                // 后续处理
                rv.taTmpl = taTmpl;
                rv.attachmentTmpl = attachmentTmpl;
                rv.after = after;
                rv.outputs = outputs;
                
                // 直接执行
                rv.run();
            } // for (int i = 0; i < N; i++) {

            sw.stop();
            LOG(sys,
                debug,
                "mailx_imap: IMAP done in %s: N=%d",
                sw.toString(),
                N);

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
