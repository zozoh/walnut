package org.nutz.walnut;

import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.lang.Mirror;
import org.nutz.mongo.ZMoCo;
import org.nutz.walnut.api.io.WnNode;
import org.nutz.walnut.api.io.WnTree;
import org.nutz.walnut.impl.io.WnTreeFactoryImpl;
import org.nutz.walnut.impl.io.mongo.MongoWnIndexer;

public abstract class BaseIndexerTest extends BaseApiTest {

    protected WnTree tree;

    protected void on_before(PropertiesProxy pp) {
        treeFactory = new WnTreeFactoryImpl(db);

        WnNode nd = _create_top_tree_node();
        tree = treeFactory.check(nd);
        nd.setTree(tree);
        tree._clean_for_unit_test();

        ZMoCo co = db.getCollectionByMount("mongo:obj");
        indexer = new MongoWnIndexer(co);
        Mirror.me(indexer).setValue(indexer, "mimes", mimes);
        indexer._clean_for_unit_test();
    }

}
