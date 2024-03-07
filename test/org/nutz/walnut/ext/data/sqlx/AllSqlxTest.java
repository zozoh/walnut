package org.nutz.walnut.ext.data.sqlx;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.nutz.walnut.ext.data.sqlx.ast.SqlCriteriaNodeTest;
import org.nutz.walnut.ext.data.sqlx.srv.WnSqlTmplTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({WnSqlTmplTest.class, SqlCriteriaNodeTest.class})
public class AllSqlxTest {}
