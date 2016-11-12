define(function (require, exports, module) {
var methods = {
    __gen_base_data : function() {
        var seq = this.$el.attr("c_seq");
        return {
            "_id"   : "com"+seq,
            "_seq"  : seq * 1,
            "_type" : this.$el.attr("ctype"),
        };
    },
    // 获取组件的属性
    getData : function() {
        return _.extend({}, this.getPropFromDom(), this.__gen_base_data());
    },
    // 通常由 hm_page::doChangeCom 调用
    setData : function(com, shallow) {
        // 直接无视吧
        if(com && com.__com_ignore_setData)
            return;

        // 执行更新
        var com2 = this.updateProp(com, shallow);

        // 更新皮肤
        if(com2._skin_old)
            this.$el.removeClass(com2._skin_old);
        if(com2._skin)
            this.$el.addClass(com2._skin);

        // 子类的绘制方法
        $z.invoke(this, "paint", [com2]);
    },
    // 合并修改组件的属性，并保存
    updateProp : function(com, shallow) {
        // 得到完整的属性
        var com2 = shallow ? _.extend(this.getData(), com)
                           : $z.extend(this.getData(), com);

        // // 删除不必要保存东东
        // delete com2.__com_ignore_setData;
        // delete com2.__prop_ignore_update;

        // 保存属性
        this.setPropToDom(com2);

        return com2;
    },
    // 发出属性修改通知，本函数自动合并其余未改动过的属性
    notifyChange : function(key, val) {
        // 如果 key 都未定义，那么表示发出这个消息的组件不希望引起自己的属性的保存和界面的绘制
        // 通常，这种 com 都是通过自身的 DOM 来存放某些数据的，它修改了自身的 DOM 
        // 剩下的就是通知 prop 那边刷新显示而已
        if(!key) {
            this.fire("change:com", _.extend(this.getData(), {__com_ignore_setData:true}), this.$el);
        } 
        // 如果 key 不是字符串，或者里面包含了 "." 那么可能是多层的 key
        // $z.setValue 提供了这个逻辑的封装 
        else {
            var prop = $z.extend(this.getData(), $z.setValue({}, key, val));
            this.fire("change:com", _.extend(prop, this.__gen_base_data()), this.$el);
        }
    },
    // 根据 Block 的属性设置，得到它的应该的矩形对象
    getBlockRectByProp : function(prop) {
        var keys = prop.posBy.split(",");
        var vals = prop.posVal.split(",");
        var rect = {};
        for(var i in keys) {
            rect[keys[i]] = $z.toPixel(vals[i]);
        }
        return $z.rect_count_auto(rect, true);
    },
    // 将属性设置到控件的 DOM 上
    setPropToDom : function(com) {
        $z.setJsonToSubScriptEle(this.$el, "hmc-prop-ele", com, true);
    },
    // 从控件的 DOM 上获取控件的属性
    getPropFromDom : function(){
        return $z.getJsonFromSubScriptEle(this.$el, "hmc-prop-ele");
    },
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
    // 判断一个 DOM 元素是否在一个激活的块中
    isInActivedBlock : function(jq) {
        return $(jq).closest(".hm-block").attr("hm-actived") == "yes";
    },
    // 判断控件自身是否是 Actived
    isActived : function() {
        return this.$el.closest(".hm-block").attr("hm-actived") == "yes";
    },
    // 在属性面板的扩展元素接口，绘制自定义 UI
    drawComEleInProp : function(uiDef, callback) {
        this.propUI("edit").drawComEle(uiDef, callback);
    }
}; // ~End methods
//====================================================================
// 得到 HMaker 所有 UI 对象的方法
var HmMethods = require("app/wn.hmaker2/hm__methods");

//====================================================================
// 输出
module.exports = function(uiCom){
    // 本函数调用的时机必须是 uiCom 实例还没有被 redraw，这时，我们统一修改
    // 控件的 DOM 结构。我们假想每个控件都为自己的 `dom` 属性设置了 .ui-arena
    // 为根元素的 HTML 结构。当然，同级的元素也可能是 code-template
    uiCom.keepDom = true;
    
    // 确保初始化的 HTML 有正确的结构
    uiCom.dom = '<div class="hm-com-W">' + uiCom.dom + '</div>';
    
    // 控件默认的布局属性
    $z.setUndefined(uiCom, "blockProp", [
        "padding",
        "border",
        "borderRadius",
        "color",
        "background",
        "boxShadow",
        "overflow",
    ]);
    
    // 扩展自身属性
    return _.extend(HmMethods(uiCom), methods, {
        // 改变 code-template 的查找方式
        findCodeTemplateDomNode : function(){
            return this.$el.find(">.hm-com-W>.ui-code-template");
        },
        // 改变 arena 的查找方式
        findArenaDomNode : function(){
            return this.$el.find(">.hm-com-W>.ui-arena");
        },
        // 初次显示的时候，确保有控制柄
        redraw : function() {
            var jW   = this.$el.children(".hm-com-W");
            if(jW.length == 0)
                throw "com without .hm-com-W : " + uiCom.cid;
                
            var jAss = jW.children(".hm-com-assist");
            if(jAss.length == 0) {
                jAss = $(`<div class="hm-com-assist">
                    <div class="rsz-N"></div>
                    <div class="rsz-W"></div>
                    <div class="rsz-E"></div>
                    <div class="rsz-S"></div>
                    <div class="rsz-NW"></div>
                    <div class="rsz-NE"></div>
                    <div class="rsz-SW"></div>
                    <div class="rsz-SE"></div>
                    <div class="hmv-hdl"></div>
                </div>`).prepentTo(jW);
            }
        }
    });
};
//=======================================================================
});