package org.nutz.walnut.api.io;

import java.util.List;

import org.nutz.json.JsonFormat;
import org.nutz.lang.Each;
import org.nutz.lang.util.Callback;

public interface WnIo extends WnStore {

    WnObj checkById(String id);

    WnObj check(WnObj p, String path);

    WnObj fetch(WnObj p, String path);

    WnObj fetch(WnObj p, String[] paths, int fromIndex, int toIndex);

    void walk(WnObj p, Callback<WnObj> callback, WalkMode mode);

    WnObj move(WnObj src, String destPath);

    void rename(WnObj o, String newName);

    void changeType(WnObj o, String tp);

    WnObj createIfNoExists(WnObj p, String path, WnRace race);

    WnObj create(WnObj p, String path, WnRace race);

    WnObj create(WnObj p, String[] paths, int fromIndex, int toIndex, WnRace race);

    void delete(WnObj o);

    WnObj get(String id);

    WnObj getOne(WnQuery q);

    int each(WnQuery q, Each<WnObj> callback);

    List<WnObj> query(WnQuery q);

    void setMount(WnObj o, String mnt);

    void writeMeta(WnObj o, Object meta);
    
    void appendMeta(WnObj o, Object meta);

    String readText(WnObj o);

    <T> T readJson(WnObj o, Class<T> classOfT);

    long writeText(WnObj o, CharSequence cs);

    long appendText(WnObj o, CharSequence cs);

    long writeJson(WnObj o, Object obj, JsonFormat fmt);
}
