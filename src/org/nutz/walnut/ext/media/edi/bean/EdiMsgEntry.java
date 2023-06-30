package org.nutz.walnut.ext.media.edi.bean;

import java.util.List;

public class EdiMsgEntry {

    private List<EdiMsgSegment> segments;

    public List<EdiMsgSegment> getSegments() {
        return segments;
    }

    public void setSegments(List<EdiMsgSegment> segments) {
        this.segments = segments;
    }

}
