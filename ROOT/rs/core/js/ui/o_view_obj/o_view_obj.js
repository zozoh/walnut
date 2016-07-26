(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/form/form'
], function(ZUI, Wn, FormUI){
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
<div class="ui-arena opreview" ui-fitparent="yes" show-info="yes">
    <div class="opreview-main"></div>
    <div class="opreview-info" ui-gasket="info"></div>
    <div class="opreview-showinfo"
        data-balloon="{{opreview.showinfo}}"
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
            UI.arena.attr("show-info", "no");
        },
        "click .opreview-showinfo" : function() {
            var UI = this;
            UI.arena.attr("show-info", "yes");
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
    __show_info : function(o, fields) {
        var UI = this;
        new FormUI({
            parent : UI,
            gasketName : "info",
            title : '<i class="oicon" otp="'+o.tp+'"></i>' + o.nm,
            fields : (fields||[]).concat(UI.my_fields)
        }).render(function(){
            this.setData(o);
        });
    },
    //...............................................................
    getCurrentEditObj : function(){
        return this.gasket.info.getData();
    },
    //...............................................................
    update : function(o) {
        var UI = this;
        UI.$el.attr("oid", o.id);
        UI.refresh();
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
            // 显示信息
            UI.__show_info(o);
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
            // 显示信息
            UI.__show_info(o, [{
                key   : "width",
                title : "i18n:obj.width",
                type  : "int",
                dft : -1,
                editAs : "label"
            },{
                key   : "height",
                title : "i18n:obj.height",
                type  : "int",
                dft : -1,
                editAs : "label"
            }]);
        }
        // 可预览的视频
        else if(/^video/.test(o.mime) && o.video_preview){
            UI.hideLoading();
            jM.attr("mode", "video");
            var jDiv = UI.ccode("showVideo").appendTo(jM);
            jVideo = jDiv.find("video");
            jVideo.find("source").prop("src", "/o/read/id:"+o.video_preview+"?_="+Date.now());
            // 显示信息
            UI.__show_info(o);
        }
        // 其他的对象
        else{
            UI.hideLoading();
            jM.attr("mode","others");
            jM.html(UI.msg("opreview.noway"));
            // 显示信息
            UI.__show_info(o);
        }
    },
    //...............................................................
    resize : function(){
        // var UI = this;
        // var jM = UI.arena.find(".opreview-main");
        // var md = jM.attr("mode");
        // // 视频
        // if("video" == md) {
        //     var jVideo = jM.find("video");
        //     var jVCtrl = jM.find(".video-ctrl");
        //     jVCtrl.css({
        //         "height"    : jVideo.height(),
        //         "font-size" : jVideo.height()/4
        //     });
        // }
    },
    //...............................................................
    init : function(){
        var UI = this;

        UI.my_fields = [{
            key   : "id",
            title : "i18n:obj.id",
            type  : "string",
            editAs : "label"
        }, {
            key   : "ph",
            title : "i18n:obj.ph",
            type  : "string",
            editAs : "label",
            uiConf : {
                escapeHtml : false,
                parseData : function(ph) {
                    return Wn.objDisplayPath(UI, ph, -3);
                }
            }
        }, {
            key   : "lm",
            title : "i18n:obj.lm",
            type  : "datetime",
            editAs : "label"
        }, {
            key   : "len",
            title : "i18n:obj.len",
            type  : "int",
            dft : 0,
            editAs : "label",
            uiConf : {
                escapeHtml : false,
                parseData : function(len) {
                    return '<b>' + $z.sizeText(len) + '</b> <em>(' + len + ')</em>';
                }
            }
        }, {
            key   : "race",
            title : "i18n:obj.race",
            type  : "string",
            editAs : "label"
        }, {
            key   : "tp",
            title : "i18n:obj.tp",
            type  : "string",
            editAs : "label"
        }, {
            key   : "mime",
            title : "i18n:obj.mime",
            type  : "string",
            editAs : "label"
        }, {
            key   : "md",
            title : "i18n:obj.md",
            type  : "string",
            editAs : "label"
        }, {
            key   : "c",
            title : "i18n:obj.c",
            type  : "string",
            editAs : "label"
        }, {
            key   : "m",
            title : "i18n:obj.m",
            type  : "string",
            editAs : "label"
        }, {
            key   : "g",
            title : "i18n:obj.g",
            type  : "string",
            editAs : "label"
        }, {
            key   : "ct",
            title : "i18n:obj.ct",
            type  : "datetime",
            editAs : "label"
        }];
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);