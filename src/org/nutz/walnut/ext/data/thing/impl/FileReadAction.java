package org.nutz.walnut.ext.data.thing.impl;

import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.data.thing.ThingDataAction;
import org.nutz.walnut.util.WnHttpResponse;

public class FileReadAction extends ThingDataAction<WnHttpResponse> {

    public String fnm;

    public boolean quiet;

    public String etag;

    public String range;

    public String userAgent;

    @Override
    public WnHttpResponse invoke() {
        WnObj oDir = myDir();
        WnObj oM = io.fetch(oDir, fnm);
        if (null == oM && !quiet) {
            throw Er.create("e.thing.read." + dirName + ".noexists", oDir.path() + "/" + fnm);
        }

        WnHttpResponse resp = new WnHttpResponse();
        resp.setEtag(etag);
        resp.prepare(io, oM, range);
        resp.setUserAgent(userAgent);

        return resp;
    }

}
