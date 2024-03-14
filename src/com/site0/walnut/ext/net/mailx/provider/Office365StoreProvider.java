package com.site0.walnut.ext.net.mailx.provider;

import java.security.Security;
import java.util.HashSet;
import java.util.Properties;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.ext.net.mailx.bean.MailxImapConifg;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;

import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.PublicClientApplication;
import com.microsoft.aad.msal4j.UserNamePasswordParameters;

import jakarta.mail.Session;
import jakarta.mail.Store;

public class Office365StoreProvider implements MailStoreProvider {

    // BouncyCastle是一个开源的第三方算法提供商，
    // 提供了很多Java标准库没有提供的哈希算法和加密算法。
    // 使用第三方算法前需要通过Security.addProvider()注册。
    static {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    private WnIo io;

    private NutBean vars;

    public Office365StoreProvider(WnSystem sys) {
        this(sys.io, sys.session.getVars());
    }

    public Office365StoreProvider(WnIo io, NutBean vars) {
        this.io = io;
        this.vars = vars;
    }

    @Override
    public Session createSession(MailxImapConifg imap) {
        NutMap setup = imap.getProvider().getSetup();

        Properties props = new Properties();
        // 启用 ssl
        if (setup.is("sslEnable", true)) {
            props.put("mail.imap.ssl.enable", "true");
        }

        // outlook 指定的鉴权机制
        String authMechanisms = setup.getString("authMechanisms", "XOAUTH2");
        props.put("mail.imap.auth.mechanisms", authMechanisms);

        // 获取会话
        Session session = Session.getInstance(props);
        return session;
    }

    @Override
    public Store createStrore(Session session, MailxImapConifg imap) {
        try {
            Store store = session.getStore("imap");
            String host = Ws.sBlank(imap.getHost(), "outlook.office365.com");
            int port = imap.getPort();
            String username = imap.getAccount();
            String ticket = this.getAccessToken(imap);

            store.connect(host, port, username, ticket);
            return store;
        }
        catch (Exception e) {
            throw Er.wrap(e);
        }

    }

    @SuppressWarnings("unchecked")
    private String getAccessToken(MailxImapConifg imap) throws Exception {
        NutMap setup = imap.getProvider().getSetup();
        String cachePath = setup.getString("cachePath");

        //
        // 尝试命中缓存
        //
        String ticket;
        WnObj oCache;
        String aCachePath = null;
        if (!Ws.isBlank(cachePath)) {
            aCachePath = Wn.normalizeFullPath(cachePath, vars);
            oCache = io.fetch(null, aCachePath);
            if (null != oCache && !oCache.isExpired()) {
                ticket = oCache.getString("ticket");
                if (!Ws.isBlank(ticket)) {
                    return ticket;
                }
            }
        }

        //
        // 没办法，只能真正的获取缓存了
        //
        String clientId = setup.getString("clientId");
        String authority = setup.getString("authority");
        String username = imap.getAccount();
        String password = imap.getPassword();
        HashSet<String> scope = (HashSet<String>) setup.getAs("scope", HashSet.class);
        if (null == scope || scope.isEmpty()) {
            // 好像有下面几种权限：
            // - "offline_access",
            // - "email",
            // - "https://outlook.office.com/IMAP.AccessAsUser.All",
            // - "https://outlook.office.com/POP.AccessAsUser.All",
            // - "https://outlook.office.com/SMTP.Send"
            scope = new HashSet<>();
            scope.add("offline_access");
            scope.add("email");
            scope.add("https://outlook.office.com/IMAP.AccessAsUser.All");
        }

        PublicClientApplication pca = PublicClientApplication.builder(clientId)
                                                             .authority(authority)
                                                             .build();

        // Attempt to acquire token when user's account is not in the
        // application's token cache
        UserNamePasswordParameters params = UserNamePasswordParameters.builder(scope,
                                                                               username,
                                                                               password.toCharArray())
                                                                      .build();
        // Try to acquire a token via username/password. If successful, you
        // should see
        // the token and account information printed out to console
        IAuthenticationResult result = pca.acquireToken(params).join();
        ticket = result.accessToken();

        //
        // 更新缓存
        //
        if (!Ws.isBlank(aCachePath)) {
            oCache = io.createIfNoExists(null, aCachePath, WnRace.FILE);
            NutMap meta = new NutMap();
            // 写入 token
            meta.put("ticket", ticket);
            // 设置过期时间: 保险期间，提前一分钟
            meta.put("expi", result.expiresOnDate().getTime() - 120000);

            // 更新缓存
            io.appendMeta(oCache, meta);
        }

        return ticket;
    }

}
