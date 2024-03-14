package com.site0.walnut.ext.data.thing.impl;

import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.data.thing.ThingDataAction;
import com.site0.walnut.ext.data.thing.util.Things;

public class FileUpdateCountAction extends ThingDataAction<WnObj> {

    @Override
    public WnObj invoke() {
        Things.update_file_count(io, oT, dirName, _Q());
        return oT;
    }

}
