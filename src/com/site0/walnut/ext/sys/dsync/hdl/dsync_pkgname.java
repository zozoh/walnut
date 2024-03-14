package com.site0.walnut.ext.sys.dsync.hdl;

import com.site0.walnut.ext.sys.dsync.DSyncContext;
import com.site0.walnut.ext.sys.dsync.DSyncFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

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
