package org.nutz.walnut.ext.hmaker.util.com;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.plugins.zdoc.markdown.Markdown;
import org.nutz.walnut.ext.hmaker.util.HmPageTranslating;
import org.nutz.walnut.ext.hmaker.util.Hms;

public class hmc_text extends AbstractNoneValueCom {

    private static final Pattern P1 = Pattern.compile("\\[#(.+)\\]\\([^)]*\\)");

    @Override
    protected String getArenaClassName() {
        return "hmc-text";
    }

    @Override
    public void joinAnchorList(Element eleCom, List<String> list) {
        NutMap propCom = Hms.loadProp(eleCom, "hm-prop-com", false);
        String code = propCom.getString("code", "");
        String ttp = propCom.getString("contentType", "auto");

        if ("markdown".equals(ttp) || ("auto".equals(ttp) && code.contains("\n"))) {
            Pattern p = P1;
            Matcher m = p.matcher(code);
            while (m.find()) {
                list.add(m.group(1));
            }
        }
    }

    @Override
    protected boolean doArena(HmPageTranslating ing, Element eleArena) {
        // 得到编辑的文本，并将文本转义成 HTML (markdown) 模式
        String code = ing.propCom.getString("code", "");
        String ttp = ing.propCom.getString("contentType", "auto");

        String html;

        // 如果包括换行，则表示是 markdown 文本
        if ("markdown".equals(ttp) || ("auto".equals(ttp) && code.contains("\n"))) {
            html = Markdown.toHtml(code, null);
        }
        // 否则就是纯文本
        else {
            html = Strings.escapeHtml(code);
            html = html.replaceAll("[\r?\n]", "<br>");
        }

        // 更新 HTML
        eleArena.appendElement("article").addClass("md-content").html(html);

        // 删掉空的 <p>
        Elements elePs = eleArena.getElementsByTag("P");
        for (Element eleP : elePs) {
            if (Strings.isBlank(eleP.html())) {
                eleP.remove();
            }
        }

        // 清理 <div>
        Elements eleDivs = eleArena.getElementsByTag("DIV");
        Element br;
        for (Element eleDiv : eleDivs) {
            // 删掉 DIV 开头和结尾的 BR
            Elements children = eleDiv.children();
            if (children.size() > 0) {
                br = children.first();
                if ("BR".equalsIgnoreCase(br.tagName())) {
                    br.remove();
                }
            }
            if (children.size() > 1) {
                br = children.last();
                if ("BR".equalsIgnoreCase(br.tagName())) {
                    br.remove();
                }
            }
            // 删掉 DIV 前的 BR
            br = eleDiv.previousElementSibling();
            if (null != br && "BR".equalsIgnoreCase(br.tagName())) {
                br.remove();
            }
            // 删掉 DIV 后的 BR
            br = eleDiv.nextElementSibling();
            if (null != br && "BR".equalsIgnoreCase(br.tagName())) {
                br.remove();
            }
        }

        // 标识标题
        eleArena.select("h1,h2,h3,h4,h5,h6").addClass("md-header");

        // ...........................................
        // 链入控件的 jQuery 插件
        ing.jsLinks.add("/gu/rs/ext/hmaker/hmc_text.js");
        String script = String.format("$('#%s').hmc_text(%s);",
                                      ing.comId,
                                      Json.toJson(ing.propCom,
                                                  JsonFormat.forLook().setIgnoreNull(false)));
        ing.scripts.add(Hms.wrapjQueryDocumentOnLoad(script));

        return true;
    }

    @Override
    public boolean isDynamic(Element eleCom) {
        return false;
    }
}
