package org.nutz.walnut.core.mapping.indexer;

import java.util.Map;

import org.nutz.walnut.api.io.WnIoIndexer;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.core.indexer.mongo.MongoIndexer;
import org.nutz.walnut.core.mapping.WnIndexerFactory;

public class MongoIndexerFactory implements WnIndexerFactory {

    private Map<String, MongoIndexer> indexers;

    @Override
    public WnIoIndexer load(WnObj oHome, String str) {
        return indexers.get(str);
    }

}
