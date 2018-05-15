package org.nutz.walnut.ext.mgadmin.hdl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.nutz.log.Log;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.impl.io.mongo.MongoDB;

import com.mongodb.BasicDBObject;
import com.mongodb.Cursor;
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
                    if ("pid_1_nm_1".equals(dbo.get("name")) && dbo.get("unique") != Boolean.TRUE) {
                        sys.out.println("drop NOT unique pid+nm index for collection obj");
                        co.dropIndex("pid_1_nm_1");
                    }
                }
                co.createIndex(toDBO("{'id':1}"), toDBO("{'unique':true}"));
                co.createIndex(toDBO("{'d0':1, 'd1':1}"));
                co.createIndex(toDBO("{'pid':1, 'nm':1}"), toDBO("{'unique':true}"));
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
            case "check_pid_nm":
                co = db.getCollection("obj");
                Set<String> keys = new HashSet<>();
                Cursor cur = co.find(toDBO("{}"), toDBO("{id:1,pid:1,nm:1}")).batchSize(128);
                Log log = sys.getLog("debug", null);
                while (cur.hasNext()) {
                    DBObject dbo = cur.next();
                    String pid = (String) dbo.get("pid");
                    String nm = (String)dbo.get("nm");
                    if (pid == null || nm == null) {
                        log.debug("FUCK(pid or nm == null) id=" + dbo.get("id"));
                        continue;
                    }
                    boolean re = keys.add(pid + "," + nm);
                    if (!re) {
                        log.debugf("| %s | %s | %s |", pid, nm, dbo.get("id"));
                    }
                }
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
