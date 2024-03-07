package org.nutz.walnut.ext.data.sqlx.expert;

public interface SqlDialect {

    void joinRegexp(StringBuilder sb, String name, String regexp);

}
