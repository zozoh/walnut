package com.site0.walnut.ext.media.edi.bean;

import java.util.LinkedList;
import java.util.List;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.ext.media.edi.bean.segment.ICS_UNB;
import com.site0.walnut.ext.media.edi.bean.segment.ICS_UNZ;
import com.site0.walnut.ext.media.edi.util.EdiInterchangeParsing;
import com.site0.walnut.util.Ws;

public class EdiInterchange {

    public static EdiInterchange parse(String input) {
        EdiInterchange ic = new EdiInterchange();
        try {
            ic.valueOf(input);
            return ic;
        }
        catch (Throwable e) {
            throw Er.create("e.edi.ParseFail", e);
        }
    }

    private EdiAdvice advice;

    private EdiSegment head;

    private EdiSegment tail;

    private List<EdiMessage> messages;

    public EdiInterchange() {}

    public EdiInterchange valueOf(String input) {
        input = input.trim();
        // 寻找第一个报文头: 直接截取前9个字符
        // UNA:+.? '
        if (input.startsWith("UNA")) {
            String una = input.substring(0, 9);
            advice = new EdiAdvice(una);
            input = input.substring(9);
        }

        // 逐个解析后面的行
        char[] cs = input.trim().toCharArray();
        this.messages = new LinkedList<>();
        EdiInterchangeParsing ing = new EdiInterchangeParsing(advice, cs);
        EdiSegment seg = ing.nextSegment();
        EdiMessage en = null;
        while (null != seg) {
            // 记入消息
            if (null != en) {
                // 结束消息,记入当前包
                if (seg.isTag("UNT")) {
                    en.setTailSegment(seg);
                    this.messages.add(en);
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
        return this;
    }

    public void joinString(StringBuilder sb) {
        char[] endl = new char[]{this.advice.segment, '\n'};
        sb.append(advice.toString()).append('\n');
        if (null != this.head) {
            this.head.joinString(sb);
            sb.append(endl);
        }

        if (null != this.messages) {
            for (EdiMessage en : this.messages) {
                en.joinString(sb);
            }
        }

        if (null != this.tail) {
            this.tail.joinString(sb);
            sb.append(endl);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        this.joinString(sb);
        return sb.toString();
    }

    public String toTree() {
        StringBuilder sb = new StringBuilder();
        String HR = Ws.repeat('.', 60);
        sb.append(advice);
        // ........................................
        sb.append("\n<HEAD>");
        if (null != head) {
            head.joinTree(sb, 1);
        }
        // ........................................
        if (null != this.messages) {
            int N = this.messages.size();
            sb.append(String.format("\n\n          <MESSAGES x %d>\n", N));
            int i = 0;
            for (EdiMessage msg : this.messages) {
                sb.append('\n').append(HR);
                sb.append(String.format("\n#<%d>: %s", i, msg.getHeadSegment()));
                msg.joinTree(sb, 1);
                sb.append(String.format("\n#<%d>: %s", i++, msg.getTailSegment()));
            }
            sb.append('\n').append(HR);
            sb.append(String.format("\n\n          <MESSAGES x %d>\n", N));
            sb.append("\n<TAIL>");
        }
        // ........................................
        if (null != tail) {
            tail.joinTree(sb, 1);
        }
        // ........................................
        return sb.toString();
    }

    /**
     * 对内部的每个Entry 都执行打包，并计算报文条数
     */
    public void packMessages() {
        int n = 0;
        if (null != this.messages) {
            for (EdiMessage msg : this.messages) {
                msg.packSelf();
            }
            n += this.messages.size();
        }
        this.tail.setComponent(1, n);
    }

    public EdiMessage getMessage(int index) {
        return this.messages.get(index);
    }

    public EdiMessage getFirstMessage() {
        return this.getMessage(0);
    }

    public EdiAdvice getAdvice() {
        return advice;
    }

    public void setAdvice(EdiAdvice advice) {
        this.advice = advice;
    }

    public ICS_UNB getHeader() {
        return new ICS_UNB(head);
    }

    public EdiSegment getHeadSegment() {
        return head;
    }

    public void setHeadSegment(EdiSegment head) {
        this.head = head;
    }

    public int getMessageCount() {
        return this.getTail().getMessageCount();
    }

    public ICS_UNZ getTail() {
        return new ICS_UNZ(tail);
    }

    public EdiSegment getTailSegment() {
        return tail;
    }

    public void setTailSegment(EdiSegment tail) {
        this.tail = tail;
    }

    public List<EdiMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<EdiMessage> entries) {
        this.messages = entries;
    }

    public void addEntry(EdiMessage entry) {
        this.messages.add(entry);
    }

}
