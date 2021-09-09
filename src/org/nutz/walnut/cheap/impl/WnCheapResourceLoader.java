package org.nutz.walnut.cheap.impl;

import org.nutz.lang.util.NutBean;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.cheap.api.CheapResourceLoader;
import org.nutz.walnut.cheap.dom.bean.CheapResource;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

public class WnCheapResourceLoader implements CheapResourceLoader {

    private WnIo io;

    private NutBean vars;

    public WnCheapResourceLoader(WnSystem sys) {
        this.io = sys.io;
        this.vars = sys.session.getVars();
    }

    public WnCheapResourceLoader(WnIo io, NutBean vars) {
        this.io = io;
        this.vars = vars;
    }

    @Override
    public CheapResource loadByPath(String path) {
        String aph = Wn.normalizeFullPath(path, vars);
        WnObj o = io.fetch(null, aph);
        return _by(o);
    }

    private CheapResource _by(WnObj o) {
        if (null != o && o.isFILE()) {
            CheapResource cr = new CheapResource();
            cr.setAlt(o.getString("title"));
            cr.setName(o.name());
            cr.setWidth(o.getInt("width"));
            cr.setHeight(o.getInt("height"));
            byte[] bs = io.readBytes(o);
            cr.setContent(bs);
            return cr;
        }
        return null;
    }

    @Override
    public CheapResource loadById(String id) {
        WnObj o = io.get(id);
        return _by(o);
    }

}
