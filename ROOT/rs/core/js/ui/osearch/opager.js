(function($z){
$z.declare('zui', function(ZUI){
//==============================================
var html = function(){/*
<div class="ui-arena ui-noselect">
    <b class="opager-btn" first>{{osearch.pager.first}}</b>
    <b class="opager-btn" prev >{{osearch.pager.prev}}</b>
    <em class="opager-tip"></em>
    <b class="opager-btn" next >{{osearch.pager.next}}</b>
    <b class="opager-btn" last >{{osearch.pager.last}}</b>
</div>
*/};
//==============================================
return ZUI.def("ui.opager", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    init : function(options){
    },
    //..............................................
    events : {
        "click .opager-btn" : function(e){
            var jBtn = $(e.currentTarget);
            if(jBtn.hasClass("opager-disable"))
                return;
            var UI = ZUI.checkInstance(e.currentTarget);
            var pg = _.extend({}, UI.getData());
            pg.skip = jBtn.attr("skip") * 1;
            pg.pn = parseInt(pg.skip / pg.pgsz) + 1;
            // console.log(pg);
            // console.log(UI.getData())
            UI.trigger("pager:change", pg);
        }
    },
    //..............................................
    redraw : function(callback){
    },
    //..............................................
    resize : function(){
    },
    //..............................................
    setData : function(pg){
        var UI = this;
        //console.log(pg)
        var last = (pg.pgnb-1)*pg.pgsz;
        UI.arena.find(".opager-tip").text(UI.msg("osearch.pager.tip", pg));
        UI.arena.find("[first]").attr("skip", 0);
        UI.arena.find("[prev]").attr("skip", Math.max(0, (pg.pn-2)*pg.pgsz));
        UI.arena.find("[next]").attr("skip", Math.min(last,(pg.pn)*pg.pgsz));
        UI.arena.find("[last]").attr("skip", last);
        // 看看能不能向前翻页
        if(pg.pn <= 1){
            UI.arena.find("[first],[prev]").addClass("opager-disable");
        }else{
            UI.arena.find("[first],[prev]").removeClass("opager-disable");
        }
        // 看看能不能向后翻页
        if(pg.pn >= pg.pgnb){
            UI.arena.find("[next],[last]").addClass("opager-disable");
        }else{
            UI.arena.find("[next],[last]").removeClass("opager-disable");
        }
        UI.$el.data("PAGER", pg);
    },
    //..............................................
    getData : function(){
        //return {off:6, pgsz:3}
        return this.$el.data("PAGER");
    }
    //..............................................
});
//==================================================
});
})(window.NutzUtil);