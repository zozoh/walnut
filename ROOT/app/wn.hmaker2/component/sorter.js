(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/support/hm__methods_com',
], function(ZUI, Wn, HmComMethods){
//==============================================
var html = '<div class="ui-arena hmc-sorter hmc-cnd">hahahahaha</div>';
//==============================================
return ZUI.def("app.wn.hm_com_sorter", {
    dom     : html,
    keepDom   : true,
    //...............................................................
    init : function(){
        HmComMethods(this);
    },
    //...............................................................
    events : {
        // 设置默认值
        "click ul li" : function(e) {
            if(this.isActived()) {
                var jLi = $(e.currentTarget);
                var index = jLi.prevAll().length;
                this.setEnabled(jLi.attr("enabled") ? -1 : index);
            }
        }
    },
    //...............................................................
    redraw : function(){
        this.arena.addClass("hmc-cnd");
    },
    //...............................................................
    paint : function(com) {
        var UI = this;

        // 清空绘制区
        UI.arena.empty();

        // 绘制
        if(_.isArray(com.fields) && com.fields.length > 0){
            // 绘制显示项目
            var jUl = $('<ul>').appendTo(UI.arena);
            for(var i=0; i<com.fields.length; i++) {
                UI.__draw_fld(com.fields[i], jUl);
            }
        }
        // 显示空
        else {
            UI.arena.html(UI.compactHTML(`<div class="empty">
                <i class="zmdi zmdi-alert-circle-o"></i>
                {{hmaker.com.filter.empty}}
            </div>`));
        }
    },
    //...............................................................
    setEnabled : function(index, com) {
        var UI  = this;
        com = com || UI.getData();

        if(_.isArray(com.fields)) {
            for(var i=0; i<com.fields.length; i++) {
                com.fields[i].enabled = (index == i);
            }
            UI.saveData(null, com, true);
        }
    },
    //...............................................................
    getComValue : function() {
        var jLi = this.arena.find('li[enabled]');
        if(jLi.length > 0) {
            var re = jLi.attr("key") + ":" + (jLi.attr("or-val")||1); 
            var or_fixed = jLi.attr("or-fixed");
            return re + (or_fixed ? "," + or_fixed : "");
        }
        return null;
    },
    //...............................................................
    // 绘制项目
    __draw_fld : function(fld, jUl) {
        var UI  = this;

        // 得到排序值的 key
        var orKey = fld.order == 1 ? "asc" : "desc";

        var jLi = $('<li>').attr({
            "key"    : fld.name,
            "modify" : fld.modify ? "yes" : null,
            "or-val" : fld.order,
            "or-nm"  : orKey,
            "enabled" : fld.enabled ? "yes" : null,
        });
        
        // 绘制字段标题
        $('<em>').text(fld.text).appendTo(jLi);

        // 绘制排序图标
        $('<span>').attr("or-icon", orKey).appendTo(jLi);

        // 设置固定排序项目
        if(_.isArray(fld.items) && fld.items.length > 0) {
            var orFixed = [];
            for(var i=0; i<fld.items.length; i++) {
                var it = fld.items[i];
                orFixed.push(it.name + ":" + it.order);
            }
            jLi.attr("or-fixed", orFixed.join(","));
        }


        // 加入 DOM
        jUl.append(jLi);
    },
    //...............................................................
    getBlockPropFields : function(block) {
        return [block.mode == 'inflow' ? "margin" : null,
                "padding","border","borderRadius",
                "color", "background",
                "boxShadow","overflow"];
    },
    //...............................................................
    getDefaultData : function(){
        return {
            // "lineHeight" : ".24rem",
            // "fontSize"   : ".14rem",
        };
    },
    //...............................................................
    // 返回属性菜单， null 表示没有属性
    getDataProp : function(){
        return {
            uiType : 'app/wn.hmaker2/com_prop/sorter_prop',
            uiConf : {}
        };
    }
});
//===================================================================
});
})(window.NutzUtil);