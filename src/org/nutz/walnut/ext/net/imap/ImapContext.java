package org.nutz.walnut.ext.net.imap;

import javax.mail.Message;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.impl.box.JvmFilterContext;

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;

public class ImapContext extends JvmFilterContext {

    public IMAPStore store;

    public IMAPFolder folder;

    public NutMap summary;

    public Message[] messages;

}
