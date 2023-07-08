package org.nutz.walnut.ext.media.edi.bean;

import java.util.LinkedList;
import java.util.List;

import org.nutz.walnut.ext.media.edi.util.EdiMsgParsing;

public class EdiMsgPack extends EdiMsgItem {

    public static EdiMsgPack parse(String input) {
        EdiMsgPack pack = new EdiMsgPack();
        pack.valueOf(input);
        return pack;
    }

    private EdiMsgSegment head;

    private EdiMsgSegment tail;

    private List<EdiMsgEntry> entries;

    public EdiMsgPack() {
        super(null);
    }

    public EdiMsgPack valueOf(String input) {
        // 寻找第一个报文头
        int pos = input.indexOf('\n');
        if (pos > 0) {
            String una = input.substring(0, pos).trim();
            advice = new EdiMsgAdvice(una);

            // 逐个解析后面的行
            char[] cs = input.substring(pos + 1).trim().toCharArray();
            this.entries = new LinkedList<>();
            EdiMsgParsing ing = new EdiMsgParsing(advice, cs);
            EdiMsgSegment seg = ing.nextSegment();
            EdiMsgEntry en = null;
            while (null != seg) {
                // 记入消息
                if (null != en) {
                    // 结束消息,记入当前包
                    if (seg.isTag("UNT")) {
                        en.setTail(seg);
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
                    en = new EdiMsgEntry(advice, seg);
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
            for (EdiMsgEntry en : this.entries) {
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
            for (EdiMsgEntry en : this.entries) {
                en.packEntry();
            }
        }
    }

    public EdiMsgEntry getEntry(int index) {
        return this.entries.get(index);
    }

    public EdiMsgEntry getFirstEntry() {
        return this.getEntry(0);
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

    public List<EdiMsgEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<EdiMsgEntry> entries) {
        this.entries = entries;
    }

    public void addEntry(EdiMsgEntry entry) {
        this.entries.add(entry);
    }

}
