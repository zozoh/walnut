package org.nutz.dao.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.nutz.dao.Chain;
import org.nutz.dao.Condition;
import org.nutz.dao.FieldMatcher;
import org.nutz.dao.entity.Entity;
import org.nutz.dao.entity.MappingField;
import org.nutz.dao.entity.PkType;
import org.nutz.dao.impl.sql.pojo.AbstractPItem;
import org.nutz.dao.impl.sql.pojo.ConditionPItem;
import org.nutz.dao.impl.sql.pojo.InsertByChainPItem;
import org.nutz.dao.interceptor.PojoInterceptor;
import org.nutz.dao.sql.Criteria;
import org.nutz.dao.sql.DaoStatement;
import org.nutz.dao.sql.Pojo;
import org.nutz.dao.sql.PojoMaker;
import org.nutz.dao.sql.SqlType;
import org.nutz.dao.util.Pojos;
import org.nutz.dao.util.cri.Static;

import com.site0.walnut.util.Wlang;

public class EntityOperator {

    protected Entity<?> entity;

    protected NutDao dao;

    protected Object myObj;

    protected List<Pojo> pojoList = new ArrayList<Pojo>();

    private int updateCount;

    public void setMyObj(Object obj) {
        if (obj.getClass().isArray()) {
            this.myObj = Wlang.array2list((Object[]) obj);
        } else {
            this.myObj = obj;
        }

    }

    /**
     * 批量执行准备好的 Dao 语句
     * 
     * @return 自身
     */
    public EntityOperator exec() {
        /*
         * 为每个语句检查一遍参数状态
         */
        if (null != entity) {
            for (Pojo pojo : pojoList) {
                if (null == pojo.getOperatingObject())
                    pojo.setOperatingObject(myObj);
                if (pojo.params().isEmpty())
                    pojo.addParamsBy(pojo.getOperatingObject());
            }
            updateCount = dao._exec(pojoList.toArray(new DaoStatement[pojoList.size()]));
        }
        return this;
    }

    public Pojo addUpdate() {
        return addUpdate(entity, myObj);
    }

    public Pojo addUpdate(Chain chain, Condition cnd) {
        Pojo pojo = dao.pojoMaker.makePojo(SqlType.UPDATE);
        pojo.setEntity(entity);
        pojo.append(Pojos.Items.entityTableName());
        pojo.append(Pojos.Items.updateFieldsBy(chain));
        pojo.append(Pojos.Items.cnd(cnd));
        pojoList.add(pojo);
        return pojo;
    }

    public Pojo addUpdate(final Entity<?> en, final Object obj) {
        if (null == en)
            return null;

        // 触发Pojo拦截器
        _fireEvent("prevUpdate", obj, en);

        Pojo pojo = dao.pojoMaker.makeUpdate(en, null)
                                 .append(Pojos.Items.cndAuto(en, Wlang.firstInAny(obj)))
                                 .setOperatingObject(obj);
        pojoList.add(pojo);
        return pojo;
    }

    public Pojo addUpdateByPkAndCnd(Condition cnd) {
        return addUpdateByPkAndCnd(entity, myObj, cnd);
    }

    public Pojo addUpdateByPkAndCnd(final Entity<?> en, final Object obj, final Condition cnd) {
        if (null == en)
            return null;

        // 触发Pojo拦截器
        _fireEvent("prevUpdate", obj, en);

        Pojo pojo = dao.pojoMaker.makeUpdate(en, null);

        boolean pureCnd = en.getPkType() == PkType.UNKNOWN;
        if (!pureCnd) {
            pojo.append(Pojos.Items.cndAuto(en, Wlang.firstInAny(obj)));
            pojo.append(new Static(" AND "));
        }
        if (cnd instanceof Criteria) {
            // 只取它的where条件
            pojo.append(((Criteria) cnd).where().setTop(pureCnd));
        } else {
            pojo.append(new ConditionPItem(cnd).setTop(pureCnd));
        }
        pojo.setOperatingObject(obj);
        pojoList.add(pojo);
        return pojo;
    }

    public List<Pojo> addUpdateForIgnoreNull(final Entity<?> en,
                                             final Object obj,
                                             final FieldMatcher fm) {

        if (null == en)
            return null;

        // 触发Pojo拦截器
        _fireEvent("prevUpdate", obj, en);

        final FieldMatcher newFM;
        if (null == fm)
            newFM = FieldMatcher.make(null, null, true);
        else {
            newFM = fm;
            newFM.setIgnoreNull(true);
        }
        final List<Pojo> re = new ArrayList<Pojo>(Wlang.eleSize(obj));

        Wlang.eachEvenMap(obj, (int i, Object ele, Object src) -> {
            Pojo pojo = dao.pojoMaker.makeUpdate(en, ele)
                                     .append(Pojos.Items.cndAuto(en, ele))
                                     .setOperatingObject(ele);
            pojo.getContext().setFieldMatcher(newFM);
            re.add(pojo);
        });
        pojoList.addAll(re);

        return re;
    }

