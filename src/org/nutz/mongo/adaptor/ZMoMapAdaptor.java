package org.nutz.mongo.adaptor;

import org.bson.Document;
import com.site0.walnut.util.Wlang;
import org.nutz.lang.Mirror;
import org.nutz.mongo.ZMo;
import org.nutz.mongo.ZMoAdaptor;
import org.nutz.mongo.ZMoDoc;
import org.nutz.mongo.entity.ZMoEntity;
import org.nutz.mongo.entity.ZMoField;

public class ZMoMapAdaptor implements ZMoAdaptor {

    ZMoMapAdaptor() {}

    @Override
    public Object toJava(ZMoField fld, Object obj) {
        if (obj instanceof Document) {
            ZMoDoc doc = ZMoDoc.WRAP((Document) obj);
            ZMoEntity en = ZMo.me().getEntity(fld.getType());
            return ZMo.me().fromDoc(doc, en);
        }
        throw Wlang.makeThrow("toJava error: %s", obj.getClass());
    }

    @Override
    public Object toMongo(ZMoField fld, Object obj) {
        Mirror<?> mi = Mirror.me(obj);
        if (mi.isMap() || mi.isPojo()) {
            ZMoEntity en = ZMo.me().getEntity(mi.getType());
            return ZMo.me().toDoc(obj, en);
        }
        throw Wlang.makeThrow("toMongo error: %s", obj.getClass());
    }

}
