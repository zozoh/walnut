package org.nutz.walnut.ext.mgadmin.hdl;

import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;

/**
 * 原生方式查询mongodb里面的数据
 * 
 * @author Administrator
 *
 */
public class mgadmin_raw_explain extends mgadmin_raw {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        sys.out.writeJson(rawQuery(hc.params, hc).explain());
    }

}
