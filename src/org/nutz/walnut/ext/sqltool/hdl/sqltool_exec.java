package org.nutz.walnut.ext.sqltool.hdl;

import java.util.Collection;

import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.pager.Pager;
import org.nutz.dao.sql.Sql;
import org.nutz.dao.util.Daos;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.sqltool.SqlToolHelper;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;

@JvmHdlParamArgs("^(force_query)$")
public class sqltool_exec implements JvmHdl {

    @SuppressWarnings("rawtypes")
    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 获取Dao对象
        String dsName = hc.oRefer.name();
        NutMap dsConf = hc.getAs("dataSource_conf", NutMap.class);
        Dao dao = SqlToolHelper.getDao(sys.me, dsName, dsConf);

        // 开始自定义SQL
        Sql sql = Sqls.create(hc.params.val_check(0));
        // 如果有参数,设置之
        if (hc.params.has("params")) {
            Object tmp = Json.fromJson(hc.params.check("params"));
            if (tmp instanceof Collection) {
                for (Object ele : (Collection) tmp) {
                    sql.params().putAll(ele);
                    sql.addBatch();
                }
            } else {
                sql.params().putAll(tmp);
            }
        }
        // 如果有变量,设置之
        if (hc.params.has("vars")) {
            NutMap tmp = Json.fromJson(NutMap.class, hc.params.check("vars"));
            sql.vars().putAll(tmp);
        }
        if (hc.params.is("force_query")) {
            sql.forceExecQuery();
        }
        // 是否带分页呢?
        Pager pager = null;
        if (sql.isSelect() && hc.params.has("limit") && hc.params.has("skip")) {
            int limit = hc.params.getInt("limit");
            int skip = hc.params.getInt("skip");
            pager = new Pager((skip + limit) / limit, limit);
            if (hc.params.is("pager")) {
                pager.setRecordCount((int) Daos.queryCount(dao, sql));
            }
            sql.setPager(pager);
        }
        // 查询的话, 有回调
        if (sql.isSelect()) {
            sql.setCallback(Sqls.callback.maps());
        }

        // 执行语句
        dao.execute(sql);

        // 查询语句,就打印sql咯
        if (sql.isSelect()) {
            sys.out.writeJson(new NutMap().setv("list", sql.getResult()).setv("pager", pager), JsonFormat.full());
        } else if (sql.isUpdate() || sql.isDelete()) {
            sys.out.println("{changed:" + sql.getUpdateCount() + "}");
        } else {
            sys.out.println("{ok:true}");
        }
    }

}
