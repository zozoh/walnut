package com.site0.walnut.ext.data.bizhook;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.each.WnEachIteratee;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;

public class BizHookGroup {

    private BizHook[] hooks;

    @SuppressWarnings("unchecked")
    public BizHookGroup(Object any) {
        final List<BizHook> list = new LinkedList<>();
        ;
        // 钩子列表包裹：{hooks:[{BizHook}...]}
        // 钩子： {BizHook}
        if (any instanceof Map) {
            NutMap map = NutMap.WRAP((Map<String, Object>) any);
            // 是包裹对象: {hooks:[{BizHook}...]}
            if (map.has("hooks")) {
                List<NutMap> mapHooks = map.getAsList("hooks", NutMap.class);
                for (NutMap mh : mapHooks) {
                    BizHook bh = new BizHook(mh);
                    list.add(bh);
                }
            }
            // 直接就是一个钩子
            else {
                BizHook bh = new BizHook(map);
                list.add(bh);
            }
        }
        // 钩子列表：[{BizHook}...]
        else {
            Wlang.each(any, new WnEachIteratee<NutMap>() {
                public void invoke(int index, NutMap map, Object src) {
                    BizHook bh = new BizHook(map);
                    list.add(bh);
                }
            });
        }
        // 设置值
        this.hooks = new BizHook[list.size()];
        list.toArray(this.hooks);
    }

    public List<BizHook> getHooks(NutBean obj, int limit) {
        List<BizHook> list = new ArrayList<>(this.hooks.length);
        for (int i = 0; i < this.hooks.length; i++) {
            BizHook bh = this.hooks[i];
            if (bh.match(obj)) {
                list.add(bh);
                if (limit > 0 && list.size() >= limit)
                    break;
            }
        }
        return list;
    }

    public List<BizHook> getHooksFromTail(NutBean obj, int limit) {
        List<BizHook> list = new ArrayList<>(this.hooks.length);
        for (int i = this.hooks.length - 1; i >= 0; i--) {
            BizHook bh = this.hooks[i];
            if (bh.match(obj)) {
                list.add(bh);
                if (limit > 0 && list.size() >= limit)
                    break;
            }
        }
        return list;
    }

    public BizHook getHook(NutBean obj) {
        for (int i = 0; i < this.hooks.length; i++) {
            BizHook bh = this.hooks[i];
            if (bh.match(obj)) {
                return bh;
            }
        }
        return null;
    }

    public BizHook getHookFromTail(NutBean obj) {
        for (int i = this.hooks.length - 1; i >= 0; i--) {
            BizHook bh = this.hooks[i];
            if (bh.match(obj)) {
                return bh;
            }
        }
        return null;
    }

}
