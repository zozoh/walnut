package org.nutz.walnut.ext.thing;

import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Strings;
import org.nutz.mvc.View;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Fail;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.Param;
import org.nutz.mvc.annotation.ReqHeader;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.web.module.AbstractWnModule;
import org.nutz.walnut.web.util.WnWeb;
import org.nutz.walnut.web.view.WnObjDownloadView;

@IocBean
@At("/th")
@Ok("void")
@Fail("void")
public class ThingModule extends AbstractWnModule {

    @At("/file/?/?/?")
    @Fail("http:404")
    public View read(String th_set,
                     String th_id,
                     String dirName,
                     @Param("id") String fId,
                     @Param("nm") String fnm,
                     @ReqHeader("User-Agent") String ua) {
        WnObj oTs = io.checkById(th_set);
        WnObj oDir = io.check(oTs, "data/" + th_id + "/" + dirName);
        WnObj o = null;

        // 根据 ID
        if (!Strings.isBlank(fId)) {
            o = io.checkById(fId);
            if (!o.isMyParent(oDir)) {
                throw Er.createf("e.web.th.file.notMyParent", "%s -> %s", o, oDir);
            }
        }
        // 根据名称
        else if (!Strings.isBlank(fnm)) {
            o = io.check(oDir, fnm);
        }
        // 抛错
        else {
            throw Er.create("e.web.th.noFile");
        }

        // 更新客户端信息
        ua = WnWeb.autoUserAgent(o, ua, false);

        // 下载
        return new WnObjDownloadView(io, o, ua);
    }

}
