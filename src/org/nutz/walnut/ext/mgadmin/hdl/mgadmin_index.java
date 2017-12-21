package org.nutz.walnut.ext.mgadmin.hdl;

import java.util.List;

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
        MongoDB mongoDB = hc.ioc.get(MongoDB.class, "mongoDB");
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
                co = db.getCollection("obj");
                List<DBObject> indexes = co.getIndexInfo();
                for (DBObject dbo : indexes) {
                    if ("id_1".equals(dbo.get("name")) && dbo.get("unique") != Boolean.TRUE) {
                        sys.out.println("drop NOT unique id index for collection obj");
                        co.dropIndex("id_1");
                    }
                }
                co.createIndex(toDBO("{'id':1}"), toDBO("{'unique':true}"));
                co.createIndex(toDBO("{'d0':1, 'd1':1}"));
                co.createIndex(toDBO("{'pid':1, 'nm':1}"));
                co.createIndex(toDBO("{'www':1}"), toDBO("{'sparse':true}"));
                co.createIndex(toDBO("{'expi':1}"), toDBO("{'sparse':true}"));
                co.createIndex(toDBO("{'d0':1, 'd1':1,'www':1}"), toDBO("{'sparse':true}"));
                co.createIndex(toDBO("{'d0':1, 'd1':1,'websocket_watch':1}"),
                               toDBO("{'sparse':true}"));

                co = db.getCollection("bucket");
                for (DBObject dbo : indexes) {
                    if ("id_1".equals(dbo.get("name")) && dbo.get("unique") != Boolean.TRUE) {
                        sys.out.println("drop NOT unique id index for collection bucket");
                        co.dropIndex("id_1");
                    }
                }
                co.createIndex(toDBO("{'id':1}"), toDBO("{'unique':true}"));
                co.createIndex(toDBO("{'sha1':1}"));
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

    protected static DBObject toDBO(String json) {
        return BasicDBObject.parse(json);
    }
}
