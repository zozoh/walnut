package org.nutz.walnut.ext.media.edi.reply;

import org.nutz.walnut.ext.media.edi.bean.segment.ICS_UCI;

/**
 * 控制信息的返回
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class EdiReplyCONTRL extends EdiReplyObj {

    private ICS_UCI uci;

    public EdiReplyCONTRL() {
        super("CONTRL");
    }

    public ICS_UCI getUci() {
        return uci;
    }

    public void setUci(ICS_UCI uci) {
        this.uci = uci;
    }

}
