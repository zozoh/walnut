(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/support/hm__methods',
], function(ZUI, Wn, HmPanelMethods){
//==============================================
var html = '<div class="ui-arena hm-combar"></div>';
//==============================================
return ZUI.def("app.wn.hmpg_combar", {
    dom  : html,
    //...............................................................
    init : function() {
        var UI = HmPanelMethods(this);
        
        UI.listenBus("active:com",  UI.updateComPath);
        UI.listenBus("active:page", function(){
            UI.emptyComPath();   
        });
    },
    //...............................................................
    events : {
        'click .hm-combar-item' : function(e){
            var UI  = this;
            var jPi = $(e.currentTarget);

            // 忽略当前
            if("current" == jPi.attr("mode"))
                return;

            // 得到关键信息
            var comId  = jPi.attr("com-id");
            var areaId = jPi.attr("area-id");

            // 区域
            if(areaId) {
                var uiCom = UI.pageUI().getCom(comId);
                uiCom.notifyActived(null, areaId);
            }
            // 组件
            else if(comId) {
                var uiCom = UI.pageUI().getCom(comId);
                // 确保取消高亮
                $z.invoke(uiCom, "highlightArea", [false]);
                // 通知
                uiCom.notifyActived(null);
            }
            // 页面
            else {
                UI.fire("active:page");
            }
        }
    },
    //...............................................................
    redraw : function(){
        this.emptyComPath();
    },
    //...............................................................
    emptyComPath : function(notCleanCache) {
        var UI = this;
        if(!notCleanCache) {
            UI.__current_com_path = [];
        }
        UI.arena.empty().prepend(UI.compactHTML(`<div class="hm-combar-item" current="yes">
            <b>{{hmaker.page.body}}</b>
            <i class="zmdi zmdi-chevron-right"></i>
        </div>`));
    },
    //...............................................................
    updateComPath : function(uiCom) {
        var UI = this;
        jCom = uiCom.$el;

        // 看看是否可以复用当前路径
        UI.__current_com_path = UI.__current_com_path || [];
        var my_com_ph = uiCom.getComPath(true);
        var can_reuse = (UI.__current_com_path.length > my_com_ph.length);
        // 仔细判断一下，必须全部匹配才可以复用
        if(can_reuse) {
            for(var i=0; i<my_com_ph.length; i++) {
                if(!_.isEqual(my_com_ph[i], UI.__current_com_path[i])){
                    can_reuse = false;
                    break;
                }
            }
        }

        // 更新高亮项目的下标
        UI.__current_index = my_com_ph.length - 1;

        // 如果不能复用，则刷新显示
        if(!can_reuse) {
            // 首先更新记录
            UI.__current_com_path = my_com_ph;

            // 清空
            UI.emptyComPath(true);

            // 重新绘制
            for(var i=0; i<UI.__current_com_path.length; i++) {
                var pi = UI.__current_com_path[i];
                var html = '<div class="hm-combar-item">';
                // 控件的话，绘制 Icon
                if("_area" != pi.ctype){
                    // 组件
                    if(pi.lib) {
                        html += '<span class="pi-icon">' + UI.msg("hmaker.lib.icon") + '</span>';
                    }
                    // 普通控件
                    else {
                        html += '<span class="pi-icon">' + UI.msg("hmaker.com." + pi.ctype + ".icon") + '</span>';
                    }
                }
                html += '<b>' + (pi.areaId || pi.comId) + '</b>';
                html += '<i class="zmdi zmdi-chevron-right"></i>';
                html += '</div>';
                $(html).attr({
                    "ctype"        : pi.ctype,
                    "com-id"       : pi.comId,
                    "area-id"      : pi.areaId,
                    "lib-name"     : pi.lib || null,
                    "data-balloon" : UI.msg("hmaker.com." + pi.ctype + ".name"),
                    "data-balloon-pos" : "down"
                }).appendTo(UI.arena);
            }
        }

        // 更新一下显示
        UI.arena.children().each(function(index){
            var n = index - 1;
            // 祖先项
            if(n < UI.__current_index){
                $(this).attr("mode", "parent");
            }
            // 当前项
            else if(n == UI.__current_index) {
                $(this).attr("mode", "current");
            }
            // 子项
            else {
                $(this).attr("mode", "child");
            }
        });
        
    },
    //...............................................................
    __update_path_display : function(){

    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);