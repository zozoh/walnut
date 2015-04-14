package org.nutz.walnut.api.io;

import java.util.List;

import org.nutz.lang.Each;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.util.UnitTestable;

public interface WnIndexer extends UnitTestable {

    WnObj get(String id);

    void set(String id, String key, Object val);

    void set(String id, NutMap map);

    int each(WnQuery q, Each<WnObj> callback);

    List<WnObj> query(WnQuery q);

    WnObj getOne(WnQuery q);

    void remove(String id);

}
