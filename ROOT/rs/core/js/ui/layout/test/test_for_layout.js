(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/layout/layout',
], function(ZUI, Wn, LayoutUI){
//==============================================
var html = function(){/*
<div class="ui-arena test-for-layout" ui-fitparent="true">
    
</div>
*/};
//==============================================
return ZUI.def("ui.test_for_thing", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //..............................................
    redraw : function() {
        var UI = this;

        // 加载对象编辑器
        new ThManagerUI({
            parent : UI,
            gasketName : "main",
            dataMode : "thing",
            objMenu : function(th){
                if(th.th_live == -1) {
                    return null;
                }
                return [{
                    text : "haha",
                    handler : function(){
                        var o = this.getData();
                        console.log(o)
                    }
                }];   
            },
            // fields : [{
            //     key   : "id",
            //     title : "ID",
            //     hide : true,
            // }, {
            //     key   : "th_nm",
            //     title : "名称",
            // }]
            ddetail : null
        }).render(function(){
            UI.defer_report("main");
        });

        // 返回延迟加载
        return ["main"];
    },
    //..............................................
    update : function(o) {
        this.gasket.main.update(o);
    }
    //..............................................
});
//==================================================
});
})(window.NutzUtil);