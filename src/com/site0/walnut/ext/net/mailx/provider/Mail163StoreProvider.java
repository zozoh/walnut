package com.site0.walnut.ext.net.mailx.provider;

import java.util.HashMap;
import java.util.Properties;

import org.nutz.lang.util.NutMap;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.ext.net.mailx.bean.MailxImapConifg;
import com.site0.walnut.impl.box.WnSystem;
import com.sun.mail.imap.IMAPStore;

import jakarta.mail.Session;
import jakarta.mail.Store;

public class Mail163StoreProvider implements MailStoreProvider {

    public Mail163StoreProvider(WnSystem sys) {}

    @Override
    public Session createSession(MailxImapConifg imap) {
        NutMap setup = imap.getProvider().getSetup();

        Properties props = new Properties();
        props.put("mail.imap.host", imap.getHost());
        props.put("mail.imap.port", imap.getPort());
        props.put("mail.imap.auth", "true");
        // 启用 ssl
        if (setup.is("sslEnable", true)) {
            props.put("mail.imap.ssl.enable", "true");
            props.put("mail.imap.starttls.enable", "true");
            props.put("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.imap.socketFactory.fallback", "false");
            props.put("mail.imap.socketFactory.port", imap.getPort());
        }

        // 获取会话
        Session session = Session.getInstance(props);
        return session;
    }

    @Override
    public Store createStrore(Session session, MailxImapConifg imap) {
        // 网易的 163 邮箱验证比较严格
        // @see
        // http://help.mail.163.com/faqDetail.do?code=d7a5dc8471cd0c0e8b4b8f4f8e49998b374173cfe9171305fa1ce630d7f67ac2eda07326646e6eb0
        //
        // 带上IMAP ID信息，由key和value组成，例如name，version，vendor，support-email等。
        HashMap<String, String> IAM = new HashMap<>();
        IAM.put("name", "Site0");
        IAM.put("version", "1.0.0");
        IAM.put("vendor", "Walnut");
        IAM.put("support-email", "walnut@site0.com");

        try {
            IMAPStore store = (IMAPStore) session.getStore("imap");
            String host = imap.getHost();
            String account = imap.getAccount();
            String passwd = imap.getPassword();
            store.connect(host, account, passwd);

            // 这里 163 需要你表明身份
            store.id(IAM);
            return store;
        }
        catch (Exception e) {
            throw Er.wrap(e);
        }
    }

}
