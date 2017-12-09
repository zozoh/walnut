package org.nutz.walnut.ext.sitesucker;

import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.http.Http;
import org.nutz.http.Response;
import org.nutz.json.Json;
import org.nutz.lang.ContinueLoop;
import org.nutz.lang.Each;
import org.nutz.lang.ExitLoop;
import org.nutz.lang.LoopException;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
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

    private static Log log = Logs.get();

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
            sys.out.println("# find-exlink");
            NutMap exlinks = NutMap.NEW();
            checkWebSite(sys, webObj, exlinks);
            // 输出log
            String frootPath = webObj.path();
            WnObj logExlinks = sys.io.createIfNoExists(webObj, "exlink.log", WnRace.FILE);
            StringBuilder logContent = new StringBuilder();
            for (String fpath : exlinks.keySet()) {
                NutMap flinks = exlinks.getAs(fpath, NutMap.class);
                String rePath = fpath.replace(frootPath + "/", "");
                sys.out.println(rePath + " " + flinks.size());
                if (!flinks.isEmpty()) {
                    logContent.append("# ").append(rePath).append("\n");
                    for (String furl : flinks.keySet()) {
                        String ftp = flinks.getString(furl);
                        logContent.append(ftp).append(" ").append(furl).append("\n");
                    }
                    logContent.append("\n");
                }
            }
            sys.io.writeText(logExlinks, logContent);

            // 开始分析链接并下载
            sys.out.println("\n# download-exlink");
            NutMap dwRecord = NutMap.NEW();
            NutMap dwFiles = NutMap.NEW();
            NutMap exDirs = NutMap.NEW();
            downloadExlink(sys,
                           webObj,
                           exlinks,
                           dwRecord,
                           dwFiles,
                           exDirs,
                           params.is("expass", false));
            // 输出log
            WnObj logDws = sys.io.createIfNoExists(webObj, "dwlink.log", WnRace.FILE);
            StringBuilder dwContent = new StringBuilder();
            dwContent.append("# ")
                     .append(webObj.name())
                     .append("(")
                     .append(dwRecord.size())
                     .append(")\n");
            for (String dwUrl : dwRecord.keySet()) {
                String result = dwRecord.getString(dwUrl);
                dwContent.append(result).append(" ").append(dwUrl);
                if ("success".equals(result)) {
                    dwContent.append(" File:")
                             .append(dwFiles.getString(dwUrl).replace(frootPath + "/", ""));
                }
                dwContent.append("\n");
            }
            sys.io.writeText(logDws, dwContent);

            // 外链内容的相对路径内容，比如css中引用了图片
            sys.out.println("\n# recheck-dwexlink");
            recheckExlinkFile(sys, exDirs);

            // 开始进行替换
            sys.out.println("\n# replace-html");
            replaceExLink(sys, webObj, frootPath, dwRecord, dwFiles);
        }
    }

    // img标签
    private static final String INNER_URL_REGEX = "(url|URL)\\((\"|\'|>|\\s+)(.*?)(\"|\'|>|\\s+)\\)";

    private void recheckExlinkFile(WnSystem sys, NutMap exDirs) {
        for (String expath : exDirs.keySet()) {
            WnObj expathDir = sys.io.fetch(null, expath);
            WnQuery query = Wn.Q.pid(expathDir.id()).sortBy("nm", 1);
            sys.io.each(query, new Each<WnObj>() {
                @Override
                public void invoke(int index, WnObj ele, int length)
                        throws ExitLoop, ContinueLoop, LoopException {
                    if (ele.isFILE()) {
                        String etp = ele.type();
                        if ("css".equalsIgnoreCase(etp)
                            || "js".equalsIgnoreCase(etp)
                            || "html".equalsIgnoreCase(etp)
                            || "htm".equalsIgnoreCase(etp)) {
                            String fcontent = sys.io.readText(ele);
                            NutMap flinks = NutMap.NEW();
                            String orgParentUrl = ele.getString("orgparent", "") + "/";
                            // 一般是css，background的url
                            Matcher m2 = Pattern.compile(INNER_URL_REGEX).matcher(fcontent);
                            while (m2.find()) {
                                String ex2Url = m2.group(3);
                                flinks.setv(ex2Url, false);
                            }
                            // 如果相对路径的则下载
                            if (!flinks.isEmpty()) {
                                for (String ex2Url : flinks.keySet()) {
                                    // 还能有这种操作？外链里面直接写死的外链？
                                    if (ex2Url.startsWith("http")) {
                                        log.errorf("2ex-care: %s", ex2Url);
                                    }
                                    // 绝对路径？
                                    else if (ex2Url.startsWith("/")) {
                                        log.errorf("2ex-care: %s", ex2Url);
                                    }
                                    // 相对路径
                                    else {
                                        String dwUrl = orgParentUrl + ex2Url;
                                        log.infof("2ex-dw: %s", dwUrl);
                                        WnObj ex2Obj = sys.io.createIfNoExists(expathDir,
                                                                               ex2Url,
                                                                               WnRace.FILE);
                                        // 下载
                                        try {
                                            Response dwResp = Http.get(dwUrl);
                                            if (!dwResp.isOK()) {

                                            } else {
                                                InputStream dwIn = dwResp.getStream();
                                                sys.io.writeAndClose(ex2Obj, dwIn);
                                            }
                                        }
                                        catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            });
        }

    }

    private void replaceExLink(WnSystem sys,
                               WnObj rootDir,
                               String rootPath,
                               NutMap dwRecord,
                               NutMap dwFiles) {
        WnQuery query = Wn.Q.pid(rootDir.id()).sortBy("nm", 1);
        sys.io.each(query, new Each<WnObj>() {
            @Override
            public void invoke(int index, WnObj ele, int length)
                    throws ExitLoop, ContinueLoop, LoopException {
                if (ele.isDIR()) {
                    replaceExLink(sys, ele, rootPath, dwRecord, dwFiles);
                } else {
                    String type = ele.type();
                    // 网页
                    if ("html".equalsIgnoreCase(type) || "htm".equalsIgnoreCase(type)) {
                        String htmlContent = sys.io.readText(ele);
                        String allinHtmlContent = allinHtml(htmlContent,
                                                            rootPath,
                                                            dwRecord,
                                                            dwFiles);
                        if (!htmlContent.equals(allinHtmlContent)) {
                            sys.out.println(ele.name());
                            sys.io.writeText(ele, allinHtmlContent);
                        }
                    }
                }
            }
        });
    }

    private String allinHtml(String htmlContent, String rootPath, NutMap dwRecord, NutMap dwFiles) {
        String[] htmlLines = htmlContent.split("\n");
        StringBuilder nhtml = new StringBuilder();
        for (int i = 0; i < htmlLines.length; i++) {
            String hline = htmlLines[i];
            Matcher elinkReg = Pattern.compile(ELINK).matcher(hline);
            while (elinkReg.find()) {
                String exUrl = elinkReg.group().substring(0, elinkReg.group().length() - 1);
                String result = dwRecord.getString(exUrl);
                if ("success".equals(result)) {
                    String localUrl = dwFiles.getString(exUrl).replace(rootPath + "/", "");
                    hline = hline.replace(exUrl, localUrl);
                }
            }
            nhtml.append(hline).append("\n");
        }
        return nhtml.substring(0, nhtml.length() - 1);
    }

    private void downloadExlink(WnSystem sys,
                                WnObj rootDir,
                                NutMap exlinks,
                                NutMap dwRecord,
                                NutMap dwFiles,
                                NutMap exDirs,
                                boolean passExisted) {
        for (Object exval : exlinks.values()) {
            NutMap flinks = (NutMap) exval;
            if (!flinks.isEmpty()) {
                for (String furl : flinks.keySet()) {
                    String dwUrl = new String(furl);
                    if ("success".equals(dwRecord.getString(dwUrl))
                        || "fail".equals(dwRecord.getString(dwUrl))) {
                        continue;
                    }
                    dwRecord.setv(dwUrl, "none");
                    // 去掉？后面
                    String dwParams = "";
                    int qmark = furl.indexOf('?');
                    if (qmark > -1) {
                        dwParams = furl.substring(qmark + 1); // ?后面的
                        furl = furl.substring(0, qmark);
                    }
                    // 去掉前面的http://或https://
                    boolean isHttps = furl.indexOf("https") > -1;
                    furl = furl.replace("http://", "").replace("https://", "");
                    // 判断该文件是否已经下载过
                    String fpath = furl;

                    // 获取文件名称与目录
                    int lslash = fpath.lastIndexOf('/');
                    String exParent = fpath.substring(0, lslash);
                    String exLink = fpath.substring(lslash + 1);
                    log.infof("ss-dw: %s with [%s] from [%s]", exLink, dwParams, dwUrl);

                    // 生成文件与目录
                    String dwLink = exLink;
                    if (!Strings.isBlank(dwParams)) {
                        dwParams = dwParams.replaceAll("\\?", "-")
                                           .replaceAll("#", "-")
                                           .replaceAll("/", "-");
                        dwLink = dwParams + "_" + dwLink;
                    }

                    WnObj exParentObj = sys.io.createIfNoExists(rootDir, exParent, WnRace.DIR);
                    WnObj exLinkObj = sys.io.createIfNoExists(exParentObj, dwLink, WnRace.FILE);

                    // 记录外链目录下的文件数量
                    exDirs.intIncrement(exParentObj.path(), 1);

                    // 已经下载过的不再下载
                    if (passExisted && exLinkObj.len() > 0) {
                        dwRecord.setv(dwUrl, "success");
                        dwFiles.setv(dwUrl, exLinkObj.path());
                        sys.out.printlnf("ex:%s\n >:%s", dwUrl, exLinkObj.path());
                        continue;
                    }

                    // 下载
                    try {
                        Response dwResp = Http.get(dwUrl);
                        if (!dwResp.isOK()) {
                            dwRecord.setv(dwUrl, "fail");
                        } else {
                            InputStream dwIn = dwResp.getStream();
                            sys.io.writeAndClose(exLinkObj, dwIn);
                            sys.io.appendMeta(exLinkObj,
                                              NutMap.NEW()
                                                    .setv("orgurl", dwUrl)
                                                    .setv("orgparent",
                                                          (isHttps ? "https://" : "http://")
                                                                       + exParent));
                            dwRecord.setv(dwUrl, "success");
                            dwFiles.setv(dwUrl, exLinkObj.path());
                            sys.out.printlnf("dw:%s\n >:%s", dwUrl, exLinkObj.path());
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        dwRecord.setv(dwUrl, "fail");
                    }
                }
            }
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