package org.nutz.walnut.impl;

import java.util.HashMap;
import java.util.Map;

import org.nutz.lang.Lang;
import org.nutz.mongo.ZMoCo;
import org.nutz.walnut.api.io.WnIndexer;
import org.nutz.walnut.api.io.WnNode;
import org.nutz.walnut.api.io.WnStore;
import org.nutz.walnut.api.io.WnStoreFactory;
import org.nutz.walnut.api.io.WnStoreTable;
import org.nutz.walnut.impl.local.sha1.LocalSha1WnStore;
import org.nutz.walnut.impl.mongo.MongoDB;
import org.nutz.walnut.impl.mongo.MongoWnStoreTable;

public class WnStoreFactoryImpl implements WnStoreFactory {

    private Map<String, WnStore> cache;

    private MongoDB mongodb;

    private WnIndexer indexer;

    private String homePath;

    public WnStoreFactoryImpl(WnIndexer indexer, MongoDB db, String home) {
        this.indexer = indexer;
        this.mongodb = db;
        this.homePath = home;
        this.cache = new HashMap<String, WnStore>();
    }

    @Override
    public WnStore get(WnNode nd) {
        String mnt = nd.tree().getTreeNode().mount();

        WnStore store = cache.get(mnt);
        if (null == store) {
            synchronized (this) {
                store = cache.get(mnt);
                if (null == store) {
                    // 如果是 MongoDB
                    if (mnt.startsWith("mongo:")) {
                        ZMoCo co = mongodb.getCollectionByMount("mongo:his");

                        WnStoreTable table = new MongoWnStoreTable(co);
                        store = new LocalSha1WnStore(indexer, table, homePath);
                    }
                    // 如果映射到一个本地目录
                    else if (mnt.startsWith("file://")) {
                        throw Lang.makeThrow("noImplement: %s", mnt);
                    }
                    // 靠，不支持
                    else {
                        throw Lang.noImplement();
                    }
                    // 计入缓存
                    cache.put(mnt, store);
                }
            }
        }

        return store;
    }
}
