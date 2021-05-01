package org.nutz.walnut.ext.sys.aclog.hdl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.sql.Sql;
import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.nutz.lang.Times;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.TextTable;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.jetty.AccessLogFilter;
import org.nutz.walnut.jetty.log.AccessLog;
import org.nutz.walnut.util.Wn;

public class aclog_analysis implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 输出格式
        // 时间段
        Date fromDate;
        Date toDate;
        if (hc.params.has("to")) {
            toDate = Times.D(hc.params.get("to"));
        }
        else {
            toDate = new Date(Wn.now());
        }
        if (hc.params.has("from")) {
            fromDate = Times.D(hc.params.get("from"));
        }
        else {
            fromDate = new Date(toDate.getTime() - Times.toMillis(hc.params.get("du", "1d")));
        }
        // 是否限制host
        String host = hc.params.get("host");
        // 是否限制ip
        String ip = hc.params.get("ip");
        // 指定uri前缀?
        String uri = hc.params.get("uri");
        // 指定UserAgent前缀?
        String userAgent = hc.params.get("ua");
        Dao dao = AccessLogFilter.dao;
        if (dao == null) {
            AccessLogFilter.setup(hc.ioc.get(PropertiesProxy.class, "conf"));
            dao = AccessLogFilter.dao;
        }
        Cnd cnd = Cnd.where("createTime", ">", fromDate).and("createTime", "<", toDate);
        cnd.andEX("host", "=", host);
        if (!Strings.isBlank(uri))
            cnd.andEX("uri", "like", uri + "%");
        if (!Strings.isBlank(ip))
            cnd.andEX("ip", "like", ip + "%");
        if (!Strings.isBlank(userAgent))
            cnd.andEX("userAgent", "like", userAgent + "%");
        
        long count = dao.count(AccessLog.class, cnd);
        String groupByFields = hc.params.get("group", "ua,host,rc");
        Sql sql = Sqls.create("select count(*),$group from t_aclog $cnd group by $group");
        sql.setCallback(Sqls.callback.maps());
        sql.setVar("cnd", cnd);
        sql.setVar("group", groupByFields);
        dao.execute(sql);
        if ("json".equals(hc.params.get("format", "md"))) {
            NutMap map = new NutMap();
            map.setv("count", count);
            map.put("list", sql.getList(NutMap.class));
            sys.out.writeJson(map, JsonFormat.full());
        }
        else {
            sys.out.println("## Data Infomation");
            sys.out.println("du   : " + Times.sDT(fromDate) + " - " + Times.sDT(toDate));
            sys.out.println("count: " + count);
            sys.out.println("group: " + groupByFields);
            sys.out.println("### Data");
            String[] groupNames = Strings.splitIgnoreBlank(groupByFields, ",");
            TextTable table = new TextTable(2+groupNames.length);
            List<String> headers = new ArrayList<>();
            headers.add("id");
            headers.add("count");
            headers.addAll(Arrays.asList(groupNames));
            table.addRow(headers);
            table.addHr();
            table.setShowBorder(true);
            List<NutMap> datas = sql.getList(NutMap.class);
            datas.stream().sorted(Comparator.comparingInt((map)->map.getInt("count(*)")));
            int index = 0;
            for (NutMap data : datas) {
                List<String> row = new ArrayList<>();
                row.add(""+index);
                row.add(""+data.getInt("count(*)"));
                for (String groupName : groupNames) {
                    String value = ""+data.get(groupName);
                    if ("ua".equals(groupName)) {
                        if (value.length() > 65) {
                            value = value.substring(0, 30) + "..." + value.substring(value.length() - 30);
                        }
                    }
                    row.add(value);
                }
                table.addRow(row);
                index ++;
            }
            sys.out.println(table.toString());
        }
    }
}
