define(function (require, exports, module) {
var CssP = require("ui/support/cssp");
var methods = {
    //........................................................
    // 获取组件的 ID
    getComId : function(){
        return this.$el.attr("id");
    },
    //........................................................
    setComId : function(newId) {
        var ele = this.el.ownerDocument.getElementById(newId);
        // 有重名的
        if(ele && ele !== this.el){
            alert(this.msg("hmaker.com._.existsID"));
            return false;
        }
        // 设置
        this.$el.attr("id", newId);

        // 返回成功
        return true;
    },
    //........................................................
    // 获取组件的 type
    getComType : function() {
        return this.$el.attr("ctype");
    },
    //........................................................
    // 获取组件的皮肤
    getComSkin : function() {
        return this.$el.attr("skin");
    },
    // 设置组件的皮肤，这里会取消掉老的皮肤
    setComSkin : function(skin) {
        var old_skin = this.getComSkin();
        if(old_skin) {
            this.pageUI().invokeSkin("reset", [this.$el]);
            this.$el.removeClass(old_skin);
            this.arena.removeClass(old_skin);
        }
        this.$el.attr("skin", skin||null);
        this.arena.addClass(skin);
        this.syncComSkinAttributes(skin);

        // 由于皮肤的改动，需要去掉所有非 POS 相关的设定
        if(skin != old_skin) {
            var block = this.getBlock();
            var block2 = $z.pick(block, 
                /^(mode|posBy|measureBy|top|left|right|bottom|width|height)$/);
            this.setBlock(block2);
        }

        // 重新调用一下激活
        $z.invoke(this, "on_actived");
    },
    // 确保皮肤被应用
    useComSkin : function(){
        var skin = this.getComSkin();
        this.arena.addClass(skin);
        this.syncComSkinAttributes(skin);
    },
    // 绘制皮肤内置开关
    syncComSkinAttributes : function(skin){
        var UI = this;
        var sattrs = $z.invoke(UI, "getSkinAttributes");
        if(_.isArray(sattrs) && sattrs.length > 0) {
            var ctype = UI.getComType();
            skin  = skin || UI.getComSkin();
            var skinItem = UI.getSkinItemForCom(ctype, skin);
            var saMap = (skinItem ? skinItem.attributes : null) || {};
            for(var i=0; i<sattrs.length; i++) {
                var saKey = sattrs[i];
                var saVal = saMap[saKey] || null;
                UI.arena.attr(saKey, saVal);
            }
        }
    },
    //........................................................
    // 获取组件的皮肤
    getComSelectors : function() {
        return this.$el.attr("selectors") || "";
    },
    // 设置组件的皮肤，这里会取消掉老的皮肤
    setComSelectors : function(selectors) {
        var old_selectors = this.getComSelectors();
        if(old_selectors) {
            this.$el.removeClass(old_selectors);
            this.arena.removeClass(old_selectors);
        }
        this.$el.attr("selectors", selectors||null);
        this.arena.addClass(selectors);
    },
    //........................................................
    // 获取组件的显示模式
    getComDisplayMode : function() {
        return this.$el.attr("hm-dis-mode") || "show";
    },
    // 设置组件的显示模式
    setComDisplayMode : function(disMode) {
        if(!/^(show|desktop|mobile)$/.test(disMode)){
            disMode = "show";
        }
        this.$el.attr("hm-dis-mode", "show" == disMode ? null : disMode);
    },
    //........................................................
    // 获取组件的库
    getComLibName : function(){
        return this.$el.closest('.hm-com[lib]').attr("lib");
    },
    // 设置或者移除（null）一个控件的共享库组件关联
    setComLibName : function(lib) {
        var libName = this.getComLibName();
        // 已经关联的组件，不能再设置了
        if(libName && lib) {
            this.alert(this.msg("hmaker.lib.e_set_lib"));
            return;
        }
        this.$el.attr("lib", lib || null);
    },
    // 仅仅是获取自己（不包括父控件）的组件库名称
    getMyLibName : function(){
        return this.$el.attr("lib");
    },
    isInLib : function(){
        var cLib = this.getComLibName();
        var mLib = this.getMyLibName();
        return cLib && !mLib;
    },
    getMyLibInfo : function(){
        var cLib = this.getComLibName();
        var mLib = this.getMyLibName();
        if(!cLib && !mLib)
            return null;
        return {
            name      : cLib,
            myLibName : mLib,
            isInLib   : (cLib && !mLib)
        };
    },
    //........................................................
    // 获取一个组件的路径数组，每个元素为
    // {
    //     ctype  : 组件类型  (@area) 表示布局控件的区域
    //     comId  : 组件ID
    //     areaId : 区域ID  
    // }
    // - includeSelf : 是否包括自己
    // - ignoreArea  : 是否忽略区域
    // - areaId : 「选」路径中还需要增加一个指定的区域路径
    getComPath : function(includSelf, ignoreArea, areaId) {
        var re = [];
        // 处理自己
        if(includSelf) {
            // 自己的高亮区域
            if(areaId && this.$el.hasClass("hm-layout")) {
                re.push({
                    ctype  : "_area",
                    comId  : this.getComId(),
                    areaId : areaId
                });
            }
            // 自己的组件
            re.push({
                ctype : this.getComType(),
                comId : this.getComId(),
                lib   : this.$el.attr("lib"),
                skin  : this.getComSkin(),
            });
        }
        // 处理自己的父组件和区域
        this.$el.parents(ignoreArea ? ".hm-com" : ".hm-com,.hm-area").each(function(){
            var jq = $(this);
            // 区域
            if(jq.hasClass("hm-area")) {
                re.push({
                    ctype  : "_area",
                    comId  : jq.closest(".hm-com").attr("id"),
                    areaId : jq.attr("area-id")
                });
            }
            // 组件
            else {
                re.push({
                    ctype : jq.attr("ctype"),
                    comId : jq.attr("id"),
                    lib   : jq.attr("lib"),
                    skin  : jq.attr("skin"),
                });
            }
        });

        // 返回
        return re.reverse();
    },
    //........................................................
    getIconHtml : function() {
        return this.msg('hmaker.com.' + this.getComType() + '.icon');
    },
    //........................................................
    getComDisplayText : function(showId) {
        var UI = this;
        var skin  = UI.getComSkin();
        var comId = UI.getComId();
        var ctype = UI.getComType();
        return UI.get_com_display_text(ctype, comId, skin, showId);
    },
    //........................................................
    // 判断组件是否是激活的
    isActived : function() {
        return this.$el.attr("hm-actived") == "yes";
    },
    // 判断一个 DOM 元素是否在一个激活的块中
    isInActivedCom : function(jq) {
        return $(jq).closest(".hm-com[hm-actived]").length > 0;
    },
    //........................................................
    notifyActived : function(mode, areaId){
        // 激活
        this.fire("active:com", this, areaId);

        // 高亮指定区域
        if(areaId) {
            $z.invoke(this, "highlightArea", [areaId]);
        }

        // 通知更新
        if(!_.isUndefined(mode)){
            this.notifyBlockChange(mode, this.getBlock());
            this.notifyDataChange(mode,  this.getData());
        }
    },
    //........................................................
    // 获取组件的属性
    getData : function() {
        var com = $z.getJsonFromSubScriptEle(this.$el, "hm-prop-com");
        if(_.isEmpty(com)) {
            return this.getDefaultData();
        }
        return com;
    },
    // 通常由 hm_page::doChangeCom 调用
    setData : function(com, shallow) {
        // 合并数据
        var com2 = shallow 
                    ? _.extend(this.getData(), com)
                    : $z.extend(this.getData(), com);
        //console.log("setData", com2);
        // 保存属性
        $z.setJsonToSubScriptEle(this.$el, "hm-prop-com", com2, true);
        
        // 返回数据
        return com2;
    },
    // 发出属性修改通知，本函数自动合并其余未改动过的属性
    // mode 可能是:
    //  - "page"  : 页编辑区发出的，因此不要重绘显示 DOM (paint)
    //  - "panel" : 属性面板发出的，因此不要重绘属性面板了
    //  - null    : 其他地方发出的，组件和面板都要重绘
    notifyDataChange : function(mode, com) {
        this.fire("change:com", mode, this, (com||this.getData()));
    },
    // 相当于先 setData 然后再 notifyDataChange
    saveData : function(mode, com, shallow) {
        var com2 = this.setData(com, shallow);
        this.notifyDataChange(mode, com2);
        return com2;
    },
    //........................................................
    // 获取组件的属性
    getBlock : function() {
        var block = $z.getJsonFromSubScriptEle(this.$el, "hm-prop-block");
        if(_.isEmpty(block)) {
            return this.getDefaultBlock();
        }
        return block;
    },
    // 通常由 hm_page::doChangeCom 调用
    setBlock : function(block, returnNew) {
        //console.log("I am setBlock")
        // 保存属性
        $z.setJsonToSubScriptEle(this.$el, "hm-prop-block", (block||{}), true);

        // 返回数据
        if(returnNew)
            return this.getBlock();
        return block;
    },
    // 发出属性修改通知，本函数自动合并其余未改动过的属性
    // mode 可能是:
    //  - "page"  : 页编辑区发出的，因此不要重绘显示 DOM (applyBlock)
    //  - "panel" : 属性面板发出的，因此不要重绘属性面板了
    //  - null    : 其他地方发出的，组件和面板都要重绘
    notifyBlockChange : function(mode, block) {
        this.fire("change:block", mode, this, (block||this.getBlock()));
    },
    // 相当于先 setBlock 然后再 notifyBlockChange
    saveBlock : function(mode, block, base) {
        if(!base)
            base = this.getBlock();
        // 挑选必要属性出来
        base = $z.pick(base, /^(mode|posBy|measureBy|width|height|top|left|right|bottom)$/);

        // 融合
        block = _.extend(base, block);

        // 保存
        this.setBlock(block);

        // 通知修改
        this.notifyBlockChange(mode, block);

        // 返回
        return block;
    },
    //........................................................
    getMeasureConf: function(mb){
        var jAreaCon = this.$el.closest(".hm-area-con");
        var conf = $D.dom.getMeasureConf(jAreaCon.length > 0 
            ? jAreaCon
            : this.el.ownerDocument);
        conf.unit = mb || "px";   // 值的单位
        conf.precision = 3;       // 精确到小数点三位
        return conf;
    },
    //........................................................
    // 格式化 block 的 top,left,right,bottom,width,height
    // 根据 block.measureBy 属性("px|rem|%") 来进行格式化
    // 默认的 block.measureBy 为 "px"
    formatBlockDimension : function(block, conf) {
        var UI  = this;
        // 准备配置参数: 默认从 block 里取
        if(!conf) {
            conf = UI.getMeasureConf(block.measureBy);
        }
        // 如果给定了仅仅一个单位
        else if(_.isString(conf)) {
            conf = UI.getMeasureConf(conf);
        }

        // 准备片段表达式
        var mbReg = new RegExp(conf.unit+"$");

        // 循环处理
        for(var key in block) {
            if(/^(top|left|right|bottom|width|height)$/.test(key)) {
                var val = block[key];
                // 如果就是数字，被认为是像素
                if(_.isNumber(val)) {
                    var s = $D.dom.toMeasureStr(key, val, conf);
                    block[key] = s;
                }
                // 参数不符合的，需要进行转换
                else if(val && "unset"!=val && !mbReg.test(val)) {
                    //console.log(key)
                    var n = $D.dom.toMeasureNum(key, val, conf);
                    var s = $D.dom.toMeasureStr(key, n, conf);
                    block[key] = s;
                }
            }
        }
    },
    //........................................................
    addMySkinRule : function(selector, rule, skin) {
        var UI = this;
        UI.css_style = UI.css_style || {};
        skin = skin || UI.getComSkin();
        if(skin && selector) {
            var ss = selector.split(/ *, */g);
            for(var i=0; i<ss.length; i++){
                ss[i] = "." + skin + " " + ss[i];
            }
            selector = ss.join(", ");
        }
        if(UI.css_style[selector]){
            _.extend(UI.css_style[selector], rule);
        } else {
            UI.css_style[selector] = rule;
        }
    },
    //........................................................
    applyBlock : function(block) {
        //console.log("applyBlock", block);
        var UI     = this;
        var jCom   = UI.$el;
        var jW     = jCom.children(".hm-com-W");

        // 没有的话，主动获取一下
        if(!block) {
            block = UI.getBlock();
        }
        // 否则创建一个副本，因为在应用自定义 skin 选择器 
        // 比如 "#B> xxx" 的时候，计算出了属性后，要把它删掉
        else {
            block = _.extend({}, block);
        }

        // console.log("applyBlock", block);
        
        // 更新控件的模式
        jCom.attr({
            "hmc-mode"    : block.mode,
            "hmc-pos-by"  : block.posBy,
        });
        
        // 确保更新了皮肤
        UI.useComSkin();
        
        // 准备 css 对象
        var css = {};
        
        // 对于绝对定位
        if("abs" == block.mode) {
            _.extend(css, 
                $z.pick(block, "!^(mode|posBy|sa-.+)$"),{
                "position" : "absolute"
            });
        }
        // 对于固定定位
        else if("fix" == block.mode) {
            _.extend(css, 
                $z.pick(block, "!^(mode|posBy|sa-.+)$"),{
                "position" : "fixed"
            });
        }
        // 相对位置
        else {
            _.extend(css, 
                $z.pick(block, "!^(mode|posBy|top|left|right|bottom|sa-.+)$"),{
                "position" : ""
            });
        }

        // 得到组件的 ID 和皮肤
        var comId = this.getComId();
        var skin  = this.getComSkin();

        // 确保移除皮肤的特殊属性
        var attrs = {};
        for(var i=0; i<UI.el.attributes.length; i++) {
            var anm = UI.el.attributes[i].name;
            // 自定义属性
            if(/^sa-.+/.test(anm)) {
                attrs[anm] = null;
            }
        }   

        // 皮肤属性的 boolean 字段，要变成存在或者不存在
        _.extend(attrs, $z.pick(block, "^sa-.+$"));
        for(var key in attrs) {
            var val = attrs[key];
            if(_.isBoolean(val))
                attrs[key] = val ? "yes" : null;
        }
        // 应用皮肤属性
        UI.arena.attr(attrs);

        // 自定义的皮肤样式选择器，子控件可以重载 applyBlockCss 函数添加更多
        UI.css_style = {};      // 重置一下自定义 CSS
        for(var key in css){
            var v = css[key];
            // 无视空值
            if(_.isNull(v) || _.isUndefined(v))
                continue;

            // 自定义快捷颜色
            var m = /^#([BCL])>(.+)$/.exec(key);
            if(m) {
                var propType = m[1];
                var selector = m[2];
                var prop;
                // 属性的集合
                if(_.isObject(v)) {
                    prop = v;
                }
                // 背景
                else if("B" == propType){
                    prop = $z.obj("background", v);
                }
                // 颜色
                else if("C" == propType){
                    prop = $z.obj("color", v);
                }
                // 边框颜色
                else {
                    prop = $z.obj("border-color", v);
                }
                // 添加到自定义规则里
                UI.addMySkinRule(selector, prop)
                css[key] = "";

                // 下一个
                continue;
            }

            // 自定义CSS属性
            m = /^\+([A-Za-z_-]+):([^\(]+)$/.exec(key);
            if(m) {
                var a_tp  = m[1];
                var a_sel = m[2];
                // 得到属性名
                var p_nm = "_align"==a_tp ? "textAlign" : a_tp;
                //var p_nm = a_tp;

                // 添加到自定义规则里
                UI.addMySkinRule(a_sel, $z.obj(p_nm, v));
                css[key] = "";

                // 下一个
                continue;
            }

            // 集合属性
            if(_.isObject(v)) {
                css[key] = "";
                _.extend(css, v);
            }
        }
        
        // 修正 css 的宽高
        if("unset" == css.width)
            css.width = "";
        if("unset" == css.height)
            css.height = "";

        // 修正 font 的 css
        if(_.isArray(css._font)){
            css.fontWeight = css._font.indexOf("bold")>=0 ? "bold" : "";
            css.textDecoration = css._font.indexOf("underline")>=0 ? "underline" : "";
            css.fontStyle = css._font.indexOf("italic")>=0 ? "italic" : "";
        }else{
            css.fontWeight = "";
            css.textDecoration = "";
            css.fontStyle = "";
        }

        // 是否需要将 jW 和 Arena 都设置成 100%
        this.$el.attr("auto-wrap-width", 
            !$D.dom.isUnset(css.width) ? "yes" : null);
        this.$el.attr("auto-wrap-height", 
            !$D.dom.isUnset(css.height) ? "yes" : null);

        // 绝对定位的话，应该忽略 margin
        if(/^(abs|fix)$/.test(block.mode)) {
            css.margin = "";
        }
            
        // 最后分别应用属性到对应的元素上
        var posKeys  = "^(position|top|left|right|bottom|margin|width|height)$";
        var cssCom   = $z.pick(css, posKeys);
        var cssArena = $z.pick(css, "!" + posKeys);
        cssCom = this.formatCss(cssCom,true);
        cssArena = this.formatCss(cssArena,true);
                
        // 应用这个修改
        //console.log("css:", css);
        UI.applyBlockCss(cssCom, cssArena);

        // 应用修改
        var jStyle = UI.$el.children("style.from-skin");
        // 空的话，移除
        if(_.isEmpty(UI.css_style)){
            jStyle.remove();
        }
        // 设置自定义 css 内容
        else{
            if(jStyle.length == 0){
                jStyle = $('<style class="from-skin hm-del-save">').appendTo(UI.$el);
            }
            var cssTxt = CssP.genCss(UI.css_style, "#" + comId);
            jStyle.html(cssTxt);
        }
        
        // 判断区域是否过小
        var comW = jCom.outerWidth();
        var comH = jCom.outerHeight();

        // 比较小
        if(comH < 32 || comW < 32){
            jCom.attr("hmc-small", "yes");
        }
        // 不小，移除标记
        else{
            jCom.removeAttr("hmc-small");
        }
        
        // 调用控件特殊的设置
        $z.invoke(UI, "on_apply_block", [block]);
        
    },
    //...............................................................
    appendToArea : function(eArea, quiet) {
        var block;
        // 移动到 Body
        if(!eArea) {
            var uiPage = this.pageUI();
            this.appendTo(uiPage, uiPage.$editBody());
            
            // zozoh: 嗯，什么都不要做了
            // 切换到相对定位
            // var block = this.getBlock();
            // block.mode = this.$el.attr("hmc-mode");
        }
        // 移动到分栏
        else {
            // 确定 Area 所在的分栏控件
            var jArea    = $(eArea).closest(".hm-area");
            var jAreaCon = jArea.children(".hm-area-con");
            var comArea  = ZUI(jArea);
            
            // 将自身移动到这个分栏内部
            this.appendTo(comArea, jAreaCon);
            
            // 切换到相对定位
            var block = this.getBlock();
            block.mode = "inflow";

            // 修改块属性
            this.checkBlockMode(block);

            // 保存块属性
            this.saveBlock(null, block);
        }
                
        // 通知一下
        if(!quiet)
            this.notifyActived();
    },
    //...............................................................
    // 得到自己关于宽高位置的 css
    getMyRectCss : function() {
        var rect = $D.rect.gen(this.$el);
        var viewport = this.getMyViewportRect();
        // console.log(rect);
        // console.log(viewport);
        return $D.rect.relative(rect, viewport, true);
    },
    //...............................................................
    // 得到控件所属的视口 DOM，如果不在分栏里，那么就是 body
    getMyViewport : function(){
        if("abs" == this.$el.attr("hmc-mode")) {
            var jArea = this.$el.closest(".hm-area-con");
            if(jArea.length > 0)
                return jArea;
        }
        return this.$el.closest("body");
    },
    getMyViewportRect : function(){
        // 用一个区域
        if("abs" == this.$el.attr("hmc-mode")) {
            var jArea = this.$el.closest(".hm-area-con");
            if(jArea.length > 0) {
                return $D.rect.gen(jArea, {
                    overflow : {x:"auto", y:"auto"}
                });
            }
        }
        // 用 body
        return $D.rect.gen(this.$el.closest("body"), {
            overflow : {x:"auto", y:"scroll"}
        });
    },
    //...............................................................
    // 将一个 json 描述的 CSS 对象变成 CSS 文本
    // css 对象，key 作为 selector，值是 JS 对象，代表 rule
    // prefix 为 selector 增加前缀，如果有的话，后面会附加空格
    genCssText : function(css, prefix) {
        prefix = prefix ? prefix + " " : "";
        var re = "";
        for(var selector in css) {
            re += prefix + selector;
            re += this.genCssRuleText(css[selector])
            re += "\n";
        }
        return re;
    },
    genCssRuleText : function(rule) {
        var re = "{";
        for(var key in rule) {
            var val = rule[key];
            if(_.isNumber(val)){
                val = val + "px";
            }
            re += "\n" +$z.lowerWord(key) + ":" + val + ";";
        }
        re += '\n}';
        return re;
    },
}; // ~End methods
//====================================================================
// 得到 HMaker 所有 UI 对象的方法
var HmMethods = require("app/wn.hmaker2/support/hm__methods");

//====================================================================
// 输出
module.exports = function(uiCom){
    // 本函数调用的时机必须是 uiCom 实例还没有被 redraw，这时，我们统一修改
    // 控件的 DOM 结构。我们假想每个控件都为自己的 `dom` 属性设置了 .ui-arena
    // 为根元素的 HTML 结构。当然，同级的元素也可能是 code-template
    //uiCom.keepDom = true;
    
    // 控件默认的布局属性
    $z.setUndefined(uiCom, "getBlockPropFields", function(block){
        return [
            "margin", "padding", "border", "color", "background",
            "borderRadius", "overflow", "boxShadow"
        ];
    });
    
    // 控件的默认布局
    $z.setUndefined(uiCom, "getDefaultBlock", function(){
        return {
            mode : "inflow",
            posBy   : "WH",
            width   : "unset",
            height  : "unset",
            padding : "",
            border : "" ,   // "1px solid #000",
            borderRadius : "",
            background : "",
            color : "",
            overflow : "",
            blockBackground : "",
        };
    });
    
    // 控件的默认数据
    $z.setUndefined(uiCom, "getDefaultData", function(){
        return {};
    });
    
    // 控件默认的确保块在各个模式下的正常显示
    $z.setUndefined(uiCom, "checkBlockMode", function(block){
        // 绝对定位的块，必须有宽高
        if(/^(fix|abs)$/.test(block.mode)) {
            // 确保定位模式正确
            if(!block.posBy || "WH" == block.posBy)
                block.posBy = "TLWH";
            // 确保有必要的位置属性
            var css = this.getMyRectCss();
            // 设置
            _.extend(block, this.pickCssForMode(css, block.posBy));
        }
        // inflow 的块，高度应该为 unset
        else if("inflow" == block.mode){
            _.extend(block, {
                top: "", left:"", bottom:"", right:"", height:"unset",
                posBy : "WH"
            });
        }
        // !!! 不支持
        else {
            throw "unsupport block mode: '" + block.mode + "'";
        }
    });
    
    // 定默认控件应用布局块属性的方法
    $z.setUndefined(uiCom, "applyBlockCss", function(cssCom, cssArena){
        this.$el.css(cssCom);
        this.arena.css(cssArena);
    });
    
    // 重定义控件的 redraw
    var com_redraw = function(){
        var ME = this;
        // 弱弱的检查一下基础结构
        var jW   = ME.$el.children(".hm-com-W");
        if(jW.length == 0)
            throw "com without .hm-com-W : " + uiCom.cid;
        
        // 确保有辅助节点
        var jAss = jW.children(".hm-com-assist");
        if(jAss.length == 0) {
            jAss = $(`<div class="hm-com-assist">
                <div class="hmc-ai" m="H" data-balloon-pos="left" data-balloon="`
                + ME.msg("hmaker.drag.com_tip")
                + `"><i class="zmdi zmdi-arrows"></i></div>
                <div class="hmc-ai rsz-hdl1" m="N"></div>
                <div class="hmc-ai rsz-hdl1" m="W"></div>
                <div class="hmc-ai rsz-hdl1" m="E"></div>
                <div class="hmc-ai rsz-hdl1" m="S"></div>
                <div class="hmc-ai rsz-hdl2" m="NW"></div>
                <div class="hmc-ai rsz-hdl2" m="NE"></div>
                <div class="hmc-ai rsz-hdl2" m="SW"></div>
                <div class="hmc-ai rsz-hdl2" m="SE"></div>
            </div>`).prependTo(jW);
        }
        
        // 试图调用一下组件自定义的 redraw
        $z.invoke(ME, "_redraw_com", []);
        
        // 绘制布局
        ME.applyBlock(ME.getBlock());
        
        // 绘制外观
        ME.paint(ME.getData());
    };
    // 判断一下是针对 Com 实例还是 Com 的定义
    if(uiCom.$ui) {
        uiCom.$ui.redraw = com_redraw;
    } else {
        uiCom.redraw = com_redraw;
    }
    
    // 扩展自身属性
    return _.extend(HmMethods(uiCom), methods, {
        // 修改 DOM 的插入点
        findDomParent : function() {
            var jW = this.$el.find(">.hm-com-W");

            // 没有 Wrapper 则插入内容
            if(jW.length == 0) {
                return $('<div class="hm-com-W">').appendTo(this.$el);
            }

            // 不保持 DOM，则返回 wrapper， ZUI 会重置内容
            if(!this.keepDom)
                return jW;
        },
        // 改变 code-template 的查找方式
        findCodeTemplateDomNode : function(){
            return this.$el.find(">.hm-com-W>.ui-code-template");
        },
        // 改变 arena 的查找方式
        findArenaDomNode : function(){
            return this.$el.find(">.hm-com-W>.ui-arena");
        }
    });
};
//=======================================================================
});