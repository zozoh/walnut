package org.nutz.walnut.ext.media.edi;

import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.ext.media.edi.bean.EdiInterchange;
import org.nutz.walnut.impl.box.JvmFilterContext;

public class EdiContext extends JvmFilterContext {

    /**
     * 报文文本
     */
    public String message;

    /**
     * 报文对象
     */
    public EdiInterchange ic;

    public void assertIC() {
        if (null == ic) {
            throw Er.create("e.cmd.edi.context.NilIC");
        }
    }

    public void assertMessage() {
        if (null == message) {
            throw Er.create("e.cmd.edi.context.NilMessage");
        }
    }
}
