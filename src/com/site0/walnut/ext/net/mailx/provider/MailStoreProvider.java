package com.site0.walnut.ext.net.mailx.provider;

import com.site0.walnut.ext.net.mailx.bean.MailxImapConifg;

import jakarta.mail.Session;
import jakarta.mail.Store;

public interface MailStoreProvider {

    Session createSession(MailxImapConifg imap);

    Store createStrore(Session session, MailxImapConifg imap);

}
