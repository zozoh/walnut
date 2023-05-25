package org.nutz.walnut.ext.net.mailx.provider;

import org.nutz.walnut.ext.net.mailx.bean.MailxConfig;

import jakarta.mail.Store;

public interface MailStoreProvider {

    Store createStrore(MailxConfig xconf);

}
