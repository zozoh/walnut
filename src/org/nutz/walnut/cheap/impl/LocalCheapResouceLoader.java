package org.nutz.walnut.cheap.impl;

import java.io.File;

import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.lang.Files;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.MimeMap;
import org.nutz.walnut.cheap.api.CheapResourceLoader;
import org.nutz.walnut.cheap.dom.bean.CheapResource;
import org.nutz.walnut.core.MimeMapImpl;
import org.nutz.walnut.util.Wlang;

public class LocalCheapResouceLoader implements CheapResourceLoader {

    private File home;

    private MimeMap mimes;

    public LocalCheapResouceLoader(String homePath) {
        this(new File(homePath));
    }

    public LocalCheapResouceLoader(File home) {
        if (null == home || !home.exists()) {
            throw Er.create("e.LCRL.home.noexists", home);
        }
        if (!home.isDirectory()) {
            throw Er.create("e.LCRL.home.mustBeDir", home);
        }
        this.home = home;
        PropertiesProxy pp = new PropertiesProxy("mime.properties");
        mimes = new MimeMapImpl(pp);
    }

    public LocalCheapResouceLoader(File home, MimeMap mimes) {
        this.home = home;
        this.mimes = mimes;
    }

    @Override
    public CheapResource loadByPath(String path) {
        File f = Files.getFile(home, path);
        return _by(f);
    }

    private CheapResource _by(File f) {
        if (null != f) {
            CheapResource cr = new CheapResource();
            cr.setAlt(Files.getMajorName(f.getName()));
            cr.setName(f.getName());
            byte[] bs = Files.readBytes(f);
            cr.setContent(bs);
            return cr;
        }
        return null;
    }

    @Override
    public CheapResource loadById(String id) {
        throw Wlang.noImplement();
    }

    @Override
    public String getMime(String typeName) {
        return mimes.getMime(typeName);
    }

}
