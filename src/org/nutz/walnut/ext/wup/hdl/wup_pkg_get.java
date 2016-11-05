package org.nutz.walnut.ext.wup.hdl;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;

/**
 * 获取一个更新包
 * @author wendal
 *
 */
public class wup_pkg_get extends wup_pkg_info {

    public void invoke(WnSystem sys, JvmHdlContext hc) {
        WnObj pkg = fetchPkg(sys, hc);
        if (pkg == null) {
            sys.err.print("no such package");
            return;
        }
        sys.io.readAndClose(pkg, sys.out.getOutputStream());
    }

}
