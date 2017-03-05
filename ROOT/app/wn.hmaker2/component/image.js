(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/support/hm__methods_com'
], function(ZUI, Wn, HmComMethods){
//==============================================
var html = '<div class="ui-arena hmc-image hm-del-save"><img></div>';
//==============================================
return ZUI.def("app.wn.hm_com_image", {
    dom     : html,
    keepDom : false,
    tagName : 'A',
    className : '!hm-com-image',
    //...............................................................
    init : function(){
        HmComMethods(this);
    },
    //...............................................................
    events : {
        "dragstart img" : function(e){
             e.preventDefault();
        },
        // 重置图片的原始宽度
        "dblclick img" : function(e){
            console.log("dblclick img")
            this.saveBlock(null, {
                "width"   : "unset",
                "height"  : "unset",
            });
        }
    },
    //...............................................................
    depose : function() {
        this.arena.children("img").off();
        this.$el.find(".hmc-image-link-tip").remove();
    },
    //...............................................................
    paint : function(com) {
        //console.log("image.paint", com);
        var UI   = this;
        var jW   = UI.$el.children(".hm-com-W");
        var jImg = UI.arena.children("img");

        // 这个是空白图片
        var BLANK_IMG = '/a/load/wn.hmaker2/img_blank.jpg';
        //console.log(com)
                
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 指定链接: 要显示链接提示图标
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        var jLinkTip = jW.children('.hmc-image-link-tip');
        if(com.href) {
            if(jLinkTip.length == 0) {
                $(`<div class="hmc-image-link-tip hm-del-save">
                    <i class="zmdi zmdi-link"></i>
                </div>`).appendTo(jW);
            }
        }
        // 清除
        else {
            jLinkTip.remove();
        }
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 更新图片的样式
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        var src  = '';
        if(com.src) {
            // 指定了绝对路径
            if(/^https?:\/\//i.test(com.src)) {
                src = com.src
            }
            // 指定了文件对象
            else if(/^id:[\w\d]+/.test(com.src)) {
                // 试图检查一下这个对象是否存在
                var re = Wn.exec('obj ' + com.src);
                // 不存在
                if(/^e./.test(re)) {
                    src = BLANK_IMG;
                    com.src = null;
                }
                // 读取 src
                else {
                    src = '/o/read/' + com.src;
                }
            }
            // 指定了相对站点的路径
            else if(/^\//.test(com.src)){
                var oHome = this.getHomeObj();
                var src = "id:" + oHome.id + com.src;
                // 试图检查一下这个对象是否存在
                var re = Wn.exec('obj ' + src);
                // 不存在
                if(/^e./.test(re)) {
                    src = BLANK_IMG;
                    com.src = null;
                }
                // 读取 src
                else {
                    src = '/o/read/' + src;
                }
            }
            // 默认是指定了相对页面的路径
            else {
                var oPage = this.pageUI().getCurrentEditObj();
                var src = "id:" + oPage.pid + com.src;
                // 试图检查一下这个对象是否存在
                var re = Wn.exec('obj ' + src);
                // 不存在
                if(/^e./.test(re)) {
                    src = BLANK_IMG;
                    com.src = null;
                }
                // 读取 src
                else {
                    src = '/o/read/' + src;
                }
            }
            //  如果归零了 com.src 更新一下
            if(!com.src)
                this.setData(com);
        }
        //console.log(jImg.attr("src"), src)
        // 如果 src 发生变更，重新加载图片后，应该重新设置图片控件宽高
        if(src != jImg.attr("src")) {
            // 开始加载图片
            if(src) {
                UI.showLoading();
                // 隐藏图片
                jImg.css("visibility","hidden");
            }
            // 附加 load 事件
            jImg.one("load", function(){
                //console.log("reset img w/h")
                jImg.css("visibility","");
                UI.hideLoading();
                // 加载完毕，重新应用一下块属性
                UI.applyBlock();
            });
            // 触发图片的内容改动
            jImg.attr("src", src || BLANK_IMG);
        }

        // 图片拉伸方式
        var fit = "";
        if(com.objectFit && "fill" != com.objectFit) {
            fit = com.objectFit;
        }
        jImg.css("objectFit", fit);

        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 准备更新文本样式
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        if(com.text) {
            // 设置文本显示
            var jTxt = UI.arena.children("section");
            if(jTxt.length == 0) {
                jTxt = $('<section>').appendTo(UI.arena);
            }
            jTxt.text(com.text);
        }
        // 标记不显示文本
        else {
            UI.arena.children("section").remove();
        }

    },
    //...............................................................
    // 自定义修改块布局的逻辑
    applyBlockCss : function(css) {
        var UI   = this;
        
        var cssCom   = $z.pick(css, /^(position|top|left|right|bottom|margin)$/);
        var cssArena = $z.pick(css, /^(borderRadius|boxShadow)$/);
        var cssImg   = $z.pick(css, /^(border|borderRadius|width|height)$/);
        var cssTxt   = $z.pick(css, "!^(position|top|left|right|bottom|margin|border|borderRadius|boxShadow|width|height)$");
        //console.log(cssTxt);

        this.$el.css(this.formatCss(cssCom, true));
        this.arena.css(this.formatCss(cssArena, true))
            .find("img").css(this.formatCss(cssImg, true));
        this.arena.find("section").css(this.formatCss(cssTxt, true));

    },
    //...............................................................
    checkBlockMode : function(block) {
        // 绝对定位的块，必须有宽高
        if("abs" == block.mode) {
            // 确保定位模式正确
            if(!block.posBy || "WH" == block.posBy)
                block.posBy = "TLWH";
            // 确保有必要的位置属性
            var css = this.getMyRectCss();
            // 设置
            _.extend(block, this.pickCssForMode(css, block.posBy));
        }
        // inflow 的块，高度应该为 auto
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
        return [block.mode == 'inflow' ? "margin" : null,
                "padding","border","borderRadius", "boxShadow",
                $z.tmpl("@t-pos({{tt}})[{{N}}=top,{{P}}=center,{{S}}=bottom]=bottom")({
                    tt : this.msg("hmaker.com.image.text_pos"),
                    N  : this.msg("hmaker.com.image.text_pos_N"),
                    P  : this.msg("hmaker.com.image.text_pos_P"),
                    S  : this.msg("hmaker.com.image.text_pos_S"),
                }),
                "_align", "fontFamily","_font","fontSize",
                "color", "background",
                "lineHeight","letterSpacing","textShadow"];
    },
    //...............................................................
    // 返回属性菜单， null 表示没有属性
    getDataProp : function(){
        return {
            uiType : 'app/wn.hmaker2/com_prop/image_prop',
            uiConf : {}
        };
    },
    //...............................................................
    getDefaultData : function(){
        return {};
    },
    //...............................................................
    getDefaultBlock : function(){
        return {
            mode : "abs",
            posBy   : "TLWH",
            //posBy   : "top,left,width,height",
            //posVal  : "10px,10px,200px,200px",
            top     : "10px",
            left    : "10px",
            width   : "unset",
            height  : "unset",
            padding : "",
            border : "" ,   // "1px solid #000",
            borderRadius : "",
            overflow : "",
            blockBackground : "",
        };
    },
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);