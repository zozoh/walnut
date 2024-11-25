package com.site0.walnut.ext.media.edi;

import java.util.ArrayList;
import java.util.List;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.ext.media.edi.bean.EdiInterchange;
import com.site0.walnut.impl.box.JvmFilterContext;
import com.site0.walnut.util.Ws;

public class EdiContext extends JvmFilterContext {

    public String raw_input;

    /**
     * 报文的原始渲染上下文
     */
    public String vars;

    /**
     * 报文文本
     */
    public String message;

    /**
     * 报文对象
     */
    public EdiInterchange ic;

    public String tidyMessage() {
        String[] lines = Ws.splitIgnoreBlank(message, "\r?\n");
        List<String> list = new ArrayList<>(lines.length);
        for (String line : lines) {
            String trimed = line.trim();
            if (!Ws.isBlank(trimed) && !trimed.startsWith("#")) {
                list.add(trimed);
            }
        }
        this.message = Ws.join(list, "\n");
        return message;
    }

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
