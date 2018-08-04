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
    init : function(opt){
        var UI = this;
        // 监控 Esc 退出全屏
        UI.watchKey(27, function(e){
            this.arena.removeAttr("fullscreen");
        });
        // 准备快捷命令列表
        UI.__commands = {
            "fullscreen" : {
                icon : '<i class="zmdi zmdi-fullscreen"></i>',
                tip  : 'i18n:oview.preview.fullscreen',
                handler : function(){
                    UI.arena.attr("fullscreen", "yes");
                }
            },
            "download" : {
                icon : '<i class="zmdi zmdi-download"></i>',
                tip  : 'i18n:oview.preview.download',
                handler : function(o){
                    $z.openUrl("/o/read/id:" + o.id, "_blank", "GET", {
                        d : true
                    });
                }
            },
            "open" : {
                icon : '<i class="zmdi zmdi-open-in-new"></i>',
                tip  : 'i18n:oview.preview.open_in_new',
                handler : function(o){
                    var url = "/a/open/"+(window.wn_browser_appName||"wn.browser");
                    $z.openUrl(url, "_blank", "GET", {
                        "ph" : "id:" + o.id
                    });
                }
            },
        };
        // 默认的快捷命令
        $z.setUndefined(opt, "commands", ["fullscreen", "download", "open"]);

    },
    //...............................................................
    events : {
        // 退出全屏
        'click .exit-fullscreen' : function(e){
            this.arena.removeAttr("fullscreen");
        },
        // 执行命令
        'click header > ul > li[cmd-key]' : function(e){
            var key = $(e.currentTarget).attr("cmd-key");
            var cmd = this.__commands[key];
            if(cmd) {
                if(this.__OBJ)
                    $z.invoke(cmd, "handler", [this.__OBJ], cmd);
            }
        }
    },
    //...............................................................
    redraw : function() {
        var UI  = this;
        var opt = UI.options;

        // 增加更多快捷命令
        /*
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
        */
        if(_.isArray(opt.commands) && opt.commands.length > 0) {
            var jUl = $('<ul>').appendTo(UI.arena.find('>header'));
            for(var i=0; i<opt.commands.length; i++) {
                var cmd = opt.commands[i];
                var key = "cmd_" + i;
                if(_.isString(cmd)) {
                    key = cmd;
                    cmd = UI.__commands[key];
                }
                if(cmd) {
                    var jLi = $('<li>').attr({
                        "cmd-key" : key
                    }).appendTo(jUl);
                    var jB  = $('<b>').attr({
                        "data-balloon-pos" : "left",
                        "data-balloon" : UI.text(cmd.tip)
                    }).html(cmd.icon).appendTo(jLi);
                    // 计入命令集合
                    UI.__commands[key] = cmd;
                }
            }
        }
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