package com.site0.walnut.core.mapping.indexer;

import java.util.Map;

import com.site0.walnut.api.io.WnIoIndexer;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.core.indexer.mongo.MongoIndexer;
import com.site0.walnut.core.mapping.WnIndexerFactory;

public class MongoIndexerFactory implements WnIndexerFactory {

    private Map<String, MongoIndexer> indexers;

    @Override
    public WnIoIndexer load(WnObj oHome, String str) {
        return indexers.get(str);
    }

}
