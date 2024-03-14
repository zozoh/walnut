package com.site0.walnut.core.mapping.indexer;

import java.io.File;

import org.nutz.lang.Files;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.MimeMap;
import com.site0.walnut.api.io.WnIoIndexer;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.core.indexer.localfile.LocalFileIndexer;
import com.site0.walnut.core.mapping.WnIndexerFactory;

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
