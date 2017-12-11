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
        // 取消高亮区域
        'click .ui-arena' : function(e){
            var jq = $(e.target);

            // 点在了菜单上
            if(jq.closest(".clp-actions").length > 0){
                return;
            }

            // 点在了 Area 布局里
            if(jq.closest(".clp-layout").length > 0) {
                return;
            }

            // 嗯，取消吧
            this.uiCom.highlightArea(false);
            this.update(this.uiCom.getData());
        },
        // 修改 Area 的 ID
        'click .clp-layout li[highlight] [key="areaId"]' : function(e) {
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
        // 修改 Area 的尺寸
        'click .clp-layout li[highlight] [key="areaSize"]' : function(e) {
            e.stopPropagation();
            var UI  = this;
            var jq  = $(e.currentTarget);
            var aid = jq.closest('li').find('[key="areaId"]').text();
            
            // 编辑文字
            $z.editIt(jq, function(newval, oldval, jEle){
                // 判断合法性
                var str = $.trim(newval) || "auto";
                // 指定关键字
                if(/^(auto|compact)$/.test(str)) {
                    UI.uiCom.setAreaSize(aid, str);
                    jEle.text(str);
                }
                // 看看给定的值是不是可接受的格式
                else {
                    var m = /^([\d.]+)(px|rem|%)?$/.exec(str);
                    if(m) {
                        // 默认用像素
                        if(!m[2]){
                            str += "px";
                        }
                        // 更新
                        UI.uiCom.setAreaSize(aid, str);
                        jEle.text(str);
                    }
                }
            });
        },
        // 修改 Area 的尺寸: 切换模式
        'click .layout-area-align-box td' : function(e) {
            var UI = this;
            var jTd = $(e.currentTarget);
            jTd.closest("table").find("td").removeAttr("checked");
            jTd.attr("checked", "yes");
        },
        // 修改 Area 的排布
        'click .clp-layout li[highlight] [key="areaAlign"]' : function(e) {
            e.stopPropagation();
            var UI  = this;
            var jq  = $(e.currentTarget);
            var jLi = jq.closest('li');
            var aid = jLi.find('[key="areaId"]').text();
            var jAlign = jLi.find('[key="areaAlign"]');
            var align = jAlign.attr("area-align");
            
            UI.__show_area_align_box(jLi, function(newAlign){
                // 修改自己的显示
                jAlign.attr({
                    "area-align": newAlign||null
                }).html(UI.__get_align_html(newAlign));
                // 修改控件
                UI.uiCom.setAreaAlign(aid, newAlign);
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
                    if(UI.uiCom.deleteArea(aid)) {
                        UI.update(UI.uiCom.getData());
                    }
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
            "highlight" : ao.highlight ? "yes" : null,
        });
       
        // 显示 ID
        $('<div key="areaId">').text(ao.areaId)
            .appendTo(jLi);

        // 显示尺寸
        $('<div key="areaSize">').text(ao.areaSize||"auto")
            .appendTo(jLi)

        // 显示排布
        $('<div key="areaAlign">').html(UI.__get_align_html(ao.areaAlign))
            .attr("area-align", ao.areaAlign)
                .appendTo(jLi);

        
        // 显示皮肤
        var jBox = $('<div key="skin" class="hm-skin-box" box-enabled="yes">')
            .appendTo(jLi);
        UI.updateSkinBox(jBox, {
            skin : ao.skin,
            getSkinText : function(skin){
                return this.getSkinTextForArea(skin);
            },
            cssSelectors : ao.selectors, 
            setSelectors : function(selectors) {
                UI.uiCom.setAreaCssSelectors(ao.areaId, selectors);
            }
        });
            
        // 返回以便链式赋值
        return jLi;
    },
    //...............................................................
    __get_align_html : function(align) {
        align = align || "none";
        return this.compactHTML('<%=hmaker.com._area.align_icon_' + align + '%>'
                               + '<span>{{hmaker.com._area.align_text_'
                                    + align
                                    + '}}</span>');
    },
    //...............................................................
    __show_area_align_box : function(jLi, callback) {
        var UI = this;
        var jAlign = jLi.find('[key="areaAlign"]');
        var align = jAlign.attr("area-align");
        console.log(jAlign, align)

        // 绘制一下 align_box
        var html = UI.compactHTML(`
        <div class="layout-area-align-box">
            <section>
                <table>
                    <tr>
                        <td val="NW">
                            <%=hmaker.com._area.align_icon_NW%>
                            <span>{{hmaker.com._area.align_text_NW}}</span>
                        </td>
                        <td val="N">
                            <%=hmaker.com._area.align_icon_N%>
                            <span>{{hmaker.com._area.align_text_N}}</span>
                        </td>
                        <td val="NE">
                            <span>{{hmaker.com._area.align_text_NE}}</span>
                            <%=hmaker.com._area.align_icon_NE%>
                        </td>
                    </tr>
                    <tr>
                        <td val="W">
                            <%=hmaker.com._area.align_icon_W%>
                            <span>{{hmaker.com._area.align_text_W}}</span>
                        </td>
                        <td val="P">
                            <%=hmaker.com._area.align_icon_P%>
                            <span>{{hmaker.com._area.align_text_P}}</span>
                        </td>
                        <td val="E">
                            <span>{{hmaker.com._area.align_text_E}}</span>
                            <%=hmaker.com._area.align_icon_E%>
                        </td>
                    </tr>
                    <tr>
                        <td val="SW">
                            <%=hmaker.com._area.align_icon_SW%>
                            <span>{{hmaker.com._area.align_text_SW}}</span>
                        </td>
                        <td val="S">
                            <%=hmaker.com._area.align_icon_S%>
                            <span>{{hmaker.com._area.align_text_S}}</span>
                        </td>
                        <td val="SE">
                            <span>{{hmaker.com._area.align_text_SE}}</span>
                            <%=hmaker.com._area.align_icon_SE%>
                        </td>
                    </tr>
                </table>
                <div><a>
                    <%=hmaker.com._area.align_icon_none%>
                    <span>{{hmaker.com._area.align_text_none}}</span>
                </a></div>
            </section>
        </div>
        `);

        // 显示 box，并设置高亮
        var jBox = $(html).appendTo(jLi.parent());
        jBox.find('td[val="'+align+'"]').attr("checked", "yes");

        // 停靠
        $z.dock(jAlign, jBox.find("section"), "H");

        // 监听事件
        jBox.one("click", "td", function(){
            $z.doCallback(callback, [$(this).attr("val")], UI);
        }).one("click", "a", function(){
            $z.doCallback(callback, [null], UI);
        }).one("click", function(){
            jBox.remove();
        });
    },
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