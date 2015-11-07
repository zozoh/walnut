package org.nutz.walnut.impl.box.cmd;

import java.util.List;
import java.util.Map;

import org.nutz.castor.Castors;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Lang;
import org.nutz.lang.MapKeyConvertor;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class cmd_json extends JvmExecutor {

    private static class _Context {
        String prefix;
        String pKey;
        boolean not;
        boolean recur;
    }

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {

        ZParams params = ZParams.parse(args, "cqn");

        // 读取输入
        String json = Streams.read(sys.in.getReader()).toString();

        // 格式化
        Object obj = Json.fromJson(json);

        // 取值模式
        String key = params.get("get");
        if (null != key) {
            _do_get_value(sys, obj, key);
            return;
        }

        // 过滤字段
        JsonFormat fmt;
        if (params.is("c"))
            fmt = JsonFormat.compact();
        else
            fmt = JsonFormat.forLook();

        fmt.setQuoteName(!params.is("q"));
        fmt.setIgnoreNull(params.is("n"));

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

        // 最后输出
        sys.out.println(Json.toJson(obj, fmt));

    }

    @SuppressWarnings("unchecked")
    private void _do_get_value(WnSystem sys, Object obj, String key) {
        // 先粗放的尝试一下取值
        if (obj instanceof Map<?, ?>) {
            Map<String, Object> map = (Map<String, Object>) obj;
            Object val = map.get(key);
            if (null != val) {
                sys.out.println(Castors.me().castToString(val));
                return;
            }
        }

        // 递归取值
        String[] keyPath = Strings.splitIgnoreBlank(key, "[.]");
        Object val = _do_get(obj, keyPath, 0);

        // 打印
        sys.out.println(Castors.me().castToString(val));
    }

    @SuppressWarnings("unchecked")
    private Object _do_get(Object obj, String[] keyPath, int off) {
        if (off >= keyPath.length)
            return obj;

        // Map
        if (obj instanceof Map<?, ?>) {
            return _do_get_from_map((Map<String, Object>) obj, keyPath, off);
        }
        // 列表
        else if (obj instanceof List<?>) {
            return _do_get_from_list((List<?>) obj, keyPath, off);
        }
        // 其他的不支持
        return null;
    }

    private Object _do_get_from_list(List<?> list, String[] keyPath, int off) {
        String key = keyPath[off];
        int index = Integer.parseInt(key);

        // -1 的话从后面取
        if (index < 0) {
            index = list.size() + index;
        }

        Object obj = list.get(index);
        if (null == obj)
            return null;
        return _do_get(obj, keyPath, off + 1);
    }

    private Object _do_get_from_map(Map<String, Object> map, String[] keyPath, int off) {
        String key = keyPath[off];
        Object obj = map.get(key);
        if (null == obj)
            return null;
        return _do_get(obj, keyPath, off + 1);
    }

}
