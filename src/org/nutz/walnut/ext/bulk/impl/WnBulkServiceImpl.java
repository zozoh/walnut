package org.nutz.walnut.ext.bulk.impl;

import org.nutz.lang.Lang;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.bulk.api.BulkIgnores;
import org.nutz.walnut.ext.bulk.api.BulkIo;
import org.nutz.walnut.ext.bulk.api.BulkRestore;
import org.nutz.walnut.ext.bulk.api.BulkService;

public class WnBulkServiceImpl implements BulkService {

    @Override
    public String backup(WnObj oHome, BulkIgnores ignores, BulkIo buIo) {
        throw Lang.noImplement();
    }

    @Override
    public void restore(WnObj oTarget,
                        String histroyId,
                        BulkIgnores ignores,
                        BulkIo buIo,
                        BulkRestore setting) {
        throw Lang.noImplement();
    }

}
