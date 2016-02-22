(function($z){
$z.declare([
    'zui',
    "ui/quartz/quartz"
], function(ZUI, Quartz){
//==============================================
var html = function(){/*
<div class="ui-arena qz-week" ui-fitparent="yes">
    <div class="qz-explain"></div>
    <h3><b>{{quartz.pick_week}}</b> <u do="clear">{{clear}}</u></h3>
    <section class="qz-week-day">
        <ul>
            <li val="1">{{quartz.exp.week.dict[0]}}</li>
            <li val="2">{{quartz.exp.week.dict[1]}}</li>
            <li val="3">{{quartz.exp.week.dict[2]}}</li>
            <li val="4">{{quartz.exp.week.dict[3]}}</li>
            <li val="5">{{quartz.exp.week.dict[4]}}</li>
            <li val="6">{{quartz.exp.week.dict[5]}}</li>
            <li val="7">{{quartz.exp.week.dict[6]}}</li>
        </ul>
    </section>
    <h3><b>{{quartz.pick_time}}</b> <u do="clear">{{clear}}</u></h3>
    <section class="qz-time-hour"></section>
</div>
*/};
//==============================================
return ZUI.def("ui.quartz_by_week", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    events : {
        "click h3 u[do=clear]" : function(e){
            var UI = this;
            var jSe = $(e.currentTarget).closest("h3").next();
            if(jSe.is(".qz-time-hour")){
                jSe.timelist("clear");
            }else{
                jSe.find("li.checked").removeClass("checked");
            }
            UI._update_explain();
        },
        "click .qz-week-day li" : function(e){
            var UI = this;
            $(e.currentTarget).toggleClass("checked");
            UI._update_explain();
        }
    },
    //...............................................................
    redraw : function(){
        var UI  = this;
        var opt = UI.options;
        var jHr = UI.arena.find(".qz-time-hour");
        jHr.timelist({
            display   : "vertical",
            groupUnit : 8,
            scopes    : opt.timeScopes,
            on_change : function(){
                UI._update_explain();
            }
        });
    },
    //...............................................................
    _update_explain : function(qz) {
        var UI = this;
        qz = Quartz(qz || UI.getData());
        UI.arena.find(".qz-explain").text(qz.toText(UI.msg("quartz.exp")));
    },
    //...............................................................
    setData : function(qz){
        var UI = this;
        // 解析表达式
        qz = Quartz(qz);

        // 显示说明
        UI._update_explain(qz);

        // 绘制星期
        UI.arena.find(".qz-week-day li").removeClass("checked")
        .each(function(){
            var jLi = $(this);
            var day = jLi.attr("val") * 1;
            if(qz.matchDayInWeek(day)){
                jLi.addClass("checked");
            }
        });

        // 绘制时间
        var tps = UI.parent._count_times_array(qz);
        UI.arena.find(".qz-time-hour").timelist("set", tps);
    },
    //...............................................................
    getData : function(){
        var UI = this;
        // 获取时间
        var hrs = UI.arena.find(".qz-time-hour").timelist("get", "H");
        var hrStr = UI.parent._compact_vals(hrs, "0");

        // 获取星期
        var weekDays = [];
        UI.arena.find(".qz-week-day li.checked").each(function(){
            weekDays.push($(this).attr("val") * 1);
        });
        var weekStr = UI.parent._compact_vals(weekDays, "?", "1-7");

        // 得到表达式
        return "0 0 " + hrStr + " * * " + weekStr;
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);