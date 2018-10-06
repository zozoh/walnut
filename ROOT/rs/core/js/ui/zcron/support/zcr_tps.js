(function($z){
$z.declare([
    'zui',
    'ui/zcron/support/zcr_methods',
    'ui/form/c_time',
], function(ZUI, ZCronMethods, TimeUI){
//==============================================
var html = function(){/*
<div class="ui-arena zcr-tps" >
    <section>
        <div class="zcr-tps-empty">{{zcron.tps_empty}}</div>
    </section>
    <footer>
        <a>
            <i class="zmdi zmdi-plus-circle-o-duplicate"></i>
            <span>{{zcron.tps_add}}</span>
        </a>
    </footer>
</div>
*/};
//==============================================
return ZUI.def("ui.zcron_tps", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    // 监听父控件的消息
    init : function(opt) {
        ZCronMethods(this);
    },
    //...............................................................
    events : {
        'click footer a' : function() {
            this.add_time_point();
        }
    },
    
    //...............................................................
    add_time_point : function(timeValue) {
        var UI = this;
        var jS = UI.arena.find(">section");

        // 清除空元素
        UI.arena.find('.zcr-tps-empty').remove();

        // 添加一个时间点
        var jUl = $(UI.compactHTML(`
            <ul>
                <li class="tps-time"></li>
                <!--li class="tps-del">
                    <a balloon="left:{{del}}"><i class="zmdi zmdi-close"></i></a>
                </li-->
            </ul>
        `)).appendTo(jS);

        // 添加时间编辑控件
        new TimeUI({
            parent : UI,
            $pel : jUl.find('li.tps-time'),
            editAs : "minute",
            on_change : function() {
                UI.notifyChange();
            }
        }).render(function(){
            if(_.isNumber(timeValue) || _.isString(timeValue))
                this.setData(timeValue);
            else
                this.setData(null);
        });
    },
    //...............................................................
    notifyChange : function(){
        var str = this.getData();
        this.cronUI().setPart(0, str).setPart(1, null);
    },
    //...............................................................
    update : function(ozc) {
        var UI = this;
        var jS = UI.arena.find(">section");

        // 清空所有的子
        UI.releaseAllChildren(true);
        jS.html(UI.compactHTML('<div class="zcr-tps-empty">{{zcron.tps_empty}}</div>'));

        //console.log(ozc)
        // this.setCronToArrayUI(this.gasket.list, ozc, "matchTime");
        // 得到时间点列表
        var tps = [];
        if(_.isArray(ozc.timeRepeaters) && ozc.timeRepeaters.length > 0) {
            tps = [].concat(ozc.timeRepeaters[0].timePoints);
        }

        // 至少有一个
        if(tps.length == 0)
            tps.push("08:00");

        // 循环追加时间点
        for(var i=0; i<tps.length; i++) {
            UI.add_time_point(tps[i]);
        }
    },
    //...............................................................
    getData : function(){
        var UI = this;
        // var tps = this.gasket.list.getData();
        // 得到时间点
        var tps = [];
        UI.arena.find(">section>ul>li.tps-time").each(function(){
            var uiTime = ZUI($(this).children('[ui-id]'));
            var tm = uiTime.getData();
            if(tm) {
                tps.push(tm);
            }
        });
        //console.log(tps)

        // 至少有一个
        if(tps.length == 0)
            tps.push("08:00");

        for(var i=0;i<tps.length;i++) {
            var sec = tps[i];
            var ti  = $z.parseTimeInfo(sec);
            tps[i]  = ti.toString();
        }
        
        return "T{" + tps.join(",") + "}";
    },
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);