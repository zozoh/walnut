package org.nutz.walnut.core.mapping.indexer;

import org.nutz.lang.Strings;
import org.nutz.mongo.ZMoCo;
import org.nutz.walnut.api.io.MimeMap;
import org.nutz.walnut.api.io.WnIoIndexer;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.core.indexer.mongo.MongoFlatIndexer;
import org.nutz.walnut.core.mapping.WnIndexerFactory;
import org.nutz.walnut.util.MongoDB;

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
