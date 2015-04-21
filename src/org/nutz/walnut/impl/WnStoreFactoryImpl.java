package org.nutz.walnut.impl;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.mongo.ZMoCo;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnIndexer;
import org.nutz.walnut.api.io.WnNode;
import org.nutz.walnut.api.io.WnStore;
import org.nutz.walnut.api.io.WnStoreFactory;
import org.nutz.walnut.api.io.WnStoreTable;
import org.nutz.walnut.impl.local.data.LocalDataWnStore;
import org.nutz.walnut.impl.local.sha1.LocalSha1WnStore;
import org.nutz.walnut.impl.local.tree.LocalTreeWnStore;
import org.nutz.walnut.impl.mongo.MongoDB;
import org.nutz.walnut.impl.mongo.MongoWnStoreTable;

public class WnStoreFactoryImpl implements WnStoreFactory {

    private Map<String, WnStore> cache;

    private MongoDB mongodb;

    private WnIndexer indexer;

    private String sha1HomePath;

    private String dataHomePath;

    public WnStoreFactoryImpl(WnIndexer indexer, MongoDB db, String sha1Home, String dataHome) {
        this.indexer = indexer;
        this.mongodb = db;
        this.sha1HomePath = sha1Home;
        this.dataHomePath = dataHome;
        this.cache = new HashMap<String, WnStore>();
    }

    @Override
    public WnStore get(WnNode nd) {
        WnNode treeNode = nd.tree().getTreeNode();
        String mnt = treeNode.mount();

        WnStore store = cache.get(mnt);
        if (null == store) {
            synchronized (this) {
                store = cache.get(mnt);
                if (null == store) {
                    // 如果是 MongoDB
                    if (mnt.startsWith("mongo:")) {
                        // 得到集合对象
                        ZMoCo co = mongodb.getCollectionByMount("mongo:his");

                        // 创建快速存储类（无 SHA1 历史记录支持）
                        if (mnt.endsWith("@quick")) {
                            store = new LocalDataWnStore(indexer, dataHomePath);
                        }
                        // 有 SHA1 历史记录支持的存储类
                        else {
                            WnStoreTable table = new MongoWnStoreTable(co);
                            store = new LocalSha1WnStore(indexer, table, sha1HomePath);
                        }
                    }
                    // 如果映射到一个本地目录
                    else if (mnt.startsWith("file://")) {
                        // 得到本地目录
                        String localPath = mnt.substring("file://".length());
                        File d = Files.createDirIfNoExists(localPath);
                        if (!d.isDirectory())
                            throw Er.create("e.io.store.mount.nodir", mnt);

                        // 创建存储类
                        store = new LocalTreeWnStore(indexer, d, treeNode.path());
                    }
                    // 靠，不支持
                    else {
                        throw Lang.noImplement();
                    }
                    // 计入缓存
                    cache.put(mnt, store);
                }
            }
        } // ~End if(null == store) {

        return store;
    }
}
