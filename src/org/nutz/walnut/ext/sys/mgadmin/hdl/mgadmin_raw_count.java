package org.nutz.walnut.ext.sys.mgadmin.hdl;

import org.nutz.mongo.ZMoCo;
import org.nutz.mongo.ZMoDoc;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.MongoDB;

import com.mongodb.client.model.CountOptions;

/**
 * 原生方式统计mongodb里面的数据
 * 
 * @author Administrator
 *
 */
public class mgadmin_raw_count extends mgadmin_raw {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        MongoDB mongoDB = hc.ioc.get(MongoDB.class, "mongoDB");
        ZMoCo co = mongoDB.getCollection(hc.params.get("co", "obj"));
        int limit = hc.params.getInt("limit", 100);
        int skip = hc.params.getInt("skip", 0);
        String _cnd = hc.params.val_check(0);
        ZMoDoc _query = ZMoDoc.NEW(_cnd);
        CountOptions opt = new CountOptions();
        opt.limit(limit);
        opt.skip(skip);
        long count = co.countDocuments(_query, opt);

        sys.out.println("{count:" + count + "}");
    }

}
