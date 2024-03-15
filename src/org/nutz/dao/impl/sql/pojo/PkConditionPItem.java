package org.nutz.dao.impl.sql.pojo;

import java.lang.reflect.Array;
import java.util.Iterator;

import org.nutz.dao.entity.Entity;
import org.nutz.dao.entity.MappingField;
import org.nutz.dao.jdbc.ValueAdaptor;
import com.site0.walnut.util.Wlang;

public class PkConditionPItem extends AbstractPItem {

    private static final long serialVersionUID = 1L;

    private ValueAdaptor[] vas;

    private Object[] pks;

    public PkConditionPItem(ValueAdaptor[] vas, Object[] pks) {
        this.vas = vas;
        this.pks = pks;
    }

    public void joinSql(Entity<?> en, StringBuilder sb) {
        if (top)
            sb.append(" WHERE ");
        Iterator<MappingField> it = _en(en).getCompositePKFields().iterator();
        sb.append(it.next().getColumnNameInSql()).append("=?");
        while (it.hasNext()) {
            sb.append(" AND ").append(it.next().getColumnNameInSql()).append("=?");
        }
        sb.append(' ');
    }

    public int joinAdaptor(Entity<?> en, ValueAdaptor[] adaptors, int off) {
        for (ValueAdaptor va : vas)
            adaptors[off++] = va;
        return off;
    }

    public int joinParams(Entity<?> en, Object obj, Object[] params, int off) {
        if ((null != pks && null == obj) || (pks == obj && null != obj))
            for (Object pk : pks)
                params[off++] = pk;

        else if (null != obj && _en(en).getType().isInstance(obj))
            for (MappingField mf : _en(en).getCompositePKFields())
                params[off++] = mf.getValue(obj);

        else if (null != obj && obj.getClass().isArray())
            for (int i = 0; i < pks.length; i++)
                params[off++] = Array.get(obj, i);

        else
            throw Wlang.impossible();

        return off;
    }

    public int paramCount(Entity<?> en) {
        return vas.length;
    }

}
