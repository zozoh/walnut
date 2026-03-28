package com.site0.walnut.ext.media.edi.msg.reply.sam;

import java.util.ArrayList;

public class SamAdv {

    private String advId;

    private ArrayList<String> locList;

    private String advDesc;

    public String getAdvId() {
        return advId;
    }

    public void setAdvId(String advId) {
        this.advId = advId;
    }

    public ArrayList<String> getLocList() {
        return locList;
    }

    public void setLocList(ArrayList<String> locList) {
        this.locList = locList;
    }

    public String getAdvDesc() {
        return advDesc;
    }

    public void setAdvDesc(String advDesc) {
        this.advDesc = advDesc;
    }
}
