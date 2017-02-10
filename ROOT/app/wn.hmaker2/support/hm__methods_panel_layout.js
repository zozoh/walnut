define(function (require, exports, module) {
//------------------------------------------------------------------
// 依赖控件
var MenuUI = require('ui/menu/menu');
//------------------------------------------------------------------
// 定义方法
//------------------------------------------------------------------
var methods = {
    events : {
        // 高亮区域
        'click .clp-layout>ul>li' : function(e){
            var UI  = this;
            var jLi = $(e.currentTarget);
            var aid = jLi.find('[key="areaId"]').text();
            
            // 先取消自己的高亮
            UI.arena.find(".clp-layout li").removeAttr("highlight");
            UI.arena.find(".clp-layout li > .hm-skin-box");
                //.removeAttr("box-enabled");
            
            // 取消高亮
            if(UI.uiCom.isHighlightArea(aid)){
                UI.uiCom.highlightArea(false);
            }
            // 开启高亮
            else {
                this.uiCom.highlightArea(aid);
                jLi.attr("highlight","yes")
                    .find(".hm-skin-box");
                        //.attr("box-enabled", "yes");
            }
        },
        // 修改 Area 的 ID
        'click .clp-layout li[highlight] > [key="areaId"]' : function(e) {
            e.stopPropagation();
            var UI  = this;
            var jq  = $(e.currentTarget);
            var aid = jq.text();
            
            // 编辑文字
            $z.editIt(jq, function(newval, oldval, jEle){
                var newId = $.trim(newval);
                if(newId && newId != oldval) {
                    UI.uiCom.setAreaId(aid, newId);
                    jEle.text(newId);
                }
            });
        },
        // 显示可选皮肤
        'click .clp-layout li > [key="skin"] > .com-skin' : function(e) {
            e.stopPropagation();
            var UI   = this;
            var jBox = $(e.currentTarget);
           
            // 得到对应皮肤列表
            var skinList = this.getSkinListForArea();
            var aid = jBox.closest("li").find('[key="areaId"]').text();
            
            // 显示列表
            this.showSkinList(jBox, skinList, function(skin){
                UI.uiCom.setAreaSkin(aid, skin);
            });
        },
        // 阻止 cssSelector 的弹出体冒泡事件，否则就会导致点弹出体切换高亮区域
        // 这个比较超出预期的现象
        "click .hm-skin-box > .page-css > div" : function(e) {
            e.stopPropagation();
        },
    },
    //...............................................................
    _do_redraw : function(iconMoveBefore, iconMoveAfter){
        var UI = this;
        
        // 创建动作菜单
        new MenuUI({
            parent : UI,
            gasketName : "actions",
            tipDirection : "up",
            setup : [{
                icon : '<i class="zmdi zmdi-plus"></i>',
                text : 'i18n:hmaker.com._area.add',
                handler : function(){
                    UI.uiCom.addArea();
                    UI.update();
                }
            }, {
                icon : '<i class="zmdi zmdi-delete"></i>',
                tip  : 'i18n:hmaker.com._area.del',
                handler : function(){
                    var aid = UI.uiCom.getHighlightAreaId();
                    if(!aid){
                        alert(UI.msg("hmaker.prop.noarea"));
                        return;
                    }
                    UI.uiCom.deleteArea(aid);
                    UI.update(UI.uiCom.getData());
                }
            }, {
                icon : iconMoveBefore,
                tip  : 'i18n:hmaker.com._area.mv_prev',
                handler : function(){
                    var aid = UI.uiCom.getHighlightAreaId();
                    if(!aid){
                        alert(UI.msg("hmaker.prop.noarea"));
                        return;
                    }
                    UI.uiCom.moveArea(aid, "prev");
                    UI.update(UI.uiCom.getData());
                }
            }, {
                icon : iconMoveAfter,
                tip  : 'i18n:hmaker.com._area.mv_next',
                handler : function(){
                    var aid = UI.uiCom.getHighlightAreaId();
                    if(!aid){
                        alert(UI.msg("hmaker.prop.noarea"));
                        return;
                    }
                    UI.uiCom.moveArea(aid, "next");
                    UI.update(UI.uiCom.getData());
                }
            }]
        }).render(function(){
            UI.defer_report("actions");
        });
            
        // 返回延迟加载
        return ["actions"];
    },
    //...............................................................
    depose : function(){
        // 如果区域内没有激活的控件，就取消高亮
        if(this.uiCom.arena.find('.hm-com[hm-actived]').length == 0){
            this.uiCom.highlightArea(false);
        }
    },
    //...............................................................
    update : function(com) {
        var UI = this;

        // 首先绘制块
        var jUl  = UI.arena.find(".clp-layout > ul").empty();
        var list = UI.uiCom.getAreaObjList();
        for(var ao of list) {
            UI._update_area_item($('<li>'), ao).appendTo(jUl);
        }
    },
    //...............................................................
    _update_area_item : function(jLi, ao) {
        var UI = this;

        // 更新属性
        jLi.attr({
            "highlight" : ao.highlight ? "yes" : null
        });
        
        // 显示 ID
        $('<div key="areaId">').text(ao.areaId).appendTo(jLi);
        
        // 显示皮肤
        var jBox = $('<div key="skin" class="hm-skin-box" box-enabled="yes">').appendTo(jLi);
        UI.updateSkinBox(jBox, ao.skin, function(skin){
            return this.getSkinTextForArea(skin);
        }, ao.selectors, function(selectors) {
            UI.uiCom.setAreaCssSelectors(ao.areaId, selectors);
        });
            
        // 返回以便链式赋值
        return jLi;
    }
    //...............................................................
}; // ~End methods
//====================================================================
// 得到 HMaker 所有 UI 对象的方法
var HmMethods = require("app/wn.hmaker2/support/hm__methods_panel");

//====================================================================
// 输出
module.exports = function(uiPanel){
    return _.extend(HmMethods(uiPanel), methods);
};
//=======================================================================
});