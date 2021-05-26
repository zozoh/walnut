package org.nutz.mongo.adaptor;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.nutz.lang.Lang;
import org.nutz.mongo.ZMo;
import org.nutz.mongo.ZMoAdaptor;
import org.nutz.mongo.ZMoDoc;
import org.nutz.mongo.entity.ZMoField;

public class ZMoDocumentAdaptor implements ZMoAdaptor {

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Object toJava(ZMoField fld, Object obj) {
        // 可能是 BasicDBList or LazyDBList
        if (obj instanceof List<?>) {
            List<?> list = (List<?>) obj;
            ArrayList arr = new ArrayList(list.size());
            for (Object o : list) {
                if (o != null && o instanceof Document) {
                    arr.add(ZMoAs.doc().toJava(null, o));
                } else {
                    arr.add(o);
                }
            }
            return arr;
        }
        // 普通 Document 变 map
        else if (obj instanceof Document) {
            return ZMo.me().fromDocToMap(ZMoDoc.WRAP((Document) obj));
        }
        // 不可忍受，抛吧 >:D
        throw Lang.makeThrow("toJava error: %s", obj.getClass());
    }

    @Override
    public Object toMongo(ZMoField fld, Object obj) {
        if (obj instanceof Document) {
            return obj;
        }
        throw Lang.makeThrow("toMongo error: %s", obj.getClass());
    }

}
