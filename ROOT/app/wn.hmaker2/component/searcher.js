(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/support/hm__methods_com',
    '/gu/rs/ext/hmaker/hmc_searcher.js'
], function(ZUI, Wn, HmComMethods){
//==============================================
var html = `<div class="ui-arena hmc-searcher hm-empty-save">
    <div class="kwd-input"><input></div>
    <div class="kwd-btn"><b></b></div>
</div>`;
//==============================================
return ZUI.def("app.wn.hm_com_searcher", {
    dom     : html,
    keepDom   : true,
    className : "!hm-com-searcher",
    //...............................................................
    init : function(){
        HmComMethods(this);
    },
    //...............................................................
    paint : function(com) {
        var UI = this;
        
        // 标识保存时属性
        UI.arena.addClass("hm-empty-save");

        // 绘制
        UI.arena.hmc_searcher(_.extend({
            forIDE : true
        }, com));
        
        // 标识是否是链接
        UI.arena.find('aside').remove();
        if(com.postAction) {
            var icon = '<i class="zmdi zmdi-open-in-new"></i>';
            if("_self" == com.postTarget) {
                icon = '<i class="zmdi zmdi-link"></i>';
            }
            $('<aside>').html(icon).appendTo(UI.arena);
        }

        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 标识动态读取请求参数
        var m = /^@<([^>]+)>$/.exec(com.defaultValue);
        if(m) {
            $('<div class="hmcs-loadreq">')
                .text(m[1]).attr({
                    "data-balloon"     : UI.msg("hmaker.com.searcher.loadreq_tip", {
                            pmnm : m[1]
                        }),
                    "data-balloon-pos" : "right",
                }).appendTo(UI.arena);
            UI.$el.attr("hm-loadreq", "yes");
        }
        // 否则去掉标识
        else {
            UI.$el.removeAttr("hm-loadreq");
        }

    },
    //...............................................................
    getComValue : function() {
        return this.arena.hmc_searcher("value");
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
            "placeholder"  : this.msg("hmaker.com.searcher.plhd_dft"),
            "btnText"      : "",
            "defaultValue" : "",
            "maxLen"       : 0,
            "trimSpace"    : true,
        };
    },
    //...............................................................
    // 返回属性菜单， null 表示没有属性
    getDataProp : function(){
        return {
            uiType : 'app/wn.hmaker2/com_prop/searcher_prop',
            uiConf : {}
        };
    }
});
//===================================================================
});
})(window.NutzUtil);