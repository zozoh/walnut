package org.nutz.walnut.ext.net.mailx.bean;

import org.nutz.lang.util.NutMap;

public class MailxImapProviderConfig {

    private String name;

    private NutMap setup;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public NutMap getSetup() {
        return setup;
    }

    public void setSetup(NutMap setup) {
        this.setup = setup;
    }

}
