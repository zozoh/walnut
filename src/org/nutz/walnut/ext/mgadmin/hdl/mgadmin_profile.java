package org.nutz.walnut.ext.mgadmin.hdl;

import org.nutz.ioc.Ioc;
import org.nutz.json.Json;
import org.nutz.mvc.Mvcs;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.impl.io.mongo.MongoDB;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.CommandResult;
import com.mongodb.DB;
import com.mongodb.DBCursor;

public class mgadmin_profile implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        Ioc ioc = Mvcs.getIoc();
        MongoDB mongoDB = ioc.get(MongoDB.class, "mongoDB");
        DB db = mongoDB.getRaw();
        CommandResult re;
        if (hc.params.vals.length > 0) {
            switch (hc.params.vals[0]) {
            case "enable": // 启用profile, 可以设置level和slowms. 其中 level=1是仅慢查询,
                           // level=2是全部查询
                BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
                builder.add("profile", hc.params.getInt("level", 1));
                builder.add("slowms", hc.params.getInt("slowms", 100));
                re = db.command(builder.get());
                sys.out.print(re.toJson());
                break;
            case "disable":// 关闭profile
                re = db.command(BasicDBObjectBuilder.start().add("profile", 0).get());
                sys.out.print(re.toJson());
                break;
            case "list": // 列出最近的profile记录
                DBCursor cur = db.getCollection("system.profile")
                                 .find()
                                 .limit(hc.params.getInt("limit", 10));
                try {
                    while (cur.hasNext()) {
                        sys.out.println(Json.toJson(cur.next().toMap()));
                    }
                }
                finally {
                    cur.close();
                }
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
            re = db.command(BasicDBObjectBuilder.start().add("profile", -1).get());
            sys.out.print(re.toJson());
        }
    }

}
