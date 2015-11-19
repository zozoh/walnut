package org.nutz.walnut.web.module;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.tmpl.Tmpl;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.View;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Fail;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.view.RawView;
import org.nutz.mvc.view.ViewWrapper;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.web.view.WnObjDownloadView;

@IocBean
@At("/www")
public class WWWModule extends AbstractWnModule {

    private static final Log log = Logs.get();

    private static final Pattern _P = Pattern.compile("^([^/]+)(/(.+))?$");

    private static final String[] ENTRIES = Lang.array("index.wnml", "index.html");

    private Tmpl tmpl_400;
    private Tmpl tmpl_404;
    private Tmpl tmpl_500;

    public WWWModule() {
        tmpl_400 = Tmpl.parse(Files.read("html/400.wnml"));
        tmpl_404 = Tmpl.parse(Files.read("html/404.wnml"));
        tmpl_500 = Tmpl.parse(Files.read("html/500.wnml"));
    }

    @At("/?/**")
    @Ok("void")
    @Fail("void")
    public View show_page(String usr, String a_path, HttpServletRequest req) {
        // 如果有的话，去掉开头的绝对路径符
        if (null == a_path) {
            a_path = "";
        }
        // 得到相对路径
        else if (a_path.startsWith("/")) {
            a_path = a_path.substring(1);
        }

        if (log.isInfoEnabled())
            log.infof("www(%s): /%s/%s", req.getRemoteAddr(), usr, a_path);

        // 找到用户和对应的命令
        WnUsr u = usrs.check(usr);
        String homePath = Strings.sBlank(u.home(), "/home/" + u.name());
        WnObj oHome = io.fetch(null, homePath);

        if (null == oHome) {
            return gen_errpage(tmpl_404, a_path, "Home not exists!");
        }

        // 准备起始查询条件
        WnQuery q = new WnQuery();
        q.setv("d0", oHome.d0());
        if (!"root".equals(usr))
            q.setv("d1", oHome.d1());

        // 开始试图找到文件对象
        WnObj o = null;

        // 找到 ROOT
        WnObj oROOT = io.getOne(q.setv("www", "ROOT"));

        // 空路径的话，那么意味着对象是 ROOT
        if (Strings.isBlank(a_path)) {
            // 又没有 ROOT 又没有 a_path ，毛，肯定啥也没有
            if (null == oROOT) {
                return gen_errpage(tmpl_404, a_path);
            }
            // 对象本身就是 ROOT 需要拿它的默认值
            o = oROOT;
        }
        // 否则如果有 ROOT 再其内查找
        else if (null != oROOT) {
            o = io.fetch(oROOT, a_path);
        }

        // 这个是一个对象
        String[] entries = ENTRIES;
        if (null != oROOT) {
            entries = oROOT.getArray("www_entry", String.class, ENTRIES);
        }

        // 如果木有，那么看看是不是存在其他的 www 映射
        if (null == o) {
            // 分析路径
            Matcher m = _P.matcher(a_path);
            String cate = null;
            String path = null;
            // 至少是二级路径
            if (m.find()) {
                cate = m.group(1);
                path = m.group(3);

                WnObj oWWW = io.getOne(q.setv("www", cate));
                if (null != oWWW) {
                    o = io.fetch(oWWW, path);
                    entries = oWWW.getArray("www_entry", String.class, entries);
                }
            }
            // 否则肯定还是啥也木有
            else {
                return gen_errpage(tmpl_404, a_path);
            }

        }

        // 找不到文件对象，就是找不到咯
        if (null == o) {
            return gen_errpage(tmpl_404, a_path);
        }

        // 如果发现找到的是个路径对象，依次尝试入口
        if (o.isDIR()) {
            for (String entry : entries) {
                WnObj o2 = io.fetch(o, entry);
                if (null != o2 && o2.isFILE()) {
                    o = o2;
                    break;
                }
            }
            // 还是目录，那就抛错吧
            if (o.isDIR()) {
                return gen_errpage(tmpl_400, a_path);
            }
        }

        try {
            // 动态网页
            // 执行命令
            if (o.isType("wnml")) {
                String cmdText = "www -in id:" + o.id();
                String html = this._run_cmd("www", usr, cmdText);

                // 返回网页
                return new ViewWrapper(new RawView("text/html"), html);
            }
            // 其他的，就直接下载了
            return new WnObjDownloadView(io, o);

        }
        catch (Exception e) {
            return gen_errpage(tmpl_500, a_path, e.toString());
        }

    }

    private View gen_errpage(Tmpl tmpl, String path) {
        String msg;
        if (tmpl_400 == tmpl) {
            msg = "Invalid Request";
        } else if (tmpl_404 == tmpl) {
            msg = "Page NoFound";
        } else {
            msg = "Server Error";
        }

        return gen_errpage(tmpl, path, msg);
    }

    private View gen_errpage(Tmpl tmpl, String path, String msg) {
        NutMap map = Lang.map("url", path);
        map.setv("msg", msg);
        String html = tmpl.render(map, false);
        return new ViewWrapper(new RawView("text/html"), html);
    }

}
