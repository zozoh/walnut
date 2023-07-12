package org.nutz.walnut.ext.media.edi.bean;

import java.util.LinkedList;
import java.util.List;

import org.nutz.walnut.ext.media.edi.bean.segment.SG_UNB;
import org.nutz.walnut.ext.media.edi.util.EdiInterchangeParsing;

public class EdiInterchange extends EdiItem {

    public static EdiInterchange parse(String input) {
        EdiInterchange ic = new EdiInterchange();
        ic.valueOf(input);
        return ic;
    }

    private EdiSegment head;

    private EdiSegment tail;

    private List<EdiMessage> entries;

    public EdiInterchange() {
        super(null);
    }

    public EdiInterchange valueOf(String input) {
        // 寻找第一个报文头
        int pos = input.indexOf('\n');
        if (pos > 0) {
            String una = input.substring(0, pos).trim();
            advice = new EdiAdvice(una);

            // 逐个解析后面的行
            char[] cs = input.substring(pos + 1).trim().toCharArray();
            this.entries = new LinkedList<>();
            EdiInterchangeParsing ing = new EdiInterchangeParsing(advice, cs);
            EdiSegment seg = ing.nextSegment();
            EdiMessage en = null;
            while (null != seg) {
                // 记入消息
                if (null != en) {
                    // 结束消息,记入当前包
                    if (seg.isTag("UNT")) {
                        en.setTailSegment(seg);
                        this.entries.add(en);
                        en = null;
                    }
                    // 报文行，记入消息
                    else {
                        en.addSegments(seg);
                    }
                }
                // 消息开始
                else if (seg.isTag("UNH")) {
                    en = new EdiMessage(advice, seg);
                }
                // 包开始
                else if (seg.isTag("UNB")) {
                    this.head = seg;

                }
                // 包结束
                else if (seg.isTag("UNZ")) {
                    this.tail = seg;
                    break;
                }
                seg = ing.nextSegment();
            }
        }
        return this;
    }

    @Override
    public void joinString(StringBuilder sb) {
        char[] endl = new char[]{this.advice.segment, '\n'};
        sb.append(advice.toString()).append('\n');
        if (null != this.head) {
            this.head.joinString(sb);
            sb.append(endl);
        }

        if (null != this.entries) {
            for (EdiMessage en : this.entries) {
                en.joinString(sb);
            }
        }

        if (null != this.tail) {
            this.tail.joinString(sb);
            sb.append(endl);
        }
    }

    /**
     * 对内部的每个Entry 都执行打包，并计算报文条数
     */
    public void packEntry() {
        if (null != this.entries) {
            for (EdiMessage en : this.entries) {
                en.packEntry();
            }
        }
    }

    public EdiMessage getEntry(int index) {
        return this.entries.get(index);
    }

    public EdiMessage getFirstMessage() {
        return this.getEntry(0);
    }

    public SG_UNB getHeader() {
        return new SG_UNB(head);
    }

    public EdiSegment getHeadSegment() {
        return head;
    }

    public void setHeadSegment(EdiSegment head) {
        this.head = head;
    }

    public EdiSegment getTailSegment() {
        return tail;
    }

    public void setTailSegment(EdiSegment tail) {
        this.tail = tail;
    }

    public List<EdiMessage> getEntries() {
        return entries;
    }

    public void setEntries(List<EdiMessage> entries) {
        this.entries = entries;
    }

    public void addEntry(EdiMessage entry) {
        this.entries.add(entry);
    }

}
