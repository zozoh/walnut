package org.nutz.dao.impl.link;

import org.nutz.dao.entity.LinkField;
import org.nutz.dao.impl.AbstractLinkVisitor;
import org.nutz.dao.impl.entity.field.ManyManyLinkField;
import org.nutz.dao.sql.Pojo;
import org.nutz.dao.util.Pojos;

import com.site0.walnut.util.Wlang;

/**
 * 在中间表中，清除关于所有的链接对象的映射。
 * <p>
 * 即，如果 A-X, B-X， 因为链接对象是 X，它会将这两个关系都清除
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class DoClearRelationByLinkedFieldLinkVisitor extends AbstractLinkVisitor {

    public void visit(Object obj, LinkField lnk) {
        if (lnk instanceof ManyManyLinkField) {
            final ManyManyLinkField mm = (ManyManyLinkField) lnk;
            Object value = mm.getValue(obj);
            if (Wlang.eleSize(value) == 0)
                return;

            final Pojo pojo = opt.maker().makeDelete(mm.getRelationName());
            pojo.append(Pojos.Items.cndColumn(mm.getToColumnName(), mm.getLinkedField(), null));

            Wlang.eachEvenMap(value, (int i, Object ele, Object src) -> {
                pojo.addParamsBy(mm.getLinkedField().getValue(ele));
            });

            opt.add(pojo);
        }
    }

}
