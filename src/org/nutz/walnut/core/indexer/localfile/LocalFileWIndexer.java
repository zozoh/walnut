package org.nutz.walnut.core.indexer.localfile;

import java.io.File;
import java.io.IOException;

import org.nutz.lang.Files;
import org.nutz.lang.Strings;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.MimeMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;

public class LocalFileWIndexer extends LocalFileIndexer {

    public LocalFileWIndexer(WnObj oHome, File dHome, MimeMap mimes) {
        super(oHome, dHome, mimes);
    }

    @Override
    public WnObj create(WnObj p, String path, WnRace race) {
        File d = this._check_file_by(p);
        File f = Files.getFile(d, path);
        if (f.exists()) {
            throw Er.create("e.io.exists", path);
        }
        try {
            Files.createNewFile(f);
            return this._gen_file_obj(f);
        }
        catch (IOException e) {
            throw Er.wrap(e);
        }

    }

    @Override
    public WnObj create(WnObj p, String[] paths, int fromIndex, int toIndex, WnRace race) {
        int len = toIndex - fromIndex;
        String path = Strings.join(fromIndex, len, "/", paths);
        return create(p, path, race);
    }

    @Override
    public WnObj createById(WnObj p, String id, String name, WnRace race) {
        return create(p, name, race);
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
                Files.deleteDir(f);
            } else {
                Files.deleteFile(f);
            }
        }

    }

}
