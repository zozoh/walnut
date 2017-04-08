package org.nutz.walnut.ext.email;

import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.random.R;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.util.WnRun;
import org.subethamail.smtp.auth.LoginAuthenticationHandlerFactory;
import org.subethamail.smtp.auth.LoginFailedException;
import org.subethamail.smtp.auth.UsernamePasswordValidator;
import org.subethamail.smtp.helper.SimpleMessageListenerAdapter;
import org.subethamail.smtp.server.SMTPServer;

@IocBean(create="start", depose="stop")
public class WnMailServer {

    protected SMTPServer smtpServer;
    
    @Inject
    protected WnRun wnRun;
    
    @Inject
    protected WnSmtpMailListener wnSmtpMailListener;
    
    @Inject
    protected PropertiesProxy conf;
    
    public void start() {
        if (conf.getInt("smtp-port", -1) < 1)
            return;
        smtpServer = new SMTPServer(new SimpleMessageListenerAdapter(wnSmtpMailListener));
        smtpServer.setAuthenticationHandlerFactory(new LoginAuthenticationHandlerFactory(new UsernamePasswordValidator() {
            public void login(String username, String password) throws LoginFailedException {
                WnUsr usr = wnRun.usrs().fetch(username);
                if (usr == null) {
                    throw new LoginFailedException("no such user");
                }
                WnObj wobj = wnRun.io().fetch(null, usr.home() + "/.email/token");
                if (wobj == null) {
                    throw new LoginFailedException("user token not set yet");
                }
                String _token = wnRun.io().readText(wobj);
                if (!_token.equalsIgnoreCase(password)) {
                    throw new LoginFailedException("user token not match");
                }
            }
        }));
        smtpServer.setPort(conf.getInt("smtp-port", -1));
        smtpServer.setSessionIdFactory(()->R.UU32());
        smtpServer.setSoftwareName("Walnut Mail Server");
        smtpServer.start();
    }
    
    public void stop() {
        if (smtpServer != null)
            smtpServer.stop();
    }
}
