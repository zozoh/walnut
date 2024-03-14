package com.site0.walnut.ext.data.thing.impl;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.data.thing.ThingDataAction;
import com.site0.walnut.util.Wn;

public class FileGetAction extends ThingDataAction<WnObj> {

    public String fnm;

    public boolean quiet;

    @Override
    public WnObj invoke() {
        WnObj oDir = this.myDir();
        WnObj oM = io.fetch(oDir, fnm);
        if (null == oM && !quiet) {
            String tph;
            if (null == oDir) {
                tph = fnm;
            } else {
                tph = Wn.appendPath(oDir.path(), fnm);
            }
            throw Er.create("e.thing.fileget.noexists", tph);
        }
        return oM;
    }

}
