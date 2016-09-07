(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/o_view_obj/o_view_meta'
], function(ZUI, Wn, ObjMetaUI){
//==============================================
var html = function(){/*
<div class="ui-code-template">
    <pre code-id="showTxt"></pre>
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
<div class="ui-arena opreview" ui-fitparent="yes">
    <div class="opreview-main"></div>
    <div class="opreview-meta" ui-gasket="meta"></div>
    <div class="opreview-showmeta"
        data-balloon="{{opreview.showmeta}}"
        data-balloon-pos="left">
        <i class="fa fa-info-circle"></i>
    </div>
</div>
*/};
//==============================================
return ZUI.def("ui.o_view_obj", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "theme/ui/o_view_obj/o_view_obj.css",
    i18n : "ui/o_view_obj/i18n/{{lang}}.js",
    //...............................................................
    init : function(opt) {
        $z.setUndefined(opt, "showMeta", true);
    },
    //...............................................................
    events : {
        "dblclick .opreview-main[mode=pic] img" : function(e){
            e.stopPropagation();
            var jImg = $(e.currentTarget);
            jImg.toggleClass("autofit");
        },
        "dragstart .opreview-main[mode=pic] img" : function(e){
            e.preventDefault();
        },
        "click .form-title" : function() {
            var UI = this;
            UI.arena.attr("show-meta", "no");
        },
        "click .opreview-showmeta" : function() {
            var UI = this;
            UI.arena.attr("show-meta", "yes");
        },
        "click .video-ctrl" : function(){
            var UI = this;
            var jCon = UI.arena.find(".video-con");
            console.log(jCon)
            // 正在播放 -> 暂停
            if("play" == jCon.attr("video-status")){
                jCon.attr("video-status","pause")
                    .find("video")[0].pause();    
            }
            // 正在暂停 -> 播放
            else {
                jCon.attr("video-status","play")
                    .find("video")[0].play();    
            }
        },
    },
    //...............................................................
    redraw : function() {
        var UI  = this;
        var opt = UI.options;

        // 根据配置显示/隐藏信息区域
        UI.arena.attr({
            "show-meta" : opt.showMeta ? "yes" : "no"
        }).find(".opreview-meta, .opreview-showmeta")
            .css({
                "display" : (opt.showMeta ? "" : "none")
            });

        // 如果显示信息，则创建元数据视图
        if(opt.showMeta) {
            new ObjMetaUI({
                parent : UI,
                gasketName : "meta"
            }).render(function(){
                UI.defer_report("meta");
            });

            // 延迟加载
            return ["meta"];
        }
       
    },
    //...............................................................
    getCurrentEditObj : function(){
        return this.gasket.meta.getData();
    },
    //...............................................................
    update : function(o) {
        var UI  = this;
        var opt = UI.options;

        UI.$el.attr("oid", o.id);
        UI.refresh();

        // 更新元数据视图
        if(opt.showMeta) {
            UI.gasket.meta.update(o);
        }
    },
    //...............................................................
    refresh : function() {
        var UI = this;
        var jM = UI.arena.find(".opreview-main");

        // 得到对象
        var oid = UI.$el.attr("oid");
        var o = Wn.getById(oid);
        
        // 不能预览文件夹
        if(o.race == "DIR"){
            throw "can.not.preview.DIR";
        }

        // 显示加载
        UI.showLoading(jM);

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
            jImg.prop("src", "/o/read/id:"+o.id+"?_="+Date.now()).one("load", function(){
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
            jM.attr("mode","others");
            jM.html(UI.msg("opreview.noway"));
        }
    },
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);