package com.site0.walnut.ext.data.sqlx;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import com.site0.walnut.ext.data.sqlx.ast.SqlCriteriaNodeTest;
import com.site0.walnut.ext.data.sqlx.loader.SqlEntryTest;
import com.site0.walnut.ext.data.sqlx.srv.WnSqlTmplTest;
import com.site0.walnut.ext.data.sqlx.util.SqlVarsPuttingTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({SqlEntryTest.class,
                     WnSqlTmplTest.class,
                     SqlCriteriaNodeTest.class,
                     SqlVarsPuttingTest.class})
public class AllSqlxTest {}
