package org.nutz.walnut.ext.media.edi.bean.segment;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.media.edi.bean.EdiSegment;
import org.nutz.walnut.util.Wlang;

public class ICS_UNZ {

    private int messageCount;

    private String controlRefNumber;

    public ICS_UNZ() {}

    public ICS_UNZ(EdiSegment seg) {
        NutMap bean = new NutMap();
        String[] keys = Wlang.array(null, "messageCount", "controlRefNumber");
        seg.fillBean(bean, keys);
        this.valueOf(bean);
    }

    public ICS_UNZ valueOf(NutBean bean) {
        this.messageCount = bean.getInt("messageCount");
        this.controlRefNumber = bean.getString("controlRefNumber");
        return this;
    }

    public int getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(int messageCount) {
        this.messageCount = messageCount;
    }

    public String getControlRefNumber() {
        return controlRefNumber;
    }

    public void setControlRefNumber(String controlRefNumber) {
        this.controlRefNumber = controlRefNumber;
    }

}
