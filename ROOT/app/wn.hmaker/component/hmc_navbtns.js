(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker/component/hmc',
    'ui/menu/menu',
    'ui/form/form'
], function(ZUI, Wn, HMC, MenuUI, FormUI){
//==============================================
var dft_btn_info = function(){
    return {
        btnSrc  : null,
        btnHref : null,
        btnText : null
    };
};
//..............................................
var html = function(){/*
<div class="navbtn">
    <div class="navbtn-img"></div>
    <div class="navbtn-txt"></div>
</div>
*/};
//==============================================
return ZUI.def("app.wn.hmaker_com_navbtns", {
    //...............................................................
    events : {
        "click .hmc-wrapper" : function(e){
            var UI   = this;
            var jCom = $(e.target).closest(".hm-com");
            var jBtn = $(e.target).closest(".navbtn");

            // 第一次点击，控件未激活，那就啥也不做
            if(!jCom.attr("actived"))
                return;

            // 已经激活了，就啥也不做了
            if(jBtn.attr("actived"))
                return;

            // 取消其他的激活按钮
            UI.arena.find(".navbtn[actived]").removeAttr("actived");

            // 准备更新对象
            var btnInfo = dft_btn_info();

            // 点了按钮
            if(jBtn.size() > 0) {
                jBtn.attr("actived", "yes");

                // 获取图片 ID
                var id = UI.objIdBySrc(jBtn.find(".navbtn-img")[0].style.backgroundImage);
                // 如果不是默认图片，那么就记录
                if(id != UI._blank_img().id){
                    btnInfo.btnSrc = {fid:id};
                }
                // 得到按钮的链接
                btnInfo.btnHref = jBtn.attr("href") || "";
                // 得到按钮文字
                btnInfo.btnText = jBtn.find(".navbtn-txt").text();
            }

            // 更新到表单里
            UI.parent.gasket.prop.update(btnInfo);
        }
    },
    //...............................................................
    formatComponentInfo : function(info){
        // 专门给选中按钮用的属性，无视
        delete info.btnHref;
        delete info.btnText;
        return info;
    },
    //...............................................................
    setProperty : function(key, val){
        var UI = this;
        // 禁止外边距
        if("margin" == key)
            val = undefined;

        // 处理当前按钮的
        if(/^btn(Src|Href|Text)$/.test(key)){
            //console.log("update current button:", key, val);
            // 获得高亮按钮
            var jBtn = UI.arena.find(".navbtn[actived]");
            // 设置按钮图片
            if("btnSrc" == key){
                jBtn.find(".navbtn-img").css("background-image", "url("+UI.imgSrc(val)+")");
            }
            // 按钮文字
            else if("btnText" == key) {
                jBtn.find(".navbtn-txt").text(val || UI.msg("hmaker.com.navbtns.nm"));
            }
            // 按钮链接
            else if("btnHref" == key) {
                if(val)
                    jBtn.attr("href", val);
                else
                    jBtn.removeAttr("href");
            }
            // 不要进行后续处理了
            return;
        }

        // 调用通用的设置方法
        HMC.setProperty.apply(UI, [key, val]);
    },
    //...............................................................
    blur : function(){
        this.arena.find(".navbtn[actived]").removeAttr("actived");
        if(this.parent.gasket.prop)
            this.parent.gasket.prop.update(dft_btn_info());
    },
    //...............................................................
    updateStyle : function(info){
        var UI = this;
        var ID = info.ID;

        // 先过滤一遍通用规则
        var styleRules = [];
        info = UI.parent.gen_rules(ID, styleRules, info);

        // 再弄一下自己的规则
        var ruleMain = {
            selector : "#"+ID+" .hmc-main",
            items    : []
        };
        var ruleBtn = {
            selector : "#"+ID+" .hmc-main .navbtn",
            items    : []
        };
        var ruleImg = {
            selector : "#"+ID+" .hmc-main .navbtn-img",
            items    : []
        };
        var ruleTxt = {
            selector : "#"+ID+" .hmc-main .navbtn-txt",
            items    : []
        };
        for(var key in info){
            if("ID" == key)
                continue;
            // 取得值并生产 style
            var val = info[key];

            // 纯粹是显示文字的
            if("showBtnText" == key){
                if(val)
                    UI.$el.removeAttr("hide-btn-text");
                else
                    UI.$el.attr("hide-btn-text", "yes");
            }
            // 属于按钮的图片
            else if(/^btn(Width|Height|BorderRadius)/.test(key)){
                key = $z.lowerFirst(key.substring(3));
                var ru = UI.parent.gen_rule_item(key, val)
                // 按钮文字采用的是宽度
                if("width" == key){
                    ruleTxt.items.push(ru);
                }
                ruleImg.items.push(ru);
            }
            // 属于按钮的
            else if(/^btn/.test(key)){
                key = $z.lowerFirst(key.substring(3));
                var ru = UI.parent.gen_rule_item(key, val)
                ruleBtn.items.push(ru);
            }
            // 剩下的就是主体的
            else {
                var ru = UI.parent.gen_rule_item(key, val)
                ruleMain.items.push(ru);
            }
        }
        styleRules.push(ruleMain, ruleBtn, ruleImg, ruleTxt);

        // 应用样式规则
        UI.parent.updateComStyle(UI.$el, styleRules);
    },
    //...............................................................
    checkDom : function(){
        var UI = this;
        var jM = UI.arena.find(".hmc-main");

        // 标记禁止默认鼠标事件
        UI.$el.attr("mouse-prevent-default", "yes");

        // 检查所有的按钮的 DOM 是否合法
        jM.find(".navbtn").each(function(){
            UI._check_btn_com(this);
        });
        
    },
    //...............................................................
    // 检查一个按钮的 DOM 结构是否合法
    _check_btn_com : function(ele){
        var UI   = this;
        var jBtn = $(ele).closest(".navbtn");

        // 检查图片
        var jImg = jBtn.find(".navbtn-img");
        if(!jImg[0].style.backgroundImage)
            jImg[0].style.backgroundImage = "url(" + UI.imgSrc() + ")";

        // 检查文字
        var jTxt = jBtn.find(".navbtn-txt");
        if(!$.trim(jTxt.text())){
            jTxt.text(UI.msg("hmaker.com.navbtns.nm"));
        }
        return jBtn;
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
                className : "hmaker-prop-compactly",
                uiWidth : "all",
                autoLineHeight : true,
                cols:2,
                fields : [{
                    key    : "showBtnText",
                    title  : "i18n:hmaker.com.navbtns.showBtnText",
                    type   : "boolean",
                    dft    : true,
                    span   : 2,
                    editAs : "switch"
                }, {
                    key    : "color",
                    title  : "i18n:hmaker.cprop.color",
                    type   : "string",
                    span   : 2,
                    nullAsUndefined : true,
                    editAs : "color",
                    uiConf : UI.parent.getColorConf()
                }, {
                    key    : "backgroundColor",
                    title  : "i18n:hmaker.cprop.backgroundColor",
                    type   : "string",
                    span   : 2,
                    nullAsUndefined : true,
                    editAs : "color",
                    uiConf : UI.parent.getColorConf()
                }, {
                    key    : "btnWidth",
                    title  : "i18n:hmaker.com.navbtns.btnWidth",
                    type   : "int",
                    uiConf : {unit : "px"}
                }, {
                    key    : "btnHeight",
                    title  : "i18n:hmaker.com.navbtns.btnHeight",
                    type   : "int",
                    uiConf : {unit : "px"}
                }, {
                    key    : "btnBorderRadius",
                    title  : "i18n:hmaker.com.navbtns.btnBorderRadius",
                    type   : "int",
                    uiConf : {unit : "px"}
                }, {
                    key    : "btnPadding",
                    title  : "i18n:hmaker.com.navbtns.btnPadding",
                    type   : "int",
                    uiConf : {unit : "px"}
                }]
            }, {
                title   : 'i18n:hmaker.com.navbtns.forTheBtn',
                uiWidth : "all",
                fields  : [{
                    key    : "btnSrc",
                    title  : "i18n:hmaker.com.navbtns.btnSrc",
                    type  : "object",
                    uiType : "ui/picker/opicker",
                    uiConf : UI.parent.getImagePickerConf()
                }, {
                    key    : "btnHref",
                    title  : "i18n:hmaker.com.navbtns.btnHref",
                    type   : "string",
                    editAs : "link",
                    uiConf : UI.parent.getLinkHrefConf()
                }, {
                    key    : "btnText",
                    title  : "i18n:hmaker.com.navbtns.btnText",
                    type   : "string"
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
            $pel    : opt.$menu,
            context : UI,
            tipDirection : "up",
            setup   : [{
                icon : '<i class="fa fa-plus"></i>',
                tip  : "i18n:hmaker.com.navbtns.add_btn",
                handler : function(){
                    var UI   = this;
                    var jM   = UI.arena.find(".hmc-main");
                    var jBtn = $($z.getFuncBodyAsStr(html,true));
                    UI._check_btn_com(jBtn).appendTo(jM).click();
                }
            }, {
                icon : '<i class="fa fa-close"></i>',
                tip  : "i18n:hmaker.com.navbtns.del_btn",
                handler : function(){
                    var UI   = this;
                    var jBtn = UI.arena.find(".navbtn[actived]");
                    if(jBtn.size() == 0){
                        alert(UI.msg("hmaker.com.navbtns.e_nobtn"));
                        return;
                    }
                    jBtn.remove();
                }
            }, {
                icon : '<i class="fa fa-long-arrow-left"></i>',
                tip  : "i18n:hmaker.com.navbtns.mv_btn_prev",
                handler : function(){
                    var UI   = this;
                    var jBtn = UI.arena.find(".navbtn[actived]");
                    if(jBtn.size() == 0){
                        alert(UI.msg("hmaker.com.navbtns.e_nobtn"));
                        return;
                    }
                    if(jBtn.prev().size()>0){
                        jBtn.insertBefore(jBtn.prev());
                    }
                }
            }, {
                icon : '<i class="fa fa-long-arrow-right"></i>',
                tip  : "i18n:hmaker.com.navbtns.mv_btn_next",
                handler : function(){
                    var UI   = this;
                    var jBtn = UI.arena.find(".navbtn[actived]");
                    if(jBtn.size() == 0){
                        alert(UI.msg("hmaker.com.navbtns.e_nobtn"));
                        return;
                    }
                    if(jBtn.next().size()>0){
                        jBtn.insertAfter(jBtn.next());
                    }
                }
            }]
        }).render(function(){
            //console.log(this.parent.uiName);
        });
        
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);