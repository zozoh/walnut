package org.nutz.walnut.ext.hmaker.hdl;

import java.io.File;
import java.io.OutputStream;

import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.tmpl.Tmpl;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.hmaker.util.Hms;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.WnHttpResponse;
import org.nutz.web.WebException;

public class hmaker_read implements JvmHdl {

    private Tmpl t_404;

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 懒加载必要的模板资源
        if (null == t_404) {
            t_404 = __load_tmpl("404.html");
        }

        // 得到资源路径
        String ph = hc.params.val_check(0);
        // ..................................................
        // 不允许 ph 里写 "id:xxx"，这可能是个漏洞
        if (ph.indexOf("id:") >= 0)
            throw Er.create("e.cmd.hmaker.read.DangerousPath", ph);
        // ..................................................
        // 准备响应对象头部
        String range = hc.params.getString("range");
        String etag = hc.params.getString("etag");
        WnHttpResponse resp = new WnHttpResponse();
        resp.setStatus(200);
        resp.setEtag(etag);

        // 准备下载
        if (hc.params.is("download")) {
            resp.setUserAgent(hc.params.getString("UserAgent"));
        }

        // ..................................................
        // 得到站点
        WnObj oSiteHome = hc.oRefer;

        // 准备返回对象
        WnObj wobj = null;

        // ..................................................
        try {
            // 如果是 _skin_var.less 则懒加载
            if ("_skin_var.less".equals(ph)) {
                __do_skin_var_less(sys, range, resp, oSiteHome);
            }
            // 如果是 skin.css 则动态编译
            else if ("skin.css".equals(ph)) {
                __do_skin_css(sys, range, etag, resp, oSiteHome);
            }
            // 否则，直接读取文件
            else {
                wobj = sys.io.check(oSiteHome, ph);
                resp.prepare(sys.io, wobj, range);
            }
        }
        catch (WebException e) {
            // 确定是 404
            if (e.isKey("e.io.obj.noexists")) {
                resp.setStatus(404);
                resp.prepare(t_404.render(Lang.map("siteName", oSiteHome.name()).setv("path", ph)));
            }
            // 否则抛出吧
            else {
                throw e;
            }
        }
        // ..................................................
        // 输出
        OutputStream ops = sys.out.getOutputStream();
        resp.writeTo(ops);
    }

    private Tmpl __load_tmpl(String fnm) {
        File f = Files.findFile("html/" + fnm);
        if (null == f)
            f = Files.findFile("org/nutz/walnut/web/html/" + fnm);
        String html = Files.read(f);
        Tmpl re = Tmpl.parse(html);
        return re;
    }

    private void __do_skin_css(WnSystem sys,
                               String range,
                               String etag,
                               WnHttpResponse resp,
                               WnObj oSiteHome) {
        // 先确保当前站点已经指定了皮肤
        String skinName = oSiteHome.getString("hm_site_skin");

        // 没指定的话返回空
        if (Strings.isBlank(skinName)) {
            resp.prepare(new byte[0]);
        }
        // 指定了皮肤的话，则看看是否有必要重新生成一下皮肤
        else {
            // 得到皮肤css 的UI想
            WnObj oSkinCss = Hms.genSiteSkinCssObj(sys, oSiteHome, skinName);

            // 准备输出流
            resp.prepare(sys.io, oSkinCss, range);
        }
    }

    private void __do_skin_var_less(WnSystem sys,
                                    String range,
                                    WnHttpResponse resp,
                                    WnObj oSiteHome) {
        String skin = oSiteHome.getString("hm_site_skin");
        WnObj oSkinVar = sys.io.fetch(oSiteHome, ".skin/_skin_var.less");

        // 如果没有皮肤，那么输出个空吧
        if (Strings.isBlank(skin)) {
            resp.prepare(new byte[0]);
        }
        // 有皮肤，但是没有变量，Copy 一个咯
        else if (null == oSkinVar) {
            oSkinVar = Hms.copy_skin_var(sys, oSiteHome, skin);
            resp.prepare(sys.io, oSkinVar, range);
        }
        // 直接使用
        else {
            resp.prepare(sys.io, oSkinVar, range);
        }
    }

}