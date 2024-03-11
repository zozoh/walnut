package org.nutz.walnut.ext.data.sqlx.hdl;

import java.util.LinkedList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.walnut.ext.data.sqlx.SqlxContext;
import org.nutz.walnut.ext.data.sqlx.SqlxFilter;
import org.nutz.walnut.ext.data.sqlx.tmpl.SqlCriParam;
import org.nutz.walnut.ext.data.sqlx.tmpl.WnSqlTmpl;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.ZParams;

public class sqlx_view extends SqlxFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "p");
    }

    @Override
    protected void process(WnSystem sys, SqlxContext fc, ZParams params) {
        String sqlName = params.val_check(0);
        // 读取 SQL 模板
        WnSqlTmpl t = fc.sqls.get(sqlName);

        // 参数模式
        List<SqlCriParam> criParams = null;
        if (params.is("p")) {
            criParams = new LinkedList<>();
        }

        // 渲染
        String str = t.render(fc.vars, criParams);
        sys.out.println(str);

        if (null != criParams) {
            String HR = Ws.repeat('-', 40);
            sys.out.println(HR);
            sys.out.printlnf("Show %s params", criParams.size());
            sys.out.println(HR);
            int i = 1;
            for (SqlCriParam pa : criParams) {
                sys.out.printlnf(" %d) %s => %s", i++, pa.getName(), Json.toJson(pa.getValue()));
            }
        }

        // 阻止末尾输出
        fc.quiet = true;
    }

}
