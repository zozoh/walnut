package org.nutz.walnut.api.io;

import java.util.List;

import org.nutz.lang.Each;
import org.nutz.lang.util.Callback;
import org.nutz.walnut.util.UnitTestable;

public interface WnTree extends UnitTestable {

    boolean exists(WnObj p, String path);

    boolean existsId(String id);

    WnObj checkById(String id);

    WnObj check(WnObj p, String path);

    WnObj fetch(WnObj p, String path);

    WnObj fetch(WnObj p, String[] paths, int fromIndex, int toIndex);

    void walk(WnObj p, Callback<WnObj> callback, WalkMode mode);

    WnObj move(WnObj src, String destPath);

    WnObj rename(WnObj o, String nm);

    void set(WnObj o, String regex);

    WnObj createIfNoExists(WnObj p, String path, WnRace race);

    WnObj create(WnObj p, String path, WnRace race);

    WnObj create(WnObj p, String[] paths, int fromIndex, int toIndex, WnRace race);

    WnObj createById(WnObj p, String id, String name, WnRace race);

    void delete(WnObj o);

    WnObj get(String id);

    WnObj getOne(WnQuery q);

    WnObj getRoot();

    String getRootId();

    boolean isRoot(String id);

    boolean isRoot(WnObj o);

    int each(WnQuery q, Each<WnObj> callback);

    List<WnObj> query(WnQuery q);

    long count(WnQuery q);

    boolean hasChild(WnObj p);

}
