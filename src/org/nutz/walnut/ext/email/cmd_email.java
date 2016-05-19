package org.nutz.walnut.ext.email;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.ImageHtmlEmail;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.Times;
import org.nutz.lang.random.R;
import org.nutz.lang.segment.CharSegment;
import org.nutz.lang.segment.Segment;
import org.nutz.lang.util.Context;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

/**
 * 发送email
 * 
 * @author pw
 * @author wendal
 *
 */
public class cmd_email extends JvmExecutor {

    private static final Log log = Logs.get();

    public void exec(WnSystem sys, String[] args) throws Exception {
        MailCtx mc = new MailCtx();
        mc.sys = sys;
        ZParams params = ZParams.parse(args, "^(debug|local)$");
        mc.debug = params.is("debug");
        mc.config = params.get("config");
        mc.receivers = params.get("r");
        mc.ccs = params.get("cc");
        mc.msg = params.get("m");
        mc.subject = params.get("s");
        mc.tmpl = params.get("tmpl");
        mc.local = params.is("local");
        mc.vars = params.get("vars");
        mc.attachs.add(params.get("attach"));

        List<String> _args = Arrays.asList(params.vals);
        if (!_args.isEmpty()) {
            String type = _args.get(0);
            int limit = 10;
            switch (type) {
            case "list":
                if (_args.size() > 1) {
                    limit = Integer.parseInt(_args.get(1));
                }
                listLocalMail(mc, limit);
                return;
            case "clear":
                if (_args.size() > 1) {
                    limit = Integer.parseInt(_args.get(1));
                }
                clearLocalMail(mc, limit);
                return;
            case "send":
                send(mc);
                return;
            default:
                sys.err.println("bad arg=" + type);
                return;
            }
        }
        send(mc);
    }

    public List<WnObj> _listLocal(MailCtx mc, int limit) {
        WnSystem sys = mc.sys;
        WnUsr u = sys.me;
        String phHome = userHome(u);
        String mailHome = phHome + "/.mail";
        WnObj mh = sys.io.fetch(null, mailHome);
        if (mh == null) {
            return null;
        }
        if (limit < 1)
            limit = 10;
        WnQuery q = new WnQuery().limit(limit);
        q.setv("pid", mh.id());
        List<WnObj> list = sys.io.query(q);
        return list;
    }

    public void listLocalMail(MailCtx mc, int limit) {
        List<WnObj> mails = _listLocal(mc, limit);
        if (mails == null)
            return;
        for (WnObj mail : mails) {
            MailCtx _mc = mc.sys.io.readJson(mail, MailCtx.class);
            if (_mc != null) {
                _mc.sys = mc.sys;
                printMail(mail, _mc);
            }
        }
    }

    public void clearLocalMail(MailCtx mc, int limit) {
        List<WnObj> mails = _listLocal(mc, limit);
        if (mails == null)
            return;
        for (WnObj mail : mails) {
            mc.sys.io.delete(mail);
        }
    }

