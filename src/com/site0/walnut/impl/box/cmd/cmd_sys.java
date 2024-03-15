package com.site0.walnut.impl.box.cmd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Times;
import org.nutz.lang.Times.TmInfo;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.impl.box.JvmExecutor;
import com.site0.walnut.impl.box.TextTable;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.WnSysRuntime;
import com.site0.walnut.util.ZParams;
import com.site0.walnut.web.WnConfig;

public class cmd_sys extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        ZParams params = ZParams.parse(args, "cqn", "^(runtime|nice)$");
        JsonFormat jfmt = Cmds.gen_json_format(params);

        NutMap map;
        // 获取运行时信息
        if (params.is("runtime")) {
            WnSysRuntime rt = Wn.getRuntime();
            map = rt.toMap();

            long now = Wn.now();
            long sta = rt.getNodeStartAtInMs();
            long liv = now - sta;

            map.put("nodeNowInMs", now);
            map.put("nodeLiveTimeInMs", liv);

            // 转换人类可阅读的信息
            String dfmt = "yyyy-MM-dd HH:mm:ss.SSS";
            map.put("startAt", Times.format(dfmt, new Date(sta)));
            map.put("nodeNow", Times.format(dfmt, new Date(now)));

            // 格式化存活时间
            long days = liv / 86400000L;
            if (days > 0) {
                liv -= days * 86400000L;
            }
            String s = days > 0 ? days + "days " : "";
            TmInfo ti = Times.Tims(liv);
            s += ti.toString();
            map.put("nodeLive", s);

        }
        // 获取启动时更详细的配置信息
        else if (params.is("setup")) {
            WnConfig conf = this.ioc.get(WnConfig.class, "conf");
            map = new NutMap();
            map.putAll(conf);
        }
        // 获取启动时更详细的配置信息
        else if (params.is("sysenv")) {
            map = new NutMap();
            map.putAll(System.getenv());
        }
        // 默认获取全局配置信息
        else {
            map = Wn.getSysConfMap(sys.io);
        }

        //
        // 打印纯粹阅读的
        //
        if (params.is("nice")) {
            TextTable tt = new TextTable(2);
            tt.setShowBorder(true);
            tt.setCellSpacing(2);

            // 标题
            tt.addRow(Wlang.list("Name", "Value"));
            tt.addHr();

            // 排序字段看的有条理点
            Set<String> keys = map.keySet();
            List<String> list = new ArrayList<>(keys.size());
            list.addAll(keys);
            Collections.sort(list);

            // 逐行计入输出表
            for (String key : list) {
                String val = map.getString(key);
                tt.addRow(Wlang.list(key, val));
            }

            // 最后一行
            tt.addHr();

            // 打印
            sys.out.println(tt.toString());
        }
        // 打印JSON
        else {
            sys.out.println(Json.toJson(map, jfmt));
        }
    }

}
