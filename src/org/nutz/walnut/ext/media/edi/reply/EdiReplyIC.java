package org.nutz.walnut.ext.media.edi.reply;

import org.nutz.walnut.ext.media.edi.bean.segment.SG_UCI;

/**
 * 控制信息的返回
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class EdiReplyIC {

    private SG_UCI uci;

    public SG_UCI getUCI() {
        return uci;
    }

    public void setUCI(SG_UCI uci) {
        this.uci = uci;
    }

}
