(function($z){
$z.declare([
    'zui',
    "ui/quartz/quartz",
    "ui/quartz/eqz_by_week",
    "ui/quartz/eqz_by_month",
    "ui/quartz/eqz_by_adv",
], function(ZUI, Quartz){
//==============================================
var html = function(){/*
<div class="ui-arena quartz" ui-fitparent="yes">
    <div class="qz-tabs">
        <ul>
            <li mode="week" >{{quartz.tab.by_week}}</li>
            <li mode="month">{{quartz.tab.by_month}}</li>
            <li mode="adv"  >{{quartz.tab.by_adv}}</li>
        </ul>
    </div>
    <div class="qz-main"><div class="qz-main-con" ui-gasket="main"></div></div>
</div>
*/};
//==============================================
return ZUI.def("ui.quartz", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "theme/ui/quartz/quartz.css",
    i18n : "ui/quartz/i18n/{{lang}}.js",
    //...............................................................
    events : {
        "click .qz-tabs li" : function(e){
            var UI  = this;
            var jLi = $(e.currentTarget);
            if(jLi.hasClass("qz-tab-current"))
                return;
            // 切换视图
            UI.showView(jLi.attr("mode"));
        }
    },
    //...............................................................
    showView : function(mode, str) {
        var UI = this;
        // 高亮标签
        UI.arena.find(".qz-tabs li").removeClass("current")
        UI.arena.find(".qz-tabs li[mode="+mode+"]").addClass("current");
        // 切换子视图
        str = str || UI.getData();
        seajs.use("ui/quartz/eqz_by_" + mode, function(SubUI){
            UI.uiMain = new SubUI({
                parent : UI,
                gasketName : "main"
            }).render(function(){
                this.setData(str);
            });
        });
    },
    //...............................................................
    setData : function(str){
        var UI = this;
        var qz = new Quartz(str);
        var mode;
        // 精确到分秒，必须高级
        if(qz.isTiny()){
            mode = "adv";
        }
        // 月视图
        else if(qz.isMonthly()){
            mode = "month";
        }
        // 默认是周
        else {
            mode = "week";
        }
        mode = "adv"
        // 显示编辑视图
        UI.showView(mode, str);
    },
    //...............................................................
    getData : function(){
        var UI = this;
        return UI.uiMain ? UI.uiMain.getData() : null;
    },
    //...............................................................
    _count_times_array : function(qz){
        var ary = [];
        ary[23] = null;
        qz.each(ary, function(ary, index){
            ary[index] = index * 3600;
        });
        return Quartz.compact(ary);
    },
    //...............................................................
    _compact_vals : function(ary, dft){
        if(ary.length == 0)
            return dft;
        // 两个以内没必要压缩
        if(ary.length<=2)
            return ary.join(",");
        // 超过三个，可能压缩成范围
        var re    = [];
        var scope = [ary[0]];
        for(var i=1;i<ary.length;i++){
            var v = ary[i];
            // 判断是否为连续连续: scope.length == 1
            if(scope.length == 1 && (scope[0]+1) == v){
                scope[1] = v;
            }
            // 判断是否为连续连续: scope.length == 2
            else if(scope.length == 2 && (scope[1]+1) == v){
                scope[1] = v;
            }
            // 那么不连续
            else{
                re.push(scope.join("-"));
                scope = [v];
            }
        }
        re.push(scope.join("-"));
        return re.join(",");
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);