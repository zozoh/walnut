package com.site0.walnut.ext.media.edi.bean;

import java.util.LinkedList;
import java.util.List;

import com.site0.walnut.ext.media.edi.bean.segment.ICS_UNH;
import com.site0.walnut.ext.media.edi.bean.segment.ICS_UNT;
import com.site0.walnut.ext.media.edi.util.EdiSegmentFinder;
import com.site0.walnut.util.Ws;

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

    public EdiSegment findSegment(String... tags) {
        if (null != segments) {
            for (EdiSegment seg : segments) {
                if (seg.is(tags)) {
                    return seg;
                }
            }
        }
        return null;
    }

    public List<EdiSegment> findSegments(String... tags) {
        List<EdiSegment> list = new LinkedList<>();
        if (null != segments) {
            for (EdiSegment seg : segments) {
                if (seg.is(tags)) {
                    list.add(seg);
                }
            }
        }
        return list;
    }

    public EdiSegmentFinder getFinder() {
        return new EdiSegmentFinder(this.segments);
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

    @Override
    public void joinTree(StringBuilder sb, int depth) {
        int r = depth - 1;
        String prefix = r > 0 ? Ws.repeat("|    ", r) : "";
        prefix += "|-- ";
        int i = 0;
        for (EdiSegment seg : segments) {
            sb.append('\n').append(prefix);
            sb.append('[').append(i++).append("] ");
            sb.append(seg.toString());
            seg.joinTree(sb, depth + 1);
        }
    }

    public void packSelf() {
        int n = 2;
        if (null != this.segments) {
            n += this.segments.size();
        }
        this.tail.setComponent(1, n);
    }

    public ICS_UNH getHeader() {
        return new ICS_UNH(head);
    }

    public EdiSegment getHeadSegment() {
        return head;
    }

    public void setHeadSegment(EdiSegment head) {
        this.head = head;
    }

    public int getSegmentCount() {
        return this.getTail().getSegmentCount();
    }

    public ICS_UNT getTail() {
        return new ICS_UNT(tail);
    }

    public EdiSegment getTailSegment() {
        return tail;
    }

    public void setTailSegment(EdiSegment tail) {
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
