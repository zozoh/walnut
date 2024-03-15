package org.nutz.dao.impl.entity.field;

import org.nutz.dao.entity.Entity;
import org.nutz.dao.entity.MappingField;
import org.nutz.dao.entity.Record;
import org.nutz.dao.entity.annotation.ColType;
import org.nutz.dao.impl.entity.EntityObjectContext;
import org.nutz.dao.jdbc.ValueAdaptor;
import org.nutz.lang.segment.Segment;
import org.nutz.log.Log;
import org.nutz.log.Logs;

import java.sql.ResultSet;
import java.sql.SQLException;

public class NutMappingField extends AbstractEntityField implements MappingField {

    private String columnName;

    private String columnNameInSql;

    private ColType columnType;

    private Segment defaultValue;

    private String columnComment;

    private int width;

    private int precision;

    private boolean isCompositePk;

    private boolean isId;

    private boolean isName;

    private boolean isVersion;

    private boolean readonly;

    private boolean notNull;

    private boolean unsigned;

    private boolean autoIncreasement;

    private boolean casesensitive;

    private boolean hasColumnComment;

    private String customDbType;

    private ValueAdaptor adaptor;

    private boolean insert = true;

    private boolean update = true;

    private static final Log log = Logs.get();

    public NutMappingField() {
        this(null);
    }

    public NutMappingField(Entity<?> entity) {
        super(entity);
        casesensitive = true;
    }

    public ValueAdaptor getAdaptor() {
        return adaptor;
    }

    public void setAdaptor(ValueAdaptor adaptor) {
        this.adaptor = adaptor;
    }

    public void injectValue(Object obj, Record rec, String prefix) {
        try {
            Object val = rec.get(prefix == null ? columnName : prefix + columnName);
            this.setValue(obj, val);
        }
        catch (Exception e) {
            if (log.isTraceEnabled()) {
                log.tracef("columnName=" + columnName, e);
            }
        }
    }

    public void injectValue(Object obj, ResultSet rs, String prefix) {
        try {
            this.setValue(obj, adaptor.get(rs, prefix == null ? columnName : prefix + columnName));
        }
        catch (SQLException e) {
            if (log.isTraceEnabled()) {
                log.tracef("columnName=" + columnName, e);
            }
        }
    }

    public String getColumnName() {
        return columnName;
    }

    public ColType getColumnType() {
        return columnType;
    }

    public String getDefaultValue(Object obj) {
        if (null == defaultValue)
            return null;
        String re;
        if (null == obj || defaultValue.keyCount() == 0)
            re = defaultValue.toString();
        else
            re = defaultValue.render(new EntityObjectContext(getEntity(), obj)).toString();
        return re;
    }

    public int getWidth() {
        return width;
    }

    public int getPrecision() {
        return precision;
    }

    public boolean isCompositePk() {
        return isCompositePk;
    }

    public boolean isPk() {
        return isId || (!isId && isName) || isCompositePk;
    }

    public boolean isId() {
        return isId;
    }

    public boolean isName() {
        return isName;
    }

    public boolean isReadonly() {
        return readonly;
    }

    public boolean hasDefaultValue() {
        return null != defaultValue;
    }

    public boolean isNotNull() {
        return notNull;
    }

    public boolean isCasesensitive() {
        return casesensitive;
    }

    public boolean isAutoIncreasement() {
        return autoIncreasement;
    }

    public boolean isUnsigned() {
        return unsigned;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public void setColumnType(ColType columnType) {
        this.columnType = columnType;
    }

    public void setColumnComment(String columnComment) {
        this.columnComment = columnComment;
    }

    public void setHasColumnComment(boolean hasColumnComment) {
        this.hasColumnComment = hasColumnComment;
    }

    public void setDefaultValue(Segment defaultValue) {
        this.defaultValue = defaultValue;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setPrecision(int precision) {
        this.precision = precision;
    }

    public void setAsCompositePk() {
        this.isCompositePk = true;
    }

    public void setAsId() {
        this.isId = true;
    }

    public void setAsName() {
        this.isName = true;
    }

    public void setAsReadonly() {
        this.readonly = true;
    }

    public void setAsNotNull() {
        this.notNull = true;
    }

    public void setAsUnsigned() {
        this.unsigned = true;
    }

    public void setCasesensitive(boolean casesensitive) {
        this.casesensitive = casesensitive;
    }

    public void setAsAutoIncreasement() {
        this.autoIncreasement = true;
    }

    public void setAutoIncreasement(boolean autoIncreasement) {
        this.autoIncreasement = autoIncreasement;
    }

    public String getColumnComment() {
        return columnComment;
    }

    public boolean hasColumnComment() {
        return hasColumnComment;
    }

    public void setCustomDbType(String customDbType) {
        this.customDbType = customDbType;
    }

    public String getCustomDbType() {
        return customDbType;
    }

    public boolean isInsert() {
        return insert;
    }

    public boolean isUpdate() {
        return update;
    }

    public void setInsert(boolean insert) {
        this.insert = insert;
    }

    public void setUpdate(boolean update) {
        this.update = update;
    }

    public String getColumnNameInSql() {
        if (columnNameInSql != null)
            return columnNameInSql;
        return columnName;
    }

    public void setColumnNameInSql(String columnNameInSql) {
        this.columnNameInSql = columnNameInSql;
    }

    public boolean isVersion() {
        return isVersion;
    }

    public void setAsVersion() {
        this.isVersion = true;
    }

    // 补充一下 Setter/Getter

    public Segment getDefaultValue() {
        return defaultValue;
    }

    public boolean isHasColumnComment() {
        return hasColumnComment;
    }

    public void setCompositePk(boolean isCompositePk) {
        this.isCompositePk = isCompositePk;
    }

    public void setId(boolean isId) {
        this.isId = isId;
    }

    public void setName(boolean isName) {
        this.isName = isName;
    }

    public void setVersion(boolean isVersion) {
        this.isVersion = isVersion;
    }

    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

    public void setNotNull(boolean notNull) {
        this.notNull = notNull;
    }

    public void setUnsigned(boolean unsigned) {
        this.unsigned = unsigned;
    }

    public NutMappingField clone() {
        NutMappingField mf = new NutMappingField(entity);
        mf.entity = this.entity;
        mf.name = this.name;
        mf.type = this.type;
        mf.typeClass = this.typeClass;
        mf.mirror = this.mirror;
        mf.injecting = this.injecting;
        mf.ejecting = this.ejecting;
        mf.columnName = this.columnName;
        mf.columnNameInSql = this.columnNameInSql;
        mf.columnType = this.columnType;
        mf.defaultValue = this.defaultValue;
        mf.columnComment = this.columnComment;
        mf.width = this.width;
        mf.precision = this.precision;
        mf.isCompositePk = this.isCompositePk;
        mf.isId = this.isId;
        mf.isName = this.isName;
        mf.isVersion = this.isVersion;
        mf.readonly = this.readonly;
        mf.notNull = this.notNull;
        mf.unsigned = this.unsigned;
        mf.autoIncreasement = this.autoIncreasement;
        mf.casesensitive = this.casesensitive;
        mf.hasColumnComment = this.hasColumnComment;
        mf.customDbType = this.customDbType;
        mf.adaptor = this.adaptor;
        mf.insert = this.insert;
        mf.update = this.update;
        return mf;
    }

}