    public void send(MailCtx mc) {
        if (mc.debug) {
            mc.sys.out.printf("/*\n%s\n*/", Json.toJson(mc));
        }
        WnIo io = mc.sys.io;
        // 加载配置
        if (mc.config == null) {
            mc.config = userHome(mc.sys.me) + "/.mail_send_conf";
        }
        WnObj tmp = io.check(null, mc.config);
        EmailServerConf hostCnf = io.readJson(tmp, EmailServerConf.class);

        // 处理模板,如果指定了的话
        if (mc.tmpl != null) {
            tmp = io.check(null, mc.tmpl);
            Segment seg = new CharSegment(io.readText(tmp));
            Context _c;
            if (mc.vars != null) {
                _c = Lang.context(Json.fromJson(NutMap.class, mc.vars));
            } else {
                _c = Lang.context();
            }
            mc.msg = seg.render(_c).toString();
        }

        // 解析收件人及抄送
        List<MailReceiver> rc = parse(mc, mc.receivers);
        List<MailReceiver> cc = parse(mc, mc.ccs);

        // 加载附件,如果有的话, TODO 实现发附件
        if (mc.local) {
            // TODO 切换到root,否则没法往其他用户的文件夹写文件吧
            String mailName = Times.sD(Times.now())
                              + "_"
                              + mc.sys.me.name()
                              + "_"
                              + R.UU32()
                              + ".mail";
            for (MailReceiver mailReceiver : rc) {
                WnUsr u = mc.sys.usrService.check(mailReceiver.name);
                String localMailHome = userHome(u) + "/.mail/";
                if (!io.exists(null, localMailHome))
                    io.create(null, localMailHome, WnRace.DIR);
                tmp = io.create(null, userHome(u) + "/.mail/" + mailName, WnRace.FILE);
                io.writeJson(tmp, mc, JsonFormat.full());
            }
            for (MailReceiver mailReceiver : cc) {
                WnUsr u = mc.sys.usrService.check(mailReceiver.name);
                String localMailHome = userHome(u) + "/.mail/";
                if (!io.exists(null, localMailHome))
                    io.create(null, localMailHome, WnRace.DIR);
                tmp = io.create(null, localMailHome + mailName, WnRace.FILE);
                io.writeJson(tmp, mc, JsonFormat.full());
            }
        } else {
            ImageHtmlEmail ihe = new ImageHtmlEmail();
            if (mc.debug)
                ihe.setDebug(true);
            ihe.setHostName(hostCnf.host);
            ihe.setSmtpPort(hostCnf.port);
            ihe.setAuthentication(hostCnf.account, hostCnf.password);
            ihe.setSSLOnConnect(hostCnf.ssl);
            ihe.setSubject(mc.subject);
            try {
                ihe.setFrom(hostCnf.from == null ? hostCnf.account : hostCnf.from,
                            mc.sys.me.name());
                ihe.setHtmlMsg(mc.msg);
                for (MailReceiver mailReceiver : rc) {
                    ihe.addTo(mailReceiver.email, mailReceiver.name);
                }
                for (MailReceiver mailReceiver : cc) {
                    ihe.addCc(mailReceiver.email, mailReceiver.name);
                }
                ihe.send();
            }
            catch (EmailException e) {
                e.printStackTrace(new PrintWriter(mc.sys.err.getWriter()));
                if (log.isDebugEnabled())
                    log.debug("send mail fail", e);
            }
        }
    }

    public void printMail(WnObj obj, MailCtx mc) {
        WnSystem sys = mc.sys;
        sys.out.printf("//Mail Id=%s\n", obj.id());
        sys.out.printf("From   : %s\n", mc.sender);
        sys.out.printf("To     : %s\n", mc.receivers);
        if (mc.ccs != null)
            sys.out.printf("CC     : %s\n", mc.ccs);
        sys.out.printf("Subject: %s\n", mc.subject);
        sys.out.print("====================================\n");
        sys.out.print(mc.msg);
        sys.out.print("====================================\n");
        if (!mc.attachs.isEmpty()) {
            sys.out.printf("Attach :\n%s\n", Strings.join2("\n", mc.attachs.toArray()));
            sys.out.print("====================================\n");
        }
        sys.out.print("====================================\n");
    }

    public String userHome(WnUsr u) {
        return "root".equals(u.name()) ? "/root" : "/home/" + u.name();
    }

    public List<MailReceiver> parse(MailCtx mc, String names) {
        List<MailReceiver> rs = new ArrayList<>();
        if (names == null)
            return rs;
        for (String rev : Strings.splitIgnoreBlank(names, ",")) {
            if (rev == null || rev.length() == 0)
                continue;
            MailReceiver mr = new MailReceiver();
            if (rev.contains("=")) {
                String[] t = rev.split("=");
                mr.email = t[0];
                mr.name = t[1];
            } else {
                if (rev.contains("@")) {
                    mr.name = rev;
                    mr.email = rev;
                } else {
                    mr.email = mc.sys.usrService.check(mr.name).email();
                    mr.name = rev;
                }
            }
            rs.add(mr);
        }
        return rs;
    }
}
