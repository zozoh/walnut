package org.nutz.walnut.ext.task.hdl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.DateRegion;
import org.nutz.lang.util.IntRegion;
import org.nutz.lang.util.Region;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.ext.task.TaskCtx;
import org.nutz.walnut.ext.task.TaskHdl;
import org.nutz.walnut.ext.task.WnTaskTable;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class Th_query implements TaskHdl {

    @Override
    public void invoke(WnSystem sys, TaskCtx sc) throws Exception {
        // 解析参数
        ZParams params = ZParams.parse(sc.args, "^line|json|color$");
        boolean logic_order = false;

        // 准备查询条件
        WnQuery q = new WnQuery();

        q.setv("tp", "task");
        // ........................................................
        // 解析所有的字符串值过滤条件
        for (String key : Lang.array("g", "lbls", "status", "ow", "c")) {
            if (params.has(key)) {
                String v = params.get(key);
                // 正则表达式
                if (v.startsWith("^")) {
                    q.setv(key, Pattern.compile(v));
                }
                // 半角逗号分隔，用 "与"
                else if (v.contains(",")) {
                    q.setv(key, Lang.arrayFirst("all", Strings.splitIgnoreBlank(v)));
                }
                // 竖线分隔用 "或"
                else if (v.contains("|")) {
                    q.setv(key, Lang.arrayFirst("in", Strings.splitIgnoreBlank(v, "[|]")));
                }
                // 默认用单个值
                else {
                    q.setv(key, v);
                }

            }
        }

        // 解析所有的时间
        for (String key : Lang.array("ct", "lm", "d_start", "d_stop")) {
            if (params.has(key)) {
                String v = params.get(key);
                DateRegion dr = Region.Date(v);
                q.setv(key, dr);
            }
        }

        // 解析持续时间
        if (params.has("du")) {
            IntRegion ir = Region.Int(params.get("du"));
            q.setv("du", ir);
        }

        // 解析完成状态
        for (String key : Lang.array("done", "verify")) {
            if (params.has(key)) {
                q.setv(key, "");
            }
        }

        // ........................................................
        // 解析排序
        if (params.has("pid")) {
            logic_order = true;
            q.setv("pid", params.get("pid"));
        }
        // 如果指定了当前任务
        else if (null != sc.oTask) {
            logic_order = true;
            q.setv("pid", sc.oTask.id());
        }
        // 用户指定了排序
        else if (params.has("order")) {
            String[] sss = Strings.splitIgnoreBlank(params.check("order"));
            for (String ss : sss) {
                String[] s = Strings.splitIgnoreBlank(ss, ":");
                String order = s[0].toLowerCase();
                if (order.equals("asc")) {
                    q.asc(s[1]);
                } else if (order.equals("desc")) {
                    q.desc(s[1]);
                } else {
                    throw Lang.impossible();
                }
            }
        }

        // 解析限制数量，默认 100 个
        q.limit(params.getInt("limit", 100));

        // 解析游标
        if (params.has("skip"))
            q.skip(params.getInt("skip"));

        // 查询出结果
        List<WnObj> list = sys.io.query(q);

        // ........................................................
        // 整理逻辑顺序
        if (logic_order) {
            // 归纳 Map
            Map<String, WnObj> map = new HashMap<String, WnObj>();
            for (WnObj o : list) {
                map.put(o.id(), o);
            }

            // 试图找到一个开头对象
            WnObj oHead = null;
            for (WnObj o : list) {
                if (!o.has("prev")) {
                    oHead = o;
                    break;
                }
            }

            // 如果有开头对象，那么则依次构建链表
            // 否则就不能构建一个逻辑链表，只能采用原来的顺序
            if (null != oHead) {
                list = new ArrayList<WnObj>(map.size());
                map.remove(oHead.id());
                list.add(oHead);
                String nextId = oHead.getString("next");
                while (null != nextId) {
                    WnObj oNext = map.remove(nextId);
                    if (null == oNext)
                        break;
                    list.add(oNext);
                    nextId = oNext.getString("next");
                }
                // 剩下的对象统统加回到列表里
                if (!map.isEmpty()) {
                    list.addAll(map.values());
                }
            }
        }

        // ........................................................
        // 最后输出
        if (params.is("json")) {
            sys.out.println(Json.toJson(list));
        }
        // 默认按行输出
        else {
            WnTaskTable wtt = new WnTaskTable("id,status,ow,g,title,lbls");
            for (WnObj o : list)
                wtt.add(o);

            // 最终输出
            sys.out.print(wtt.toString());
        }

    }

}
