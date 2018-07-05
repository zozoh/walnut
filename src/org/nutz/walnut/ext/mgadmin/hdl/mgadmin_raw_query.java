package org.nutz.walnut.ext.mgadmin.hdl;

import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;

import com.mongodb.DBCursor;

/**
 * 原生方式查询mongodb里面的数据
 * 
 * @author Administrator
 *
 */
public class mgadmin_raw_query extends mgadmin_raw {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        DBCursor cur = rawQuery(hc.params, hc);
        sys.out.println("// count=" + cur.count());
        sys.out.print("[");
        while (cur.hasNext()) {
            sys.out.writeJson(cur.next());
            if (cur.hasNext())
                sys.out.println(",");
        }
        sys.out.print("]");
    }

}
