package com.site0.walnut.impl.hook;

import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.hook.WnHook;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.util.WnObjMatcher;

public abstract class AbstractWnHook implements WnHook {

    private String name;

    private WnObjMatcher[] ms;

    private String brief;

    private String runby;

    @Override
    public String getName() {
        return name;
    }

    public String toString() {
        return String.format("%s(%s) : %s", name, ms == null ? "*" : ms.length, brief);
    }

    public AbstractWnHook init(WnIo io, WnObj oHook) {
        NutMap[] maps = oHook.getArray("hook_by", NutMap.class);
        // 有特殊的匹配
        if (null != maps && maps.length > 0) {
            ms = new WnObjMatcher[maps.length];
            for (int i = 0; i < ms.length; i++) {
                ms[i] = new WnObjMatcher().set(maps[i]);
            }
        }
        // 没有匹配表示匹配任何对象
        else {
            ms = null;
        }

        // 读取文件内容获得处理方式
        String text = io.readText(oHook);
        _init(text);

        // 保存名称
        name = oHook.name();
        brief = Strings.trim(text.length() > 80 ? text.substring(0, 80) + " ..." : text);
        runby = oHook.getString("hook_user");
        // 返回自身以便链式赋值
        return this;
    }

    protected abstract void _init(String text);

    @Override
    public boolean match(WnObj o) {
        // 匹配任何对象
        if (null == ms)
            return true;

        // 任意一个匹配器匹配上了都算
        for (WnObjMatcher om : ms) {
            if (om.match(o))
                return true;
        }

        // 匹配不上
        return false;
    }

    public final void setRunby(String runby) {
        this.runby = runby;
    }

    public final String getRunby() {
        return runby;
    }
}
