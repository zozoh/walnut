package org.nutz.walnut.ext.bulk;

import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.walnut.BaseSessionTest;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.bulk.api.BulkIo;
import org.nutz.walnut.ext.bulk.api.BulkService;
import org.nutz.walnut.ext.bulk.impl.WnBulkIoImpl;
import org.nutz.walnut.ext.bulk.impl.WnBulkServiceImpl;

public class BulkTest extends BaseSessionTest {

    protected WnObj oHome;
    protected BulkIo buIo;
    protected BulkService bulks;

    public BulkTest() {
        super();
    }

    @Override
    protected void on_before(PropertiesProxy pp) {
        super.on_before(pp);

        bulks = new WnBulkServiceImpl();
        buIo = new WnBulkIoImpl();
        oHome = io.check(null, me.getHomePath());
    }

}