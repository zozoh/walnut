package org.nutz.walnut.util.obj;

import java.io.InputStream;

import org.nutz.lang.util.NutBean;
import org.nutz.walnut.api.auth.WnAuthSession;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.stream.WnInputStreamFactory;
import org.nutz.walnut.util.stream.WnInputStreamInfo;

public class WnObjGetter implements WnInputStreamFactory {

    private InputStream stdInput;

    private NutBean vars;

    private WnIo io;

    public WnObjGetter(WnSystem sys) {
        this(sys.io, sys.session);
        this.stdInput = sys.in.getInputStream();
    }

    public WnObjGetter(WnIo io, WnAuthSession session) {
        this(io, session.getVars());
    }

    public WnObjGetter(WnIo io, NutBean vars) {
        this.io = io;
        this.vars = vars;
    }

    @Override
    public WnInputStreamInfo getStreamInfo(String path) {
        WnInputStreamInfo info = new WnInputStreamInfo();
        if (">>INPUT".equals(path)) {
            info.stream = stdInput;
            return info;
        }
        WnObj o = this.checkObj(path);
        info.stream = this.io.getInputStream(o, 0);
        info.name = o.name();
        info.mime = o.mime();
        info.length = o.len();
        return info;
    }

    public WnObj getObj(String ph) {
        return Wn.getObj(io, vars, ph);
    }

    public WnObj checkObj(String ph) {
        return Wn.checkObj(io, vars, ph);
    }

    public InputStream getStdInput() {
        return stdInput;
    }

    public void setStdInput(InputStream stdInput) {
        this.stdInput = stdInput;
    }

}
