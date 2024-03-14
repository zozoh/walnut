package com.site0.walnut.core.mapping.indexer;

import java.io.File;

import org.nutz.lang.Files;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.MimeMap;
import com.site0.walnut.api.io.WnIoIndexer;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.core.indexer.localfile.LocalFileWIndexer;
import com.site0.walnut.core.mapping.WnIndexerFactory;

public class LocalFileWIndexerFactory implements WnIndexerFactory {

    private MimeMap mimes;

    public LocalFileWIndexerFactory() {}

    public LocalFileWIndexerFactory(MimeMap mimes) {
        this.mimes = mimes;
    }

    @Override
    public WnIoIndexer load(WnObj oHome, String str) {
        File dHome = Files.findFile(str);
        if (null == dHome) {
            throw Er.create("e.io.mapping.filew.noexists", str);
        }
        return new LocalFileWIndexer(oHome, mimes, dHome);
    }
    
}
