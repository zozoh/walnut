package org.nutz.walnut.ext.media.lessc;

import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptException;

import org.nutz.lessc4j.LesscService;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;

public class WnLesscService extends LesscService {

    protected WnIo io;
    protected List<WnObj> bases;

    /**
     * @param wobj
     *            要渲染的 less 文件对象
     * @param bases
     *            基础引入路径
     * @param pris
     *            高优先级引入路径
     * @return 渲染的 css 结果
     * @throws ScriptException
     */
    public synchronized String renderWnObj(WnObj wobj, List<WnObj> bases, List<WnObj> pris)
            throws ScriptException {
        int size = null == bases ? 0 : bases.size();
        if (null != pris)
            size += pris.size();
        this.bases = new ArrayList<>(size + 1);
        if (null != pris && pris.size() > 0)
            this.bases.addAll(pris);
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
