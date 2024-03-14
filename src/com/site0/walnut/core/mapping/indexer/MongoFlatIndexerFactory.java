package com.site0.walnut.core.mapping.indexer;

import org.nutz.lang.Strings;
import org.nutz.mongo.ZMoCo;
import com.site0.walnut.api.io.MimeMap;
import com.site0.walnut.api.io.WnIoIndexer;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.core.indexer.mongo.MongoFlatIndexer;
import com.site0.walnut.core.mapping.WnIndexerFactory;
import com.site0.walnut.util.MongoDB;

public class MongoFlatIndexerFactory implements WnIndexerFactory {

    private MimeMap mimes;

    private MongoDB db;

    public MongoFlatIndexerFactory(MimeMap mimes, MongoDB db) {
        this.mimes = mimes;
        this.db = db;
    }

    @Override
    public WnIoIndexer load(WnObj oHome, String str) {
        String coName = Strings.sBlank(str, "obj");
        ZMoCo co = db.getCollection(coName);

        return new MongoFlatIndexer(oHome, mimes, co);
    }

}