    public Pojo addUpdateAndIncrIfMatch(final Entity<?> en, final Object obj, String fieldName) {
        if (null == en)
            return null;

        // 触发Pojo拦截器
        _fireEvent("prevUpdate", obj, en);

        MappingField mf = en.getField(fieldName);
        Pojo pojo = dao.pojoMaker.makeUpdate(en, null)
                                 .append(new Static(","
                                                    + mf.getColumnNameInSql()
                                                    + "="
                                                    + mf.getColumnNameInSql()
                                                    + "+1"))
                                 .append(Pojos.Items.cndAuto(en, Wlang.firstInAny(obj)))
                                 .setOperatingObject(obj);
        pojo.append(new Static("AND"))
            .append(((AbstractPItem) Pojos.Items.cndColumn(mf, null)).setTop(false));
        pojoList.add(pojo);
        return pojo;
    }

    public Pojo addUpdate(Condition cnd) {
        if (null == entity)
            return null;

        Pojo pojo = dao.pojoMaker.makeUpdate(entity, null).append(Pojos.Items.cnd(cnd));
        pojoList.add(pojo);
        return pojo;
    }

    public Pojo addDeleteSelfOnly(long id) {
        if (null == entity)
            return null;

        Pojo pojo = dao.pojoMaker.makeDelete(entity);
        pojo.append(Pojos.Items.cndId(entity, id));
        pojo.addParamsBy(myObj);
        pojoList.add(pojo);
        return pojo;
    }

    public Pojo addDeleteSelfOnly(String name) {
        if (null == entity)
            return null;

        Pojo pojo = dao.pojoMaker.makeDelete(entity);
        pojo.append(Pojos.Items.cndName(entity, name));
        pojo.addParamsBy(name);
        pojoList.add(pojo);
        return pojo;
    }

    public Pojo addDeleteSelfOnly() {
        if (null == entity)
            return null;
        // 触发Pojo拦截器
        _fireEvent("prevDelete", myObj, this.entity);
        Pojo pojo = dao.pojoMaker.makeDelete(entity);
        pojo.append(Pojos.Items.cndAuto(entity, myObj));
        pojo.addParamsBy(myObj);
        pojoList.add(pojo);
        return pojo;
    }

    public List<Pojo> addInsert() {
        return addInsert(entity, myObj);
    }

    public List<Pojo> addInsert(Entity<?> en, Object obj) {
        if (null == en)
            return null;

        // 触发Pojo拦截器
        _fireEvent("prevInsert", obj, en);

        int len = Map.class.isAssignableFrom(obj.getClass()) ? 1 : Wlang.eleSize(obj);
        List<Pojo> re = new ArrayList<Pojo>(len);
        if (len > 0) {
            if (len == 1) {
                for (Pojo pojo : en.cloneBeforeInsertMacroes())
                    re.add(pojo.setOperatingObject(obj));
            }
            re.add(dao.pojoMaker.makeInsert(en).setOperatingObject(obj));
            if (len == 1) {
                for (Pojo pojo : en.cloneAfterInsertMacroes())
                    re.add(pojo.setOperatingObject(obj));
            }
            pojoList.addAll(re);
        }
        return re;
    }

    public Pojo addInsertSelfOnly() {
        return addInsertSelfOnly(entity, myObj);
    }

    public Pojo addInsertSelfOnly(Entity<?> en, Object obj) {
        if (null == en)
            return null;
        Pojo pojo;

        if (obj instanceof Chain) {
            pojo = dao.pojoMaker.makePojo(SqlType.INSERT);
            pojo.append(Pojos.Items.entityTableName());
            pojo.append(new InsertByChainPItem((Chain) obj));
            pojo.setEntity(en);
        } else {
            // 触发Pojo拦截器
            _fireEvent("prevInsert", obj, en);
            pojo = dao.pojoMaker.makeInsert(en).setOperatingObject(obj);
        }
        pojoList.add(pojo);
        return pojo;
    }

    public EntityOperator add(Pojo pojo) {
        pojoList.add(pojo);
        return this;
    }

    public int getUpdateCount() {
        return updateCount;
    }

    public PojoMaker maker() {
        return dao.pojoMaker;
    }

    public Entity<?> getEntityBy(Object obj) {
        return dao.holder.getEntityBy(obj);
    }

    public Entity<?> getEntity(Class<?> type) {
        return dao.holder.getEntity(type);
    }

    public Entity<?> makeEntity(String tableName, Map<String, Object> map) {
        return dao.holder.makeEntity(tableName, map);
    }

    public int getPojoListSize() {
        return pojoList.size();
    }

    protected void _fireEvent(final String event, Object obj, final Entity<?> entity) {
        final PojoInterceptor pint = entity.getInterceptor();
        if (pint != null && pint.isAvailable()) {
            if (obj.getClass().isArray() || obj instanceof Collection<?>) {

                Wlang.eachEvenMap(obj, (int index, Object ele, Object src) -> {
                    pint.onEvent(ele, entity, event);
                });
            } else
                pint.onEvent(obj, entity, event);
        }
    }
}
