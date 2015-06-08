package org.nutz.walnut.api.io;

import java.util.List;

import org.nutz.lang.Each;
import org.nutz.walnut.util.UnitTestable;

public interface WnIndexer extends UnitTestable {

    WnObj get(String id);

    WnObj getOne(WnQuery q);

    WnObj toObj(WnNode nd, ObjIndexStrategy ois);

    void set(WnObj o, String regex);

    int each(WnQuery q, Each<WnObj> callback);

    List<WnObj> query(WnQuery q);

    void remove(String id);

}
