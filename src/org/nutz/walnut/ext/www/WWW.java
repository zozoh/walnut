package org.nutz.walnut.ext.www;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.util.Wn;

public abstract class WWW {

    public static final String AT_SEID = "DSEID";

    public static NutMap read_conf(WnIo io, String grp) {
        String phConf = Wn.appendPath(Wn.getUsrHome(grp), ".wwwrc");
        WnObj oConf = io.fetch(null, phConf);

        NutMap conf;
        if (null == oConf) {
            conf = new NutMap();
        } else {
            conf = io.readJson(oConf, NutMap.class);
        }
        return conf;
    }

    private WWW() {}
}
