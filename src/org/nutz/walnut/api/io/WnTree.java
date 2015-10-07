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

    /**
     * 修改某对象的某个整型元数据
     * 
     * @param id
     *            对象 ID
     * @param key
     *            元数据名称
     * @param val
     *            修改的值
     * @return 修改前的值
     */
    int inc(String id, String key, int val);

    int getInt(String id, String key, int dft);

    long getLong(String id, String key, long dft);

    String getString(String id, String key, String dft);

    <T> T getAs(String id, String key, Class<T> classOfT, T dft);

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
