package com.site0.walnut.ext.util.jsonx.hdl;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.nutz.json.Json;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.ext.util.jsonx.JsonXContext;
import com.site0.walnut.ext.util.jsonx.JsonXFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;
import com.site0.walnut.util.validate.WnMatch;
import com.site0.walnut.util.validate.impl.AutoMatch;

public class jsonx_flatten extends JsonXFilter {

    static class Flattening {
        String key;
        boolean noTop;
        boolean onlyLeaf;
        WnMatch leaf;
        WnMatch ignore;
        WnMatch filter;
        List<Object> results;
        int depth = 0;

        void flatOther(Object any) {
            // 无视根节点
            if (noTop && 0 == depth) {
                return;
            }
            // 黑名单命中
            if (null != ignore && ignore.match(any)) {
                return;
            }

            // 白名单未命中
            if (null != filter && !filter.match(any)) {
                return;
            }

            // 记入到结果
            results.add(any);
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        void flatAny(Object input) {
            // 防守
            if (null == input) {
                return;
            }
            // 数组
            if (input.getClass().isArray()) {
                this.depth++;
                flatArray(input);
                this.depth--;
            }
            // 集合
            else if (input instanceof Collection<?>) {
                this.depth++;
                Collection<?> col = (Collection<?>) input;
                flatCollection(col);
                this.depth--;
            }
            // Map
            else if (input instanceof Map) {
                NutMap map = NutMap.WRAP((Map) input);
                flatMap(map);
            }
            // 其他
            else {
                flatOther(input);
            }
        }

        void flatArray(Object ary) {
            int len = Array.getLength(ary);
            for (int i = 0; i < len; i++) {
                Object ele = Array.get(ary, i);
                flatAny(ele);
            }
        }

        void flatCollection(Collection<?> col) {
            for (Object ele : col) {
                flatAny(ele);
            }
        }

        void flatMap(NutMap map) {
            Object children = map.remove(key);
            boolean isLeaf = null == children;
            if (null != this.leaf) {
                isLeaf = this.leaf.match(map);
            }

            // 自己
            flatOther(map);

            // 子
            if (!isLeaf) {
                flatAny(children);
            }
        }

    }

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(notop|onlyleaf)$");
    }

    @Override
    protected void process(WnSystem sys, JsonXContext fc, ZParams params) {
        // 防守:上下文为空，就不要执行了
        if (null == fc.obj) {
            fc.obj = new LinkedList<>();
            return;
        }

        // 分析参数
        Flattening ing = new Flattening();
        ing.key = params.val(0, "children");
        ing.noTop = params.is("noTop");
        ing.onlyLeaf = params.is("onlyleaf");
        ing.leaf = genMatch(params.getString("leaf"));
        ing.ignore = genMatch(params.getString("ignore"));
        ing.filter = genMatch(params.getString("filter"));

        // 准备输出结果
        ing.results = new LinkedList<>();

        // 执行
        ing.flatAny(fc.obj);

        // 搞定
        fc.obj = ing.results;
    }

    private WnMatch genMatch(String input) {
        if (!Ws.isBlank(input)) {
            Object ino = Json.fromJson(input);
            return AutoMatch.parse(ino);
        }
        return null;
    }
}
