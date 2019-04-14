package org.nutz.walnut.ext.titanium.hdl;

import org.nutz.json.Json;
import org.nutz.lang.Stopwatch;
import org.nutz.lang.Strings;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.Mvcs;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.titanium.creation.TiCreation;
import org.nutz.walnut.ext.titanium.creation.TiCreationOutput;
import org.nutz.walnut.ext.titanium.creation.TiCreationService;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

public class ti_creation implements JvmHdl {

    private static Log log = Logs.get();

    private static TiCreationService creations;

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        Stopwatch sw = Stopwatch.begin();
        // 初始化服务类
        if (null == creations) {
            synchronized (ti_views.class) {
                if (null == creations) {
                    creations = Mvcs.getIoc().get(TiCreationService.class);
                }
            }
        }
        sw.tag("ok:init-creations");

        // 获取要操作的对象
        String aph = hc.params.val_check(0);
        WnObj o = Wn.checkObj(sys, aph);
        sw.tag("ok:checkObj");

        // 获取视图搜寻路径
        String VIEW_PATH = Strings.sBlank(sys.se.varString("VIEW_PATH"), "/rs/ti/view/");
        String[] viewHomePaths = Strings.splitIgnoreBlank(VIEW_PATH, ":");

        // 准备获取的创建
        TiCreation creation = null;

        // 读取映射文件
        for (String viewHomePath : viewHomePaths) {
            String phCreation = Wn.appendPath(viewHomePath, "creation.json");
            String aphCreation = Wn.normalizeFullPath(phCreation, sys);
            WnObj oCreation = sys.io.fetch(null, aphCreation);
            if (null == oCreation)
                continue;
            sw.tagf("ok:(%s):oCreation", oCreation.path());
            creation = creations.getCreation(oCreation);
            if (null != creation) {
                sw.tagf("ok:(%s):loaded::%s/%s", oCreation.path());
                break;
            }
        }
        sw.tag("prepare to output");

        // 准备 lang
        String lang = hc.params.get("lang", "zh-cn");

        // 得到输出
        TiCreationOutput tcout = creation.getOutput(o.type(), lang);
        sw.tag("Got the outut");

        // 输出
        String json = Json.toJson(tcout, hc.jfmt);
        sys.out.println(json);
        sw.tag("done for println");
        sw.stop();
        if (log.isDebugEnabled()) {
            log.debugf("ti_creation done: %s", sw.toString());
        }
    }

}
