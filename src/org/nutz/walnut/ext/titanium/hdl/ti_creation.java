package org.nutz.walnut.ext.titanium.hdl;

import java.util.HashMap;
import java.util.Map;

import org.nutz.json.Json;
import org.nutz.lang.Strings;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.titanium.creation.TiCreateType;
import org.nutz.walnut.ext.titanium.creation.TiCreation;
import org.nutz.walnut.ext.titanium.creation.TiCreationService;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

@JvmHdlParamArgs("cqn")
public class ti_creation implements JvmHdl {

    private static TiCreationService creations;

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 初始化服务类
        if (null == creations) {
            synchronized (ti_views.class) {
                if (null == creations) {
                    creations = hc.ioc.get(TiCreationService.class);
                }
            }
        }

        // 获取映射文件名
        String mappFileName = hc.params.get("m", "creation.json");

        // 获取视图搜寻路径
        String VIEW_PATH = sys.session.getVars().getString("VIEW_PATH", "/rs/ti/view/");
        String[] viewHomePaths = Strings.splitIgnoreBlank(VIEW_PATH, ":");

        // 准备获取的创建
        TiCreation creation = null;

        // 直接指明
        String viewName = hc.params.get("name");
        if (!Strings.isBlank(viewName)) {
            creation = creations.getView(viewName, viewHomePaths);
        }

        // 如果没有，继续尝试
        if (null == creation) {
            // 获取要操作的对象
            String aph = hc.params.val_check(0);
            WnObj o = Wn.checkObj(sys, aph);

            // 在对象里做了声明
            viewName = o.getString("creation");
            if (!Strings.isBlank(viewName)) {
                creation = creations.getView(viewName, viewHomePaths);
            }

            // 读取映射文件
            if (null == creation) {
                for (String viewHomePath : viewHomePaths) {
                    String phMapping = Wn.appendPath(viewHomePath, mappFileName);
                    String aphMapping = Wn.normalizeFullPath(phMapping, sys);
                    WnObj oMapping = sys.io.fetch(null, aphMapping);
                    if (null == oMapping)
                        continue;
                    creation = creations.getView(oMapping, o, viewHomePaths);
                    if (null != creation) {
                        break;
                    }
                }
            }
        }

        // 收集类型列表
        if (null != creation && creation.hasTypeNames()) {
            Map<String, TiCreateType> typeDict = new HashMap<>();
            for (String viewHomePath : viewHomePaths) {
                String phTypes = Wn.appendPath(viewHomePath, "types.json");
                String aphTypes = Wn.normalizeFullPath(phTypes, sys);
                WnObj oTypes = sys.io.fetch(null, aphTypes);
                if (null == oTypes)
                    continue;
                TiCreateType[] types = sys.io.readJson(oTypes, TiCreateType[].class);
                if (null != types) {
                    for (TiCreateType type : types) {
                        typeDict.put(type.getName(), type);
                    }
                }
            }
            creation.loadTypes(typeDict);
        }

        // 输出
        String json = Json.toJson(creation, hc.jfmt);
        sys.out.println(json);

    }

}
