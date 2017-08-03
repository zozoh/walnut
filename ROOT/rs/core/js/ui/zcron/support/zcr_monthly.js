(function($z){
$z.declare([
    'zui',
    'ui/zcron/support/zcr_methods',
    'ui/form/c_array',
], function(ZUI, ZCronMethods, ArrayUI){
//==============================================
var html = function(){/*
<div class="ui-arena zcr-monthly">
    <div class="zm-days" ui-gasket="list"></div>
    <div class="zm-bool zm-work">
        <span class="zmw-icon">
            <i class="zmdi zmdi-square-o"></i>
            <i class="zmdi zmdi-check-square"></i>
        </span>
        <em>工作日</em>
    </div>
    <div class="zm-bool zm-last">
        <span class="zmw-icon">
            <i class="zmdi zmdi-square-o"></i>
            <i class="zmdi zmdi-check-square"></i>
        </span>
        <em>倒数模式</em>
    </div>
</div>
*/};
//==============================================
return ZUI.def("ui.zcron_monthly", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    // 监听父控件的消息
    init : function(opt) {
        ZCronMethods(this);
    },
    //...............................................................
    events : {
        'click .zm-bool > *' : function(e) {
            var jBo = $(e.currentTarget).closest(".zm-bool");
            $z.toggleAttr(jBo, "enabled", "yes");
            // 通知改动
            this.cronUI().notifyStdDatePartChange();
        }
    },
    //...............................................................
    redraw : function(){
        var UI = this;

        // 日期范围
        new ArrayUI({
            parent : UI,
            gasketName : "list",
            groupSize  : 7,
            keepArray  : true,
            items : function(){
                var re = [];
                for(var i=0;i<31;i++)
                    re[i] = i+1;
                return re;
            },
            on_change : function(v){
                UI.cronUI().notifyStdDatePartChange();
            }
            
        }).render(function(){
            UI.defer_report("list");
        });

        // 返回延迟加载
        return ["list"];
    },
    //...............................................................
    update : function(ozc) {
        var UI  = this;
        var jWo = UI.arena.find(".zm-work");
        var jLa = UI.arena.find(".zm-last");
        //console.log(ozc)
        
        // 工作日
        jWo.attr("enabled", ozc.isWorkingDay()  ? "yes" : null);
        // 倒数模式
        jLa.attr("enabled", ozc.isFromLastDay() ? "yes" : null);
        // 更新日历表单选模式
        if(jWo.attr("enabled") || jLa.attr("enabled")){
            UI.gasket.list.setMulti(false);
        }else{
            UI.gasket.list.setMulti(true);
        }

        // 更新日期
        UI.setCronToArrayUI(UI.gasket.list, ozc, "matchDayInMonth");
    },
    //...............................................................
    getData : function(){
        var UI  = this;
        var jWo = UI.arena.find(".zm-work");
        var jLa = UI.arena.find(".zm-last");

        var str = UI.getStrFromArrayUI(UI.gasket.list, "1-31", "*");
        //console.log("getData", str, UI.gasket.list.getData())
        // 工作日选中
        if(jWo.attr("enabled") || jLa.attr("enabled")){
            // 所有日期的话，忽略倒数模式
            if("*" == str){
                return jWo.attr("enabled") ? "W" : "*";
            }
            //............................................
            // 不能有范围和步长
            var ss = str.split(/[,-]/g);
            // 准备填充工作日和倒数模式
            var aa = ["","",""];
            //............................................
            // 倒数模式选最后一个日期
            if(jLa.attr("enabled") && "*"!=ss[0]){
                aa[0] = 32 - (ss[ss.length-1]*1);
                aa[1] = "L";
            }
            // 否则选第一个日期
            else {
                aa[0] = ss[0];
            }
            //............................................
            // 工作日
            if(jWo.attr("enabled")){
                aa[2] = "W";
            }
            //............................................
            // 嗯，返回吧
            return aa.join("");
        }
        //................................................
        // 没选中就是日期吧
        return str;
    },
    //...............................................................
    resize : function(){
        var UI  = this;
        var jDa = UI.arena.find(".zm-days");
        var jBo = UI.arena.find(".zm-bool");
        jBo.css("width", jDa.children().first().outerWidth());
    },
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);