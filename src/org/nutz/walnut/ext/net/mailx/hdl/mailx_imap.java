package org.nutz.walnut.ext.net.mailx.hdl;

import java.util.ArrayList;
import java.util.List;

import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.net.mailx.MailxContext;
import org.nutz.walnut.ext.net.mailx.MailxFilter;
import org.nutz.walnut.ext.net.mailx.bean.WnImapMail;
import org.nutz.walnut.ext.net.mailx.provider.MailStoreProvider;
import org.nutz.walnut.ext.net.mailx.provider.MailStoreProviders;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.ZParams;

import com.sun.mail.imap.IMAPFolder;

import jakarta.mail.Flags;
import jakarta.mail.Message;
import jakarta.mail.Store;
import jakarta.mail.MessagingException;
import jakarta.mail.search.SearchTerm;
import jakarta.mail.search.FlagTerm;
import jakarta.mail.search.OrTerm;
import jakarta.mail.search.AndTerm;

public class mailx_imap extends MailxFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "cqn", "^(decrypt|or|json)$");
    }

    @Override
    protected void process(WnSystem sys, MailxContext fc, ZParams params) {
        // JsonFormat jfmt = Cmds.gen_json_format(params);
        boolean isOr = params.is("or");
        // boolean isDecrypt = params.is("decrypt");
        // boolean isJson = params.is("json");

        // 得到输出目标
        String taPath = params.getString("to");
        WnObj oTa = null;
        if (!Ws.isBlank(taPath)) {
            String aph = Wn.normalizeFullPath(taPath, sys);
            oTa = sys.io.fetch(null, aph);
        }
        // boolean hasTarget = null != oTa;
        boolean debug = null == oTa;

        // 分析查询 flag
        String[] ss = params.getAs("flags", String[].class);
        List<SearchTerm> terms = new ArrayList<>(ss.length);
        // Flags flags = new Flags();
        // 默认标签
        if (null == ss || ss.length == 0) {
            terms.add(new FlagTerm(new Flags(Flags.Flag.RECENT), true));
        }
        // 分析标签
        else {
            for (String f : params.vals) {
                if (f.startsWith("!")) {
                    f = f.substring(1);
                    terms.add(new FlagTerm(new Flags(f), false));
                } else {
                    terms.add(new FlagTerm(new Flags(f), true));
                }
            }
        }

        Store store = null;
        try {
            // 获取邮箱交互类
            MailStoreProvider provider;
            if (fc.config.imap.hasProvider()) {
                String providerName = fc.config.imap.getProvider().getName();
                provider = MailStoreProviders.me().createProvider(sys, providerName);
            } else {
                provider = MailStoreProviders.me().createDefaultProvider(sys);
            }
            store = provider.createStrore(fc.config);

            // 获取查询目录
            String folderName = Ws.sBlank(params.val(0, fc.config.imap.getInboxName()), "INBOX");
            IMAPFolder folder = (IMAPFolder) store.getFolder(folderName);
            LOG(sys, debug, "Get folder: [%s]", folderName);

            // 得到搜索条件
            SearchTerm term;

            if (terms.size() == 1) {
                term = terms.get(0);
            } else {
                SearchTerm[] tt = terms.toArray(new SearchTerm[terms.size()]);
                if (isOr) {
                    term = new OrTerm(tt);
                } else {
                    term = new AndTerm(tt);
                }
            }

            // 收取到消息
            Message[] messages = folder.search(term);
            LOG(sys, debug, "find %s messages", messages.length);

            // 循环处理消息
            int N = messages.length;

            for (int i = 0; i < N; i++) {
                Message msg = messages[i];
                WnImapMail mail = new WnImapMail(msg);
                LOG(sys,
                    debug,
                    " > %d/%d) <%s>: %s\n%s",
                    i,
                    N,
                    Ws.join(msg.getFrom(), ","),
                    msg.getSubject(),
                    mail.toString());
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

    private void LOG(WnSystem sys, boolean showDebug, String fmt, Object... args) {
        if (showDebug) {
            String msg = String.format(fmt, args);
            sys.out.println(msg);
        }
    }

}
