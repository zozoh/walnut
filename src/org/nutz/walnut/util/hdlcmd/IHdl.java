package org.nutz.walnut.util.hdlcmd;

import org.nutz.walnut.impl.box.WnSystem;

public abstract class IHdl {

    public abstract void invoke(WnSystem sys, HdlCtx ctx) throws Exception;

}
