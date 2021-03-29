package org.nutz.walnut.ext.dsync.hdl;

import org.nutz.walnut.ext.dsync.DSyncContext;
import org.nutz.walnut.ext.dsync.DSyncFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class dsync_pkgname extends DSyncFilter {

    @Override
    protected void process(WnSystem sys, DSyncContext fc, ZParams params) {
        String sha1 = params.val(0);
        String pkgName;
        if (null == sha1) {
            pkgName = fc.api.getPackageName(fc.config, fc.trees);
        } else {
            pkgName = fc.api.getPackageName(fc.config, sha1);
        }
        sys.out.println(pkgName);
    }

}
