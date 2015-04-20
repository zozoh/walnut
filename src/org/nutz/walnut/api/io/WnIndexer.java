package org.nutz.walnut.api.io;

import java.util.Date;
import java.util.List;

import org.nutz.lang.Each;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.util.UnitTestable;

public interface WnIndexer extends UnitTestable {

    WnObj get(String id);

    WnObj getBy(String id, String... keys);

    void set(String id, NutMap map);

    void set(WnObj o);

    Object getValue(String id, String key);

    void setValue(String id, String key, Object val);

    <T> T getAs(String id, Class<T> type, String key);

    <T> T getAs(String id, Class<T> type, String key, T dft);

    int getInt(String id, String key);

    int getInt(String id, String key, int dft);

    long getLong(String id, String key);

    long getLong(String id, String key, long dft);

    String getString(String id, String key);

    String getString(String id, String key, String dft);

    boolean getBoolean(String id, String key);

    boolean getBoolean(String id, String key, boolean dft);

    float getFloat(String id, String key);

    float getFloat(String id, String key, float dft);

    double getDouble(String id, String key);

    double getDouble(String id, String key, double dft);

    Date getTime(String id, String key);

    Date getTime(String id, String key, Date dft);

    <T extends Enum<T>> T getEnum(String id, String key, Class<T> classOfEnum);

    <T extends Enum<T>> T getEnum(String id, String key, Class<T> classOfEnum, T dft);

    boolean isEnum(String id, String key, Enum<?>... eus);

    int each(WnQuery q, Each<WnObj> callback);

    List<WnObj> query(WnQuery q);

    WnObj getOne(WnQuery q);

    void remove(String id);

}
