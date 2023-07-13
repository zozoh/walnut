package org.nutz.walnut.ext.media.edi.reply;

import org.nutz.walnut.ext.media.edi.bean.segment.ICS_UCI;

/**
 * 控制信息的返回
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class EdiReplyIC {

    private ICS_UCI uci;

    public ICS_UCI getUCI() {
        return uci;
    }

    public void setUCI(ICS_UCI uci) {
        this.uci = uci;
    }

}
