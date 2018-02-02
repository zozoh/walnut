package org.nutz.walnut.web.module;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Strings;
import org.nutz.mvc.View;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.By;
import org.nutz.mvc.annotation.Fail;
import org.nutz.mvc.annotation.Filters;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.ReqHeader;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.usr.WnSession;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.web.filter.WnCheckSession;
import org.nutz.walnut.web.view.WnObjDownloadView;

/**
 * 处理各个 app 界面的主题
 * 
 * 专门加载 css 文件
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
@IocBean
@At("/theme")
@Filters(@By(type = WnCheckSession.class, args = {"true"}))
public class ThemeModule extends AbstractWnModule {

    @Inject("java:$conf.get('theme-home','/rs/theme')")
    private String themeHome;

    @Inject("java:$conf.get('jquery-plugin-home','/rs/core/js/jquery-plugin')")
    private String jqueryPluginHome;

    @Inject("java:$conf.get('ui-home','/rs/core/js/ui')")
    private String uiHome;

    @Inject("java:$conf.get('app-rs','/gu/rs')")
    private String app_rs;

    @At("/r/?/**")
    @Ok("void")
    @Fail("http:404")
    public View getUiTheme(String themeCate,
                           String rsName,
                           @ReqHeader("User-Agent") String ua,
                           HttpServletRequest req,
                           HttpServletResponse resp) {
        WnObj oCss = null;

        // 分析
        String uiName = null;
        int pos = rsName.indexOf('/');
        if (pos > 0) {
            uiName = rsName.substring(0, pos);
            rsName = rsName.substring(pos + 1);
        }

        // 看看会话中当前的 Theme 是啥
        WnSession se = Wn.WC().checkSE();

        // 如果声明了主题，则试图从主题目录里查找
        String theme = se.vars().getString("MY_THEME");

        // 寻找各个 UI 的主题
        if (!Strings.isBlank(theme)) {
            String aph = Wn.appendPath(themeHome, theme, uiName, rsName);
            oCss = io.fetch(null, aph);
        }

        // 如果主题没有，则试图从默认位置查找
        if (null == oCss) {
            WnObj oBase;
            String ph;

            // 内置 UI 直接读取内部的 UI Home
            if ("ui".equals(themeCate)) {
                oBase = io.check(null, uiHome);
                ph = Wn.appendPath(uiName, rsName);
            }
            // 内置 jQueryPlugin 直接读取内部的 jQueryPlugin Home
            else if ("jqp".equals(themeCate)) {
                oBase = io.check(null, jqueryPluginHome);
                ph = Wn.appendPath(uiName, rsName);
            }
            // UIX 从环境变量里读取
            else if ("uix".equals(themeCate)) {
                String base = se.vars().getString("MY_UIX_BASE");
                if (null == base) {
                    String uix = se.vars().getString("MY_UIX");
                    if (null == uix || !uix.startsWith("/gu/"))
                        throw Er.create("e.theme.uix.nobase", themeCate);
                    base = uix.substring(3);
                }
                // 解析路径
                String aph = Wn.normalizeFullPath(base, se);
                // 取得主目录
                oBase = io.check(null, aph);
                ph = Wn.appendPath(uiName, rsName);
            }
            // 如果是 app 则试图先找到这个应用
            else if ("app".equals(themeCate)) {
                oBase = this._check_app_home(uiName);
                ph = rsName;
            }
            //
            // 错误的主题种类
            else {
                throw Er.create("e.theme.cate", themeCate);
            }

            // 读取吧少年
            oCss = io.fetch(oBase, ph);
        }

        // 还没有，就抛 404 吧
        if (null == oCss) {
            throw Er.create("e.theme.noexists", uiName);
        }
        if (Wn.checkEtag(oCss, req, resp))
            return HTTP_304;

        // 读取 CSS 内容返回以便输出
        return new WnObjDownloadView(io, oCss, null, null, null);
    }

}
