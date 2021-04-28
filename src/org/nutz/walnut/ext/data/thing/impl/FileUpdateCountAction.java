package org.nutz.walnut.ext.data.thing.impl;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.data.thing.ThingDataAction;
import org.nutz.walnut.ext.data.thing.util.Things;

public class FileUpdateCountAction extends ThingDataAction<WnObj> {

    @Override
    public WnObj invoke() {
        Things.update_file_count(io, oT, dirName, _Q());
        return oT;
    }

}
