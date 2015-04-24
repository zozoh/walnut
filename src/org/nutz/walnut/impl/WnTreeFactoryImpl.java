package org.nutz.walnut.impl;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.nutz.lang.Files;
import org.nutz.lang.Strings;
import org.nutz.mongo.ZMoCo;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnTree;
import org.nutz.walnut.api.io.WnTreeFactory;
import org.nutz.walnut.impl.local.LocalWnNode;
import org.nutz.walnut.impl.local.LocalWnTree;
import org.nutz.walnut.impl.mongo.MongoDB;
import org.nutz.walnut.impl.mongo.MongoWnNode;
import org.nutz.walnut.impl.mongo.MongoWnTree;

public class WnTreeFactoryImpl implements WnTreeFactory {

    private Map<String, WnTree> cache;

    private MongoDB mongodb;

    public WnTreeFactoryImpl(MongoDB db) {
        cache = new HashMap<String, WnTree>();
        mongodb = db;
    }

    @Override
    public WnTree check(String path, String mnt) {
        if (Strings.isBlank(mnt)) {
            return null;
        }
        WnTree tree = cache.get(mnt);
        if (null == tree) {
            synchronized (this) {
                tree = cache.get(mnt);
                if (null == tree) {
                    // 本地文件
                    if (mnt.startsWith("file://")) {
                        tree = _create_local_tree(path, mnt);
                    }
                    // MongoDB
                    else if (mnt.startsWith("mongo:")) {
                        tree = _create_mongo_tree(path, mnt);
                    }
                    // 不支持的挂载类型
                    else {
                        throw Er.create("e.io.tree.unsupport.mnt", mnt);
                    }
                    // 计入缓存
                    cache.put(mnt, tree);
                }
            }
        }
        return tree;
    }

    private WnTree _create_mongo_tree(String path, String mnt) {
        // 得到集合
        ZMoCo co = mongodb.getCollectionByMount(mnt);

        // 生成树实例
        WnTree tree = new MongoWnTree(this, co);

        // 创建树的顶级节点
        MongoWnNode nd = new MongoWnNode();
        nd.setTree(tree);
        nd.id(mnt);
        nd.path(path);
        nd.mount(mnt);
        nd.name(mnt);

        // 搞定，返回
        tree.setTreeNode(nd);
        return tree;
    }

    private WnTree _create_local_tree(String path, String mnt) {
        WnTree tree = new LocalWnTree(this);
        // 得到本地目录
        String localPath = mnt.substring("file://".length());
        File d = Files.createDirIfNoExists(localPath);
        if (!d.isDirectory())
            throw Er.create("e.io.tree.mount.nodir", mnt);

        // 创建树的顶级节点
        LocalWnNode nd = new LocalWnNode(d);
        nd.setTree(tree);
        nd.id(mnt);
        nd.path(path);
        nd.mount(mnt);

        // 搞定，返回
        tree.setTreeNode(nd);
        return tree;
    }

}
