package org.nutz.walnut;

import java.io.File;

import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.walnut.api.io.WnNode;
import org.nutz.walnut.impl.local.LocalWnNode;
import org.nutz.walnut.impl.mongo.MongoWnNode;

public class WnTUs {

    public static WnNode create_tree_node(PropertiesProxy pp, String key) {
        String mnt = pp.get(key);

        // 本地
        if (mnt.startsWith("file://")) {
            String localPath = mnt.substring("file://".length());
            File d = Files.createDirIfNoExists(localPath);

            LocalWnNode nd = new LocalWnNode(d);
            nd.id(mnt);
            nd.path("/");
            nd.mount(mnt);
            return nd;
        }
        // Mongo
        else if (mnt.startsWith("mongo:")) {
            MongoWnNode nd = new MongoWnNode();
            nd.id(mnt);
            nd.path("/");
            nd.mount(mnt);
            nd.name(mnt);
            return nd;
        }
        throw Lang.impossible();
    }

}
