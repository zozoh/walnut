package org.nutz.walnut.ext.task.hdl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.DateRegion;
import org.nutz.lang.util.IntRegion;
import org.nutz.lang.util.Region;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.ext.task.TaskCtx;
import org.nutz.walnut.ext.task.TaskHdl;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public abstract class AbstractTaskQueryHdl implements TaskHdl {

    protected List<WnObj> _sort_by_logic_order(List<WnObj> list) {
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

        // 嗯搞定了
        return list;
    }

    protected boolean _fill_query(WnSystem sys, TaskCtx sc, ZParams params, WnQuery q) {
        boolean logic_order = false;

        q.setv("tp", "task").setv("g", sys.se.group());
        // ........................................................
        // 解析所有的字符串值过滤条件
        for (String key : Lang.array("lbls", "status", "ow", "c")) {
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

        // ........................................................
        // 解析所有的整数值过滤条件
        for (String key : Lang.array("lv")) {
            if (params.has(key)) {
                String v = params.get(key);
                // 区间
                if (v.matches("^[\\[\\(][0-9,]+[\\]\\)]$")) {
                    q.range(key, Region.Int(v));
                }
                // 默认用单个值
                else {
                    q.setv(key, Integer.parseInt(v));
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
        // 指定一个用户组
        if (params.has("G")) {
            String grp = params.get("G");
            WnObj oGrp = sys.io.check(sc.oTaskHome, "others/" + grp);
            logic_order = false;
            q.setv("pid", oGrp.id());
        }
        // 直接指定父任务
        else if (params.has("pid")) {
            logic_order = true;
            q.setv("pid", params.get("pid"));
        }
        // 指定了当前任务
        else if (null != sc.oTask) {
            logic_order = true;
            q.setv("pid", sc.oTask.id());
        }

        // 用户指定了排序
        if (params.has("order")) {
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

        // 最后返回排序方式
        return logic_order;
    }
}
