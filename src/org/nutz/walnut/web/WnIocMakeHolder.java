package org.nutz.walnut.web;

import org.nutz.mongo.ZMoCo;
import org.nutz.walnut.api.io.WnIndexer;
import org.nutz.walnut.api.io.WnNode;
import org.nutz.walnut.api.io.WnTree;
import org.nutz.walnut.api.io.WnTreeFactory;
import org.nutz.walnut.impl.io.mongo.MongoDB;
import org.nutz.walnut.impl.io.mongo.MongoWnIndexer;

public class WnIocMakeHolder {

    private WnTree tree;

    private WnIndexer indexer;

    public WnIocMakeHolder(WnTreeFactory tf, WnNode rootNode, MongoDB db, String coName) {
        tree = tf.check(rootNode);
        ZMoCo co = db.getCollection(coName);
        indexer = new MongoWnIndexer(co);
    }

    public WnTree getTree() {
        return tree;
    }

    public WnIndexer getIndexer() {
        return indexer;
    }

}
