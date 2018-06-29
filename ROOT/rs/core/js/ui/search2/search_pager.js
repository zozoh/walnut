(function($z){
$z.declare('zui', function(ZUI){
//==============================================
var html = function(){/*
<div class="ui-arena search-pager">
    <b class="pgr-btn" first>{{search.pager.first}}</b>
    <b class="pgr-btn" prev >{{search.pager.prev}}</b>
    <span><input></span>
    <b class="pgr-btn" next >{{search.pager.next}}</b>
    <b class="pgr-btn" last >{{search.pager.last}}</b>
    <em class="pgr-tip" balloon="up:search.pager.modify_pgnb"></em>
</div>
*/};
//==============================================
return ZUI.def("ui.search_pager", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //..............................................
    init : function(opt) {
        this.__DATA = _.extend({
            pn   : 1,     // 第几页
            pgsz : 50,    // 每页多少数据
            pgnb : 1,     // 一共多少页
            sum  : 0,     // 一共多少记录
            nb   : 0      // 本页实际获取了多少数据
        }, opt.dft);
    },
    //..............................................
    events : {
        // 按钮调整页数
        "click .pgr-btn" : function(e){
            var UI   = this;
            var jBtn = $(e.currentTarget);
            if(jBtn.hasClass("pgr-disable"))
                return;

            // 得到数据
            var pg = UI.__DATA;
            pg.pn  = jBtn.attr("pn") * 1;
            // console.log(pg);
            // console.log(UI.getData(pg))
            UI.trigger("pager:change", UI.getData());
        },
        // 手动调整页数
        "change span > input" : function(e) {
            //console.log("hahah I am pager")
            var UI = this;
            var jInput = $(e.currentTarget);
            var val = jInput.val();
            var pn  = parseInt(val);

            // 得到数据
            var pg = UI.__DATA;

            // 必须是数字
            if(isNaN(pn)) {
                jInput.val(pg.pn);
                return;
            }

            // 确保在有效范围
            pn = Math.max(1, Math.min(pg.pgnb, val));

            // 修正值
            if(pn != val) {
                jInput.val(pn);
            }

            // 跳转
            if(pn != pg.pn) {
                pg.pn = pn;
                UI.trigger("pager:change", UI.getData(pg));
            }
        },
        // 箭头翻页
        "keydown span > input" : function(e) {
            var UI = this;

            // 得到数据
            var pg = UI.__DATA;
            var pn = -1;

            // 上箭头
            if(38 == e.which) {
                // 首页
                if(e.metaKey || e.ctrlKey) {
                    pn = 1;
                }
                // 前页
                else {
                    pn = Math.max(1, pg.pn - 1);
                }
            }
            // 下箭头
            else if(40 == e.which) {
                // 尾页
                if(e.metaKey || e.ctrlKey) {
                    pn = pg.pgnb;
                }
                // 后页
                else {
                    pn = Math.min(pg.pn + 1, pg.pgnb);
                }
            }

            // 跳转
            if(pn > 0 && pn != pg.pn) {
                pg.pn = pn;
                UI.trigger("pager:change", UI.getData(pg));
            }
        },
        // 手动调整页大小
        "click .pgr-tip" : function(){
            var UI = this;

            // 得到数据
            var pg = UI.__DATA;

            // 修改
            UI.prompt(UI.msg("search.pager.modify_tip", pg), function(str){
                str = $.trim(str);
                var pgsz = parseInt(str);
                // 必须为数字
                if(isNaN(pgsz)){
                    UI.alert("search.e.pgsz_must_int");
                    return;
                }
                // 不能为负数
                if(pgsz < 1) {
                    UI.alert("search.e.pgsz_less_then_zero");
                    return;
                }
                // 超过 10000 暂时禁止吧
                if(pgsz > 10000) {
                    UI.alert("search.e.pgsz_too_big");
                    return;
                }
                // 超过 1000 要让用户确认
                if(pgsz > 1000) {
                    UI.confirm("search.e.pgsz_is_big", function(){
                        pg.pgsz = pgsz;
                        pg.pn = 1;
                        UI.trigger("pager:change", UI.getData(pg));
                    });
                    return;
                }
                // 直接通知
                pg.pgsz = pgsz;
                pg.pn = 1;
                UI.trigger("pager:change", UI.getData());
            });

        }
    },
    //..............................................
    redraw : function(){
        this.balloon();
    },
    //..............................................
    // 
    setData : function(pg){
        var UI  = this;

        // 保存一下数据
        _.extend(UI.__DATA, pg);

        // 默认值
        var pg = UI.__DATA;

        // 进行计算
        var last = Math.max(1, pg.pgnb);
        UI.arena.find(".pgr-tip").text(UI.msg("search.pager.tip", pg));
        UI.arena.find("[first]").attr("pn", 1);
        UI.arena.find("[prev]" ).attr("pn", Math.max(1, pg.pn-1));
        UI.arena.find("[next]" ).attr("pn", Math.min(last, pg.pn+1));
        UI.arena.find("[last]" ).attr("pn", last);
        UI.arena.find(">span>input").val(pg.pn);
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
    },
    //..............................................
    getData : function(){
        var pg = _.extend({}, this.__DATA)
        pg.limit = pg.pgsz;
        pg.skip  = (pg.pn - 1) * pg.pgsz;
        return pg;
    }
    //..............................................
});
//==================================================
});
})(window.NutzUtil);