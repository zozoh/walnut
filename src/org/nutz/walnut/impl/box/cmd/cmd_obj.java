package org.nutz.walnut.impl.box.cmd;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.Times;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class cmd_obj extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) {
        ZParams params = ZParams.parse(args, null);

        // 首先获取对象
        // 计算要列出的要处理的对象
        List<WnObj> list = new LinkedList<WnObj>();
        evalCandidateObjs(sys, params.vals, list, false);

        // 检查是否候选对象列表为空
        checkCandidateObjsNoEmpty(args, list);

        // 一次处理所有对象
        for (WnObj o : list) {
            // 更新对象
            if (params.has("u")) {
                NutMap map = Lang.map(params.get("u"));
                // 将日期的字符串，搞一下
                for (Map.Entry<String, Object> en : map.entrySet()) {
                    Object v = en.getValue();
                    if (null != v && v instanceof String) {
                        String s = v.toString();
                        if (s.startsWith("$date:")) {
                            String str = s.substring("$date:".length());
                            if ("now".equals(str)) {
                                en.setValue(Times.now());
                            } else {
                                en.setValue(Times.D(str));
                            }
                        }
                    }
                }
                sys.io.appendMeta(o, map);
            }
            // 显示对象的值
            else if (params.has("e")) {
                Pattern p = Pattern.compile(params.get("e"));
                NutMap map = new NutMap();
                for (String key : o.keySet()) {
                    if (p.matcher(key).matches()) {
                        map.put(key, o.get(key));
                    }
                }
                // 只有一个值的话，则显示值
                if (map.size() == 1) {
                    sys.out.writeLine(map.values().iterator().next().toString());
                }
                // 否则显示全部匹配
                else {
                    sys.out.writeLine(Json.toJson(map));
                }
            }
            // 显示对象全部的值
            else {
                sys.out.writeLine(Json.toJson(o));
            }
        }
    }

}
