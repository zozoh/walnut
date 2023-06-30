package org.nutz.walnut.ext.media.edi.bean;

import java.util.LinkedList;
import java.util.List;

public class EdiMsgEntry {

    private EdiMsgSegment head;

    private EdiMsgSegment tail;

    private List<EdiMsgSegment> segments;

    public EdiMsgEntry() {}

    public EdiMsgEntry(EdiMsgSegment head) {
        this.head = head;
        this.segments = new LinkedList<>();
    }

    public EdiMsgSegment getHead() {
        return head;
    }

    public void setHead(EdiMsgSegment head) {
        this.head = head;
    }

    public EdiMsgSegment getTail() {
        return tail;
    }

    public void setTail(EdiMsgSegment tail) {
        this.tail = tail;
    }

    public List<EdiMsgSegment> getSegments() {
        return segments;
    }

    public void addSegments(EdiMsgSegment segment) {
        this.segments.add(segment);
    }

    public void setSegments(List<EdiMsgSegment> segments) {
        this.segments = segments;
    }

}
