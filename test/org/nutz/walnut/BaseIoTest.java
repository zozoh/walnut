package org.nutz.walnut;

import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.lang.Mirror;
import org.nutz.mongo.ZMoCo;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.api.io.WnTree;
import org.nutz.walnut.impl.io.WnBean;
import org.nutz.walnut.impl.io.WnIoImpl;
import org.nutz.walnut.impl.io.mongo.MongoWnTree;

public abstract class BaseIoTest extends BaseStoreTest {

    protected WnIo io;

    protected void on_before(PropertiesProxy pp) {
        super.on_before(pp);

        WnTree tree = _create_tree();
        tree._clean_for_unit_test();

        io = new WnIoImpl();

        Mirror.me(io).setValue(io, "tree", tree);
        Mirror.me(io).setValue(io, "store", store);
        Mirror.me(io).setValue(io, "mimes", mimes);
    }

    private WnTree _create_tree() {
        ZMoCo co = db.getCollectionByMount("mongo:obj");

        String id = pp.get("root-id");

        WnObj root = new WnBean();
        root.id(id);
        root.path("/");
        root.race(WnRace.DIR);
        root.name("");
        root.lastModified(System.currentTimeMillis());
        root.createTime(System.currentTimeMillis());
        root.creator("root").mender("root").group("root");
        root.mode(0755);

        return new MongoWnTree(co, root, mimes);
    }

}
