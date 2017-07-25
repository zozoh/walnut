(function($z){
$z.declare([
    'zui',
    'ui/zcron/support/zcr_methods',
    'ui/form/c_array',
], function(ZUI, ZCronMethods, ArrayUI){
//==============================================
var html = function(){/*
<div class="ui-arena zcr-hms">
    <table>
        <tr><td>时</td><td ui-gasket="hrs"></td></tr>
        <tr><td>分</td><td ui-gasket="min"></td></tr>
    </table>
</div>
*/};
//==============================================
return ZUI.def("ui.zcron_hms", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    // 监听父控件的消息
    init : function(opt) {
        var UI = ZCronMethods(this);
        UI.listenUI(this.parent.parent, "data:change", this.update);
    },
    //...............................................................
    redraw : function(){
        var UI = this;
        //..............................................
        // 小时
        new ArrayUI({
            parent : UI,
            gasketName : "hrs",
            items : function(){
                var re = [];
                for(var i=0;i<24;i++)
                    re[i] = i;
                return re;
            },
            on_change : function(v){
                UI.notifyChange();
            }
            
        }).render(function(){
            UI.defer_report("hrs");
        });
        //..............................................
        // 分钟
        new ArrayUI({
            parent : UI,
            gasketName : "min",
            items : function(){
                var re = [];
                for(var i=0;i<60;i++)
                    re[i] = i;
                return re;
            },
            on_change : function(v){
                UI.notifyChange();
            }
            
        }).render(function(){
            UI.defer_report("min");
        });
        //..............................................
        // 返回延迟加载
        return ["hrs", "min"];
    },
    //...............................................................
    notifyChange : function(){
        var str = this.getData();
        this.cronUI().setPart(0, null).setPart(1, str);
    },
    //...............................................................
    update : function(ozc) {
        var UI  = this;
        
        // 更新日期
        this.setCronToArrayUI(this.gasket.hrs, ozc, "matchHour");
        this.setCronToArrayUI(this.gasket.min, ozc, "matchMinute");
    },
    //...............................................................
    getData : function(){
        var HH = this.getStrFromArrayUI(this.gasket.hrs, "0-23", "0");
        var mm = this.getStrFromArrayUI(this.gasket.min, "0-59", "0");
        
        return [0,mm,HH].join(" ");
    },
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);