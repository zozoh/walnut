(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/hm__methods_com'
], function(ZUI, Wn, HmComMethods){
//==============================================
var html = `
<div class="ui-code-template">
    <div code-id="noapi" class="warn">
        <i class="zmdi zmdi-alert-polygon"></i> {{hmaker.com.objlist.noapi}}
    </div>
    <div code-id="notemplate" class="warn">
        <i class="zmdi zmdi-alert-polygon"></i> {{hmaker.com.objlist.notemplate}}
    </div>
    <div code-id="filter" class="filter">

    </div>
</div>
<div class="hmc-objlist ui-arena hm-del-save"></div>
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

        //console.log("paint:", com)

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
        UI.arena.empty();

        // 确保有数据接口
        if(!com.api) {
            UI.ccode("noapi").appendTo(UI.arena);
            return false;
        }

        // 确保有显示模板
        if(!com.template) {
            UI.ccode("notemplate").appendTo(UI.arena);
            return false;
        }

        // 通过检查
        return true;
        
    },
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);