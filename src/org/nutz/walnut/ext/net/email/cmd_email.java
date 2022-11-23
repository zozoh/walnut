package org.nutz.walnut.ext.net.email;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.activation.DataSource;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.ImageHtmlEmail;
import org.apache.commons.mail.resolver.DataSourceUrlResolver;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Encoding;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.Times;
import org.nutz.lang.random.R;
import org.nutz.walnut.util.tmpl.Tmpl;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.walnut.util.Wlog;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;
import org.nutz.web.ajax.Ajax;

/**
 * 发送email
 * 
 * @author pw
 * @author wendal
 *
 */
public class cmd_email extends JvmExecutor {

    private static final Log log = Wlog.getEXT();

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        MailCtx mc = new MailCtx();
        mc.sys = sys;
        ZParams params = ZParams.parse(args, "^(debug|local)$");
        mc.debug = params.is("debug");
        mc.lang = params.get("lang");
        mc.config = params.get("config");
        mc.receivers = params.get("r");
        mc.ccs = params.get("cc");
        mc.from = params.get("from");
        mc.msg = params.get("m");
        mc.subject = params.get("s");
        mc.tmpl = params.get("tmpl");
        mc.dataSourceResolver = params.get("dsr");
        mc.local = params.is("local");
        mc.vars = params.get("vars");
        if (params.has("attachs")) { // 多附件用数组
        	mc.attachs = Json.fromJsonAsList(NutMap.class, params.get("attachs"));
        }
        if (params.has("attach")) { // 单附件直接是map
        	if (params.get("attach").contains("\""))
        		mc.attachs.add(Lang.map(params.get("attach")));
        	else
        		mc.attachs.add(new NutMap("path", params.get("attach")));
        }

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
                send(sys, mc);
                return;
            default:
                sys.err.println("bad arg=" + type);
                return;
            }
        }
        send(sys, mc);
    }

    public List<WnObj> _listLocal(MailCtx mc, int limit) {
        WnSystem sys = mc.sys;
        WnAccount u = sys.getMe();
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

    public void send(WnSystem sys, MailCtx mc) {
        if (mc.debug) {
            mc.sys.out.printf("/*\n%s\n*/", Json.toJson(mc));
        }
        // ............................................
        // 配置文件
        WnIo io = mc.sys.io;
        WnObj oEmailHome = Wn.checkObj(mc.sys, "~/.email");
        WnObj oConf = io.fetch(oEmailHome, Strings.sBlank(mc.config, "config_default"));
        // TODO 删掉?? 兼容老版本版: 加载配置默认配置
        if (null == oConf) {
            oConf = Wn.checkObj(mc.sys, "~/.mail_send_conf");
        }

        // 解析配置文件
        EmailServerConf hostCnf = io.readJson(oConf, EmailServerConf.class);

        // ............................................
        // 确定语言
        if (Strings.isBlank(mc.lang)) {
            mc.lang = Strings.sBlank(hostCnf.lang, "zh-cn");
        }
        // ............................................
        // 读取并解析变量
        if (Strings.isBlank(mc.vars) || "true".equals(mc.vars)) {
            mc.vars = mc.sys.in.readAll();
        }
        NutMap _c = Lang.map(Strings.sBlank(mc.vars, "{}"));
        // ............................................
        // 处理标题: 多国语言
        if (!Strings.isBlank(mc.subject) && mc.subject.startsWith("i18n:")) {
            String subjKey = Strings.trim(mc.subject.substring("i18n:".length()));
            WnObj oSubjects = io.check(oEmailHome, "i18n/" + mc.lang + "/_subjects");
            NutMap subjectMap = io.readJson(oSubjects, NutMap.class);
            mc.subject = subjectMap.getString(subjKey);
        }
        // 渲染标题
        if (!Strings.isBlank(mc.subject)) {
            mc.subject = Tmpl.exec(mc.subject, _c, false);
        }
        // ............................................
        // 处理模板:多国语言
        if (!Strings.isBlank(mc.tmpl)) {
            WnObj oTmpl = null;
            // 多国语言
            if (mc.tmpl.startsWith("i18n:")) {
                String tmplName = Strings.trim(mc.tmpl.substring("i18n:".length()));
                oTmpl = io.check(oEmailHome, "i18n/" + mc.lang + "/" + tmplName);
            }
            // 兼容老版本
            else {
                oTmpl = io.check(oEmailHome, mc.tmpl);
            }

            // 渲染消息正文
            String tmpl = io.readText(oTmpl);
            mc.msg = Tmpl.exec(tmpl, _c, false);
        }
        // ............................................
        // 解析收件人及抄送
        List<MailReceiver> rc = parse(mc, mc.receivers);
        List<MailReceiver> cc = parse(mc, mc.ccs);
        // ............................................
        // 加载附件,如果有的话, TODO 实现发附件
        if (mc.local) {
            // TODO 切换到root,否则没法往其他用户的文件夹写文件吧
            String mailName = Times.sD(Times.now())
                              + "_"
                              + mc.sys.getMyName()
                              + "_"
                              + R.UU32()
                              + ".mail";
            for (MailReceiver mailReceiver : rc) {
                WnAccount u = mc.sys.auth.checkAccount(mailReceiver.name);
                String localMailHome = userHome(u) + "/.mail/";
                if (!io.exists(null, localMailHome))
                    io.create(null, localMailHome, WnRace.DIR);
                WnObj oTmpl = io.create(null, userHome(u) + "/.mail/" + mailName, WnRace.FILE);
                io.writeJson(oTmpl, mc, JsonFormat.full());
            }
            for (MailReceiver mailReceiver : cc) {
                WnAccount u = mc.sys.auth.checkAccount(mailReceiver.name);
                String localMailHome = userHome(u) + "/.mail/";
                if (!io.exists(null, localMailHome))
                    io.create(null, localMailHome, WnRace.DIR);
                WnObj oTmpl = io.create(null, localMailHome + mailName, WnRace.FILE);
                io.writeJson(oTmpl, mc, JsonFormat.full());
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
            ihe.setCharset(Encoding.UTF8);
            try {
                if (mc.dataSourceResolver != null) {
                    ihe.setDataSourceResolver(new DataSourceUrlResolver(new URL(mc.dataSourceResolver)));
                }
                else {
                	ihe.setDataSourceResolver(new DataSourceUrlResolver(null));
                }
                String fnm = mc.from;
                if (Strings.isBlank(fnm)) {
                    fnm = hostCnf.from == null ? mc.sys.getMyName() : hostCnf.from;
                }
                ihe.setFrom(hostCnf.account, fnm);
                ihe.setHtmlMsg(mc.msg);
                for (MailReceiver mailReceiver : rc) {
                    ihe.addTo(mailReceiver.email, mailReceiver.name);
                }
                for (MailReceiver mailReceiver : cc) {
                    ihe.addCc(mailReceiver.email, mailReceiver.name);
                }
                if (!mc.attachs.isEmpty()) {
                	for (NutMap at : mc.attachs) {
                		String path = Wn.normalizeFullPath(at.getString("path"), sys);
                		WnObj wobj = io.check(null, path);
                		DataSource ds = new WnFileDataSource(wobj, io);
						ihe.attach(ds, at.getString("name", wobj.name()), at.getString("desc", wobj.name()));
					}
                }
                ihe.buildMimeMessage();
                ihe.sendMimeMessage();

                Object reo = Ajax.ok();
                sys.out.println(Json.toJson(reo, JsonFormat.nice().setQuoteName(true)));
            }
            catch (EmailException | MalformedURLException e) {
                // e.printStackTrace(new PrintWriter(mc.sys.err.getWriter()));
                if (log.isWarnEnabled())
                    log.warn("send mail fail", e);
                Object ree = Ajax.fail().setErrCode("e.cmd.mail.fail");
                sys.err.println(Json.toJson(ree, JsonFormat.nice().setQuoteName(true)));
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

    public String userHome(WnAccount u) {
        return u.isRoot() ? "/root" : "/home/" + u.getName();
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
                    mr.email = mc.sys.auth.checkAccount(mr.name).getEmail();
                    mr.name = rev;
                }
            }
            rs.add(mr);
        }
        return rs;
    }
    
    static class WnFileDataSource implements javax.activation.DataSource {
    	
    	protected WnObj wobj;
    	protected WnIo io;

		public WnFileDataSource(WnObj wobj, WnIo io) {
			this.wobj = wobj;
			this.io = io;
		}

		public WnObj getWobj() {
			return wobj;
		}

		public void setWobj(WnObj wobj) {
			this.wobj = wobj;
		}

		public WnIo getIo() {
			return io;
		}

		public void setIo(WnIo io) {
			this.io = io;
		}

		public String getContentType() {
			return wobj.mime();
		}

		public InputStream getInputStream() throws IOException {
			return io.getInputStream(wobj, 0);
		}

		public String getName() {
			return wobj.name();
		}

		public OutputStream getOutputStream() throws IOException {
			return io.getOutputStream(wobj, 0);
		}
    	
    }
}
