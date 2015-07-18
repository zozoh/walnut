package org.nutz.walnut.api.io;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.List;

import org.nutz.json.JsonFormat;
import org.nutz.lang.Each;
import org.nutz.lang.util.Callback;

public interface WnIo extends WnStore {

    boolean exists(WnObj p, String path);

    boolean existsId(String id);

    WnObj checkById(String id);

    WnObj check(WnObj p, String path);

    WnObj fetch(WnObj p, String path);

    WnObj fetch(WnObj p, String[] paths, int fromIndex, int toIndex);

    void walk(WnObj p, Callback<WnObj> callback, WalkMode mode);

    WnObj move(WnObj src, String destPath);

    WnObj rename(WnObj o, String newName);

    WnObj createIfNoExists(WnObj p, String path, WnRace race);

    WnObj create(WnObj p, String path, WnRace race);

    WnObj create(WnObj p, String[] paths, int fromIndex, int toIndex, WnRace race);

    WnObj createById(WnObj p, String id, String name, WnRace race);

    void delete(WnObj o);

    WnObj toObj(WnNode nd);

    WnObj get(String id);

    WnObj get(WnNode nd);

    WnObj getOne(WnQuery q);

    WnObj getRootObj();

    WnObj getParent(WnObj o);

    int each(WnQuery q, Each<WnObj> callback);

    List<WnObj> query(WnQuery q);

    int eachChildren(WnObj p, String str, Each<WnObj> each);

    long countChildren(WnObj p, String tp, boolean withHidden);

    boolean hasChild(WnObj p);

    List<WnObj> getChildren(WnObj p, String str);

    void setMount(WnObj o, String mnt);

    void writeMeta(WnObj o, Object meta);

    void appendMeta(WnObj o, Object meta);

    String readText(WnObj o);

    long readAndClose(WnObj o, OutputStream ops);

    <T> T readJson(WnObj o, Class<T> classOfT);

    long writeText(WnObj o, CharSequence cs);

    long appendText(WnObj o, CharSequence cs);

    long writeJson(WnObj o, Object obj, JsonFormat fmt);

    long writeAndClose(WnObj o, InputStream ins);

    Reader getReader(WnObj o, long off);

    Writer getWriter(WnObj o, long off);

    MimeMap mimes();

    String getRealPath(WnObj o);
}
