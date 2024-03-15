package org.nutz.mongo;

import java.util.LinkedList;
import java.util.List;

import org.bson.Document;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.api.err.Er;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.CreateCollectionOptions;

/**
 * 对于 DB 对象的薄封装
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class ZMoDB {

    private MongoDatabase db;

    public ZMoDB(MongoDatabase db) {
        this.db = db;
    }

    /**
     * 获取集合，如果集合不存在，则抛错
     * 
     * @param name
     *            集合名称
     * @return 集合薄封装
     */
    public ZMoCo c(String name) {
        if (!cExists(name))
            throw Wlang.makeThrow("Colection noexitst: %s.%s", db.getName(), name);
        try {
            return new ZMoCo(db.getCollection(name));
        }
        catch (Exception e) {
            String msg = String.format("e.mongo.getCollection:%s:%s", db.getName(), name);
            throw Er.create(msg, e);
        }
    }

    /**
     * 获取一个集合，如果集合不存在，就创建它
     * 
     * @param name
     *            集合名
     * @param dropIfExists
     *            true 如果存在就清除
     * @return 集合薄封装
     */
    public ZMoCo cc(String name, boolean dropIfExists) {
        // 不存在则创建
        if (!cExists(name)) {
            return createCollection(name, null);
        }
        // 固定清除
        else if (dropIfExists) {
            db.getCollection(name).drop();
            return createCollection(name, null);
        }
        // 已经存在
        return new ZMoCo(db.getCollection(name));
    }

    /**
     * 是否存在某个集合
     * 
     * @param name
     *            集合名
     * @return 是否存在
     */
    public boolean cExists(String name) {
        for (String cnm : db.listCollectionNames()) {
            if (cnm.equals(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 创建一个集合
     * 
     * @param name
     *            集合名
     * @param options
     *            集合配置信息
     * @return 集合薄封装
     */
    public ZMoCo createCollection(String name, CreateCollectionOptions options) {
        if (cExists(name)) {
            throw Wlang.makeThrow("Colection exitst: %s.%s", db.getName(), name);
        }

        // 创建默认配置信息
        if (null == options) {
            options = new CreateCollectionOptions();
            options.capped(false);
        }

        db.createCollection(name, options);
        MongoCollection<Document> co = db.getCollection(name);

        return new ZMoCo(co);
    }

    /**
     * @return 当前数据库所有可用集合名称
     */
    public List<String> cNames() {
        List<String> list = new LinkedList<>();
        MongoIterable<String> it = db.listCollectionNames();
        return it.into(list);
    }

    public MongoDatabase getRawApi() {
        return db;
    }
}
