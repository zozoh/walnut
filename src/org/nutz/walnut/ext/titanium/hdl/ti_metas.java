package org.nutz.walnut.ext.titanium.hdl;

import org.nutz.json.Json;
import org.nutz.lang.Stopwatch;
import org.nutz.lang.Strings;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.titanium.impl.TiMetaService;
import org.nutz.walnut.ext.titanium.impl.TiViewService;
import org.nutz.walnut.ext.titanium.util.TiMetas;
import org.nutz.walnut.ext.titanium.util.TiView;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

@JvmHdlParamArgs("cqn")
public class ti_metas implements JvmHdl {

    private static Log log = Logs.get();

    private static TiMetaService metas;

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        Stopwatch sw = Stopwatch.begin();
        // 初始化服务类
        if (null == metas) {
            synchronized (ti_metas.class) {
                if (null == metas) {
                    metas = hc.ioc.get(TiMetaService.class);
                }
            }
        }
        sw.tag("ok:init-metas");

        // 获取映射文件名
        String mappFileName = hc.params.get("m", "metas.json");

        // 获取视图搜寻路径
        String VIEW_PATH = sys.session.getVars().getString("VIEW_PATH", "/rs/ti/view/");
        String[] viewHomePaths = Strings.splitIgnoreBlank(VIEW_PATH, ":");

        // 准备获取的视图
        TiMetas mdef = null;

        // 直接指明编辑器
        String mdefName = hc.params.get("name");
        if (!Strings.isBlank(mdefName)) {
            mdef = metas.getView(mdefName, viewHomePaths);
        }

        // 如果没有视图，继续尝试
        if (null == mdef) {
            // 获取要操作的对象
            String aph = hc.params.val_check(0);
            WnObj o = Wn.checkObj(sys, aph);
            sw.tag("ok:checkObj");

            // 在对象里做了声明
            mdefName = o.getString("metas");
            if (!Strings.isBlank(mdefName)) {
                mdef = metas.getView(mdefName, viewHomePaths);
            }

            // 读取映射文件
            if (null == mdef) {
                for (String viewHomePath : viewHomePaths) {
                    String phMapping = Wn.appendPath(viewHomePath, mappFileName);
                    String aphMapping = Wn.normalizeFullPath(phMapping, sys);
                    WnObj oMapping = sys.io.fetch(null, aphMapping);
                    if (null == oMapping)
                        continue;
                    sw.tagf("ok:(%s):oMapping", oMapping.path());
                    mdef = metas.getView(oMapping, o, viewHomePaths);
                    if (null != mdef) {
                        sw.tagf("ok:(%s):metas", viewHomePath);
                        break;
                    }
                }
            }
            // 找到视图，打印一下
            else {
                sw.tagf("ok-mdefName:(%s):metas", mdefName);
            }
        }
        // 找到了视图，打印一下
        else {
            sw.tagf("ok-mdefName:(%s):metas", mdefName);
        }
        sw.tag("prepare to output");
        // 输出
        String json = Json.toJson(mdef, hc.jfmt);
        sys.out.println(json);
        sw.tag("done for println");
        sw.stop();
        if (log.isDebugEnabled()) {
            log.debugf("ti_views done: %s", sw.toString());
        }
    }

}
