package org.nutz.walnut.ext.hmaker.util.com;

import org.jsoup.nodes.Element;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.hmaker.util.HmPageTranslating;
import org.nutz.walnut.ext.hmaker.util.Hms;

public class hmc_video extends AbstractNoneValueCom {

    @Override
    public boolean isDynamic(Element eleCom) {
        return false;
    }

    @Override
    protected boolean doArena(HmPageTranslating ing, Element eleArena) {
        // 处理 DOM: 视频源
        String src = ing.propCom.getString("src");
        Element eleCon = eleArena.appendElement("DIV").addClass("hmc-video-con");

        // 图片不存在，那么输出一个假的
        if (Strings.isBlank(src)) {
            eleCon.attr("no-src", "yes")
                  .append("<div class=\"hmcv-s-blank\"><i class=\"fa fa-video\"></i></div>");
        }
        // 否则输出视频标签
        else {
            Element eleVideo = eleCon.appendElement("video");
            Element eleSource = eleVideo.appendElement("source");

            // 打开控制条
            if (ing.propCom.is("controls", "dft"))
                eleVideo.attr("controls", true);

            // 开启静音
            if (ing.propCom.getBoolean("muted"))
                eleVideo.attr("muted", true);

            // 自动播放
            if (ing.propCom.getBoolean("autoplay"))
                eleVideo.attr("autoplay", true);

            // 封面:这里需要预先转换一下
            if (ing.propCom.has("poster")) {
                String poster = ing.explainLink(ing.propCom.getString("poster"), true);
                eleVideo.attr("poster", poster);
            }

            // 预先标识一下正在加载中
            eleCon.attr("st", "loading");

            // zozoh: src 就不用展开了，因为所有控件输出的结果后，最后会被统一转换的
            eleSource.attr("src", src);

            // ..........................................
            // 处理 CSS
            NutMap cssVCon = new NutMap();
            NutMap cssVideo = new NutMap();

            // 如果设置了宽高，则需要指定视频自适应
            if (!Hms.isUnset(ing.cssEle.getString("width"))) {
                cssVCon.put("width", "100%");
                cssVideo.put("width", "100%");
            }
            if (!Hms.isUnset(ing.cssEle.getString("height"))) {
                cssVCon.put("height", "100%");
                cssVideo.put("height", "100%");
            }
            ing.addMyRule(".hmc-video-con", cssVideo);
            ing.addMyRule("video", cssVideo);

            // 增加一层覆盖层
            eleCon.append("<div class=\"hmcv-s-load\"><i class=\"zmdi zmdi-spinner zmdi-hc-spin\"></i></div>");
            eleCon.append("<div class=\"hmcv-s-play\"><b><i class=\"zmdi zmdi-play\"></i></b></div>");
            eleCon.append("<div class=\"hmcv-s-pause\"><b><i class=\"zmdi zmdi-pause\"></i></b></div>");
            eleCon.append("<div class=\"hmcv-s-replay\"><b><i class=\"zmdi zmdi-replay\"></i></b></div>");
        }

        // ...........................................
        // 链入控件的 jQuery 插件
        ing.jsLinks.add("/gu/rs/ext/hmaker/hmc_video.js");
        String script = String.format("$('#%s > .hmc-video').hmc_video(%s);",
                                      ing.comId,
                                      Json.toJson(ing.propCom,
                                                  JsonFormat.forLook().setIgnoreNull(false)));
        ing.scripts.add(Hms.wrapjQueryDocumentOnLoad(script));

        // 返回成功吧
        return true;
    }

    @Override
    protected String getArenaClassName() {
        return "hmc-video";
    }

}
