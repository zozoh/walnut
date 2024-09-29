package com.site0.walnut.util.explain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.each.WnEachIteratee;

public class WnExplainArray implements WnExplain {

    private boolean asArray;

    private int N;

    private WnExplain mapper;

    private List<WnExplain> list;

    private WnExplain scope;

    public WnExplainArray(Object input, String scope) {
        this(input, false, scope);
    }

    public WnExplainArray(Object input, boolean asArray, String scope) {
        this.asArray = asArray;
        this.N = Wlang.count(input);

        // 归一化参数
        Object[] inputs = new Object[N];
        Wlang.each(input, new WnEachIteratee<Object>() {
            public void invoke(int index, Object ele, Object src) {
                inputs[index] = ele;
            }
        });

        // 明确指定挑选 scope
        if (2 == N && (inputs[0] instanceof String) && inputs[0].toString().startsWith(":scope=")) {
            this.scope = WnExplains.parse(inputs[0].toString().substring(6));
            this.mapper = WnExplains.parse(inputs[1]);
        }
        // 默认的 scope，就是在 map 里直接指定一个1元素的对象数组
        else if (1 == N && null != scope && (inputs[0] instanceof Map)) {
            this.scope = WnExplains.parse(scope);
            this.mapper = WnExplains.parse(inputs[0]);
        }
        // 自由挑选数组元素
        else {
            this.list = new ArrayList<>(N);
            for (Object in : inputs) {
                WnExplain exp = WnExplains.parse(in);
                list.add(exp);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object explain(NutBean context) {
        // 深入数组 mapping
        if (null != this.scope && null != this.mapper) {
            List<Object> srcList = (List<Object>) this.scope.explain(context);
            if (null == srcList) {
                return null;
            }
            List<Object> re = new ArrayList<>(srcList.size());
            for (Object src : srcList) {
                NutMap vars = NutMap.WRAP((Map<String, Object>) src);
                Object it = this.mapper.explain(vars);
                re.add(it);
            }
            return re;
        }
        // 从上下文挑选
        else if (null != list) {
            List<Object> re = new ArrayList<>(N);
            for (WnExplain ex : list) {
                Object v = ex.explain(context);
                re.add(v);
            }
            if (asArray) {
                Object[] arr = new Object[N];
                re.toArray(arr);
                return arr;
            }
            return re;
        }

        // 啥都不是
        return null;
    }

}
