package org.nutz.walnut.ext.sys.mgadmin.hdl;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.nutz.json.Json;
import org.nutz.lang.Streams;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.MongoDB;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

@JvmHdlParamArgs(value = "cqn", regex = "^(quiet)$")
public class mgadmin_profile implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        MongoDB mongoDB = hc.ioc.get(MongoDB.class, "mongoDB");
        MongoDatabase db = mongoDB.getRawApi();
        Document cmd;
        Document re;
        if (hc.params.vals.length > 0) {
            switch (hc.params.vals[0]) {
            case "enable": // 启用profile, 可以设置level和slowms. 其中 level=1是仅慢查询,
                           // level=2是全部查询
                cmd = new Document();
                cmd.put("profile", hc.params.getInt("level", 1));
                cmd.put("slowms", hc.params.getInt("slowms", 100));
                re = db.runCommand(cmd);
                if (!hc.params.is("quiet"))
                    sys.out.print(re.toJson());
                break;
            case "disable":// 关闭profile
                cmd = new Document();
                cmd.put("profile", 0);
                re = db.runCommand(cmd);
                if (!hc.params.is("quiet"))
                    sys.out.print(re.toJson());
                break;
            case "list": // 列出最近的profile记录
                int lm = hc.params.getInt("limit", 10);
                MongoCollection<Document> co = db.getCollection("system.profile");
                FindIterable<Document> it = co.find().limit(lm);

                // 得到结果集
                List<NutMap> list = new ArrayList<>(lm);
                MongoCursor<Document> cu = it.iterator();
                try {

                    while (cu.hasNext()) {
                        NutMap map = NutMap.WRAP(cu.next());
                        list.add(map);
                    }
                }
                finally {
                    Streams.safeClose(cu);
                }
                // 输出
                sys.out.println(Json.toJson(list, hc.jfmt));
                break;
            case "clear": // 清除profile记录
                db.getCollection("system.profile").drop();
                break;
            default:
                sys.err.print("unkown cmd=" + hc.params.val(0));
                break;
            }
        } else {
            // 显示profile的状态
            cmd = new Document();
            cmd.put("profile", -1);
            re = db.runCommand(cmd);
            sys.out.print(re.toJson());
        }
    }

}
