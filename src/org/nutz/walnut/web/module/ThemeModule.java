package org.nutz.walnut.web.module;

import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Strings;
import org.nutz.mvc.View;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.By;
import org.nutz.mvc.annotation.Fail;
import org.nutz.mvc.annotation.Filters;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.view.HttpStatusView;
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

    @Inject("java:$conf.get('ui-home','/rs/core/js/ui')")
    private String uiHome;

    @Inject("java:$conf.get('app-rs','/gu/rs')")
    private String app_rs;

    @At("/r/?/?/**")
    @Ok("void")
    @Fail("http:404")
    public View getUiTheme(String themeCate, String uiName, String rsName) {
        WnObj oCss = null;

        // 看看会话中当前的 Theme 是啥
        WnSession se = Wn.WC().checkSE();

        // 如果声明了主题，则试图从主题目录里查找
        String theme = se.vars().getString("MY_THEME");
        if (!Strings.isBlank(theme)) {
            String aph = Wn.appendPath(themeHome, theme, uiName, rsName);
            oCss = io.fetch(null, aph);
        }

        // 如果主题没有，则试图从默认位置查找
        if (null == oCss) {
            String base = uiHome;
            // 如果主题类型不是 "ui" 通常会是 "uix"，那么从环境变量里读取
            if (!"ui".equals(themeCate)) {
                base = se.vars().getString("MY_" + themeCate.toUpperCase());
                if (null == base) {
                    throw Er.create("e.theme.nocate", themeCate);
                }
                // 通常这个路径是个网络访问路径，如果不是绝对路径，则需要拼装上 $rs 的声明
                if (!base.startsWith("/")) {
                    base = app_rs + "/" + base;
                }
                // 分析一下，如果是 /gu 开头的路径，则从根目录访问
                if (base.startsWith("/gu")) {
                    base = base.substring(3);
                }
                // 其他开头的路径暂不支持
                else {
                    throw HttpStatusView.makeThrow(500, "unsupport path: " + base);
                }
            }

            // 读取吧少年
            String ph = Wn.appendPath(base, uiName, rsName);
            oCss = io.fetch(null, ph);
        }

        // 还没有，就抛 404 吧
        if (null == oCss) {
            throw Er.create("e.theme.noexists", uiName);
        }

        // 读取 CSS 内容返回以便输出
        return new WnObjDownloadView(io, oCss);
    }

}
