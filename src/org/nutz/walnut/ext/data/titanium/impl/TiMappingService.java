package org.nutz.walnut.ext.data.titanium.impl;

import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Strings;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.data.titanium.util.TiViewMapping;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnObjDataCachedFactory;

@IocBean(create = "on_create")
public abstract class TiMappingService<T> {

    @Inject("refer:io")
    private WnIo io;

    private WnObjDataCachedFactory<TiViewMapping> mappings;

    private WnObjDataCachedFactory<T> mObjs;

    private Class<T> defType;

    private String dirName;

    public TiMappingService(Class<T> defType, String dirName) {
        this.defType = defType;
        this.dirName = dirName;
    }

    public void on_create() {
        mappings = new WnObjDataCachedFactory<>(io);
        mObjs = new WnObjDataCachedFactory<>(io);
    }

    public TiViewMapping getMapping(WnObj oMapping) {
        if (null == oMapping)
            return null;

        TiViewMapping mapping = mappings.get(oMapping, o -> {
            return io.readJson(oMapping, TiViewMapping.class);
        });
        return mapping;
    }

    public T getView(WnObj oMapping, WnObj o, String[] viewHomePaths) {
        // 获取映射
        TiViewMapping mapping = this.getMapping(oMapping);

        // 获取视图名称
        String viewName = mapping.match(o);

        if (Strings.isBlank(viewName))
            return null;

        // 得到视图
        WnObj oViewHome = oMapping.parent();
        return this.getView(oViewHome, viewName, viewHomePaths);
    }

    public T getView(WnObj oViewHome, String viewName, String[] viewHomePaths) {
        // 必须有视图名称
        if (Strings.isBlank(viewName)) {
            throw Er.create("e.ti.view.blankName");
        }

        // 确保是 JSON 文件
        String fnm = viewName;
        if (!fnm.endsWith(".json"))
            fnm += ".json";

        // 得到视图对象
        WnObj oView = io.fetch(oViewHome, this.dirName + "/" + fnm);

        // 如果在指定 viewHome 里找不到，在给定查找路径下再找一轮
        if (null == oView) {
            for (String viewHomePath : viewHomePaths) {
                String viewPath = Wn.appendPath(viewHomePath, this.dirName, fnm);
                // 已经找过了，跳过之
                if (oViewHome.path().equals(viewHomePath)) {
                    continue;
                }
                // 找一哈，看看有木有
                oView = io.fetch(null, viewPath);
                if (null != oView)
                    break;
            }
        }

        // 获取视图
        T view = mObjs.get(oView, o -> {
            return io.readJson(o, this.defType);
        });
        return view;
    }

    public T getView(String viewName, String[] viewHomePaths) {
        // 必须有视图名称
        if (Strings.isBlank(viewName)) {
            throw Er.create("e.ti.view.blankName");
        }

        // 确保是 JSON 文件
        String fnm = viewName;
        if (!fnm.endsWith(".json"))
            fnm += ".json";

        // 得到视图对象
        WnObj oView = null;

        // 如果在指定 viewHome 里找不到，在给定查找路径下再找一轮
        if (null == oView) {
            for (String viewHomePath : viewHomePaths) {
                String viewPath = Wn.appendPath(viewHomePath, this.dirName, fnm);
                // 找一哈，看看有木有
                oView = io.fetch(null, viewPath);
                if (null != oView)
                    break;
            }
        }

        // 获取视图
        T view = mObjs.get(oView, o -> {
            return io.readJson(o, this.defType);
        });
        return view;
    }

}
