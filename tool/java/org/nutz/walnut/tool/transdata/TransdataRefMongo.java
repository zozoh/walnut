package org.nutz.walnut.tool.transdata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.json.Json;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Stopwatch;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.mongo.ZMo;
import org.nutz.mongo.ZMoCo;
import org.nutz.mongo.ZMoDB;
import org.nutz.mongo.ZMoDoc;
import org.nutz.mongo.ZMongo;
import com.mongodb.Bytes;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

public class TransdataRefMongo {

    private static final String VERSION = "1.0";

    private static final String HR0 = Strings.dup('#', 60);
    private static final String HR1 = Strings.dup('-', 60);

    public static void L(String fmt, Object... args) {
        System.out.println(String.format(fmt, args));
    }

    private static void AppendFile(File fTa, File fn) throws FileNotFoundException {
        OutputStream ops = new FileOutputStream(fTa, true);
        InputStream ins = new FileInputStream(fn);
        Streams.writeAndClose(ops, ins);
    }

    public static void TransBucket(NutMap buck, String phTargetHome) throws FileNotFoundException {
        String oid = buck.getString("oid");
        String sha1 = buck.getString("sha1");
        String data = buck.getString("data");
        long size = buck.getLong("sz");
        List<File> files = buck.getList("files", File.class);
        L(" = Trans %d Bucket Blocks ...", files.size());
        // 原始目录为空，诡异啊，记录一下
        if (files.isEmpty()) {
            L(" = 空桶目录，跳过！");
            emptyBucks.add(data);
            return;
        }
        // 目标桶文件路径
        String phTa = String.format("%s/%s/%s",
                                    phTargetHome,
                                    sha1.substring(0, 4),
                                    sha1.substring(4));
        File fTa = new File(phTa);
        String phDr = Files.getParent(phTa);
        File dDr = new File(phDr);
        // 如果目标桶文件存在，那么校验一下 sha1
        if (fTa.exists()) {
            L(" = 目标桶已经存，开始校验 ... (%s) : %dbytes ", sha1, size);
            String taSha1 = GetFileSha1(fTa);
            if (sha1.equals(taSha1)) {
                L(" = OK = 校验通过");
                return;
            } else {
                L(" = KO ! 校验失败，移除这个文件");
                sha1Cache.remove(phTa);
                NutMap ko = new NutMap();
                ko.put("oid", oid);
                ko.put("data", data);
                ko.put("mongoSha1", sha1);
                ko.put("fileSha1", taSha1);
                koBucks.add(ko);
                fTa.delete();
            }
        }

        // 确保目标桶的散列目录存在
        if (!dDr.exists()) {
            L(" = 为目标桶建立父路径: %s", phDr);
            dDr.mkdir();
        }
        // 如果目标桶不存在，或者校验失败，那么copy第一个桶过去
        L(" = 先 Copy 第一个桶块");
        File f0 = files.get(0);
        Files.copy(f0, fTa);
        L(" =  ... OK");

        // 如果还有剩余的桶块，附加
        if (files.size() > 1) {
            L(" = 依次附加其余的桶块");
            for (int i = 1; i < files.size(); i++) {
                File fn = files.get(i);
                L(" = + %3s >> %s", fn.getName(), phTa);
                AppendFile(fTa, fn);
            }
        }

        // 转换完毕，得到一下真实的文件大小
        long taSize = fTa.length();
        String taSha1 = GetFileSha1(fTa);
        if (taSize != size) {
            NutMap neq = new NutMap();
            neq.put("oid", oid);
            neq.put("data", data);
            neq.put("ta_sz", taSize);
            neq.put("ta_sha1", taSha1);
            neq.put("bu_sz", size);
            neq.put("bu_sha1", sha1);
            neqBucks.add(neq);
            // 嗯，目标比桶大，那么真实的截一下
            if (taSize > size) {
                RandomAccessFile raf = null;
                FileChannel chan = null;

                try {
                    raf = new RandomAccessFile(fTa, "rw");
                    chan = raf.getChannel();
                    chan.truncate(size);
                    chan.force(false);
                }
                catch (Exception e) {
                    throw Lang.wrapThrow(e);
                }
                finally {
                    Streams.safeClose(chan);
                    Streams.safeClose(raf);
                }

            }
        }
        L(" =  ~~~ 转换完毕 ~~~ =");
    }

    private static String GetFileSha1(File fTa) {
        String ph = fTa.getAbsolutePath();
        String sha1 = sha1Cache.getString(ph);
        if (null == sha1) {
            sha1 = Lang.sha1(fTa);
            sha1Cache.put(ph, sha1);
        }
        return sha1;
    }

