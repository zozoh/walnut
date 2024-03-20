package com.site0.walnut.ext.data.sqlx.hdl;

import java.util.LinkedList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.ext.data.sqlx.SqlxContext;
import com.site0.walnut.ext.data.sqlx.SqlxFilter;
import com.site0.walnut.ext.data.sqlx.tmpl.SqlParam;
import com.site0.walnut.ext.data.sqlx.tmpl.WnSqlTmpl;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;

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
        List<SqlParam> criParams = null;
        if (params.is("p")) {
            criParams = new LinkedList<>();
        }

        // 渲染
        NutMap context;
        if (fc.hasVarList()) {
            context = fc.getVarList().get(0);
        } else if(fc.hasVarMap()){
            context = fc.getVarMap();
        }else {
            context = new NutMap();
        }
        String str = t.render(context, criParams);
        sys.out.println(str);

        if (null != criParams) {
            String HR = Ws.repeat('-', 40);
            sys.out.println(HR);
            sys.out.printlnf("Show %s params", criParams.size());
            sys.out.println(HR);
            int i = 1;
            for (SqlParam pa : criParams) {
                sys.out.printlnf(" %d) %s => %s", i++, pa.getName(), Json.toJson(pa.getValue()));
            }
        }

        // 阻止末尾输出
        fc.quiet = true;
    }

}
