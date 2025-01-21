package com.site0.walnut.ext.data.sqlx.hdl;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.nutz.json.Json;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.ext.data.sqlx.SqlxContext;
import com.site0.walnut.ext.data.sqlx.SqlxFilter;
import com.site0.walnut.ext.data.sqlx.tmpl.SqlParam;
import com.site0.walnut.ext.data.sqlx.tmpl.WnSqlTmpl;
import com.site0.walnut.ext.data.sqlx.util.Sqlx;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;
import com.site0.walnut.util.explain.WnExplain;
import com.site0.walnut.util.explain.WnExplains;

public class sqlx_query extends SqlxFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "p");
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void process(WnSystem sys, SqlxContext fc, ZParams params) {
        String sqlName = params.val_check(0);
        boolean useParam = params.is("p");

        WnSqlTmpl sqlt = fc.sqls.get(sqlName);
        Connection conn = fc.getConnection();

        NutBean context = fc.getVarMap();
        List<NutBean> beans;
        // 参数模式防止注入
        if (useParam) {
            List<SqlParam> cps = new ArrayList<>();
            String sql = sqlt.render(context, cps);
            Object[] sqlParams = Sqlx.getSqlParamsValue(cps);
            beans = fc.query.runWithParams(conn, sql, sqlParams);
        }
        // 普通模式
        else {
            String sql = sqlt.render(context, null);
            beans = fc.query.run(conn, sql);
        }

        // 循环处理一个转换结果
        if (params.has("by")) {
            NutBean by = params.getMap("by");
            List<NutBean> list = new ArrayList<>(beans.size());
            for (NutBean bean : beans) {
                NutBean b2 = (NutBean) Wn.explainObj(bean, by);
                list.add(b2);
            }
            beans = list;
        }

        // 设置到管道上下文
        if (params.has("set")) {
            String pipeKey = params.getString("set");
            String as = params.getString("as", "list");
            String vstr = params.getString("val");
            Object vinput;
            if (Ws.isQuoteBy(vstr, '{', '}')) {
                vinput = Json.fromJson(NutMap.class, vstr);
            } else {
                vinput = vstr;
            }
            WnExplain vt = null == vinput ? null : WnExplains.parse(vinput);
            Object val;
            if ("obj".equals(as)) {
                if (beans.isEmpty()) {
                    val = null;
                }
                // 仅仅变化第一个
                else {
                    NutBean b0 = beans.get(0);
                    if (null == vt) {
                        val = b0;
                    } else {
                        val = vt.explain(b0);
                    }
                }
            }
            // 列表
            else {
                if (null == vt) {
                    val = beans;
                } else {
                    List<Object> vlist = new ArrayList<>(beans.size());
                    for (NutBean bean : beans) {
                        Object v = vt.explain(bean);
                        vlist.add(v);
                    }
                    val = vlist;
                }
            }

            // 结构推入
            if ("...".equals(pipeKey)) {
                Map<String, Object> map = (Map<String, Object>) val;
                fc.putAllPipeContext(NutMap.WRAP(map));
            }
            // 直接设置
            else {
                fc.putPipeContext(pipeKey, val);
            }
        }
        // 设置 beans 到结果集
        else {
            fc.result = beans;
        }
    }

}
