package org.nutz.walnut.core.indexer.dao.obj.race;

import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.core.indexer.dao.obj.WnObjEjecting;

public class WnObjRaceEjecting extends WnObjEjecting {

    public WnObjRaceEjecting(String stdName) {
        super(stdName);
    }

    @Override
    public Object eject(Object obj) {
        Object val = super.eject(obj);
        // 默认当作文件
        if (null == val || WnRace.FILE == val || "FILE".equals(val) || "1".equals(val)) {
            return 1;
        }
        // 目录？
        if ("DIR".equals(val) || WnRace.DIR == val || "0".equals(val)) {
            return 0;
        }
        // 默认文件吧
        return 1;
    }

}
