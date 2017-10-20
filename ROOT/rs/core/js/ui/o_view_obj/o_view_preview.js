(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/o_view_obj/o_view_meta'
], function(ZUI, Wn, ObjMetaUI){
//==============================================
var html = `
<div class="ui-code-template">
    <pre code-id="showTxt"></pre>
    <div code-id="showThumb" class="thumb-con"><img></div>
    <div code-id="showImg" class="img-con"><img style="visibility:hidden"></div>
    <div code-id="showVideo" class="video-con" video-status="pause">
        <video width="100%" controls>
            <source></source>
        </video>
        <!--div class="video-ctrl">
            <div class="vc-play"><i class="fa fa-play-circle"></i></div>
            <div class="vc-pause"><i class="fa fa-pause-circle"></i></div>
        </div>
        <div class="video-bar">
            <div class="vb-loop"><i class="fa fa-retweet"></i></div>
            <div class="vb-go-head"><i class="fa fa-step-backward"></i></div>
            <div class="vb-play"><i class="fa fa-play"></i></div>
            <div class="vb-stop"><i class="fa fa-stop"></i></div>
            <div class="vb-go-end"><i class="fa fa-step-forward"></i></div>
            <div class="vb-progress">
                <div class="vbp-current" style="width:100px;"></div>
            </div>
            <div class="vb-time">00:00:00</div>
            <div class="vb-volume"><i class="fa fa-volume-up"></i></div>
        </div-->
    </div>
</div>
<div class="ui-arena opreview">
    <header>
        <ul>
            <li><b a="fullscreen" balloon="left:oview.preview.fullscreen">
                <i class="zmdi zmdi-fullscreen"></i>
            </b></li>
            <li><b a="download" balloon="left:oview.preview.download">
                <i class="zmdi zmdi-download"></i>
            </b></li>
            <li><b a="open" balloon="left:oview.preview.open_in_new">
                <i class="zmdi zmdi-open-in-new"></i>
            </b></li>
        </ul>
    </header>
    <section></section>
    <aside class="exit-fullscreen">
        <i class="zmdi zmdi-fullscreen-exit"></i>
        <span>{{oview.preview.exit_fullscreen}}</span>
    </aside>
</div>
`;
//==============================================
return ZUI.def("ui.o_view_preview", {
    dom  : html,
    css  : "ui/o_view_obj/theme/o_view_obj-{{theme}}.css",
    i18n : "ui/o_view_obj/i18n/{{lang}}.js",
    //...............................................................
    init : function(){
        var UI = this;
        // 监控 Esc 退出全屏
        UI.watchKey(27, function(e){
            this.arena.removeAttr("fullscreen");
        });
    },
    //...............................................................
    events : {
        // 点击全屏
        'click ul b[a="fullscreen"]' : function(e){
            this.arena.attr("fullscreen", "yes");
        },
        // 退出全屏
        'click .exit-fullscreen' : function(e){
            this.arena.removeAttr("fullscreen");
        },
        // 下载
        'click ul b[a="download"]' : function(e){
            var o = this.__OBJ;
            if(o)
                $z.openUrl("/o/read/id:" + o.id, "_blank", "GET", {
                    d : true
                });
        },
        // 在新窗口打开
        'click ul b[a="open"]' : function(e){
            var o = this.__OBJ;
            if(o){
                var url = "/a/open/"+(window.wn_browser_appName||"wn.browser");
                $z.openUrl(url, "_blank", "GET", {
                    "ph" : "id:" + o.id
                });
            }
        },
    },
    //...............................................................
    redraw : function() {
        this.balloon();
    },
    //...............................................................
    update : function(o) {
        var UI   = this;
        UI.__OBJ = o;
        var opt  = UI.options;
        var jM   = UI.arena;
        var jW   = jM.find(">section").empty();
        
        // 显示加载
        UI.showLoading();

        // 文本内容
        if(/text|javascript|json/.test(o.mime)){
            jM.attr("mode", "text");
            Wn.read(o, function(content){
                UI.hideLoading();
                jM.empty();
                var jPre = UI.ccode("showTxt").appendTo(jW);
                jPre.text(content);
            });
        }
        // 可以预览的图像
        else if(/\/(jpeg|png|gif)/.test(o.mime)){
            jM.attr("mode", "pic");
            var jDiv = UI.ccode("showImg").appendTo(jW);
            jImg = jDiv.find("img");
            jImg.prop("src", "/o/read/id:"+encodeURIComponent(o.id)+"?_="+Date.now()).one("load", function(){
                UI.hideLoading();
                jImg.attr({
                    "old-width"  : this.width,
                    "old-height" : this.height
                }).css("visibility", "");
                var W  = jDiv.width();
                var H  = jDiv.height();
                if(this.width > W || this.height > H){
                    jImg.addClass("autofit");
                }else{
                    jImg.removeClass("autofit");
                }
            });
        }
        // 可预览的视频
        else if(/^video/.test(o.mime) && o.video_preview){
            UI.hideLoading();
            jM.attr("mode", "video");
            var jDiv = UI.ccode("showVideo").appendTo(jW);
            jVideo = jDiv.find("video");
            jVideo.find("source").prop("src", "/o/read/id:"+o.video_preview+"?_="+Date.now());
        }
        // 其他的对象
        else{
            UI.hideLoading();
            jM.attr("mode","thumb");
            var jDiv = UI.ccode("showThumb").appendTo(jW);
            jDiv.find("img").attr({
                "src" : '/o/thumbnail/id:'+o.id+'?sh=128'
            });
        }
    },
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);