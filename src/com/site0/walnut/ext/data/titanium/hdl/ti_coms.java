package com.site0.walnut.ext.data.titanium.hdl;

import java.util.List;

import org.nutz.json.Json;
import org.nutz.lang.Strings;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.ext.data.titanium.util.TiCom;
import com.site0.walnut.ext.data.titanium.util.TiComScaning;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlParamArgs;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;

@JvmHdlParamArgs("cqnf")
public class ti_coms implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        // 分析参数: 库目录
        String libHome = hc.params.val(0, "/rs/ti/com");
        WnObj oHome = Wn.checkObj(sys, libHome);

        // 分析参数: 缓存目录
        String[] ss = Strings.splitIgnoreBlank(libHome, "/");
        String dftNm = Strings.join("-", ss);
        String cacheRph = hc.params.getString("cache", "/etc/ti/coms/" + dftNm + ".json");
        String cacheAph = Wn.normalizeFullPath(cacheRph, sys);
        WnObj oCache = null;

        // 分析参数：-f
        boolean force = hc.params.is("f");

        // 准备返回值
        List<TiCom> list = null;
        String json;

        // 看看能否利用缓存
        if (!force) {
            oCache = sys.io.fetch(null, cacheAph);
            if (null != oCache) {
                json = sys.io.readText(oCache);
                list = Json.fromJsonAsList(TiCom.class, json);
            }
        }

        // 没有结果，那么就扫描一下咯
        if (null == list) {
            TiComScaning scan = new TiComScaning(sys.io, oHome);
            list = scan.doScan();
        }

        // 得到返回结果
        json = Json.toJson(list, hc.jfmt);

        // 缓存结果
        if (null == oCache) {
            oCache = sys.io.createIfNoExists(null, cacheAph, WnRace.FILE);
            sys.io.writeText(oCache, json);
        }

        // 输出结果
        sys.out.println(json);
    }

}
