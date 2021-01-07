package org.nutz.walnut.tool.transdata;

import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Stopwatch;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.mongo.ZMo;
import org.nutz.mongo.ZMoCo;
import org.nutz.mongo.ZMoDB;
import org.nutz.mongo.ZMoDoc;
import org.nutz.mongo.ZMongo;

import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

public class RebuildRefsMongo {

    private static final String VERSION = "1.0";

    private static final String HR0 = Strings.dup('#', 60);
    private static final String HR1 = Strings.dup('-', 60);

    public static void L(String fmt, Object... args) {
        System.out.println(String.format(fmt, args));
    }

    public static void main(String[] args) {
        String configPath = args.length > 0 ? args[0] : "transdata.properties";
        Stopwatch sw = Stopwatch.begin();
        // 读取配置文件
        PropertiesProxy pp = new PropertiesProxy(configPath);

        L(HR0);
        L("#");
        L("#   Walnut 索引恢复程序(采用 MongoDB.$refs 存储引用)");
        L("#   > 根据 MongoDB 恢复 Redis 索引");
        L("#  ", VERSION);
        L("#");
        L(HR0);

        // 建立 MongoDB 连接
        String host = pp.get("mongo-host", "127.0.0.1");
        int port = pp.getInt("mongo-port", 27017);
        String user = pp.get("mongo-user");
        String pass = pp.get("mongo-pass");
        String dbName = pp.get("mongo-db", "walnut");

        L("准备连接 MongoDB: %s:%s", host, port);

        ServerAddress sa = ZMongo.NEW_SA(host, port);
        MongoCredential cred = null;
        if (!Strings.isBlank(user))
            cred = MongoCredential.createScramSha1Credential(user, dbName, pass.toCharArray());
        // 连接数据库
        ZMongo mongo = ZMongo.me(sa, cred, null);
        ZMoDB db = mongo.db(dbName);
        L("...成功的连接了 MongoDB");

        ZMoCo coObj = db.c("obj");
        ZMoCo coRef = db.cc("refs", false);

        L("\n开始从 MongoDB 查找索引 ...");
        L(HR1);

        int limit = pp.getInt("limit", 10);
        int skip = pp.getInt("skip", 0);
        String filter = pp.get("filter-rebuild");

        if (Strings.isBlank(filter)) {
            L("过滤条件：");
            L(Json.toJson(filter, JsonFormat.nice()));
        } else {
            L("~ 无过滤条件 ~");
        }

        L("\n开始从 MongoDB 查找索引 ...");
        L(HR1);
        ZMoDoc qDoc = Strings.isBlank(filter) ? ZMoDoc.NEW() : ZMoDoc.NEW(filter);
        DBCursor cu = coObj.find(qDoc);
        int count = 0;
        int sha1_count = 0;
        try {
            //cu.addOption(Bytes.QUERYOPTION_NOTIMEOUT);
            if (limit > 0)
                cu.limit(limit);
            if (skip > 0)
                cu.skip(skip);

            while (cu.hasNext()) {
                // 获取对象
                DBObject dbobj = cu.next();
                NutMap obj = ZMo.me().fromDocToMap(dbobj, NutMap.class);
                // -------------------------------------
                String oid = obj.getString("id");
                String onm = obj.getString("nm");
                String sha1 = obj.getString("sha1");
                String race = obj.getString("race");
                // -------------------------------------
                // 看来有些对象是坏的
                if (Strings.isBlank(oid) || Strings.isBlank(onm)) {
                    continue;
                }
                // -------------------------------------
                // 打印一下
                L("\n%d. %s %s : '%s'", count, oid, sha1, onm);
                count += 1;
                // -------------------------------------
                // 无视
                if ("DIR".equals(race) || Strings.isBlank(sha1)) {
                    L("  ~~ skip ~~");
                    continue;
                }
                L("  Add to Refs");
                sha1_count += 1;
                // -------------------------------------
                // 记录到索引中
                ZMoDoc refDoc = ZMoDoc.NEWf("tid:'%s',rid:'%s'", sha1, oid);
                coRef.update(refDoc, ZMoDoc.NEW("$setOnInsert", refDoc), true, false);
            }
        }
        finally {
            cu.close();
        }

        // ===================================================
        // 结束
        sw.stop();
        L(HR1);
        L("Found %d objs (%d files)", count, sha1_count);
        L(HR1);
        L("All done in %s", sw.toString());
    }

}
