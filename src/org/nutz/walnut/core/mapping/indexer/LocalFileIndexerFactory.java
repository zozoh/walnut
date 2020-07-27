package org.nutz.walnut.core.mapping.indexer;

import java.io.File;

import org.nutz.lang.Files;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.MimeMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.core.WnIoIndexer;
import org.nutz.walnut.core.indexer.localfile.LocalFileIndexer;
import org.nutz.walnut.core.mapping.WnIndexerFactory;

public class LocalFileIndexerFactory implements WnIndexerFactory {

    private MimeMap mimes;

    @Override
    public WnIoIndexer load(WnObj oHome, String str) {
        File dHome = Files.findFile(str);
        if (null == dHome) {
            throw Er.create("e.io.mapping.file.noexists", str);
        }
        return new LocalFileIndexer(oHome, dHome, mimes);
    }

}
