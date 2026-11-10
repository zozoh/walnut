package com.site0.walnut.core.indexer.localfile;

import java.io.File;
import java.io.IOException;

import com.site0.walnut.util.Wfiles;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.MimeMap;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;

public class LocalFileWIndexer extends LocalFileIndexer {

    public LocalFileWIndexer(WnObj oHome, MimeMap mimes, File dHome) {
        super(oHome, mimes, dHome);
    }

    @Override
    public WnObj create(WnObj p, String path, WnRace race) {
        File d = this._check_file_by(p);
        File f = Wfiles.getFile(d, path);
        if (f.exists()) {
            throw Er.create("e.io.exists", path);
        }
        try {
            if (WnRace.DIR == race) {
                Wfiles.makeDir(f);
            } else {
                Wfiles.createNewFile(f);
            }
            return this._gen_file_obj(p, f);
        }
        catch (IOException e) {
            throw Er.wrap(e);
        }
    }

    @Override
    public void delete(WnObj o) {
        if (null == o) {
            return;
        }
        if (this.isRoot(o)) {
            throw Er.create("e.io.localfile.deleteRoot", o.id());
        }
        File f = this._check_file_by(o);
        if (f.exists()) {
            if (f.isDirectory()) {
                Wfiles.deleteDir(f);
            } else {
                Wfiles.deleteFile(f);
            }
        }
    }

    @Override
    public WnObj rename(WnObj o, String nm) {
        return rename(o, nm, false);
    }

    @Override
    public WnObj rename(WnObj o, String nm, boolean keepType) {
        File f = this._check_file_by(o);
        // 如果要保持类型的话，就仅仅改主名
        if (keepType && o.hasType()) {
            String nwName = Wfiles.getMajorName(nm);
            String fType = o.type();
            nm = nwName + "." + fType;
        }
        // 执行改名
        Wfiles.rename(f, nm);
        return new WnLocalFileObj(root, dHome, f, mimes);
    }

    @Override
    public WnObj rename(WnObj o, String nm, int mode) {
        return rename(o, nm, false);
    }

}
