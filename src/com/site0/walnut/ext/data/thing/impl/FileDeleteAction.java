package com.site0.walnut.ext.data.thing.impl;

import java.util.ArrayList;
import java.util.List;

import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.data.thing.ThingDataAction;
import com.site0.walnut.ext.data.thing.util.Things;

public class FileDeleteAction extends ThingDataAction<List<WnObj>> {

    public String[] fnms;

    @Override
    public List<WnObj> invoke() {
        if (null == fnms)
            return null;

        WnObj oDir = this.myDir();
        List<WnObj> list = new ArrayList<>(fnms.length);
        for (int i = 0; i < fnms.length; i++) {
            String fnm = fnms[i];
            WnObj oM = io.fetch(oDir, fnm);
            if (null != oM) {
                io.delete(oM);
                list.add(oM);
            }
        }

        // 更新计数
        if (list.size() > 0)
            Things.update_file_count(io, oT, dirName, this._Q());

        return list;
    }

}
