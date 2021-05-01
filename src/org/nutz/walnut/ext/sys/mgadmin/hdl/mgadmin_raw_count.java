package org.nutz.walnut.ext.sys.mgadmin.hdl;

import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;

import com.mongodb.DBCursor;

/**
 * 原生方式统计mongodb里面的数据
 * 
 * @author Administrator
 *
 */
public class mgadmin_raw_count extends mgadmin_raw {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        DBCursor cur = rawQuery(hc.params, hc);
        sys.out.println("{count:" + cur.count() + "}");
    }

}
