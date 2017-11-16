package org.nutz.walnut.ext.mgadmin.hdl;

import org.nutz.ioc.Ioc;
import org.nutz.mongo.ZMoDoc;
import org.nutz.mvc.Mvcs;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.io.mongo.MongoDB;
import org.nutz.walnut.util.ZParams;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;

/**
 * 辅助查询/修改mongodb里面的数据,以原生API的方式
 * @author wendal
 *
 */
public abstract class mgadmin_raw implements JvmHdl {

    public DBCursor rawQuery(ZParams params) {
    	Ioc ioc = Mvcs.getIoc();
        MongoDB mongoDB = ioc.get(MongoDB.class, "mongoDB");
        DB db = mongoDB.getRaw();
    	DBCollection co = db.getCollection(params.get("co", "obj"));
        int limit = params.getInt("limit", 100);
        int skip = params.getInt("skip", 0);
        String _cnd = params.val_check(0);
        ZMoDoc _query = ZMoDoc.NEW(_cnd);
        return co.find(_query).skip(skip).limit(limit);
    }
}
