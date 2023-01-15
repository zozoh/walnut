package org.nutz.walnut.ext.media.sheet.impl;

import org.nutz.lang.Strings;
import org.nutz.walnut.util.tmpl.WnTmpl;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.WnOutputable;
import org.nutz.walnut.ext.media.sheet.SheetHandler;

public abstract class AbstractSheetHandler implements SheetHandler {

    protected WnOutputable out;

    protected WnTmpl process;

    public void setProcess(WnOutputable out, String process) {
        this.out = out;
        this.process = WnTmpl.parse(process);
    }

    public void setProcess(WnOutputable out, WnTmpl process) {
        this.out = out;
        this.process = process;
    }

    protected void _on_process(int i, int len, NutBean obj) {
        if (null != out && null != process) {
            NutMap map = new NutMap();
            map.putAll(obj);
            map.put("P", String.format("%%[%d/%d]", i, len));
            map.put("I", i);
            String msg = process.render(map);
            out.println(msg);
        }
    }

    protected void _on_end(int len) {
        if (null != out && null != process) {
            out.println(Strings.dup('-', 20));
            out.printlnf("All done for %d records", len);
        }
    }

}
