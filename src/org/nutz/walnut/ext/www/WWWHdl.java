package org.nutz.walnut.ext.www;

import org.nutz.walnut.impl.box.WnSystem;

public interface WWWHdl {

    void invoke(WnSystem sys, WWWContext wwc);

}
