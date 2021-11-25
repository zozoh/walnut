package org.nutz.walnut.ext.data.thing.impl;

import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.data.thing.ThingDataAction;
import org.nutz.walnut.util.Wn;

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
