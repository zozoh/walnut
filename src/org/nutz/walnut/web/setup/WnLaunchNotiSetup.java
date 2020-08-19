package org.nutz.walnut.web.setup;

import java.util.Date;
import java.util.List;

import org.apache.commons.mail.HtmlEmail;
import org.nutz.lang.Encoding;
import org.nutz.lang.Lang;
import org.nutz.lang.Times;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.NutConfig;
import org.nutz.mvc.Setup;
import org.nutz.walnut.web.WnConfig;

public class WnLaunchNotiSetup implements Setup {

    private static final Log log = Logs.get();

    @Override
    public void init(NutConfig nc) {

        WnConfig conf = nc.getIoc().get(WnConfig.class, "conf");

        // 发送邮件通知，服务器已启动
        if (conf.getBoolean("startup-noti", true)) {
            Lang.runInAnThread(new Runnable() {
                @Override
                public void run() {
                    List<String> receivers = conf.getList("noti-receivers", ",");
                    for (String to : receivers) {
                        log.infof("send startup-noti email to %s", to);
                        String serverName = conf.get("noti-server", "未命名");
                        try {
                            HtmlEmail email = new HtmlEmail();
                            email.setHostName(conf.get("noti-host"));
                            email.setSmtpPort(conf.getInt("noti-port"));
                            email.setAuthentication(conf.get("noti-account"),
                                                    conf.get("noti-password"));
                            email.setSSLOnConnect(conf.getBoolean("noti-ssl"));
                            email.setCharset(Encoding.UTF8);
                            email.setSubject("WalnutServer[" + serverName + "] Restart!");
                            email.setHtmlMsg("WalnutServer["
                                             + serverName
                                             + "] is started on "
                                             + Times.sDT(new Date()));
                            email.setFrom(conf.get("noti-account"), "NutzTeam");
                            email.addTo(to);
                            email.buildMimeMessage();
                            email.sendMimeMessage();
                        }
                        catch (Throwable e) {
                            log.info("send email fail", e);
                        }
                        Lang.sleep(1000);
                    }
                }
            });
        }
    }

    @Override
    public void destroy(NutConfig nc) {}

}