package org.nutz.walnut.ext.net.sendmail.bean;

import org.nutz.lang.Strings;
import org.nutz.lang.util.NutBean;

public class WnMailReceiver {

    private String account;

    private String name;

    public WnMailReceiver() {}

    public WnMailReceiver(String str) {
        String[] ss = Strings.splitIgnoreBlank(str, "=");
        this.account = ss[0];
        if (ss.length > 1) {
            this.name = ss[1];
        }
    }

    public WnMailReceiver(String account, String name) {
        this.account = account;
        this.name = name;
    }

    public WnMailReceiver(NutBean bean) {
        this.account = bean.getString("account");
        this.name = bean.getString("name");
    }

    public String toString() {
        if (this.hasName()) {
            return String.format("%s <%s>", name, account);
        }
        return account;
    }

    public WnMailReceiver clone() {
        return new WnMailReceiver(account, name);
    }

    public boolean hasAccount() {
        return !Strings.isBlank(account);
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public boolean hasName() {
        return !Strings.isBlank(name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