    public static void main(String[] args) {
        String configPath = args.length > 0 ? args[0] : "transdata.properties";
        Stopwatch sw = Stopwatch.begin();
        // 读取配置文件
        PropertiesProxy pp = new PropertiesProxy(configPath);

        L(HR0);
        L("#");
        L("#   Walnut 数据迁移程序(采用 MongoDB.$refs 存储引用)");
        L("#  ", VERSION);
        L("#");
        L(HR0);

        // 建立 MongoDB 连接
        String host = pp.get("mongo-host", "127.0.0.1");
        int port = pp.getInt("mongo-port", 27017);
        String user = pp.get("mongo-user");
        String pass = pp.get("mongo-pass");
        String dbName = pp.get("mongo-db", "walnut");

        String phBucketHome = pp.get("bucket-home");
        String phTargetHome = pp.get("target-home");

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
        ZMoCo coBuk = db.c("bucket");
        ZMoCo coRef = db.cc("refs", false);

        L("\n开始从 MongoDB 查找索引 ...");
        L(HR1);

        int limit = pp.getInt("limit", 10);
        int skip = pp.getInt("skip", 0);
        String filter = pp.get("filter-trans");

        L("\n开始从 MongoDB 查找索引 ...");
        L(HR1);
        ZMoDoc qDoc = Strings.isBlank(filter) ? ZMoDoc.NEW() : ZMoDoc.NEW(filter);
        DBCursor cu = coBuk.find(qDoc);
        int index = 0;
        int count = 0;
        try {
            cu.addOption(Bytes.QUERYOPTION_NOTIMEOUT);
            if (limit > 0)
                cu.limit(limit);
            if (skip > 0)
                cu.skip(skip);

            while (cu.hasNext()) {
                // 获取对象
                DBObject dbobj = cu.next();
                NutMap bu = ZMo.me().fromDocToMap(dbobj, NutMap.class);
                // -------------------------------------
                String oid = null;
                String data = bu.getString("id"); // 桶 ID
                int buNb = bu.getInt("b_nb", 0); // 桶块数量
                long buSz = bu.getLong("sz", 0); // 桶总大小
                String sha1 = bu.getString("sha1"); // 桶指纹
                int refs = bu.getInt("refer", 0); // 得到引用数量
                // -------------------------------------
                index = skip + count;
                // -------------------------------------
                // 查询一下这个桶对应的对象 ID
                ZMoDoc qObj = ZMoDoc.NEWf("data:'%s',sha1:'%s'", data, sha1);
                ZMoDoc obj = coObj.findOne(qObj);
                if (null == obj) {
                    lostBucks.add(data);
                    continue;
                } else {
                    oid = obj.getString("id");
                }
                // -------------------------------------
                // 打印一下
                L("\n%d. %s (B)x%d : '%s' <-%d", index, data, buNb, sha1, refs);
                L(" = OID: %s", oid);
                // 计算桶文件
                String path = String.format("%s/%s/%s",
                                            phBucketHome,
                                            data.substring(0, 2),
                                            data.substring(2));
                File buDir = Files.findFile(path);
                if (null == buDir || !buDir.isDirectory()) {
                    badBucks.add(data);
                    continue;
                }
                List<File> files = new ArrayList<>(buNb);
                boolean isbad = false;
                for (int i = 0; i < buNb; i++) {
                    File f = new File(path + "/" + i);
                    L("      %3d) %s : %s", i, f.exists(), f.getPath());
                    if (f.exists()) {
                        files.add(f);
                    } else {
                        isbad = true;
                        badBucks.add(data);
                        break;
                    }
                }
                if (isbad) {
                    continue;
                }
                // -------------------------------------
                // 记录到索引中
                ZMoDoc refDoc = ZMoDoc.NEWf("tid:'%s',rid:'%s'", sha1, oid);
                coRef.update(refDoc, ZMoDoc.NEW("$setOnInsert", refDoc), true, false);

                // -------------------------------------
                // 准备桶对象，进行转换
                NutMap buck = new NutMap();
                buck.put("index", index);
                buck.put("oid", oid);
                buck.put("data", data);
                buck.put("nb", buNb);
                buck.put("sz", buSz);
                buck.put("sha1", sha1);
                buck.put("refs", refs);
                buck.put("files", files);
                TransBucket(buck, phTargetHome);
                // -------------------------------------
                // 计数
                count++;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            cu.close();
        }

        // ===================================================
        // 结束
        sw.stop();
        L(HR1);
        L("Lost Buckets x(%d)", lostBucks.size());
        int i = 0;
        for (String lbu : lostBucks) {
            L(" %d. %s", i, lbu);
            i += 1;
        }
        L(HR1);
        L("KO Buckets x(%d)", koBucks.size());
        i = 0;
        for (NutMap lbu : koBucks) {
            L(" %d. %s", i, Json.toJson(lbu));
            i += 1;
        }
        L(HR1);
        L("NE Buckets x(%d)", koBucks.size());
        i = 0;
        for (NutMap lbu : neqBucks) {
            L(" %d. %s", i, Json.toJson(lbu));
            i += 1;
            L(HR1);
        }
        L("BAD Buckets x(%d)", badBucks.size());
        i = 0;
        for (String lbu : badBucks) {
            L(" %d. %s", i, Json.toJson(lbu));
            i += 1;
        }
        L(HR1);
        L("SHA1 bucket x(%d)", sha1Cache.size());
        L("Found %d buckets", count);
        L(HR1);
        L("All done in %s", sw.toString());
    }

    static List<String> lostBucks = new LinkedList<>();
    static List<String> emptyBucks = new LinkedList<>();
    static NutMap sha1Cache = new NutMap();
    static List<NutMap> koBucks = new LinkedList<>();
    static List<NutMap> neqBucks = new LinkedList<>();
    static List<String> badBucks = new LinkedList<>();

}
