package org.nutz.walnut.util.hdlcmd;

import org.nutz.walnut.impl.box.WnSystem;

public interface IHdl {

    public void invoke(WnSystem sys, HdlCtx ctx) throws Exception;

}
