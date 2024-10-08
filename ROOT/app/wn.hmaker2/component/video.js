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

        //console.log("paint", com)

        // 确立 DOM
        var jCon = UI.arena.find(".hmc-video-con");
        if(jCon.length == 0)
            jCon = $('<div class="hmc-video-con">').appendTo(UI.arena.empty());
        else
            jCon.empty();
        
        // 得到视频对象
        var oVideo = com.src ? UI.explain_src(com.src) : null;
        
        // 显示视频预览
        if(oVideo) {
            // 站外视频
            if(_.isString(oVideo)) {
                $('<div m="blank"><i class="fa fa-video"></i></div>').appendTo(jCon.attr({
                    "no-src" : "yes"
                }));
            }
            // 站内视频
            else if(oVideo) {
                var jImg = jCon.find('>img');
                if(jImg.length == 0)
                    jImg = $('<img>').appendTo(jCon);
                
                var vi_src = "/o/read/"+oVideo.videoc_dir+"/_preview.jpg";
                
                // 显示图片
                jImg.attr("src", vi_src);
            }
            // 视频封面
            if(com.poster) {
                var jPoster = $('<div m="poster">').appendTo(jCon)
                jPoster.css({
                    "background-image" : 'url("'+("/o/read/id:"+oHome.id+"/"+com.poster)+'")'
                })
            }

            // 播放图标
            $('<div m="play"><b><i class="zmdi zmdi-play"></i></b></div>').appendTo(jCon);
        }
        // 显示空区域
        else {
            // 确保 src 为空
            if(com.src) {
                com.src = null;
                UI.setData(com);
            }

            $('<div m="blank"><i class="fa fa-video"></i></div>').appendTo(jCon.attr({
                "no-src" : "yes"
            }));
        }

        // 显示静音图标
        if(com.muted) {
            $('<div m="muted"><b><i class="zmdi zmdi-volume-off"></i></b></div>').appendTo(jCon);
        }
        // 显示自动播放图标
        if(com.autoplay) {
            $('<div m="autoplay"><b><i class="zmdi zmdi-flash-auto"></i></b></div>').appendTo(jCon);
        }

        // 确保应用了属性修改
        UI.applyBlock();

    },
    //...............................................................
    // 自定义修改块布局的逻辑
    applyBlockCss : function(cssCom, cssArena) {
        var UI = this;
        // 处理图像的宽高
        //console.log(cssCom)
        var cssSize = {
            "width"  : $D.dom.isUnset(cssCom.width)  ? "" : "100%",
            "height" : $D.dom.isUnset(cssCom.height) ? "" : "100%",
        };

        // 同步内容区域宽高
        UI.arena.find('.hmc-video-con > img').css(cssSize);
        UI.arena.find('.hmc-video-con > div[m="blank"]').css(cssSize);

        // 处理文字区域属性
        UI.$el.css(cssCom);
        UI.arena.css(cssArena);
    },
    //...............................................................
    checkBlockMode : function(block) {
        // 绝对定位的块，必须有宽高
        if(/^(abs|fix)$/.test(block.mode)) {
            // 确保定位模式正确
            if(!block.posBy || "WH" == block.posBy)
                block.posBy = "TLWH";
            // 确保有必要的位置属性
            var css = this.getMyRectCss();
            // 设置
            _.extend(block, this.pickCssForMode(css, block.posBy));
        }
        // inflow 的块，去掉 top/left/bottom/right 的约束
        else if("inflow" == block.mode){
            _.extend(block, {
                top: "", left:"", bottom:"", right:"", 
                posBy : "WH"
            });
        }
        // !!! 不支持
        else {
            throw "unsupport block mode: '" + block.mode + "'";
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