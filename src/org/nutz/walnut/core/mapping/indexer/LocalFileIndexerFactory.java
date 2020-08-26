package org.nutz.walnut.core.mapping.indexer;

import java.io.File;

import org.nutz.lang.Files;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.MimeMap;
import org.nutz.walnut.api.io.WnIoIndexer;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.core.indexer.localfile.LocalFileIndexer;
import org.nutz.walnut.core.mapping.WnIndexerFactory;

public class LocalFileIndexerFactory implements WnIndexerFactory {

    private MimeMap mimes;

    public LocalFileIndexerFactory() {}

    public LocalFileIndexerFactory(MimeMap mimes) {
        this.mimes = mimes;
    }

    @Override
    public WnIoIndexer load(WnObj oHome, String str) {
        File dHome = Files.findFile(str);
        if (null == dHome) {
            throw Er.create("e.io.mapping.file.noexists", str);
        }
        return new LocalFileIndexer(oHome, mimes, dHome);
    }

}
