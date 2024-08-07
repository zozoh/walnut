package com.site0.walnut.ext.data.titanium.hdl;

import org.nutz.json.Json;
import org.nutz.lang.Strings;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.data.titanium.impl.TiViewService;
import com.site0.walnut.ext.data.titanium.util.TiView;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlParamArgs;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;

@JvmHdlParamArgs("cqn")
public class ti_views implements JvmHdl {

    private static TiViewService views;

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 初始化服务类
        if (null == views) {
            synchronized (ti_views.class) {
                if (null == views) {
                    views = hc.ioc.get(TiViewService.class);
                }
            }
        }

        // 获取映射文件名
        String mappFileName = hc.params.get("m", "mapping.json");

        // 获取视图搜寻路径
        String VIEW_PATH = sys.session.getVars().getString("VIEW_PATH", "/rs/ti/view/");
        String[] viewHomePaths = Strings.splitIgnoreBlank(VIEW_PATH, ":");

        // 准备获取的视图
        TiView view = null;

        // 直接指明编辑器
        String viewName = hc.params.get("name");
        if (!Strings.isBlank(viewName)) {
            view = views.getView(viewName, viewHomePaths);
        }

        // 如果没有视图，继续尝试
        if (null == view) {
            // 获取要操作的对象
            String aph = hc.params.val_check(0);
            WnObj o = Wn.checkObj(sys, aph);

            // 在对象里做了声明
            viewName = o.getString("view");
            if (!Strings.isBlank(viewName)) {
                view = views.getView(viewName, viewHomePaths);
            }

            // 读取映射文件
            if (null == view) {
                for (String viewHomePath : viewHomePaths) {
                    String phMapping = Wn.appendPath(viewHomePath, mappFileName);
                    String aphMapping = Wn.normalizeFullPath(phMapping, sys);
                    WnObj oMapping = sys.io.fetch(null, aphMapping);
                    if (null == oMapping)
                        continue;
                    view = views.getView(oMapping, o, viewHomePaths);
                    if (null != view) {
                        break;
                    }
                }
            }
        }

        // 输出
        String json = Json.toJson(view, hc.jfmt);
        sys.out.println(json);
    }

}
