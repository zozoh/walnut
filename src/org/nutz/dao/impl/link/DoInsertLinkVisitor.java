package org.nutz.dao.impl.link;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.nutz.dao.entity.Entity;
import org.nutz.dao.entity.LinkField;
import org.nutz.dao.impl.AbstractLinkVisitor;
import org.nutz.dao.sql.Pojo;
import org.nutz.dao.sql.PojoCallback;
import org.nutz.dao.util.Pojos;

import com.site0.walnut.util.Wlang;

public class DoInsertLinkVisitor extends AbstractLinkVisitor {

    public void visit(final Object obj, final LinkField lnk) {
        final Object value = lnk.getValue(obj);
        if (Wlang.eleSize(value) == 0)
            return;

        // 从宿主对象更新关联对象
        opt.add(Pojos.createRun(new PojoCallback() {
            public Object invoke(Connection conn, ResultSet rs, Pojo pojo, Statement stmt)
                    throws SQLException {
                lnk.updateLinkedField(obj, value);
                return pojo.getOperatingObject();
            }
        }).setOperatingObject(obj));

        // 为其循环生成插入语句 : holder.getEntityBy 会考虑到集合和数组的情况的
        final Entity<?> en = lnk.getLinkedEntity();

        Wlang.eachEvenMap(value, (int i, Object ele, Object src) -> {
            if (ele == null)
                throw new NullPointerException("null ele in linked field!!");
            // 执行插入
            opt.addInsert(en, ele);
            // 更新字段
            opt.add(Pojos.createRun(new PojoCallback() {
                public Object invoke(Connection conn, ResultSet rs, Pojo pojo, Statement stmt)
                        throws SQLException {
                    lnk.saveLinkedField(obj, pojo.getOperatingObject());
                    return pojo.getOperatingObject();
                }
            }).setOperatingObject(ele));
        });

    }

}
