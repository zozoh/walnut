package com.site0.walnut.ext.media.edi.msg.reply;

import com.site0.walnut.ext.media.edi.bean.segment.ICS_UCI;
import com.site0.walnut.ext.media.edi.bean.segment.ICS_UCM;

import java.util.List;

/**
 * 控制信息的返回
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class EdiReplyCONTRL extends EdiReplyObj {

    private ICS_UCI uci;

    private List<ICS_UCM> msgs;

    public EdiReplyCONTRL() {
        super("CONTRL");
    }

    public ICS_UCI getUci() {
        return uci;
    }

    public void setUci(ICS_UCI uci) {
        this.uci = uci;
    }

    public List<ICS_UCM> getMsgs() {
        return msgs;
    }

    public void setMsgs(List<ICS_UCM> msgs) {
        this.msgs = msgs;
    }
}
