package org.nutz.dao.impl.entity.field;

import org.nutz.dao.Cnd;
import org.nutz.dao.Condition;
import org.nutz.dao.Sqls;
import org.nutz.dao.entity.Entity;
import org.nutz.dao.entity.LinkType;
import org.nutz.dao.entity.PkType;
import org.nutz.dao.impl.EntityHolder;
import org.nutz.dao.impl.entity.EntityName;
import org.nutz.dao.impl.entity.info.LinkInfo;
import org.nutz.dao.util.cri.SimpleCriteria;
import org.nutz.lang.Strings;

import com.site0.walnut.util.Wlang;

public class ManyManyLinkField extends AbstractLinkField {

    private EntityName relationTableName;

    private String fromColumnName;

    private String toColumnName;

    public ManyManyLinkField(Entity<?> host,
                             EntityHolder holder,
                             LinkInfo info,
                             Class<?> klass,
                             String from,
                             String to,
                             String relation,
                             String key) {
        super(host, holder, info);
        this.targetType = klass;
        this.mapKey = key;
        this.relationTableName = EntityName.create(relation);

        String[] ss = Strings.splitIgnoreBlank(from, ":");
        this.fromColumnName = ss[0];
        String fromField = ss.length > 1 ? ss[1] : null;

        ss = Strings.splitIgnoreBlank(to, ":");
        this.toColumnName = ss[0];
        String toField = ss.length > 1 ? ss[1] : null;
        _make(host, fromField, toField);
    }

    public ManyManyLinkField(Entity<?> host, EntityHolder holder, LinkInfo info) {
        super(host, holder, info);
        this.targetType = guessTargetClass(info, info.manymany.target());
        this.mapKey = info.manymany.key();
        this.relationTableName = EntityName.create(info.manymany.relation());

        String[] ss = Strings.splitIgnoreBlank(info.manymany.from(), ":");
        this.fromColumnName = ss[0];
        String fromField = ss.length > 1 ? ss[1] : null;

        ss = Strings.splitIgnoreBlank(info.manymany.to(), ":");
        this.toColumnName = ss[0];
        String toField = ss.length > 1 ? ss[1] : null;
        _make(host, fromField, toField);
    }

    protected void _make(Entity<?> host, String fromField, String toField) {
        /*
         * 开始分析两个实体的链接字段
         */
        Entity<?> ta = this.getLinkedEntity();

        // 用户指定了 "from" 的 Java 字段名
        if (fromField != null) {
            hostField = host.getField(fromField);
            if (hostField == null) {
                // 指定了from的字段名,但找不到?!!!
                throw Wlang.makeThrow("@ManyMany(from='%s') is invalid, no such field!! Host class=%s",
                                      fromField,
                                      host.getType().getName());
            }
        }
        // 用户指定了 "to" 的 Java 字段名
        if (null != toField) {
            linkedField = ta.getField(toField);
            if (linkedField == null) {
                // 指定了from的字段名,但找不到?!!!
                throw Wlang.makeThrow("@ManyMany(to='%s') is invalid, no such field!! Host class=%s",
                                      toField,
                                      host.getType().getName());
            }
        }

        // 用户仅仅指定了 "from" 的 Java 字段
        if (null != hostField && linkedField == null) {
            linkedField = ta.getPkType() == PkType.ID ? ta.getIdField() : ta.getNameField();
        }
        // 用户仅仅指定了 "to" 的 Java 字段
        else if (null == hostField && linkedField != null) {
            hostField = host.getPkType() == PkType.ID ? host.getIdField() : host.getNameField();
        }
        // 都没指定，优先使用 Id 的链接主键
        else {
            // 都有 ID
            if (null != host.getIdField() && null != ta.getIdField()) {
                hostField = host.getIdField();
                linkedField = ta.getIdField();
            }
            // 宿主ID，链 Name
            else if (null != host.getIdField() && null != ta.getNameField()) {
                hostField = host.getIdField();
                linkedField = ta.getNameField();
            }
            // 宿主Name 链 ID
            else if (null != host.getNameField() && null != ta.getIdField()) {
                hostField = host.getNameField();
                linkedField = ta.getIdField();
            }
            // 都有 Name
            else if (null != host.getNameField() && null != ta.getNameField()) {
                hostField = host.getNameField();
                linkedField = ta.getNameField();
            }

        }
        // 最后再检查一下 ...
        if (null == hostField) {
            throw Wlang.makeThrow("@ManyMany at [%s#%s] is Invalid: lack @Id or @Name at class=%s",
                                  host.getType().getName(),
                                  getName(),
                                  host.getType().getName());
        }
        if (null == linkedField) {
            throw Wlang.makeThrow("@ManyMany at [%s#%s] is Invalid: lack @Id or @Name at class=%s",
                                  host.getType().getName(),
                                  getName(),
                                  target.getType().getName());
        }

    }

    public Condition createCondition(Object host) {
        SimpleCriteria cri = Cnd.cri();
        cri.where()
           .andInBySql(linkedField.getColumnName(),
                       "SELECT %s FROM %s WHERE %s=%s",
                       toColumnName,
                       this.getRelationName(),
                       fromColumnName,
                       Sqls.formatFieldValue(hostField.getValue(host)));
        return cri;
    }

    public void updateLinkedField(Object obj, Object linked) {}

    public void saveLinkedField(Object obj, Object linked) {}

    public LinkType getLinkType() {
        return LinkType.MANYMANY;
    }

    public String getRelationName() {
        return this.relationTableName.value();
    }

    public String getFromColumnName() {
        return fromColumnName;
    }

    public String getToColumnName() {
        return toColumnName;
    }

    /**
     * 返回关联两个实体的主键 Java 字段名数组
     * <p>
     * 数组的第一个元素是宿主主键的字段名，第二个元素是映射实体的主键字段名
     * 
     * @return 关联两个实体的主键 Java 字段名数组
     */
    public String[] getLinkedPkNames() {
        String[] re = new String[2];
        re[0] = hostField.getName();
        re[1] = linkedField.getName();
        return re;
    }

}
