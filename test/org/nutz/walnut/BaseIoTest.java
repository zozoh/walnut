package org.nutz.walnut;

import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.lang.Mirror;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.impl.io.WnIoImpl;

public abstract class BaseIoTest extends BaseStoreTest {

    protected WnIo io;

    protected void on_before(PropertiesProxy pp) {
        super.on_before(pp);

        io = new WnIoImpl();
        Mirror.me(io).setValue(io, "tree", tree);
        Mirror.me(io).setValue(io, "indexer", indexer);
        Mirror.me(io).setValue(io, "stores", storeFactory);
        Mirror.me(io).setValue(io, "mimes", mimes);
    }

}
