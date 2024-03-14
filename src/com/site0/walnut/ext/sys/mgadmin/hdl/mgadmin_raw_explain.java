package com.site0.walnut.ext.sys.mgadmin.hdl;

import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.WnSystem;

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
