define(function (require, exports, module) {
var methods = {
    //...............................................................
    events : {
        // 单击取消高亮模式
        "click > .hm-com-W" : function(e){
            if(this.isHighlightMode() 
                && $(e.target).closest('.hm-area[highlight]').length == 0) {
                e.stopPropagation();
                this.highlightArea(false);
                this.notifyDataChange("page");
            }
        },
        // 高亮模式，切换高亮区
        "click > .hm-com-W > .ui-arena > .hm-area" : function(e) {
            var UI = this;
            var jq = $(e.currentTarget);
            if(UI.$el.attr("highlight-mode")) {
                if(!jq.attr("highlight")) {
                    UI.highlightArea(jq);
                    this.notifyActived("page", jq.attr("area-id"));
                }
            }
        }
    },
    //...............................................................
    paint : function(com) {
        var UI = this;
        
        // 确保有删除属性
        UI.$el.attr("del-attrs", "highlight-mode");
                
        // 确保有一个区域
        var jAreas = UI.arena.children(".hm-area");
        if(jAreas.length == 0) {
            UI.addArea();
        }
        
        // 检查每个区域，确保有辅助节点
        jAreas.each(function(){
            UI.checkAreaAssistDOM(this);
        });

        // 特殊处理，如果只有一个区域，那么尽量将其撑满
        UI.makeFullIfOnlyOneArea();
    },
    //...............................................................
    makeFullIfOnlyOneArea : function(){
        var UI = this;
        var jAreas = UI.arena.children(".hm-area");

        //console.log(UI._is_defined_size_max_value)

        // 特殊处理，如果只有一个区域，那么尽量将其撑满
        if(jAreas.length == 1 && UI._is_defined_size_max_value) {
            var jA0 = jAreas.eq(0);
            UI._apply_area_size(jA0, "100%");
        }
        // 否则，重新应用一遍 AreaSize
        else {
            jAreas.each(function(){
                UI._apply_area_size(this);
            });
        }
    },
    //...............................................................
    // 确保某个区域是否有辅助节点，如果没有就加上它
    checkAreaAssistDOM : function(aid) {
        var jArea = this.getArea(aid);
        
        // 确保删除的属性
        jArea.attr("del-attrs","highlight");
        
        // 辅助节点
        var jAss = jArea.children(".hm-area-assist");
        if(jAss.length == 0) {
            $($z.compactHTML(`<div class="hm-area-assist hm-del-save">
                <div class="hma-ai" m="N"></div>
                <div class="hma-ai" m="W"></div>
                <div class="hma-ai" m="E"></div>
                <div class="hma-ai" m="S"></div>            
            </div>`)).prependTo(jArea);
        }
        
        // 内容区
        var jCon = jArea.children(".hm-area-con");
        if(jCon.length == 0){
            $('<div class="hm-area-con"></div>').appendTo(jArea);
        }
        
        // 返回区域的 DOM
        return jArea;
    },
    //...............................................................
    // 分配一个区域 ID
    assignAreaId : function() {
        // 首选收集所有区域的 ID
        var areaIds = [];
        this.arena.children(".hm-area").each(function(){
            areaIds.push($(this).attr("area-id"));
        });
        // 从 1 开始循环，找到一个没有分配的 ID
        var n = 1;
        var aid = "Area1"; 
        while(areaIds.indexOf(aid)>=0) {
            aid = "Area" + (++n);
        }
        // 返回 ID
        return aid;
    },
    //...............................................................
    setAreaId : function(aid, newId) {
        if(newId && newId != aid) {
            var jArea = this.getArea(aid);
            if(jArea.length > 0) {
                jArea.attr("area-id", newId);
            }
        }
    },
    setAreaSize : function(aid, asize) {
        var jArea = this.getArea(aid);

        // 标识属性
        if("auto" == asize)
            asize = null;
        jArea.attr("area-size", asize || null);

        // 修改 CSS
        $z.invoke(this, "_apply_area_size", [jArea]);
    },
    setAreaSkin : function(aid, skin) {
        var jArea = this.getArea(aid);
        var ao = this.getAreaObj(jArea);
        
        // 移除老皮肤
        if(ao.skin)
            jArea.removeClass(ao.skin);
        
        // 设置新皮肤
        jArea.addClass(skin).attr("skin", skin);
    },
    //...............................................................
    setAreaCssSelectors : function(aid, selectors) {
        var jArea = this.getArea(aid);
        var ao = this.getAreaObj(jArea);
        
        // 移除老皮肤
        if(ao.selectors)
            jArea.removeClass(ao.selectors);
        
        // 设置新皮肤
        jArea.addClass(selectors).attr("selectors", selectors);
    },
    //...............................................................
    // 增加一个区域
    addArea : function() {
        var jArea = $('<div class="hm-area"><div class="hm-area-con"></div></div>')
            .appendTo(this.arena);
        jArea.attr("area-id", this.assignAreaId());

        // 特殊处理，如果只有一个区域，那么尽量将其撑满
        this.makeFullIfOnlyOneArea();

        return this.checkAreaAssistDOM(jArea);
    },
    //...............................................................
    // 得到一个区域的 jQuery 对象
    getArea : function(aid){
        if(_.isString(aid))
            return this.arena.children('[area-id="'+aid+'"]');
        if(_.isElement(aid) || $z.isjQuery(aid))
            return $(aid).closest(".hm-area");
        throw "Don't known how to find area by : '" + aid + "'";
    },
    //...............................................................
    // 得到区域的 JSON 描述
    getAreaObj : function(aid){
        var jArea = this.getArea(aid);
        return {
            areaId    : jArea.attr("area-id"),
            areaSize  : jArea.attr("area-size"),
            highlight : jArea.attr("highlight") == "yes",
            skin      : jArea.attr("skin") || "",
            selectors : jArea.attr("selectors") || "",
        };
    },
    //...............................................................
    // 得到所有区域的列表
    getAreaObjList : function() {
        var UI = this;
        var re = [];
        UI.arena.children(".hm-area").each(function(){
            re.push(UI.getAreaObj(this));
        });
        return re;
    },
    //...............................................................
    // 删除这个区域
    deleteArea : function(aid) {
        var jArea = this.getArea(aid);

        // 试图寻找下一个区域
        var jA2 = jArea.next();
        if(jA2.length == 0) {
            jA2 = jArea.prev();
        }

        // 只有一个区域的话，不能删的!!!
        if(jA2.length == 0) {
            return false;
        }
        
        // 找到这个区域包含的所有的组件
        jArea.children(".hm-com").each(function(){
            var uiCom = ZUI(this);
            uiCom.destroy();
        });
        
        // 如果自身是高亮区域，试图寻找下一个区域
        // if(this.isHighlightArea(jArea)){

        //     this.$el.removeAttr("highlight-mode");
        // }
        
        // 移除自身
        jArea.remove();

        // 高亮下一个区域
        this.highlightArea(jA2);

        // 特殊处理，如果只有一个区域，那么尽量将其撑满
        this.makeFullIfOnlyOneArea();

        // 删除成功
        return true;
    },
    //...............................................................
    // 移动区域
    // direction : "prev" || "next"
    moveArea : function(aid, direction) {
        var jArea = this.getArea(aid);
        
        // 向前
        if("prev" == direction) {
            var jPrev = jArea.prev();
            if(jPrev.length > 0) {
                jArea.insertBefore(jPrev);
            }
        }
        // 向后
        else {
            var jNext = jArea.next();
            if(jNext.length > 0) {
                jArea.insertAfter(jNext);
            }
        }

        // 闪动一下做个标记
        $z.blinkIt(jArea);
    },
    //...............................................................
    // 得到一个高亮区域的 ID，没有高亮返回 null
    getHighlightAreaId : function(){
        var jArea = this.arena.children('[highlight]');
        return jArea.attr("area-id") || null;
    },
    // 判断当前控件是否是高亮模式
    isHighlightMode : function(){
        return this.$el.attr("highlight-mode");
    },
    // 判断一个区域是否高亮
    isHighlightArea : function(aid) {
        var jArea = false === aid ? null : this.getArea(aid);
        return jArea && jArea.attr("highlight") == "yes";
    },
    // 高亮一个区域 
    highlightArea : function(aid) {
        var jArea = false === aid ? null : this.getArea(aid);
        
        // 反正要取消之前高亮的区域
        this.arena.children(".hm-area[highlight]").removeAttr("highlight");
        
        // 设置某个块高亮，那么其他的块就需要虚掉
        if(jArea && jArea.length > 0) {
            this.$el.attr("highlight-mode", "yes");
            jArea.attr("highlight", "yes");
        }
        // 否则就是表示取消全部高亮
        else {
            this.$el.removeAttr("highlight-mode");
        }
    }
    //...............................................................
}; // ~End methods
//====================================================================
// 得到 HMaker 所有 UI 对象的方法
var HmMethods = require("app/wn.hmaker2/support/hm__methods_com");

//====================================================================
// 输出
module.exports = function(uiCom){
    return _.extend(HmMethods(uiCom), methods);
};
//=======================================================================
});