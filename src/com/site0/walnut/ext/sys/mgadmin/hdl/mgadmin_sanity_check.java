package com.site0.walnut.ext.sys.mgadmin.hdl;

import java.util.Map.Entry;
import java.util.TreeMap;

import org.bson.Document;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.mongo.ZMoCo;
import org.nutz.mongo.ZMoDoc;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.MongoDB;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;

public class mgadmin_sanity_check implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        MongoDB mongoDB = hc.ioc.get(MongoDB.class, "mongoDB");
        String colName = hc.params.get("co", "obj");
        ZMoCo co = mongoDB.getCollection(colName);

        // 执行操作
        TreeMap<String, Document> maps = new TreeMap<>();

        MongoCursor<Document> cu = null;
        try {
            ZMoDoc q = ZMoDoc.NEW("d0", "home");
            //ZMoDoc p = ZMoDoc.NEW("d0", 1).set("d1", 1).set("pid", 1).set("id", 1);
            FindIterable<Document> it = co.find(q);
            cu = it.iterator();
            while (cu.hasNext()) {
                Document dbo = cu.next();
                // CHECK: 每个DBObject都应该有pid
                if (dbo.get("pid") == null) {
                    sys.out.println("record without pid?!: " + dbo);
                    continue;
                }
                // CHECK: 每个DBObject都应该有id
                String id = dbo.getString("id");
                if (id == null) {
                    sys.out.println("record without id?!: " + dbo);
                    continue;
                }
                maps.put(id, dbo);
            }
        }
        finally {
            Streams.safeClose(cu);
        }
        // 只需确保每个WnObj的pid存在, 自身的d0/d1与parent一致,就可以了嘛
        for (Entry<String, Document> en : maps.entrySet()) {
            Document current = en.getValue();
            String pid = current.getString("pid");
            // 例如 /home , pid就等于 @WnRoot
            if ("@WnRoot".equals(pid)) {
                continue; //
            }
            Document parent = maps.get(pid);
            // CHECK: 所有pid都应该存在
            if (parent == null) {
                sys.out.println("record with unkown pid?!: " + current);
                continue;
            }
            if ("@WnRoot".equals(parent.get("pid"))) {
                continue;
            }
            // CHECK: 所有pid都应该是文件夹或链接
            // TODO 完成"所有pid都应该是文件夹或链接"

            String current_d0 = (String) current.get("d0");
            String current_d1 = (String) current.get("d1");
            String parent_d0 = (String) parent.get("d0");
            String parent_d1 = (String) parent.get("d1");

            // CHECK: d0和d1要匹配
            if (Strings.equals(current_d0, parent_d0) && Strings.equals(current_d1, parent_d1)) {
                // 相等,那就是对的啦,不需要做任何事了
            } else {
                sys.out.printlnf("id=%s pid=%s d0[%s -- %s] d1[%s -- %s]",
                                 current.get("id"),
                                 pid,
                                 current_d0,
                                 parent_d0,
                                 current_d1,
                                 parent_d1);
            }
        }
        sys.out.printlnf("checked %s DBObjects", maps.size());
    }
}
