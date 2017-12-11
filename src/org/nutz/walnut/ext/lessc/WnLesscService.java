package org.nutz.walnut.ext.lessc;

import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptException;

import org.nutz.lessc4j.LesscService;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;

public class WnLesscService extends LesscService {

    protected WnIo io;
    protected List<WnObj> bases;

    public synchronized String renderWnObj(WnObj wobj, List<WnObj> bases) throws ScriptException {
        int size = null == bases ? 0 : bases.size();
        this.bases = new ArrayList<>(size + 1);
        this.bases.add(wobj.parent());
        if (null != bases && bases.size() > 0)
            this.bases.addAll(bases);
        return render(io.readText(wobj));
    }

    public String readLess(String path) {
        // 遍历base,看看到底在哪里
        for (WnObj base : bases) {
            WnObj wobj = io.fetch(base, path);
            if (wobj != null)
                return io.readText(wobj);
        }
        return null;
    }

    public void setIo(WnIo io) {
        this.io = io;
    }

    public WnIo getIo() {
        return io;
    }
}
