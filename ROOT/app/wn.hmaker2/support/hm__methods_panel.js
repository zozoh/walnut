define(function (require, exports, module) {
var methods = {
    // 设置面板标题
    setTitle : function(titleKey) {
        this.arena.find(">header .hmpn-tt").html(this.msg(titleKey));
    },
    // 更新皮肤选择框
    updateSkinBox : function(jBox, skin, getSkinText, cssSelectors, setSelectors){
        var UI = this;
        jBox = $(jBox).closest(".hm-skin-box");
        // 确保有回调
        getSkinText = getSkinText || jBox.data("getSkinText");
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 清空选区
        jBox.attr("skin-selector", skin||"")
            .html(UI.compactHTML(`<span class="com-skin">
                <i class="zmdi zmdi-texture"></i><b></b>
            </span><span class="page-css">
                <i class="fa fa-css3"></i><b></b>
            </span>`));
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 显示文字
        var jSkinTxt = jBox.find(">.com-skin>b").attr("skin-none", skin ? null : "yes");
        // 选择皮肤样式名
        if(skin){
            jSkinTxt.text(getSkinText.call(this, skin));
        }
        // 显示默认
        else{
            jSkinTxt.text(this.msg("hmaker.prop.skin_none"));
        }
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 记录获取 Text 的回调
        if(_.isFunction(getSkinText))
            jBox.data("getSkinText", getSkinText);
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 还要更新显示页面的样式
        if(!_.isUndefined(cssSelectors)) {
            UI.updateSkinBoxCssSelector(jBox, cssSelectors);
        }
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 记录设置选择器的回调
        if(_.isFunction(setSelectors))
            jBox.data("setSelectors", setSelectors);
        
    },
    // 更新 jBox 的 cssSelector
    updateSkinBoxCssSelector : function(jBox, cssSelectors) {
        var jSpan   = jBox.find(">.page-css");
        var jCssTxt = jSpan.find(">b");
        // 必须是 Array
        if(!_.isArray(cssSelectors)){
            cssSelectors = $z.splitIgnoreEmpty(cssSelectors, /[ \t]+/);
        }
        jCssTxt.text(cssSelectors.length||"");
        jBox.attr("css-selectors", cssSelectors.join(" "));
    },
    // 显示皮肤下拉列表
    showSkinList : function(jBox, skinList, callback){
        var UI = this;
        jBox = $(jBox).closest(".hm-skin-box");
        var skin = jBox.attr("skin-selector") || "";
        
        // 准备绘制
        var jList = $('<div class="hm-skin-list">');

        // 站点没设置皮肤
        if(!_.isArray(skinList)){
            jList.attr("warn","unset").html(UI.msg("hmaker.prop.skin_unset"));
        }
        // 没有可用样式
        else if(skinList.length == 0) {
            jList.attr("warn","empty").html(UI.msg("hmaker.prop.skin_empty"));
        }          
        // 绘制项
        else {
            var jUl = $('<ul>').appendTo(jList);
            
            // 绘制第一项
            $('<li class="skin-none">').text(UI.msg("hmaker.prop.skin_none")).attr({
                "value"   : "",
                "checked" : !skin ? "yes" : null
            }).appendTo(jUl);

            // 循环绘制其余项目
            for(var si of skinList) {
                $('<li>').text(si.text).attr({
                    "value"   : si.selector,
                    "checked" : si.selector == skin ? "yes" : null
                }).appendTo(jUl);
            }
        }

        // 最后显示出来
        var jMask = $('<div class="hm-skin-mask"></div>').appendTo(jBox);
        jBox.append(jList);
        
        // 确保停靠在正确的位置
        $z.dock(jBox, jList, "H");
                
        // 响应事件: 取消
        var do_hide = function(e){
            e.stopPropagation();
            $(document).off("keyup", do_hide);
            jBox.find(".hm-skin-mask").off().remove();
            jBox.find(".hm-skin-list").off().remove();
        };
        jMask.on("click", do_hide);
        
        // 响应事件: 取消（键盘)
        $(document).on("keyup", do_hide);
        
        // 响应事件: 选中
        jList.on("click", "li", function(e){
            // 得到选中的 skin
            var skin = $(e.currentTarget).attr("value");
            
            // 设置显示的值
            UI.updateSkinBox(jBox, skin);
            
            // 执行回调
            $z.doCallback(callback, [skin]);
            
            // 取消
            do_hide(e);
        });
        
    },
    // 显示 cssSelector 的列表
    showCssSelectorList : function(jBox) {
        var UI = this;
        jBox = $(jBox).closest(".hm-skin-box");
        var jSpan = jBox.find(">.page-css");
        var selectors = jBox.attr("css-selectors") || "";
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 不要重复创建
        if(jSpan.children(".css-current").length > 0)
            return;
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 初始化 DOM 结构
        var jCurrent = $(UI.compactHTML(`<div class="css-current">
            <header>{{hmaker.prop.css_tt}} <i class="fa fa-css3"></i></header>
            <section><ul></ul></section>
            <footer>
                <b a="edit">{{hmaker.prop.css_edit}}</b>
                <b a="ok">{{hmaker.prop.css_edit_ok}}</b>
                <b a="cancel">{{hmaker.prop.css_edit_cancel}}</b>
            </footer>
        </div>`));
        var jUl = jCurrent.find("section>ul");
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 声明创建 LI 的函数
        var gen_LI = function(selector, text, path) {
            var jLi = $('<li>').attr("path", path);
            $('<b>').text(selector).appendTo(jLi);
            if(text) {
                $('<em>').text(text).appendTo(jLi);
            }
            var pos = path.lastIndexOf('/');
            var fnm = pos > 0 ? path.substring(pos+1) : path;
            jLi.attr("fnm", fnm);
            // $('<span class="del"><i class="zmdi zmdi-close"></i></span>')
            //     .appendTo(jLi);
            return jLi;
        };
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 循环添加
        var pageUI = UI.pageUI();
        selectors = $z.splitIgnoreEmpty(selectors, /[ \t]+/);
        if(selectors.length > 0) {
            for(var i=0; i<selectors.length; i++) {
                var s = selectors[i];
                var t = pageUI.getCssSelectorText(s);
                var p = pageUI.getCssSelectorPath(s);
                gen_LI(s,t,p).appendTo(jUl);
            }
        }
        // 显示没有内容
        else {
            jUl.attr("empty","yes").html(UI.msg("hmaker.prop.css_none"));
        }

        // 记入 DOM
        jCurrent.appendTo(jSpan);
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 进入编辑模式
        jSpan.on("click", '>.css-current>footer>b[a="edit"]', function(){
            // 标识编辑模式
            jCurrent.attr("edit", "yes");

            // 绘制遮罩
            $('<div class="hm-skin-mask">').insertBefore(jCurrent);

            // 将原来的列表对象变成 fix 模式浮动
            var cu_css = $z.rect_relative($z.rect(jCurrent,false,true), $z.winsz(), true);
            jCurrent.css(_.extend(_.pick(cu_css, "top","right"), {
                "position" : "fixed",
                "left":"", "bottom":"", "width":"", "height":"",
            }));

            // 绘制全部选择框
            var jAll = $('<div class="css-all"></div>');
            var map = UI.pageUI().getCssSelectors();

            // 没有可选规则
            if(_.isEmpty(map)) {
                jAll.attr("nolinks", "yes").text(UI.msg("hmaker.prop.css_nolinks"));
            }
            // 有可选规则 
            else {
                for(var key in map) {
                    // 标题
                    var jH4 = $('<h4>').appendTo(jAll);
                    jH4.text(key);
                    // 列表
                    var list = map[key];
                    var jUl = $('<ul>').attr("key", key).appendTo(jAll);
                    var count = 0;
                    for(var i=0; i<list.length; i++ ) {
                        var so = list[i];
                        // 没有被使用的选择器会被加入
                        if(selectors.indexOf(so.selector)<0) {
                            gen_LI(so.selector, so.text, key).appendTo(jUl);
                            count ++;
                        }
                    }
                    // 如果为空的话
                    if(count == 0) {
                        jUl.attr("empty", "yes")
                            .text(UI.msg("hmaker.prop.css_none"));
                    }
                }
            }

            // 加入 DOM
            jAll.insertBefore(jCurrent);

            // 确保 jCurrent 不比 jAll 窄
            if(jCurrent.outerWidth() < jAll.outerWidth())
                jCurrent.css("width", jAll.outerWidth());

            // 停靠
            $z.dock(jCurrent, jAll, "V");

            // 确保不超过边界
            var viewport = $z.winsz();
            var rect = $z.rect(jAll, true);
            var rect2 = $z.rect_clip_boundary(rect, viewport);
            jAll.css($z.rectObj(rect2, "top,left,height"));

        });
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 放弃
        var do_hide = function(e){
            e.stopPropagation();
            $(document).off("keyup", do_hide);
            UI.hideCssSelectorList(jBox);
        };
        jSpan.on("click", '>.css-current>footer>b[a="cancel"]', do_hide);
        jSpan.on("click", '>.hm-skin-mask', do_hide);
        // 响应事件: 取消（键盘)
        $(document).on("keyup", do_hide);
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 添加到
        jSpan.on("click", '>.css-all li', function(e){
            var jLi = $(e.currentTarget);
            var jMyUl = jLi.parent();
            $z.removeIt(jLi, {
                appendTo : jUl,
                before : function(){
                    if(jUl.attr("empty")){
                        jUl.removeAttr("empty").empty();
                    }
                },
                // 闪烁一下目标对象
                remove : function(){
                    $z.blinkIt(jLi);
                },
                // 如果源没有内容了，标识一下
                after : function() {
                    if(jMyUl.children().length == 0) {
                        jMyUl.attr("empty", "yes")
                            .text(UI.msg("hmaker.prop.css_none"));
                    }
                }
            });
        });
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 移除
        jSpan.on("click", '>.css-current[edit] section li', function(e){
            console.log("hahah")
            var jLi   = $(e.currentTarget);
            var path  = jLi.attr("path");
            var jTaUl = jSpan.find('>.css-all ul[key="'+path+'"]');
            $z.removeIt(jLi, {
                appendTo : jTaUl,
                before : function(){
                    if(jTaUl.attr("empty")){
                        jTaUl.removeAttr("empty").empty();
                    }
                },
                // 闪烁一下目标对象
                remove : function(){
                    $z.blinkIt(jLi);
                },
                // 如果源没有内容了，标识一下
                after : function(){
                    if(jUl.children().length == 0) {
                        jUl.attr("empty", "yes")
                            .text(UI.msg("hmaker.prop.css_none"));
                    }
                }
            });
        });
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 确定修改
        jSpan.on("click", '>.css-current[edit]>footer>b[a="ok"]', function(e){
            // 得到全部选择器
            var list = [];
            jCurrent.find("section li").each(function(){
                var jLi = $(this);
                list.push(jLi.find("b").text());
            });
            // 得到类选择器字符串 
            var cssSelectors = list.join(" ");
            //console.log(cssSelectors);
            // 隐藏
            do_hide(e);
            // 更新属性面板的 css 选择器缓存
            UI.updateSkinBoxCssSelector(jBox, cssSelectors);
            // 调用回调
            var setSelectors = jBox.data("setSelectors");
            $z.doCallback(setSelectors, [cssSelectors], jBox);
            
        });
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 停靠
        $z.dock(jSpan, jCurrent, "V");
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    },
    // 消除 cssSelector 的列表
    hideCssSelectorList : function(jBox) {
        jBox = $(jBox).closest(".hm-skin-box");
        var jSpan = jBox.find(">.page-css");
        jSpan.off().find(">div").remove();
    }
}; // ~End methods
//====================================================================
// 得到 HMaker 所有 UI 对象的方法
var HmMethods = require("app/wn.hmaker2/support/hm__methods");

//====================================================================
// 输出
module.exports = function(uiPanel){
    return _.extend(HmMethods(uiPanel), methods);
};
//=======================================================================
});