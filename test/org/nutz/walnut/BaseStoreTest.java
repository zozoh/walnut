package org.nutz.walnut;

import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.lang.Lang;
import org.nutz.walnut.api.io.WnStore;

public abstract class BaseStoreTest extends BaseApiTest {

    protected WnStore store;

    protected void on_before(PropertiesProxy pp) {
        super.on_before(pp);

        store = _create_store();
        store._clean_for_unit_test();
    }

    private WnStore _create_store() {
        throw Lang.noImplement();
    }
}
