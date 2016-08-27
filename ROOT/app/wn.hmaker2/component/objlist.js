(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/hm__methods_com'
], function(ZUI, Wn, HmComMethods){
//==============================================
var html = `
<div class="ui-code-template">
    <div code-id="item" class="cnavmp-item">
        <span><i class="fa fa-circle-thin"></i></span>
        <span key="newtab">
            <u><i class="fa fa-check-square"></i><i class="fa fa-square-o"></i></u>
            <em>{{hmaker.com.navmenu.newtab}}</em>
        </span>
        <span key="text"></span>
        <span key="href"></span>
    </div>
    <div code-id="empty" class="cnavmp-empty">
        <i class="fa fa-warning"></i> {{hmaker.com.navmenu.empty}}
    </div>
</div>
<div class="hmc-objlist hm-del-save">
    <section class="hmc-ol-tip">
    </section>
    <section class="hmc-ol-main">
    </section>
</div>
`
//==============================================
return ZUI.def("app.wn.hm_com_objlist", {
    dom  : html,
    //...............................................................
    init : function(){
        var UI = HmComMethods(this);
    },
    //...............................................................
    events : {
        
    },
    //...............................................................
    setupProp : function(){
        return {
            uiType : 'app/wn.hmaker2/com_prop/objlist_prop.js',
            uiConf : {}
        };
    },
    //...............................................................
    paint : function(com) {
        var UI = this;

        // console.log(com)

        // 检查显示模式
        if(!UI.__check_mode(com)){
            return ;
        }

        // 绘制
        UI.__paint_filter(com);
        UI.__paint_sorter(com);
        UI.__paint_item(com);
        UI.__paint_pager(com);

    },
    //...............................................................
    __paint_filter : function(com) {
        var UI = this;

     },
     //...............................................................
    __paint_sorter : function(com) {
        var UI = this;

     },
     //...............................................................
    __paint_pager : function(com) {
        var UI = this;

    },
     //...............................................................
    __paint_item : function(com) {
        var UI = this;

    },
    //...............................................................
    __check_mode : function(com) {
        var UI = this;

        
    },
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);