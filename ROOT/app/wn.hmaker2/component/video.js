(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/support/hm__methods_com'
], function(ZUI, Wn, HmComMethods){
//==============================================
var html = `<div class="ui-arena hmc-video hm-del-save"></div>`;
//==============================================
return ZUI.def("app.wn.hm_com_video", {
    dom     : html,
    keepDom : false,
    tagName : 'A',
    className : '!hm-com-video',
    //...............................................................
    init : function(){
        HmComMethods(this);
    },
    //...............................................................
    events : {
        "dragstart video" : function(e){
             e.preventDefault();
        },
        // 禁止任何默认行为
        "click *" : function(e) {
            e.preventDefault();
        }
    },
    //...............................................................
    depose : function() {
        this.arena.children("img").off();
        this.$el.find(".hmc-image-link-tip").remove();
    },
    //...............................................................
    paint : function(com) {
        var UI = this;
        var oHome = UI.getHomeObj();
        var jCon = $('<div class="hmc-video-con">').appendTo(UI.arena.empty());
        
        // 加载预览图
        if(com.src) {
            var imgSrc;
            // 预览图
            if(com.poster) {
                imgSrc = "/o/read/id:"+oHome.id+"/"+com.poster;
            }
            // 用视频的预览图
            else {
                var oVideo = Wn.fetch(Wn.appendPath(oHome.ph, com.src));
                imgSrc = "/o/read/"+oVideo.videoc_dir+"/_preview.jpg";
            }
            // 显示图片
            var jVid = $('<img>').appendTo(jCon);
            jVid.attr("src", imgSrc);
            // 播放图标
            $('<div m="play"><i class="zmdi zmdi-play"></i></div>').appendTo(jCon);
        }
        // 显示空区域
        else {
            $('<div m="blank"><i class="fa fa-video"></i></div>').appendTo(jCon.attr({
                "no-src" : "yes"
            }));
        }

        // 显示静音图标
        if(com.muted) {
            $('<div m="muted"><i class="zmdi zmdi-volume-off"></i></div>').appendTo(jCon);
        }
        // 显示自动播放图标
        if(com.autoplay) {
            $('<div m="autoplay"><i class="zmdi zmdi-flash-auto"></i></div>').appendTo(jCon);
        }

    },
    //...............................................................
    getBlockPropFields : function(block) {
        return [
            "margin", "padding", "border","background",
            "boxShadow","borderRadius"
        ];
    },
    //...............................................................
    // 返回属性菜单， null 表示没有属性
    getDataProp : function(){
        return {
            uiType : 'app/wn.hmaker2/com_prop/video_prop',
            uiConf : {}
        };
    },
    //...............................................................
    getDefaultData : function(){
        return {};
    },
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);