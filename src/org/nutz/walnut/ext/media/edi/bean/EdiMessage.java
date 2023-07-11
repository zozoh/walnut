package org.nutz.walnut.ext.media.edi.bean;

import java.util.LinkedList;
import java.util.List;

public class EdiMessage extends EdiItem {

    private EdiSegment head;

    private EdiSegment tail;

    private List<EdiSegment> segments;

    public EdiMessage(EdiAdvice advice) {
        super(advice);
    }

    public EdiMessage(EdiAdvice advice, EdiSegment head) {
        this(advice);
        this.head = head;
        this.segments = new LinkedList<>();
    }

    public EdiSegment findSegment(String name) {
        if (null != segments) {
            for (EdiSegment seg : segments) {
                if (seg.isTag(name)) {
                    return seg;
                }
            }
        }
        return null;
    }

    public List<EdiSegment> findSegments(String name) {
        List<EdiSegment> list = new LinkedList<>();
        if (null != segments) {
            for (EdiSegment seg : segments) {
                if (seg.isTag(name)) {
                    list.add(seg);
                }
            }
        }
        return list;
    }

    public void joinString(StringBuilder sb) {
        char[] endl = new char[]{this.advice.segment, '\n'};
        if (null != this.head) {
            this.head.joinString(sb);
            sb.append(endl);
        }

        if (null != this.segments) {
            for (EdiSegment seg : this.segments) {
                seg.joinString(sb);
                sb.append(endl);
            }
        }

        if (null != this.tail) {
            this.tail.joinString(sb);
            sb.append(endl);
        }
    }

    public void packEntry() {
        int n = 2;
        if (null != this.segments) {
            n += this.segments.size();
        }
        this.tail.setComponent(1, n);
    }

    public EdiSegment getHead() {
        return head;
    }

    public void setHead(EdiSegment head) {
        this.head = head;
    }

    public EdiSegment getTail() {
        return tail;
    }

    public void setTail(EdiSegment tail) {
        this.tail = tail;
    }

    public List<EdiSegment> getSegments() {
        return segments;
    }

    public void addSegments(EdiSegment segment) {
        this.segments.add(segment);
    }

    public void setSegments(List<EdiSegment> segments) {
        this.segments = segments;
    }

}
