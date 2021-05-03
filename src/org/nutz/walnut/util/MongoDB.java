package org.nutz.walnut.util;

import org.nutz.lang.Strings;
import org.nutz.log.Log;
import org.nutz.mongo.ZMoCo;
import org.nutz.mongo.ZMoDB;
import org.nutz.mongo.ZMongo;

import com.mongodb.DB;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

public class MongoDB {

    private static final Log log = Wlog.getMAIN();

    public String host;
    public int port;
    public String usr;
    public String pwd;
    public String db;
    public String uri;

    private ZMongo _zm;
    private ZMoDB _zdb;

    public void on_create() {
        if (Strings.isBlank(uri)) {
            ServerAddress sa = ZMongo.NEW_SA(host, port);
            MongoCredential cred = null;
            if (!Strings.isBlank(usr))
                cred = MongoCredential.createScramSha1Credential(usr, db, pwd.toCharArray());
            if (log.isInfoEnabled())
                log.infof("MongoCredential : '%s'", cred);
            // 连接数据库
            _zm = ZMongo.me(sa, cred, null);
        } else {
            _zm = ZMongo.uri(uri);
        }

        _zdb = _zm.db(db);

    }

    public void on_depose() {
        _zm.close();
    }

    public ZMoCo getCollection(String coName) {
        return _zdb.cc(coName, false);
    }

    public DB getRaw() {
        return _zdb.getNativeDB();
    }

}
