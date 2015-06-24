package org.nutz.walnut.impl.io.mongo;

import org.nutz.lang.Strings;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mongo.ZMoCo;
import org.nutz.mongo.ZMoDB;
import org.nutz.mongo.ZMongo;

import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

public class MongoDB {

    private static final Log log = Logs.get();

    private String host;
    private int port;
    private String usr;
    private String pwd;
    private String db;

    private ZMongo _zm;
    private ZMoDB _zdb;

    public void on_create() {
        ServerAddress sa = ZMongo.NEW_SA(host, port);
        MongoCredential cred = null;
        if (!Strings.isBlank(usr))
            cred = MongoCredential.createPlainCredential(usr, db, pwd.toCharArray());
        if (log.isInfoEnabled())
            log.infof("MongoCredential : '%s'", cred);
        // 连接数据库
        _zm = ZMongo.me(sa, cred, null);
        _zdb = _zm.db(db);

    }

    public void on_depose() {
        _zm.close();
    }

    public ZMoCo getCollectionByMount(String mnt) {
        int pos = mnt.lastIndexOf('@');
        String coName;
        if (pos > 0) {
            coName = mnt.substring("mongo:".length(), pos);
        } else {
            coName = mnt.substring("mongo:".length());
        }
        return _zdb.cc(coName, false);
    }

    public ZMoCo getCollection(String coName) {
        return _zdb.cc(coName, false);
    }

}
