package org.nutz.walnut.ext.net.imap;

import java.util.ArrayList;
import java.util.List;

import javax.mail.Message;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.ext.net.imap.bean.WnEmailMessage;
import org.nutz.walnut.impl.box.JvmFilterContext;

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;

public class ImapContext extends JvmFilterContext {

    public IMAPStore store;

    public IMAPFolder folder;

    public NutMap summary;

    public Message[] messages;

    public List<WnEmailMessage> list;

    public void setMessages(Message[] messages) {
        this.messages = messages;

        list = new ArrayList<>(messages.length);

        // 逐个消息拆解
        try {
            for (Message msg : this.messages) {
                WnEmailMessage it = new WnEmailMessage(msg);
                list.add(it);
            }
        }
        catch (Exception e) {
            throw Er.create("e.imap.parseMessage", e);
        }
    }

    public boolean hasSummary() {
        return null != summary && !summary.isEmpty();
    }

    public boolean hasList() {
        return null != list && list.size() > 0;
    }

    public boolean hasMessages() {
        return null != messages && messages.length > 0;
    }

}
