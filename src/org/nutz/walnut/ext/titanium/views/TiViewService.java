package org.nutz.walnut.ext.titanium.views;

import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Strings;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.titanium.util.WnObjCachedFactory;
import org.nutz.walnut.util.Wn;

@IocBean
public class TiViewService {

    @Inject("refer:io")
    private WnIo io;

    private WnObjCachedFactory<TiViewMapping> mappings;

    private WnObjCachedFactory<TiView> views;

    public TiViewService() {
        mappings = new WnObjCachedFactory<>();
        views = new WnObjCachedFactory<>();
    }

    public TiViewMapping getMapping(WnObj oMapping) {
        if (null == oMapping)
            return null;

        TiViewMapping mapping = mappings.get(oMapping, o -> {
            return io.readJson(oMapping, TiViewMapping.class);
        });
        return mapping;
    }

    public TiView getView(WnObj oMapping, WnObj o, String[] viewHomePaths) {
        // 获取映射
        TiViewMapping mapping = this.getMapping(oMapping);

        // 获取视图名称
        String viewName = mapping.match(o);

        // 得到视图
        WnObj oViewHome = oMapping.parent();
        return this.getView(oViewHome, viewName, viewHomePaths);
    }

    public TiView getView(WnObj oViewHome, String viewName, String[] viewHomePaths) {
        // 必须有视图名称
        if (Strings.isBlank(viewName)) {
            throw Er.create("e.ti.view.blankName");
        }

        // 确保是 JSON 文件
        String fnm = viewName;
        if (!fnm.endsWith(".json"))
            fnm += ".json";

        // 得到视图对象
        WnObj oView = io.fetch(oViewHome, "views/" + fnm);

        // 如果在指定 viewHome 里找不到，在给定查找路径下再找一轮
        if (null == oView) {
            for (String viewHomePath : viewHomePaths) {
                String viewPath = Wn.appendPath(viewHomePath, "views", fnm);
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
        TiView view = views.get(oView, o -> {
            return io.readJson(o, TiView.class);
        });
        return view;
    }

}
