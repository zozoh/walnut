package org.nutz.walnut.ext.www.hdl;

import org.nutz.walnut.ext.www.WWWContext;
import org.nutz.walnut.ext.www.WWWHdl;
import org.nutz.walnut.impl.box.WnSystem;

public class www_html implements WWWHdl {

    @Override
    public void invoke(WnSystem sys, WWWContext wwc) {
        sys.out.println(wwc.input);
    }

}
