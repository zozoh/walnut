package com.site0.walnut.ext.media.edi.bean.segment;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.ext.media.edi.bean.EdiSegment;
import com.site0.walnut.util.Wlang;

public class ICS_UNT {

    private int segmentCount;

    private String refNumber;

    public ICS_UNT() {}

    public ICS_UNT(EdiSegment seg) {
        NutMap bean = new NutMap();
        String[] keys = Wlang.array(null, "segmentCount", "refNumber");
        seg.fillBean(bean, keys);
        this.valueOf(bean);
    }

    public ICS_UNT valueOf(NutBean bean) {
        this.segmentCount = bean.getInt("segmentCount");
        this.refNumber = bean.getString("refNumber");
        return this;
    }

    public int getSegmentCount() {
        return segmentCount;
    }

    public void setSegmentCount(int messageCount) {
        this.segmentCount = messageCount;
    }

    public String getRefNumber() {
        return refNumber;
    }

    public void setRefNumber(String controlRefNumber) {
        this.refNumber = controlRefNumber;
    }
}
