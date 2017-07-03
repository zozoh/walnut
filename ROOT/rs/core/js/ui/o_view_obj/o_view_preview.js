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
    <div code-id="showThumb" class="thumb-con"></div>
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
<div class="ui-arena opreview"></div>
`;
//==============================================
return ZUI.def("ui.o_view_preview", {
    dom  : html,
    css  : "ui/o_view_obj/theme/o_view_obj-{{theme}}.css",
    i18n : "ui/o_view_obj/i18n/{{lang}}.js",
    //...............................................................
    update : function(o) {
        var UI  = this;
        var opt = UI.options;
        var jM  = UI.arena.empty();
        
        // 显示加载
        UI.showLoading();

        // 文本内容
        if(/text|javascript|json/.test(o.mime)){
            jM.attr("mode", "text");
            Wn.read(o, function(content){
                UI.hideLoading();
                jM.empty();
                var jPre = UI.ccode("showTxt").appendTo(jM);
                jPre.text(content);
            });
        }
        // 可以预览的图像
        else if(/\/(jpeg|png|gif)/.test(o.mime)){
            jM.attr("mode", "pic");
            var jDiv = UI.ccode("showImg").appendTo(jM);
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
            var jDiv = UI.ccode("showVideo").appendTo(jM);
            jVideo = jDiv.find("video");
            jVideo.find("source").prop("src", "/o/read/id:"+o.video_preview+"?_="+Date.now());
        }
        // 其他的对象
        else{
            UI.hideLoading();
            jM.attr("mode","thumb");
            var jDiv = UI.ccode("showThumb").appendTo(jM);
            jDiv.css({
                "background-image" : 'url("/o/thumbnail/id:'+o.id+'?sh=128")'
            });
        }
    },
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);