package org.nutz.mongo.adaptor;

import com.site0.walnut.util.Wlang;
import org.nutz.lang.Mirror;
import org.nutz.mongo.ZMoAdaptor;
import org.nutz.mongo.entity.ZMoField;

/**
 * 根据值的类型而不是字段类型类判断如何适配
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class ZMoSmartAdaptor implements ZMoAdaptor {

    ZMoSmartAdaptor() {}

    @Override
    public Object toJava(ZMoField fld, Object obj) {
        if (null == obj) {
            return null;
        }
        Mirror<?> mi = Mirror.me(obj);
        try {
            return ZMoAs.get(mi).toJava(fld, obj);
        }
        catch (Exception e) {
            throw Wlang.wrapThrow(e, "I am not such smart toJava -_-! : %s", obj.getClass());
        }
    }

    @Override
    public Object toMongo(ZMoField fld, Object obj) {
        if (null == obj) {
            return null;
        }
        Mirror<?> mi = Mirror.me(obj);
        try {
            return ZMoAs.get(mi).toMongo(fld, obj);
        }
        catch (Exception e) {
            throw Wlang.wrapThrow(e, "I am not such smart toMongo -_-! : %s", obj.getClass());
        }
    }

}
