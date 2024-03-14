package com.site0.walnut.ext.sys.mgadmin.hdl;

import org.bson.Document;
import org.nutz.mongo.ZMoCo;
import org.nutz.mongo.ZMoDoc;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.util.MongoDB;
import com.site0.walnut.util.ZParams;

import com.mongodb.client.FindIterable;

/**
 * 辅助查询/修改mongodb里面的数据,以原生API的方式
 * 
 * @author wendal
 *
 */
public abstract class mgadmin_raw implements JvmHdl {

    public FindIterable<Document> rawQuery(ZParams params, JvmHdlContext hc) {
        MongoDB mongoDB = hc.ioc.get(MongoDB.class, "mongoDB");
        ZMoCo co = mongoDB.getCollection(params.get("co", "obj"));
        int limit = params.getInt("limit", 100);
        int skip = params.getInt("skip", 0);
        String _cnd = params.val_check(0);
        ZMoDoc _query = ZMoDoc.NEW(_cnd);
        return co.find(_query).skip(skip).limit(limit);
    }
}
