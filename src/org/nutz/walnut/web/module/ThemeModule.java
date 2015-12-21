package org.nutz.walnut.web.module;

import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Strings;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.By;
import org.nutz.mvc.annotation.Fail;
import org.nutz.mvc.annotation.Filters;
import org.nutz.mvc.annotation.Ok;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.usr.WnSession;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.web.filter.WnCheckSession;

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

    @At("/ui/**")
    @Ok("raw:text/css")
    @Fail("http:404")
    public String getUiTheme(String uiName) {
        WnObj oCss = null;

        // 格式化 ui 的相对路径
        String uiCssPath;
        if (uiName.endsWith(".css")) {
            uiCssPath = uiName;
            uiName = uiName.substring(0, uiName.length() - 4);
        } else {
            uiCssPath = uiName + ".css";
        }

        // 看看会话中当前的 Theme 是啥
        WnSession se = Wn.WC().checkSE();

        // 如果声明了主题，则试图从主题目录里查找
        String theme = se.vars().getString("MY_THEME");
        if (!Strings.isBlank(theme)) {
            String ph = Wn.appendPath(themeHome, theme, uiCssPath);
            oCss = io.fetch(null, ph);
        }

        // 如果主题没有，则试图从默认位置查找
        if (null == oCss) {
            String ph = Wn.appendPath(uiHome, uiName, uiCssPath);
            oCss = io.fetch(null, ph);
        }

        // 还没有，就抛 404 吧
        if (null == oCss) {
            throw Er.create("e.theme.noexists", uiName);
        }

        // 读取 CSS 内容返回以便输出
        return io.readText(oCss);
    }

}
