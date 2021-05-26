package org.nutz.walnut.ext.sys.mgadmin.hdl;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bson.Document;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.mongo.ZMoDoc;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.MongoDB;
import org.nutz.walnut.util.Wlang;

import com.mongodb.client.FindIterable;
import com.mongodb.client.ListIndexesIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;

public class mgadmin_index implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        MongoDB mongoDB = hc.ioc.get(MongoDB.class, "mongoDB");
        MongoDatabase db = mongoDB.getRawApi();
        String colName = hc.params.get("co", "obj");
        MongoCollection<Document> co = db.getCollection(colName);

        // 执行操作
        if (hc.params.vals.length > 0) {
            switch (hc.params.vals[0]) {
            case "create":
                if (hc.params.vals.length > 2) {
                    ZMoDoc keys = ZMoDoc.NEW(hc.params.val(1));
                    NutMap opts = Wlang.map(hc.params.val(2));
                    IndexOptions iopts = Wlang.map2Object(opts, IndexOptions.class);
                    co.createIndex(keys, iopts);
                } else {
                    ZMoDoc keys = ZMoDoc.NEW(hc.params.val(1));
                    co.createIndex(keys);
                }
                break;
            case "drop":
                co.dropIndex(hc.params.val(1));
                break;
            case "cbasic":
                co = db.getCollection("obj");
                ListIndexesIterable<Document> it = co.listIndexes();
                List<Document> indexes = new LinkedList<>();
                it.into(indexes);
                for (Document dbo : indexes) {
                    if ("id_1".equals(dbo.get("name")) && dbo.get("unique") != Boolean.TRUE) {
                        sys.out.println("drop NOT unique id index for collection obj");
                        co.dropIndex("id_1");
                    }
                    if ("pid_1_nm_1".equals(dbo.get("name")) && dbo.get("unique") != Boolean.TRUE) {
                        sys.out.println("drop NOT unique pid+nm index for collection obj");
                        co.dropIndex("pid_1_nm_1");
                    }
                }
                co.createIndex(toDBO("{'id':1}"), toIXO("{'unique':true}"));
                co.createIndex(toDBO("{'d0':1, 'd1':1}"));
                co.createIndex(toDBO("{'pid':1, 'nm':1}"), toIXO("{'unique':true}"));
                co.createIndex(toDBO("{'www':1}"), toIXO("{'sparse':true}"));
                co.createIndex(toDBO("{'expi':1}"), toIXO("{'sparse':true}"));
                co.createIndex(toDBO("{'d0':1, 'd1':1,'www':1}"), toIXO("{'sparse':true}"));
                co.createIndex(toDBO("{'d0':1, 'd1':1,'websocket_watch':1}"),
                               toIXO("{'sparse':true}"));

                // zozoh 不再需要 bucket 这个集合了
                // co = db.getCollection("bucket");
                // for (Document dbo : indexes) {
                // if ("id_1".equals(dbo.get("name")) && dbo.get("unique") !=
                // Boolean.TRUE) {
                // sys.out.println("drop NOT unique id index for collection
                // bucket");
                // co.dropIndex("id_1");
                // }
                // }
                // co.createIndex(toDBO("{'id':1}"), toDBO("{'unique':true}"));
                // co.createIndex(toDBO("{'sha1':1}"));
                break;
            case "check_pid_nm":
                co = db.getCollection("obj");
                Set<String> keys = new HashSet<>();
                FindIterable<Document> ite = co.find(toDBO("{}"));
                ite.batchSize(128);
                Log log = sys.getLog("debug", null);
                Map<String, String> renameMap = new LinkedHashMap<>();
                boolean doFix = hc.params.vals.length > 1 && hc.params.vals[1].equals("fix");
                int count = 0;
                MongoCursor<Document> cur = ite.cursor();
                try {
                    while (cur.hasNext()) {
                        Document dbo = cur.next();
                        String pid = (String) dbo.get("pid");
                        String nm = (String) dbo.get("nm");
                        String id = (String) dbo.get("id");
                        if (Strings.isBlank(pid) || Strings.isBlank(nm) || Strings.isBlank(id)) {
                            log.debug("FUCK(pid or nm or id is blank) id=" + dbo.get("id"));
                            continue;
                        }
                        boolean re = keys.add(pid + "," + nm);
                        if (!re) {
                            log.debugf("| %s | %s | %s |", pid, nm, id);
                            if (doFix)
                                renameMap.put(id, R.UU32().substring(0, 4) + "_" + nm);
                            count++;
                        }
                    }
                    log.debug("count=" + count);
                }
                finally {
                    Streams.safeClose(cur);
                }
                if (doFix) {
                    for (Entry<String, String> en : renameMap.entrySet()) {
                        log.debugf("fix id=%s as nm=%s", en.getKey(), en.getValue());
                        ZMoDoc filter = ZMoDoc.NEW("id", en.getKey());
                        ZMoDoc set = ZMoDoc.NEW("nm", en.getValue());
                        set.put("nm_dup", 1);
                        ZMoDoc update = ZMoDoc.NEW("$set", set);
                        co.updateOne(filter, update);
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
            ListIndexesIterable<Document> it = co.listIndexes();
            List<Document> indexes = new LinkedList<>();
            it.into(indexes);
            for (Document dbo : indexes) {
                sys.out.println(dbo.toString());
            }
        }
    }

    protected static ZMoDoc toDBO(String json) {
        return ZMoDoc.NEW(json);
    }

    protected static IndexOptions toIXO(String json) {
        NutMap map = Wlang.map(json);
        return Wlang.map2Object(map, IndexOptions.class);
    }
}
