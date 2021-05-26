package org.nutz.walnut.ext.sys.mgadmin.hdl;

import org.bson.Document;
import org.nutz.lang.Streams;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;

/**
 * 原生方式查询mongodb里面的数据
 * 
 * @author Administrator
 *
 */
public class mgadmin_raw_query extends mgadmin_raw {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        FindIterable<Document> cur = rawQuery(hc.params, hc);
        MongoCursor<Document> it = null;
        int count = 0;
        try {
            it = cur.iterator();
            sys.out.print("[");
            while (it.hasNext()) {
                sys.out.writeJson(it.next());
                if (it.hasNext())
                    sys.out.println(",");
            }
        }
        finally {
            Streams.safeClose(it);
        }
        sys.out.print("]");
        sys.out.println("// count=" + count);
    }

}
