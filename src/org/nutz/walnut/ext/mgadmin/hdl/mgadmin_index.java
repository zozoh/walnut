package org.nutz.walnut.ext.mgadmin.hdl;

import java.util.List;

import org.nutz.ioc.Ioc;
import org.nutz.mvc.Mvcs;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.impl.io.mongo.MongoDB;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

public class mgadmin_index implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        Ioc ioc = Mvcs.getIoc();
        MongoDB mongoDB = ioc.get(MongoDB.class, "mongoDB");
        DB db = mongoDB.getRaw();
        String colName = hc.params.get("co", "obj");
        DBCollection co = db.getCollection(colName);
        
        // 执行操作
        if (hc.params.vals.length > 0) {
            switch (hc.params.vals[0]) {
            case "create":
                if (hc.params.vals.length > 2) {
                    co.createIndex(BasicDBObject.parse(hc.params.val(1)),
                                   BasicDBObject.parse(hc.params.val(2)));
                } else {
                    co.createIndex(BasicDBObject.parse(hc.params.val(1)));
                }
                break;
            case "drop":
                co.dropIndex(hc.params.val(1));
                break;
            case "cbasic":
                db.getCollection("obj").createIndex(BasicDBObject.parse("{'id':1}"));
                db.getCollection("obj").createIndex(BasicDBObject.parse("{'d0':1, 'd1':1}"));
                db.getCollection("obj").createIndex(BasicDBObject.parse("{'pid':1, 'nm':1}"));
                db.getCollection("obj").createIndex(BasicDBObject.parse("{'www':1}"),
                                                    BasicDBObject.parse("{'sparse':true}"));
                db.getCollection("obj").createIndex(BasicDBObject.parse("{'expi':1}"),
                                                    BasicDBObject.parse("{'sparse':true}"));
                db.getCollection("obj").createIndex(BasicDBObject.parse("{'d0':1, 'd1':1,'www':1}"),
                                                    BasicDBObject.parse("{'sparse':true}"));
                db.getCollection("obj").createIndex(BasicDBObject.parse("{'d0':1, 'd1':1,'websocket_watch':1}"),
                                                    BasicDBObject.parse("{'sparse':true}"));

                db.getCollection("bucket").createIndex(BasicDBObject.parse("{'id':1}"));
                db.getCollection("bucket").createIndex(BasicDBObject.parse("{'sha1':1}"));
                break;
            default:
                sys.err.print("unkown cmd=" + hc.params.val(0));
                break;
            }
        }
        // 仅查看
        else {
            List<DBObject> indexes = co.getIndexInfo();
            for (DBObject dbo : indexes) {
                sys.out.println(dbo.toString());
            }
        }
    }

}
