package org.nutz.walnut.impl.box.cmd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Lang;
import org.nutz.lang.MapKeyConvertor;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.lang.tmpl.Tmpl;
import org.nutz.lang.util.NutMap;
import org.nutz.mapl.Mapl;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.ZParams;

public class cmd_json extends JvmExecutor {

    private static class _Context {
        String prefix;
        String pKey;
        boolean not;
        boolean recur;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {

        ZParams params = ZParams.parse(args, "cqnr", "^(err|mapping_only)$");

        // 读取输入
        String json = Streams.read(sys.in.getReader()).toString();

        // 容忍错误
        if (params.is("err") && (Strings.isBlank(json) || json.startsWith("e."))) {
            sys.out.println(json);
            return;
        }

        // 格式化
        Object obj = Json.fromJson(json);

        // 取值模式
        String getKey = params.get("get");
        if (null != getKey) {
            Object val = Mapl.cell(obj, getKey);
            obj = val;
        }

        // 过滤一层 key
        if (params.has("key") && obj instanceof Map) {
            Map map = (Map) obj;
            // 分析
            String keyRegex = params.get("key");
            boolean isNot = keyRegex.startsWith("!");
            if (isNot)
                keyRegex = keyRegex.substring(1);
            // 循环看看那些要删除
            List<String> delKeys = new ArrayList<String>(map.size());
            for (Object mapKey : map.keySet()) {
                String key = mapKey.toString();
                if (!(key.matches(keyRegex) ^ isNot)) {
                    delKeys.add(key);
                }
            }
            // 依次删除
            for (String key : delKeys)
                map.remove(key);
        }

        // JSON 输出的格式化
        JsonFormat fmt = Cmds.gen_json_format(params);

        if (params.has("e")) {
            String regex = params.get("e");
            if (regex.startsWith("!")) {
                fmt.setLocked(regex.substring(1));
            } else {
                fmt.setActived(regex);
            }
        }

        if (params.has("d")) {
            fmt.setDateFormat(params.get("d"));
        }

        // 映射字段的值
        if (params.has("mapping")) {
            NutMap mapping = Lang.map(params.get("mapping"));
            boolean is_mapping_only = params.is("mapping_only");
            obj = __do_mapping(obj, mapping, is_mapping_only);
        }

        // 深层的修改键值
        if (params.has("prefix")) {

            // 分析参数
            final _Context C = new _Context();
            C.prefix = params.get("prefix");
            C.pKey = params.get("prefix_key");
            C.recur = params.is("r");
            if (null != C.pKey) {
                C.not = C.pKey.startsWith("!");
                if (C.not)
                    C.pKey = C.pKey.substring(1);
            }

            // 执行修改
            Lang.convertMapKey(obj, new MapKeyConvertor() {
                public String convertKey(String key) {
                    // 所有
                    if (null == C.pKey)
                        return C.prefix + key;

                    // 匹配
                    if (key.matches(C.pKey))
                        return C.not ? key : C.prefix + key;

                    // 不匹配
                    return C.not ? C.prefix + key : key;
                }
            }, C.recur);
        }

        // 修改模式
        if (params.has("u")) {
            NutMap map = Lang.map(params.get("u"));
            if (map != null && map.size() > 0) {
                obj = NutMap.WRAP(((Map<String, Object>) obj)).mergeWith(map);
            }
        }

        // 添加模式
        if (params.has("put")) {
            obj = Lang.map(params.get("put"), obj);
        }

        // 模板方式输出
        if (params.has("out")) {
            String out = params.get("out");
            Tmpl tmpl = Tmpl.parse(out, "@");
            // 如果是 Map 则直接渲染
            if (obj instanceof Map<?, ?>) {
                __output(sys, tmpl, obj);
            }
            // 集合
            else if (obj instanceof Collection<?>) {
                for (Object ele : (Collection<?>) obj) {
                    __output(sys, tmpl, ele);
                }
            }

        }
        // 直接输出 JSON
        else {
            sys.out.println(Json.toJson(obj, fmt));
        }

    }

    private Object __do_mapping(Object obj, NutMap mapping, boolean is_mapping_only) {
        // 输入的值就是对象的话 ...
        if (obj instanceof Map<?, ?>) {
            return __do_mapping_obj(mapping, is_mapping_only, (Map<?, ?>) obj);
        }
        // 输入的对象是个列表
        else if (obj instanceof List) {
            List<?> list = (List<?>) obj;
            if (list.size() > 0) {
                List<Object> list2 = new ArrayList<>(list.size());
                for (Object ele : list) {
                    // 如果是个对象就映射
                    if (ele instanceof Map<?, ?>) {
                        list2.add(this.__do_mapping_obj(mapping, is_mapping_only, (Map<?, ?>) ele));
                    }
                    // 如果不是对象，就加回去
                    else {
                        list2.add(ele);
                    }
                }
                return list2;
            }
        }
        // 原样返回
        return obj;
    }

    private NutMap __do_mapping_obj(NutMap mapping, boolean is_mapping_only, Map<?, ?> map) {
        NutMap map2 = new NutMap();
        for (Map.Entry<?, ?> en : map.entrySet()) {
            String key = en.getKey().toString();
            Object val = en.getValue();
            // 需要映射
            String k2 = mapping.getString(key);
            if (!Strings.isBlank(k2)) {
                map2.put(k2, val);
            }
            // 不需要映射的话，如果不是强制输出
            else if (!is_mapping_only) {
                map2.put(key, val);
            }
        }
        return map2;
    }

    @SuppressWarnings("unchecked")
    private void __output(WnSystem sys, Tmpl tmpl, Object obj) {
        // 是 Map 就输出
        if (obj instanceof Map<?, ?>) {
            NutMap c = NutMap.WRAP((Map<String, Object>) obj);
            String str = tmpl.render(c, false);
            str = Strings.evalEscape(str);
            sys.out.print(str);
        }
        // 不支持
        else {
            throw Er.create("e.cmd.json.nomap", obj.getClass());
        }
    }

}
