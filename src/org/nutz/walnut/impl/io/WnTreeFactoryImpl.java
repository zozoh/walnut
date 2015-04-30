package org.nutz.walnut.impl.io;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.nutz.lang.Files;
import org.nutz.mongo.ZMoCo;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnNode;
import org.nutz.walnut.api.io.WnTree;
import org.nutz.walnut.api.io.WnTreeFactory;
import org.nutz.walnut.impl.io.local.LocalWnTree;
import org.nutz.walnut.impl.io.mongo.MongoDB;
import org.nutz.walnut.impl.io.mongo.MongoWnTree;

public class WnTreeFactoryImpl implements WnTreeFactory {

    private Map<String, WnTree> cache;

    private MongoDB mongodb;

    public WnTreeFactoryImpl(MongoDB db) {
        cache = new HashMap<String, WnTree>();
        mongodb = db;
    }

    @Override
    public WnTree get(String key) {
        return cache.get(key);
    }

    @Override
    public WnTree check(WnNode nd) {
        String key = nd.id();
        WnTree tree = cache.get(key);
        if (null == tree) {
            synchronized (this) {
                tree = cache.get(key);
                if (null == tree) {
                    String mnt = nd.mount();
                    // 本地文件
                    if (mnt.startsWith("file://")) {
                        tree = _create_local_tree(nd, mnt);
                    }
                    // MongoDB
                    else if (mnt.startsWith("mongo:")) {
                        tree = _create_mongo_tree(nd, mnt);
                    }
                    // 不支持的挂载类型
                    else {
                        throw Er.create("e.io.tree.unsupport.mnt", mnt);
                    }
                    // 计入缓存
                    cache.put(key, tree);
                }
            }
        }
        return tree;
    }

    private WnTree _create_mongo_tree(WnNode nd, String mnt) {
        // 得到集合
        ZMoCo co = mongodb.getCollectionByMount(mnt);

        // 生成树实例
        WnTree tree = new MongoWnTree(this, co);

        // 搞定，返回
        tree.setTreeNode(nd);
        return tree;
    }

    private WnTree _create_local_tree(WnNode nd, String mnt) {
        WnTree tree = new LocalWnTree(this);
        // 得到本地目录
        String localPath = mnt.substring("file://".length());
        File d = Files.createDirIfNoExists(localPath);
        if (!d.isDirectory())
            throw Er.create("e.io.tree.mount.nodir", mnt);

        // 搞定，返回
        tree.setTreeNode(nd);
        return tree;
    }

}
