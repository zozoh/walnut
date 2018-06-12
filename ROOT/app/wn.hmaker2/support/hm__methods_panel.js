define(function (require, exports, module) {
var methods = {
    // 设置面板标题
    setTitle : function(titleKey) {
        this.arena.find(">header .hmpn-tt").html(this.msg(titleKey));
    },
    /*..............................................
    更新链接编辑框
    */
    setLinkToBox : function(jBox, str) {
        var jIcon = jBox.find('.cel-icon');
        var jHref = jBox.find('.cel-href');

        // 记录值
        str = $.trim(str);
        jBox.data("@LINK", str);

        // 解析值
        var data = $z.parseHref(str, true);
        
        // 无数据
        if(!data) {
            jIcon.html('<i class="fa fa-unlink"></i>');
            jHref.text(this.msg('hmaker.link.none'));
            jBox.attr('link-type', "none");
        }
        // 动作
        else if(data.invoke) {
            //jIcon.html('<i class="fas fa-i-cursor"></i>');
            //jIcon.html('<i class="fab fa-js"></i>');
            jIcon.html('<i class="zmdi zmdi-turning-sign"></i>');
            jHref.text(data.value);
            jBox.attr('link-type', "action");
        }
        // 超链接
        else {
            jIcon.html('<i class="fa fa-link"></i>');
            jHref.text(data.value);
            jBox.attr('link-type', "href");
        }
    },
    getLinkFromBox : function(jBox) {
        return jBox.data("@LINK");
    },
    /*..............................................
     更新皮肤选择框
      - jBox : 选择器盒子
      - opt : {
            noneSkinText : null,                  // 「选」无皮肤时显示什么
            skin         : "xxx",                 // 皮肤
            getSkinText  : {this}F(skin):皮肤名称, // 如何获得皮肤显示名
            cssSelectors : [..]                   // 现有的样式选择器
            setSelectors : {jBox}F(newSelector)   // 如何修改样式选择器 
        }
    */
    updateSkinBox : function(jBox, opt){
        var UI = this;
        jBox = $(jBox).closest(".hm-skin-box").empty();
        // 确保参数
        opt = opt || {};
        if(_.isString(opt)) {
            opt = {skin : opt};
        }
        var skin         = opt.skin;
        var noneSkinText = opt.noneSkinText 
                            || jBox.data("noneSkinText")
                            || UI.msg("hmaker.prop.skin_none");
        var getSkinIcon  = opt.getSkinIcon || jBox.data("getSkinIcon");
        var getSkinText  = opt.getSkinText || jBox.data("getSkinText");
        var cssSelectors = opt.cssSelectors;
        var setSelectors = opt.setSelectors;
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 记录 skin
        jBox.attr("skin-selector", skin || null);
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 清空选区
        var jSkBox  = $('<span class="com-skin">')
                        .html(_.isFunction(getSkinIcon) 
                               ? getSkinIcon.call(this, skin)
                               : '<i class="zmdi zmdi-texture"></i>')
                            .appendTo(jBox);
        var jCssBox = $('<span class="page-css">')
                        .html('<i class="fa fa-css3"></i><b></b>')
                            .appendTo(jBox);
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 显示择皮肤样式文字
        $('<b>').appendTo(jSkBox)
            .attr("skin-none", skin ? null : "yes")
            .text(skin ? getSkinText.call(this, skin)
                       : noneSkinText);
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 记录获取 Text 的回调
        jBox.data("noneSkinText", noneSkinText);
        if(_.isFunction(getSkinText))
            jBox.data("getSkinText", getSkinText);
        if(_.isFunction(getSkinIcon))
            jBox.data("getSkinIcon", getSkinIcon);
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
            $('<li class="skin-none">').attr({
                "value"   : "",
                "checked" : !skin ? "yes" : null
            }).text(jBox.data("noneSkinText"))
                .appendTo(jUl);

            // 循环绘制其余项目
            for(var si of skinList) {
                //console.log(si.selector, "["+skin+"]")
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
            if(path) {
                var pos = path.lastIndexOf('/');
                var fnm = pos > 0 ? path.substring(pos+1) : path;
                jLi.attr("fnm", fnm);
                // $('<span class="del"><i class="zmdi zmdi-close"></i></span>')
                //     .appendTo(jLi);
                return jLi;
            }
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
                var jLi = gen_LI(s,t,p);
                if(jLi)
                    jLi.appendTo(jUl);
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
            var cu_css = $D.rect.relative($D.rect.gen(jCurrent,false,true), $z.winsz(), true);
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
                            var jLi = gen_LI(so.selector, so.text, key);
                            if(jLi)
                                jLi.appendTo(jUl);
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
            var rect = $D.rect.gen(jAll, true);
            var rect2 = $D.rect.boundaryIn(rect, viewport);
            jAll.css($z.pick(rect2, "top,left,height"));

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
    },
    // 解析字符串描述的列表值
    // @str 格式为 "文字[=值]?", 譬如 "黑色=black,白色=white"
    parseStringItems : function(str) {
        // 解析值列表
        var items = [];
        var ss = str.split(/[ ,\n\r\t]+/);
        for(var i=0; i<ss.length; i++) {
            var s = $.trim(ss[i]);
            var pos = s.indexOf('=');
            var it;
            if(pos > 0) {
                it = {
                    text  : $.trim(s.substring(0, pos)),
                    value : $.trim(s.substring(pos+1))
                };
            } else {
                it = {
                    text  : s,
                    value : s
                };
            }
            // 如果值为数字形式，变成数字
            if(/^-?[0-9.]+$/.test(it.value)) {
                it.value = it.value * 1;
            }
            // 计入
            items.push(it);
        }
        // 搞定收工
        return items;
    },
    //...............................................................
    // 根据一个 JSON 对象，来生成一个 form 控件的字段配置信息
    // 具体支持什么格式的参数，文档上有描述 hmc_dynamic.md
    _eval_form_fields_by_dsetting : function(setting) {
        var UI = this;
        var re = [];

        // 解析参数
        var flds = HmRT.parseSetting(setting || {});

        // 循环输出表单字段配置信息
        for(var i=0; i<flds.length; i++) {
            var F = flds[i];
            //console.log(F)
            // 准备字段
            var fld = {
                key      : F.key,
                title    : F.title || F.key,
                tip      : F.tip,
                dft      : F.dft,
                required : F.required,
                uiWidth  : F.uiWidth,
            };

            // 默认增加 key 的说明
            if(!fld.key_tip && F.key != F.title) {
                fld.key_tip = F.key;
            }

            // 字段: thingset
            if("thingset" == F.type) {
                fld.uiWidth = "all";
                fld.uiType = "@droplist";
                fld.uiConf = {
                    emptyItem : {},
                    items : "obj -mine -match \"tp:'thing_set'\" -json -l -sort 'nm:1' -e '^(id|nm|title)'",
                    icon  : '<i class="fa fa-cubes"></i>',
                    text  : function(o){
                        return o.title || o.nm;
                    },
                    value : function(o){
                        return o.id;
                    }
                };
            }
            // 字段: TSS
            else if("TSS" == F.type) {
                fld.uiWidth = "all";
                fld.uiType = "@droplist";
                fld.uiConf = {
                    multi : true,
                    items : "obj -mine -match \"tp:'thing_set'\" -json -l -sort 'nm:1' -e '^(id|nm|title)'",
                    icon  : '<i class="fa fa-cubes"></i>',
                    text  : function(o){
                        return o.title || o.nm;
                    },
                    value : function(o){
                        return o.id;
                    },
                    parseData : function(ids) {
                        return ids ? ids.split(/ *[, ] */g) : [];
                    }
                };
            }
            // 字段: sites
            else if("site" == F.type) {
                fld.uiWidth = "all";
                fld.uiType = "@droplist";
                fld.valueKey = F.arg;
                fld.uiConf = {
                    emptyItem : {
                        icon  : '<i class="zmdi zmdi-flash"></i>',
                        text  : "i18n:auto",
                        value : "id" == F.arg ? "${siteId}" : "${siteName}",
                    },
                    items : "obj -mine -match \"tp:'hmaker_site', race:'DIR'\" -json -l -sort 'nm:1'",
                    icon  : '<i class="fa fa-sitemap"></i>',
                    text  : function(o){
                        return o.title || o.nm;
                    },
                    value : function(o){
                        return o[this.valueKey || "nm"];
                    }
                };
            }
            // 字段: com
            else if("com" == F.type) {
                fld.uiWidth = "all";
                fld.uiType = "@droplist";
                fld.uiConf = {
                    emptyItem : {
                        text : "i18n:hmaker.com.dynamic.com_none",
                    },
                    itemArgs  : F.arg,
                    items : function(ctype, callback){
                        callback(UI.pageUI().getComList(ctype));
                    },
                    itemData : function(uiCom) {
                        if(uiCom) {
                            return {
                                icon  : uiCom.getIconHtml(),
                                text  : uiCom.getComDisplayText(false),
                                value : "#<" + uiCom.getComId() + ">",
                                tip   : "#" + uiCom.getComId(),
                            }
                        }
                    },
                };
            }
            // 字段: link
            else if("link" == F.type) {
                fld.uiWidth = "all";
                fld.uiType = "app/wn.hmaker2/support/c_edit_link";
                fld.uiConf = {
                    emptyItem : {
                        icon  : '<i class="zmdi zmdi-flash-auto"></i>',
                        text  : UI.compactHTML('<span>@auto</span><em>{{hmaker.link.auto}}</em>'),
                        value : '@auto'
                    }
                };
            }
            // 字段: 映射表
            else if("mapping" == F.type) {
                fld.uiWidth = "all";
                fld.type = "object";
                fld.uiType = "@pair";
                fld.uiConf = {
                    mergeWith   : true,
                    templateAsDefault : false,
                    objTemplate : F.mapping || {}
                };
            }
            // 字段: 字段列表
            else if("fields" == F.type) {
                fld.uiWidth = "all";
                fld.type = "object";
                fld.uiType = "@text";
                fld.uiConf = {
                    height : 100,
                    formatData : function(s){
                        var re = null;
                        s = $.trim(s);
                        if(s) {
                            re = {};
                            var lines = s.split(/(\r?\n)+/g);
                            for(var i=0; i<lines.length; i++) {
                                var line = $.trim(lines[i]);
                                if(line) {
                                    var ss = line.split(/[ \t]*[:：][ \t]*/);
                                    var key = ss[0];
                                    var val = ss.length > 1 ? ss[1] : null;
                                    re[key] = val;
                                }
                            }
                        }
                        return re;
                    },
                    parseData : function(obj) {
                        var re = "";
                        for(var key in obj) {
                            var val = obj[key];
                            re += key;
                            if(val) {
                                re += " : " + val + "\n";
                            }
                        }
                        return re;
                    }
                };
            }
            // 字段：字段布局
            else if("layout" == F.type) {
                fld.uiWidth = "all";
                fld.type = "string";
                fld.uiWidth = "all";
                fld.uiType = "@text";
                fld.uiConf = F.arg ? $z.fromJson('{'+F.arg+'}') : {
                    height: 230
                };
            }
            // 字段：开关
            else if("toggle" == F.type) {
                fld.type = /^true(\/false)?/.test(F.arg)
                                ? "boolean"
                                : "string";
                fld.uiType = "@toggle";
                fld.uiConf = {
                    values : ({
                        "yes/no" : ["no", "yes"],
                        "yes"    : [null, "yes"],
                        "on/off" : ["off", "on"],
                        "on"     : [null, "on"],
                        "true/false" : [false, true],
                        "true"       : [false, true],
                    })[F.arg]
                }
            }
            // 字段：切换开关
            // 其 F.arg 格式为 "文字[=值]?", 譬如
            //  "黑色=black,白色=white"
            else if("switch" == F.type || "droplist" == F.type) {
                fld.type = "string";
                fld.uiType = "@" + F.type;
                fld.uiConf = {
                    items : UI.parseStringItems(F.arg)
                }
            }
            // 字段：多行文本
            else if("text" == F.type) {
                fld.type = "string";
                fld.uiWidth = "all";
                fld.uiType = "@text";
                fld.uiConf = F.arg ? $z.fromJson('{'+F.arg+'}') : {};
            }
            // 字段：JSON
            else if("json" == F.type) {
                fld.type = "object";
                fld.uiWidth = "all";
                fld.uiType = "@text";
                fld.uiConf = _.extend({
                        height: 90
                    }, (F.arg ? $z.fromJson('{'+F.arg+'}') : {}), {
                        formatData : function(val) {
                            return $z.map(val);
                        },
                        parseData : function(val) {
                            return $z.toJson(val, null, '  ');
                        }
                    });
            }
            // 直接使用
            else {
                _.extend(fld, F);
                // input 作为默认选项
                $z.setUndefined(fld, "type", "string");
                $z.setUndefined(fld, "uiType", "@input");
                $z.setUndefined(fld, "uiWidth", "all");
            }

            // 默认uiWidth为 auto
            $z.setUndefined(fld, "uiWidth", "auto");

            // 计入结果
            re.push(fld);
        }

        // 返回
        return re;
    },
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