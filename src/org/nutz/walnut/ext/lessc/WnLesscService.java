package org.nutz.walnut.ext.lessc;

import javax.script.ScriptException;

import org.nutz.lessc4j.LesscService;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;

public class WnLesscService extends LesscService {

    protected WnIo io;
    protected WnObj base;

    public WnLesscService(WnIo io) {
        this.io = io;
    }

    public synchronized String renderWnObj(WnObj wobj, WnObj base) throws ScriptException {
        if (base == null)
            this.base = wobj.parent();
        else
            this.base = base;
        return render(io.readText(wobj));
    }

    public String readLess(String path) {
        return io.readText(io.check(base, path));
    }
}
