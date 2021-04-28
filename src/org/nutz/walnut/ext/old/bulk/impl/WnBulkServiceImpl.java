package org.nutz.walnut.ext.old.bulk.impl;

import org.nutz.lang.Lang;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.old.bulk.api.BulkIgnores;
import org.nutz.walnut.ext.old.bulk.api.BulkIo;
import org.nutz.walnut.ext.old.bulk.api.BulkRestore;
import org.nutz.walnut.ext.old.bulk.api.BulkService;

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
