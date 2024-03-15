package org.nutz.dao.impl.sql.pojo;

import java.util.List;

import org.nutz.dao.FieldMatcher;
import org.nutz.dao.entity.Entity;
import org.nutz.dao.entity.MappingField;
import com.site0.walnut.util.Wlang;

public class QueryEntityFieldsPItem extends NoParamsPItem {

    private static final long serialVersionUID = 1L;

    public void joinSql(Entity<?> en, StringBuilder sb) {
        FieldMatcher fm = getFieldMatcher();
        if (null == fm) {
            sb.append("* ");
        } else {
            List<MappingField> efs = _en(en).getMappingFields();

            int old = sb.length();

            for (MappingField ef : efs) {
                if (fm.match(ef.getName()))
                    sb.append(ef.getColumnNameInSql()).append(',');
            }

            if (sb.length() == old)
                throw Wlang.makeThrow("No columns be queryed: '%s'", _en(en));

            sb.setCharAt(sb.length() - 1, ' ');
        }
    }

}
