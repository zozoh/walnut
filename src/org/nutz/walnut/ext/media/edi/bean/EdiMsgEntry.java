package org.nutz.walnut.ext.media.edi.bean;

import java.util.LinkedList;
import java.util.List;

public class EdiMsgEntry extends EdiMsgItem {

    private EdiMsgSegment head;

    private EdiMsgSegment tail;

    private List<EdiMsgSegment> segments;

    public EdiMsgEntry(EdiMsgAdvice advice) {
        super(advice);
    }

    public EdiMsgEntry(EdiMsgAdvice advice, EdiMsgSegment head) {
        this(advice);
        this.head = head;
        this.segments = new LinkedList<>();
    }

    public void joinString(StringBuilder sb) {
        char[] endl = new char[]{'\n'};
        if (null != this.head) {
            this.head.joinString(sb);
            sb.append(endl);
        }

        if (null != this.segments) {
            for (EdiMsgSegment seg : this.segments) {
                seg.joinString(sb);
                sb.append(this.advice.segment).append(endl);
            }
        }

        if (null != this.tail) {
            this.tail.joinString(sb);
            sb.append(endl);
        }
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
