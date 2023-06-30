package org.nutz.walnut.ext.media.edi.bean;

import java.util.List;

public class EdiMsgPack {

    private EdiMsgSetup setup;

    private List<EdiMsgEntry> entries;

    public EdiMsgPack valueOf(String input) {
        return this;
    }

    public EdiMsgSetup getSetup() {
        return setup;
    }

    public void setSetup(EdiMsgSetup setup) {
        this.setup = setup;
    }

    public List<EdiMsgEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<EdiMsgEntry> entries) {
        this.entries = entries;
    }

}
