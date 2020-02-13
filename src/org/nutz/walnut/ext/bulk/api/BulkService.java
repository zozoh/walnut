package org.nutz.walnut.ext.bulk.api;

import org.nutz.walnut.api.io.WnObj;

public interface BulkService {

    /**
     * 针对源目录做一次增量备份
     * 
     * @param oHome
     *            源目录。必须是目录，否则抛错
     * @param ignores
     *            备份配置。如果为 null 则自动读取 `oHome/.bulk/bulk.json`
     * @param buIo
     *            备份包读写接口
     * @return 增量备份包编号
     */
    String backup(WnObj oHome, BulkIgnores ignores, BulkIo buIo);

    void restore(WnObj oTarget,
                 String histroyId,
                 BulkIgnores ignores,
                 BulkIo buIo,
                 BulkRestoreSetting setting);
}
