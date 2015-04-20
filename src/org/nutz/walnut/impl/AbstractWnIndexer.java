package org.nutz.walnut.impl;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.nutz.castor.Castors;
import org.nutz.lang.Each;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.walnut.api.io.WnIndexer;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;

public abstract class AbstractWnIndexer implements WnIndexer {

    public WnObj get(String id) {
        return getBy(id);
    }

    @Override
    public List<WnObj> query(WnQuery q) {
        final List<WnObj> list = new LinkedList<WnObj>();
        each(q, new Each<WnObj>() {
            public void invoke(int index, WnObj obj, int length) {
                list.add(obj);
            }
        });
        return list;
    }

    @Override
    public WnObj getOne(WnQuery q) {
        final WnObj[] re = new WnObj[1];
        each(q, new Each<WnObj>() {
            public void invoke(int index, WnObj obj, int length) {
                re[0] = obj;
                Lang.Break();
            }
        });
        return re[0];
    }

    @Override
    public void set(WnObj o) {
        set(o.id(), o.toMap4Update(null));
    }

    @Override
    public <T> T getAs(String id, Class<T> type, String key) {
        return getAs(id, type, key, null);
    }

    @Override
    public <T> T getAs(String id, Class<T> type, String key, T dft) {
        Object v = getValue(id, key);
        return null == v ? dft : Castors.me().castTo(v, type);
    }

    @Override
    public int getInt(String id, String key) {
        return getInt(id, key, -1);
    }

    @Override
    public int getInt(String id, String key, int dft) {
        Object v = getValue(id, key);
        return null == v ? dft : Castors.me().castTo(v, Integer.class);
    }

    @Override
    public long getLong(String id, String key) {
        return getLong(id, key, -1);
    }

    @Override
    public long getLong(String id, String key, long dft) {
        Object v = getValue(id, key);
        return null == v ? dft : Castors.me().castTo(v, Long.class);
    }

    @Override
    public String getString(String id, String key) {
        return getString(id, key, null);
    }

    @Override
    public String getString(String id, String key, String dft) {
        Object v = getValue(id, key);
        return null == v ? dft : Castors.me().castTo(v, String.class);
    }

    @Override
    public boolean getBoolean(String id, String key) {
        return getBoolean(id, key, false);
    }

    @Override
    public boolean getBoolean(String id, String key, boolean dft) {
        Object v = getValue(id, key);
        return null == v ? dft : Castors.me().castTo(v, Boolean.class);
    }

    @Override
    public float getFloat(String id, String key) {
        return getFloat(id, key, Float.NaN);
    }

    @Override
    public float getFloat(String id, String key, float dft) {
        Object v = getValue(id, key);
        return null == v ? dft : Castors.me().castTo(v, Float.class);
    }

    @Override
    public double getDouble(String id, String key) {
        return getDouble(id, key, Double.NaN);
    }

    @Override
    public double getDouble(String id, String key, double dft) {
        Object v = getValue(id, key);
        return null == v ? dft : Castors.me().castTo(v, Double.class);
    }

    @Override
    public Date getTime(String id, String key) {
        return getTime(id, key, null);
    }

    @Override
    public Date getTime(String id, String key, Date dft) {
        Object v = getValue(id, key);
        return null == v ? dft : Castors.me().castTo(v, Date.class);
    }

    @Override
    public <T extends Enum<T>> T getEnum(String id, String key, Class<T> classOfEnum) {
        return getEnum(id, key, classOfEnum, null);
    }

    @Override
    public <T extends Enum<T>> T getEnum(String id, String key, Class<T> classOfEnum, T dft) {
        String s = getString(id, key);
        if (Strings.isBlank(s))
            return dft;
        return Castors.me().castTo(s, classOfEnum);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean isEnum(String id, String key, Enum<?>... eus) {
        if (null == eus || eus.length == 0)
            return false;
        try {
            Enum<?> v = getEnum(id, key, eus[0].getClass());
            for (Enum<?> eu : eus)
                if (!v.equals(eu))
                    return false;
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

}
