package com.site0.walnut.util.obj;

import java.io.InputStream;

import org.nutz.lang.util.NutBean;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.login.WnSession;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.stream.WnInputStreamFactory;
import com.site0.walnut.util.stream.WnInputStreamInfo;

public class WnObjGetter implements WnInputStreamFactory {

    private InputStream stdInput;

    private NutBean vars;

    private WnIo io;

    public WnObjGetter(WnSystem sys) {
        this(sys.io, sys.session);
        this.stdInput = sys.in.getInputStream();
    }

    public WnObjGetter(WnIo io, WnSession session) {
        this(io, session.getEnv());
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
