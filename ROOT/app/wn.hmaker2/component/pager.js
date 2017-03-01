(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/support/hm__methods_com',
    '/gu/rs/ext/hmaker/hmc_pager.js'
], function(ZUI, Wn, HmComMethods){
//==============================================
//==============================================
var html = '<div class="ui-arena hmc-pager hm-empty-save"></div>';
//==============================================
return ZUI.def("app.wn.hm_com_pager", {
    dom     : html,
    keepDom : true,
    className : "!hm-com-pager",
    //...............................................................
    init : function(){
        HmComMethods(this);
    },
    //...............................................................
    events : {
        // 跳转页码
        "click .pg_nbs a" : function(e) {
            var UI = this;

            // 在激活的组件内容才生效
            if(!UI.isActived())
                return;

            // 跳转页码
            var pn = $(e.currentTarget).text() * 1;
            UI.arena.hmc_pager("jumpTo", pn);
        },
        // 首/尾页
        "click .pg_btn [jump-to]" : function(e){
            var UI = this;

            // 在激活的组件内容才生效
            if(!UI.isActived())
                return;

            // 跳转页码
            var pn = $(e.currentTarget).attr("jump-to") * 1;
            UI.arena.hmc_pager("jumpTo", pn);
        },
        // 前/后页
        "click .pg_btn [jump-off]" : function(e){
            var UI = this;

            // 在激活的组件内容才生效
            if(!UI.isActived())
                return;

            // 跳转页码
            var off = $(e.currentTarget).attr("jump-off") * 1;
            UI.arena.hmc_pager("jumpOff", off);
        }
    },
    //...............................................................
    paint : function(com) {
        var UI = this;

        // 标识保存时属性
        UI.arena.addClass("hm-empty-save");

        // 得到旧值
        var pg  = UI.arena.hmc_pager("getData");
        pg.pgsz = com.dftPageSize;
        pg.pn   = pg.pn   || 1;
        pg.pgnb = pg.pgnb || 24;
        pg.sum  = pg.sum  || pg.pgsz * pg.pgnb;
        pg.pgnb = Math.ceil(pg.sum / pg.pgsz);
        console.log(pg)
                

        // 绘制
        UI.arena.hmc_pager(_.extend({forIDE:true}, com));
        UI.arena.hmc_pager("value", pg);
    },
    //...............................................................
    getComValue : function() {
        return this.arena.hmc_pager("value");
    },
    setComValue : function(pg) {
        this.arena.hmc_pager("value", pg);
    },
    //...............................................................
    getBlockPropFields : function(block) {
        return [block.mode == 'inflow' ? "margin" : null,
                "padding","border","borderRadius","_align",
                "color", "background",
                "boxShadow","overflow"];
    },
    //...............................................................
    getDefaultData : function(){
        return {
            pagerType    : "button",
            freeJump     : true,
            dftPageSize  : 50,
            showFirstLast: "auto",
            showPrevNext : "auto",
            btnFirst     : "|<<",
            btnPrev      : "<",
            btnNext      : ">",
            btnLast      : ">>|",
            showBrief    : true,
            briefText    : this.msg("hmaker.com.pager.brief_dft"),
        };
    },
    //...............................................................
    // 返回属性菜单， null 表示没有属性
    getDataProp : function(){
        return {
            uiType : 'app/wn.hmaker2/com_prop/pager_prop',
            uiConf : {}
        };
    }
});
//===================================================================
});
})(window.NutzUtil);