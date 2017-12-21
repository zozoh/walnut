package org.nutz.walnut.ext.mgadmin.hdl;

import java.util.Map.Entry;
import java.util.TreeMap;

import org.nutz.lang.Strings;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.impl.io.mongo.MongoDB;

import com.mongodb.BasicDBObject;
import com.mongodb.Cursor;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

public class mgadmin_sanity_check implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        MongoDB mongoDB = hc.ioc.get(MongoDB.class, "mongoDB");
        DB db = mongoDB.getRaw();
        String colName = hc.params.get("co", "obj");
        DBCollection co = db.getCollection(colName);

        // 执行操作
        TreeMap<String, DBObject> maps = new TreeMap<>();
        try (Cursor cur = co.find(new BasicDBObject("d0", "home"), new BasicDBObject("d0", 1).append("d1", 1).append("pid", 1).append("id", 1))) {
            while (cur.hasNext()) {
                DBObject dbo = cur.next();
                // CHECK: 每个DBObject都应该有pid
                if (dbo.get("pid") == null) {
                    sys.out.println("record without pid?!: " + dbo);
                    continue;
                }
                // CHECK: 每个DBObject都应该有id
                String id = (String)dbo.get("id");
                if (id == null) {
                    sys.out.println("record without id?!: " + dbo);
                    continue;
                }
                maps.put(id, dbo);
            }
        }
        // 只需确保每个WnObj的pid存在, 自身的d0/d1与parent一致,就可以了嘛
        for (Entry<String, DBObject> en : maps.entrySet()) {
            DBObject current = en.getValue();
            String pid = (String) current.get("pid");
            // 例如 /home , pid就等于 @WnRoot
            if ("@WnRoot".equals(pid)) {
                continue; //
            }
            DBObject parent = maps.get(pid);
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
            }
            else {
                sys.out.printlnf("id=%s pid=%s d0[%s -- %s] d1[%s -- %s]", current.get("id"), pid, current_d0, parent_d0, current_d1, parent_d1);
            }
        }
        sys.out.printlnf("checked %s DBObjects", maps.size());
    }
}
