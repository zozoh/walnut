(function($z){
$z.declare([
    'zui',
    "ui/quartz/quartz",
], function(ZUI, Quartz){
//==============================================
var html = function(){/*
<div class="ui-arena qz-month" ui-fitparent="yes">
    <div class="qz-explain"></div>
    <div class="qz-month-mo">
        <h3><b>{{quartz.pick_month_mo}}</b> <u do="clear">{{clear}}</u></h3>
        <section>
            <ul>
                <li val="1">{{quartz.exp.month.dict[0]}}</li>
                <li val="2">{{quartz.exp.month.dict[1]}}</li>
                <li val="3">{{quartz.exp.month.dict[2]}}</li>
                <li val="4">{{quartz.exp.month.dict[3]}}</li>
                <li val="5">{{quartz.exp.month.dict[4]}}</li>
                <li val="6">{{quartz.exp.month.dict[5]}}</li>
                <li val="7">{{quartz.exp.month.dict[6]}}</li>
                <li val="8">{{quartz.exp.month.dict[7]}}</li>
                <li val="9">{{quartz.exp.month.dict[8]}}</li>
                <li val="10">{{quartz.exp.month.dict[9]}}</li>
                <li val="11">{{quartz.exp.month.dict[10]}}</li>
                <li val="12">{{quartz.exp.month.dict[11]}}</li>
            </ul>
            <div class="qz-month-W">
                <span><i class="fa fa-square-o"></i><i class="fa fa-check-square"></i></span>
                <b>{{quartz.pick_month_W}}</b>
            </div>
        </section>
    </div>
    <div class="qz-month-da">
        <h3><b>{{quartz.pick_month_da}}</b> <u do="clear">{{clear}}</u></h3>
        <section>
            <ul>
                <li>1</li><li>2</li><li>3</li><li>4</li><li>5</li>
                <li>6</li><li>7</li><li>8</li><li>9</li><li>10</li>
                <li>11</li><li>12</li><li>13</li><li>14</li><li>15</li>
                <li>16</li><li>17</li><li>18</li><li>19</li><li>20</li>
                <li>21</li><li>22</li><li>23</li><li>24</li><li>25</li>
                <li>26</li><li>27</li><li>28</li><li>29</li><li>30</li>
                <li>31</li>
            </ul>
        </section>
    </div>
    <h3><b>{{quartz.pick_time}}</b> <u do="clear">{{clear}}</u></h3>
    <section class="qz-time-hour"></section>
</div>
*/};
//==============================================
return ZUI.def("ui.quartz_by_month", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    events : {
        "click .qz-month section li" : function(e){
            var UI = this;
            var jq = $(e.currentTarget);
            // 不标记日期
            if(jq.attr("checked")){
                jq.removeAttr("checked");
            }
            // 如果选上了日期 ...
            else{
                jq.attr("checked", "yes");
                // 那么就自动去掉工作日
                UI.arena.find(".qz-month-W").removeAttr("checked");
            }
            // 更新解释说明
            UI._update_explain();
        },
        "click .qz-month-W" : function(e){
            var UI = this;
            var jq = $(e.currentTarget);
            // 不标记工作日
            if(jq.attr("checked")){
                jq.removeAttr("checked");
            }
            // 如果选上了工作日 ...
            else{
                jq.attr("checked", "yes");
                // 那么久全部清空日期
                UI.arena.find(".qz-month-da li[checked]").removeAttr("checked");
            }
            // 更新解释说明
            UI._update_explain();
        },
        "click h3 u[do=clear]" : function(e){
            var UI = this;
            var jSe = $(e.currentTarget).closest("h3").next();
            if(jSe.is(".qz-time-hour")){
                jSe.timelist("clear");
            }else{
                jSe.find("li[checked]").removeAttr("checked");
            }
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
        //console.log(qz.toString())
        UI.arena.find(".qz-explain").text(qz.toText(UI.msg("quartz.exp")));
    },
    //...............................................................
    setData : function(qz){
        var UI = this;
        // 解析表达式
        qz = Quartz(qz);

        // 显示说明
        UI._update_explain(qz);

        // 绘制月
        UI.arena.find(".qz-month-mo li").removeClass("checked")
        .each(function(){
            var jLi = $(this);
            var mon = jLi.attr("val") * 1;
            if(qz.matchMonth(mon)){
                jLi.attr("checked", "yes");
            }
        });

        // 工作日
        if(qz.isWorkingDay()){
            UI.arena.find(".qz-month-W").attr("checked","yes");
        }

        // 绘制日
        UI.arena.find(".qz-month-da li").removeClass("checked")
        .each(function(){
            var jLi = $(this);
            var day = jLi.text() * 1;
            if(qz.matchDayInMonth(day)){
                jLi.attr("checked", "yes");
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

        // 获取月份
        var mons = [];
        UI.arena.find(".qz-month-mo li[checked]").each(function(){
            mons.push($(this).attr("val") * 1);
        });
        var monStr = UI.parent._compact_vals(mons, "*", "1-12");

        // 获取日期
        var days = [];
        UI.arena.find(".qz-month-da li[checked]").each(function(){
            days.push($(this).text() * 1);
        });
        var dayStr = UI.parent._compact_vals(days, "*", "1-31");

        // 是否是工作日
        if(UI.arena.find(".qz-month-W").attr("checked")){
            dayStr = dayStr == "*" ? "W" : dayStr+"W";
        }

        // 得到表达式
        return "0 0 " + hrStr + " " + dayStr + " " + monStr + " ?";
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);