(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker/component/hmc',
    'ui/menu/menu',
    'ui/form/form'
], function(ZUI, Wn, HMC, MenuUI, FormUI){
//==============================================
return ZUI.def("app.wn.hmaker_com_image", {
    //...............................................................
    events : {
        "dblclick .hmc-wrapper" : function(e){
            var UI  = this;
            var src = UI.arena.find("img").prop("src");
            var id = HMC.objIdBySrc(src);
            if(!id)
                return;
            var o  = Wn.getById(id);
            if(o.width && o.height){
                UI.setProperty({
                    "width"  : o.width,
                    "height" : o.height
                });
            }
        }
    },
    //...............................................................
    updateStyle : function(info){
        var UI = this;
        var ID = info.ID;

        // 确保是有 src 属性的
        if(!info.src){
            info.src = null
        }

        // 先过滤一遍通用规则
        var styleRules = [];
        info = UI.parent.gen_rules(ID, styleRules, info);

        // 再弄一下自己的规则
        var rule = {
            selector : "#"+ID+" .hmc-main img",
            items    : []
        };
        for(var key in info){
            if("ID" == key)
                continue;
            // 取得值
            var val = info[key];
            // src 的话，给 image 显示上
            if("src" == key ){
                UI.arena.find("img").prop("src", UI.imgSrc(val));
            }
            // 其他的设置到 style 里
            else{
                var ru  = UI.parent.gen_rule_item(key, val);
                rule.items.push(ru);
            }
        }
        styleRules.push(rule);

        // 应用样式规则
        UI.parent.updateComStyle(UI.$el, styleRules);
    },
    //...............................................................
    checkDom : function(){
        var UI = this;
        var jM = UI.arena.find(".hmc-main");
        var jImg = jM.children("img");
        if(jImg.size() == 0){
            jImg = $('<img>').appendTo(jM);
        }
        if(!jImg.prop("src")){
            jImg.prop("src", UI.imgSrc());
        }
    },
    //...............................................................
    redraw : function(){
        var UI  = this;
        var opt = UI.options;

        // 确保 DOM 结构合法
        UI.checkDom();

        // 标题
        opt.$title.html(opt.titleHtml);

        // 获得属性
        var info = UI.parent.getComponentInfo(UI.$el);

        new FormUI({
            $pel   : opt.$prop,
            fields : [opt.propSetup, {
                title  : 'i18n:hmaker.cprop_special',
                uiWidth : "all",
                fields : [{
                    key    : "src",
                    title  : "i18n:hmaker.com.image.src",
                    type  : "object",
                    uiType : "ui/picker/opicker",
                    uiConf : UI.parent.getImagePickerConf()
                }, {
                    key    : "borderRadius",
                    title  : "i18n:hmaker.com.image.borderRadius",
                    type   : "int",
                    uiConf : {unit : "px"}
                }]
            }],
            on_change : function(key, val) {
                // console.log("detect form change: ", key, val);
                UI.setProperty(key, val);
            }
        }).render(function(){
            //console.log(this.parent.uiName);
            this.setData(info);
        });

        // 菜单
        new MenuUI({
            $pel   : opt.$menu,
            setup  : []
        }).render(function(){
            //console.log(this.parent.uiName);
        });
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);