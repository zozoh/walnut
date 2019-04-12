package org.nutz.walnut.ext.titanium.hdl;

import org.nutz.json.Json;
import org.nutz.lang.Stopwatch;
import org.nutz.lang.Strings;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.Mvcs;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.titanium.views.TiView;
import org.nutz.walnut.ext.titanium.views.TiViewService;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

@JvmHdlParamArgs("cqn")
public class ti_views implements JvmHdl {

    private static Log log = Logs.get();

    private static TiViewService views;

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        Stopwatch sw = Stopwatch.begin();
        // 初始化服务类
        if (null == views) {
            synchronized (ti_views.class) {
                if (null == views) {
                    views = Mvcs.getIoc().get(TiViewService.class);
                }
            }
        }
        sw.tag("ok:init-views");

        // 获取要操作的对象
        String aph = hc.params.val_check(0);
        WnObj o = Wn.checkObj(sys, aph);
        sw.tag("ok:checkObj");

        // 获取映射文件名
        String mappFileName = hc.params.get("m", "mapping.json");

        // 获取视图搜寻路径
        String VIEW_PATH = Strings.sBlank(sys.se.varString("VIEW_PATH"), "/rs/ti/view/");
        String[] viewHomePaths = Strings.splitIgnoreBlank(VIEW_PATH, ":");

        // 准备获取的视图
        TiView view = null;

        // 读取映射文件
        for (String viewHomePath : viewHomePaths) {
            String phMapping = Wn.appendPath(viewHomePath, mappFileName);
            String aphMapping = Wn.normalizeFullPath(phMapping, sys);
            WnObj oMapping = sys.io.fetch(null, aphMapping);
            if (null == oMapping)
                continue;
            sw.tagf("ok:(%s):oMapping", viewHomePath);
            view = views.getView(oMapping, o, viewHomePaths);
            if (null != view) {
                sw.tagf("ok:(%s):view::%s/%s", viewHomePath, view.getComType(), view.getModType());
                break;
            }
        }
        sw.tag("prepare to output");
        // 输出
        String json = Json.toJson(view, hc.jfmt);
        sys.out.println(json);
        sw.tag("done for println");
        sw.stop();
        if (log.isDebugEnabled()) {
            log.debugf("ti_views done: %s", sw.toString());
        }
    }

}
