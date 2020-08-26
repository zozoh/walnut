package org.nutz.walnut.ext.mgadmin.hdl;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.nutz.log.Log;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.MongoDB;

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
                Map<String, String> renameMap = new LinkedHashMap<>();
                boolean doFix = hc.params.vals.length > 1 && hc.params.vals[1].equals("fix");
                int count = 0;
                while (cur.hasNext()) {
                    DBObject dbo = cur.next();
                    String pid = (String) dbo.get("pid");
                    String nm = (String)dbo.get("nm");
                    String id = (String)dbo.get("id");
                    if (Strings.isBlank(pid) || Strings.isBlank(nm) || Strings.isBlank(id)) {
                        log.debug("FUCK(pid or nm or id is blank) id=" + dbo.get("id"));
                        continue;
                    }
                    boolean re = keys.add(pid + "," + nm);
                    if (!re) {
                        log.debugf("| %s | %s | %s |", pid, nm, id);
                        if (doFix)
                            renameMap.put(id, R.UU32().substring(0, 4) + "_" + nm);
                        count ++;
                    }
                }
                log.debug("count=" + count);
                cur.close();
                if (doFix) {
                    for (Entry<String, String> en : renameMap.entrySet()) {
                        log.debugf("fix id=%s as nm=%s", en.getKey(), en.getValue());
                        co.update(new BasicDBObject("id", en.getKey()), new BasicDBObject("$set", new BasicDBObject("nm", en.getValue()).append("nm_dup", 1)));
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
