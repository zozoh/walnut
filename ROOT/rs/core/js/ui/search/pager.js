(function($z){
$z.declare('zui', function(ZUI){
//==============================================
var html = function(){/*
<div class="ui-arena srh-pgr">
    <b class="pgr-btn" first>{{srh.pager.first}}</b>
    <b class="pgr-btn" prev >{{srh.pager.prev}}</b>
    <b class="pgr-btn" next >{{srh.pager.next}}</b>
    <b class="pgr-btn" last >{{srh.pager.last}}</b>
    <em class="pgr-tip"></em>
</div>
*/};
//==============================================
return ZUI.def("ui.srh_pgr", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    init : function(options){
    },
    //..............................................
    events : {
        "click .pgr-btn" : function(e){
            var UI   = this;
            var jBtn = $(e.currentTarget);
            if(jBtn.hasClass("pgr-disable"))
                return;

            // 得到数据
            var pg = UI._parse_pager(UI.$el.data("@DATA"));
            pg.pn  = jBtn.attr("pn") * 1;
            // console.log(pg);
            // console.log(UI.getData(pg))
            UI.trigger("pager:change", UI.getData(pg));
        }
    },
    //..............................................
    redraw : function(callback){
    },
    //..............................................
    resize : function(){
    },
    //..............................................
    // 
    setData : function(pg){
        var UI  = this;
        var opt = UI.options;
        // 默认值
        pg = UI._parse_pager(pg);

        // 进行计算
        var last = Math.max(1, pg.pgnb);
        UI.arena.find(".pgr-tip").text(UI.msg("srh.pager.tip", pg));
        UI.arena.find("[first]").attr("pn", 1);
        UI.arena.find("[prev]" ).attr("pn", Math.max(1, pg.pn-1));
        UI.arena.find("[next]" ).attr("pn", Math.min(last, pg.pn+1));
        UI.arena.find("[last]" ).attr("pn", last);
        // 看看能不能向前翻页
        if(pg.pn <= 1){
            UI.arena.find("[first],[prev]")
                .addClass("pgr-disable")
                .removeClass("pgr-enable");
        }else{
            UI.arena.find("[first],[prev]")
                .addClass("pgr-enable")
                .removeClass("pgr-disable");
        }
        // 看看能不能向后翻页
        if(pg.pn >= pg.pgnb){
            UI.arena.find("[next],[last]")
                .addClass("pgr-disable")
                .removeClass("pgr-enable");
        }else{
            UI.arena.find("[next],[last]")
                .addClass("pgr-enable")
                .removeClass("pgr-disable");
        }
        // 保存一下数据
        UI.$el.data("@DATA", _.extend({}, pg));
    },
    //..............................................
    _parse_pager : function(pg){
        return _.extend({
            pn   : 1,     // 第几页
            pgsz : 50,    // 每页多少数据
            pgnb : 1,     // 一共多少页
            sum  : 0,     // 一共多少记录
            nb   : 0      // 本页实际获取了多少数据
        }, pg || this.options.dft);
    },
    //..............................................
    getData : function(pg){
        var UI = this;
        pg = pg || UI._parse_pager(UI.$el.data("@DATA"));
        return {
            limit : pg.pgsz,
            skip  : (pg.pn - 1) * pg.pgsz
        };
    }
    //..............................................
});
//==================================================
});
})(window.NutzUtil);