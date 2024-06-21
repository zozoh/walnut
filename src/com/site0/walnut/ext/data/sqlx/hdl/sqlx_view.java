package com.site0.walnut.ext.data.sqlx.hdl;

import java.util.LinkedList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.ext.data.sqlx.SqlxContext;
import com.site0.walnut.ext.data.sqlx.SqlxFilter;
import com.site0.walnut.ext.data.sqlx.tmpl.SqlParam;
import com.site0.walnut.ext.data.sqlx.tmpl.WnSqlTmpl;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;

public class sqlx_view extends SqlxFilter {

    final static private String HR = Ws.repeat('-', 40);

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "picqn", "^(explain)$");
    }

    @Override
    protected void process(WnSystem sys, SqlxContext fc, ZParams params) {
        String sqlName = params.val_check(0);

        // 自动展开上下文
        if (params.is("explain")) {
            fc.explainVars();
        }

        // 读取 SQL 模板
        WnSqlTmpl t = fc.sqls.get(sqlName);

        // 参数模式
        List<SqlParam> criParams = null;
        if (params.is("p")) {
            criParams = new LinkedList<>();
        }

        // 阻止末尾输出
        fc.quiet = true;

        // 列表模式: INSERT,UPDATE,DELETE
        if (fc.hasVarList()) {
            int startI = params.getInt("start", 1);
            boolean showI = params.is("i");
            // 渲染原生 SQL
            if (null == criParams) {
                int index = startI;
                for (NutBean ctx : fc.getVarList()) {
                    String str = t.render(ctx, criParams);
                    if (!str.endsWith(";")) {
                        str += ';';
                    }
                    if (showI) {
                        sys.out.printlnf("%d) %s", index++, str);
                    } else {
                        sys.out.println(str);
                    }
                }
            }
            // 渲染 SQL 模板
            else {
                NutBean c0 = fc.getVarList().get(0);
                String str = t.render(c0, criParams);
                sys.out.println(str);
                outputParams(sys, criParams);
                // 逐个显示 Bean
                if (showI) {
                    sys.out.println(HR);
                    JsonFormat jfmt = Cmds.gen_json_format(params);
                    int index = startI;
                    for (NutBean ctx : fc.getVarList()) {
                        String json = Json.toJson(ctx, jfmt);
                        sys.out.printlnf("%d) %s", index++, json);
                    }
                }
                // 直接将列表变成 JSON 展示
                else {
                    sys.out.println(HR);
                    JsonFormat jfmt = Cmds.gen_json_format(params);
                    sys.out.println(Json.toJson(fc.getVarList(), jfmt));
                }
            }

            // 嗯，搞定
            return;
        }

        // 渲染
        NutBean context;

        // 单对象: SELECT,UPDATE,DELETE
        if (fc.hasVarMap()) {
            context = fc.getVarMap();
        }
        // 默认给一个空上下文
        else {
            context = new NutMap();
        }
        String str = t.render(context, criParams);
        sys.out.println(str);

        if (null != criParams) {
            outputParams(sys, criParams);
        }

    }

    private void outputParams(WnSystem sys, List<SqlParam> criParams) {
        sys.out.println(HR);
        sys.out.printlnf("Show %s params", criParams.size());
        sys.out.println(HR);
        int i = 1;
        for (SqlParam pa : criParams) {
            sys.out.printlnf(" %d) %s => %s", i++, pa.getName(), Json.toJson(pa.getValue()));
        }
    }

}
