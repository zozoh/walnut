package org.nutz.mongo.adaptor;

import java.util.regex.Pattern;

import org.bson.Document;
import org.bson.types.Decimal128;
import org.bson.types.ObjectId;
import com.site0.walnut.util.Wlang;
import org.nutz.lang.Mirror;
import org.nutz.mongo.ZMoAdaptor;
import org.nutz.mongo.entity.ZMoField;

/**
 * 各个适配器的单例工厂方法
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class ZMoAs {

    private static ZMoAdaptor _id = new ZMoIdAdaptor();

    private static ZMoAdaptor _dbc = new ZMoDocumentAdaptor();

    private static ZMoAdaptor _collection = new ZMoCollectionAdaptor();

    private static ZMoAdaptor _array = new ZMoArrayAdaptor();

    private static ZMoAdaptor _enum = new ZMoEnumAdaptor();

    private static ZMoAdaptor _map = new ZMoMapAdaptor();

    private static ZMoAdaptor _pojo = new ZMoPojoAdaptor();

    private static ZMoAdaptor _simple = new ZMoSimpleAdaptor();

    private static ZMoAdaptor _smart = new ZMoSmartAdaptor();

    public static ZMoAdaptor get(Mirror<?> mi) {
        // ID 对象
        if (mi.isOf(ObjectId.class)) {
            return ZMoAs.id();
        } else if (mi.is(Decimal128.class)) {
            return new ZMoAdaptor() {
                public Object toJava(ZMoField fld, Object obj) {
                    return ((Decimal128) obj).bigDecimalValue().doubleValue();
                }

                @Override
                public Object toMongo(ZMoField fld, Object obj) {
                    return Decimal128.parse(Double.toString(((Number) obj).doubleValue()));
                }

            };
        }
        // 简单类型
        else if (mi.isSimple() || mi.is(Pattern.class)) {
            return ZMoAs.simple();
        }
        // Document
        else if (mi.isOf(Document.class)) {
            return ZMoAs.doc();
        }
        // 集合
        else if (mi.isCollection()) {
            return ZMoAs.collection();
        }
        // 数组
        else if (mi.isArray()) {
            return ZMoAs.array();
        }
        // 枚举
        else if (mi.isEnum()) {
            return ZMoAs.ENUM();
        }
        // Map
        else if (mi.isMap()) {
            return ZMoAs.map();
        }
        // POJO
        else if (mi.isPojo()) {
            return ZMoAs.pojo();
        }
        // 错误
        throw Wlang.makeThrow("fail to found adaptor for type %s", mi.getType());
    }

    public static ZMoAdaptor id() {
        return _id;
    }

    public static ZMoAdaptor doc() {
        return _dbc;
    }

    public static ZMoAdaptor collection() {
        return _collection;
    }

    public static ZMoAdaptor array() {
        return _array;
    }

    public static ZMoAdaptor ENUM() {
        return _enum;
    }

    public static ZMoAdaptor map() {
        return _map;
    }

    public static ZMoAdaptor pojo() {
        return _pojo;
    }

    public static ZMoAdaptor simple() {
        return _simple;
    }

    public static ZMoAdaptor smart() {
        return _smart;
    }

}
