package com.site0.walnut.cheap.impl;

import org.nutz.lang.util.NutBean;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.cheap.api.CheapResourceLoader;
import com.site0.walnut.cheap.dom.bean.CheapResource;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;

public class WnCheapResourceLoader implements CheapResourceLoader {

    private WnIo io;

    private NutBean vars;

    public WnCheapResourceLoader(WnSystem sys) {
        this.io = sys.io;
        this.vars = sys.session.getEnv();
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

    @Override
    public String getMime(String typeName) {
        return this.io.mimes().getMime(typeName);
    }

}
