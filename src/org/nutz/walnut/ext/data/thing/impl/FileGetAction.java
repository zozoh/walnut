package org.nutz.walnut.ext.data.thing.impl;

import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.data.thing.ThingDataAction;

public class FileGetAction extends ThingDataAction<WnObj> {

    public String fnm;

    public boolean quiet;

    @Override
    public WnObj invoke() {
        WnObj oDir = this.myDir();
        WnObj oM = io.fetch(oDir, fnm);
        if (null == oM && !quiet) {
            throw Er.create("e.thing.get." + dirName + ".noexists", oDir.path() + "/" + fnm);
        }
        return oM;
    }

}
