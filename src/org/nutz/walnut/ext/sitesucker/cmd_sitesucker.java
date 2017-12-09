package org.nutz.walnut.ext.sitesucker;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.json.Json;
import org.nutz.lang.ContinueLoop;
import org.nutz.lang.Each;
import org.nutz.lang.ExitLoop;
import org.nutz.lang.LoopException;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

/**
 * 爬网站
 * 
 * 
 * @author pw
 *
 */
public class cmd_sitesucker extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        ZParams params = ZParams.parse(args, null);

        // 获取指定文件或目录
        WnObj webObj = null;
        if (params.vals.length > 0) {
            webObj = sys.io.fetch(null, Wn.normalizeFullPath(params.val(0), sys));
        } else {
            webObj = sys.getCurrentObj();
        }

        // TODO 下载指定网站
        if (params.has("url")) {

        }

        // 获取某个网页的外链信息
        if (params.is("exlink")) {
            if (webObj.isDIR()) {
                sys.err.print("-exlink only for html/htm file");
                return;
            }
            NutMap flinks = NutMap.NEW();
            checkHtml(sys, webObj, flinks);
            if (params.is("json")) {
                sys.out.print(Json.toJson(flinks));
            } else {
                StringBuilder logContent = new StringBuilder();
                logContent.append("# ")
                          .append(webObj.name())
                          .append("(")
                          .append(flinks.size())
                          .append(")\n");
                for (String furl : flinks.keySet()) {
                    String ftp = flinks.getString(furl);
                    logContent.append(ftp).append(" ").append(furl).append("\n");
                }
                sys.out.print(logContent);
            }
        }

        // 修改网站外链内容，改为内联
        if (params.is("allin")) {
            // 遍历所有问题，获取外链信息
            NutMap exlinks = NutMap.NEW();
            checkWebSite(sys, webObj, exlinks);
            // 输出log
            String frootPath = webObj.path();
            WnObj logExlinks = sys.io.createIfNoExists(webObj, "exlink.log", WnRace.FILE);
            StringBuilder logContent = new StringBuilder();
            for (String fpath : exlinks.keySet()) {
                NutMap flinks = exlinks.getAs(fpath, NutMap.class);
                if (!flinks.isEmpty()) {
                    logContent.append("# ").append(fpath.replace(frootPath + "/", "")).append("\n");
                    for (String furl : flinks.keySet()) {
                        String ftp = flinks.getString(furl);
                        logContent.append(ftp).append(" ").append(furl).append("\n");
                    }
                    logContent.append("\n");
                }
            }
            sys.io.writeText(logExlinks, logContent);
        }

    }

    private void checkWebSite(WnSystem sys, WnObj rootDir, NutMap exlinks) {
        WnQuery query = Wn.Q.pid(rootDir.id()).sortBy("nm", 1);
        sys.io.each(query, new Each<WnObj>() {
            @Override
            public void invoke(int index, WnObj ele, int length)
                    throws ExitLoop, ContinueLoop, LoopException {
                if (ele.isDIR()) {
                    checkWebSite(sys, ele, exlinks);
                } else {
                    String type = ele.type();
                    // 网页
                    if ("html".equalsIgnoreCase(type) || "htm".equalsIgnoreCase(type)) {
                        String fpath = ele.path();
                        NutMap flinks = exlinks.getAs(fpath, NutMap.class);
                        if (flinks == null) {
                            flinks = NutMap.NEW();
                            exlinks.setv(fpath, flinks);
                        }
                        // 开始分析文件
                        checkHtml(sys, ele, flinks);
                    }
                }
            }
        });
    }

    // img标签
    private static final String IMG_REGEX = "<(img|IMG).*src=(.*?)[^>]*?>";
    // js标签
    private static final String JS_REGEX = "<(script|SCRIPT).*src=(.*?)[^>]*?>";
    // js标签
    private static final String CSS_REGEX = "<(link|Link).*href=(.*?)[^>]*?>";
    // 外链
    private static final String ELINK = "(http|https):\"?(.*?)(\"|\'|>|\\s+)";

    private void checkHtml(WnSystem sys, WnObj htmlObj, NutMap flinks) {
        String htmlContent = sys.io.readText(htmlObj);
        // 获取图片
        findExLink(flinks, htmlContent, "image", IMG_REGEX, ELINK);
        // 获取js
        findExLink(flinks, htmlContent, "js", JS_REGEX, ELINK);
        // 获取css
        findExLink(flinks, htmlContent, "css", CSS_REGEX, ELINK);
    }

    private void findExLink(NutMap flinks,
                            String htmlContent,
                            String tagTp,
                            String tagRegex,
                            String linkRegex) {
        Matcher m1 = Pattern.compile(tagRegex).matcher(htmlContent);
        while (m1.find()) {
            String imgBlock = m1.group();
            Matcher m2 = Pattern.compile(linkRegex).matcher(imgBlock);
            while (m2.find()) {
                String imgUrl = m2.group().substring(0, m2.group().length() - 1);
                flinks.setv(imgUrl, tagTp);
            }
        }
    }

}