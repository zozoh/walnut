package org.nutz.walnut.api.io;

import org.nutz.lang.util.Callback;

public interface WnIo extends WnIndexer, WnStore {

    WnObj fetch(WnObj p, String path);

    WnObj fetch(WnObj p, String[] paths, int fromIndex, int toIndex);

    void walk(WnObj p, Callback<WnObj> callback, WalkMode mode);

    WnObj move(WnObj o, String destPath);

    WnObj create(WnObj p, String path, WnRace race);

    WnObj create(WnObj p, String[] paths, int fromIndex, int toIndex, WnRace race);

    void delete(WnObj o);

    void setMount(WnObj o, String mnt);
}
