/**
Pager 控件的运行时行为:

opt : {
    // 来自编辑器
    "pagerType": "button",
    "freeJump": true,
    "dftPageSize": 50,
    "showFirstLast": true,
    "btnFirst": "|<<",
    "btnPrev": "<",
    "btnNext": ">",
    "btnLast": ">>|",
    "showBrief": true,
    "briefText": "第 {{pn}} 页，共 {{pgnb}}页, {{sum}} 条记录"

    // 下面由 IDE 指定
    forIDE : true,
    
}
*/
(function($, $z){
//...........................................................
// 基础 DOM 结构
var html = '<div class="pg_ele pg_btn"><a key="first"></a><a key="prev"></a></div>';
html += '<div class="pg_ele pg_nbs"></div>';
html += '<div class="pg_ele pg_btn"><a key="next"></a><a key="last"></a></div>';
html += '<div class="pg_ele pg_brief"></div>';
//...........................................................
function __redraw(jq, opt) {
    //console.log(opt)
    // 重置 DOM
    jq.html(html);

    // 设置属性开关
    jq.attr({
        "pager-type" : opt.pagerType || "button",
        "free-jump"  : opt.freeJump  ?  "yes" : null,
        "show-brief" : opt.showBrief ?  "yes" : null,
        "show-first-last" : opt.showFirstLast || "auto",
        "show-prev-next"  : opt.showPrevNext  || "auto",
        "brief-text" : opt.briefText,
        "max-btn-nb" : opt.maxBarNb || 9,
        "pgsz"  : opt.dftPageSize,
        "pn"    : 1,
    });

    // 设置按钮文本
    jq.find('.pg_btn a[key="first"]').attr("jump-to",1).text(opt.btnFirst);
    jq.find('.pg_btn a[key="prev"]').attr("jump-off",-1).text(opt.btnPrev);
    jq.find('.pg_btn a[key="next"]').attr("jump-off",1).text(opt.btnNext);
    jq.find('.pg_btn a[key="last"]').attr("jump-to",-1).text(opt.btnLast);

}
//...........................................................
// 命令模式
var CMD = {
    // 得到当前的完整翻页信息
    getData : function(){
        return {
            pn   : this.attr("pn") * 1,
            pgsz : this.attr("pgsz") * 1,
            pgnb : this.attr("pgnb") * 1,
            sum  : this.attr("sum") * 1,
        };
    },
    // 根据偏移量跳转, -1 表示向前一页，1 表示向后一页
    // 0 表示还是当前页，不过相当于重新刷新了一下
    jumpOff : function(off, reloadDynamic) {
        var pg = CMD.getData.call(this);
        pg.pn  = Math.min(pg.pgnb, Math.max(1, pg.pn + off));
        CMD.value.call(this, pg);

        if(reloadDynamic)
            HmRT.invokeDynamicReload(this);
    },
    // 跳转到固定页码, -1 表示最后一页, -2 表示倒数第二页。
    // 0 表示还是当前页，不过相当于重新刷新了一下
    jumpTo : function(pn, reloadDynamic) {
        var pg = CMD.getData.call(this);
        // 直接刷新
        if(0 == pn) {}
        // 从后面数的页码
        else if(pn < 0) {
            pg.pn = Math.max(1, pg.pgnb + 1 + pn);
        }
        // 指定的页面
        else{
            pg.pn = Math.min(pg.pgnb, Math.max(1, pn));
        }
        CMD.value.call(this, pg);

        if(reloadDynamic)
            HmRT.invokeDynamicReload(this);
    },
    // 获取值
    value : function(pg) {
        // 设置模式
        if(pg) {
            //console.log("pager setValue", pg);
            // 计算按钮个数
            var maxBarNb = this.attr("max-btn-nb") * 1;
            var nb_l = 1;
            var nb_r = pg.pgnb;
            if(pg.pgnb > maxBarNb) {
                nb_l = Math.max(1, pg.pn - Math.floor((maxBarNb-1)/2));
                nb_r = Math.min(pg.pgnb, nb_l + maxBarNb - 1);
            }
            // 补全前面的页码
            nb_prev = maxBarNb - (nb_r - nb_l) - 1;
            if(nb_prev > 0) {
                nb_l = Math.max(1, nb_l - nb_prev);
            }

            // 绘制翻页按钮
            var jNbs = this.find(".pg_nbs").empty();
            for(var i=nb_l; i<=nb_r; i++) {
                if(i == pg.pn){
                    $('<b>').text(i).appendTo(jNbs);
                }else{
                    $('<a>').text(i).appendTo(jNbs);
                }
            }
            // 更新设置翻页属性
            this.attr({
                "pgsz" : pg.pgsz,
                "pn"   : pg.pn,
                "pgnb" : pg.pgnb,
                "sum"  : pg.sum,
                "is-first-page" : pg.pn == 1 ? "yes" : null,
                "is-last-page"  : pg.pn == pg.pgnb ? "yes" : null,
            });
            // 设置消息文本
            var brief = $z.tmpl(this.attr("brief-text")||"No Brief")(pg);
            this.find(".pg_brief").html(brief);
            return this;
        }
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 获取值
        var pn = this.attr("pn") * 1;
        var pgsz = this.attr("pgsz") * 1;
        return {
            pn : pn, 
            pgsz : pgsz,
            skip : (pn-1) * pgsz
        };
    }
}
//...........................................................
$.fn.extend({ "hmc_pager" : function(opt){
    // 命令模式
    if(_.isString(opt)) {
        var args = $z.toArgs(arguments);
        return CMD[opt].apply(this, args.slice(1));
    }
    
    // 得到自己所在控件
    var jPager = this.empty();
    
    ///~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // 开始绘制
    __redraw(jPager, opt);

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // 监控事件
    if(!opt.forIDE) {
        // 跳转页码
        jPager.on("click", ".pg_nbs a", function(e){
            var pn = $(e.currentTarget).text() * 1;
            CMD.jumpTo.call(jPager, pn, true);
        });
        // 首/尾页
        jPager.on("click", ".pg_btn [jump-to]", function(e){
            var pn = $(e.currentTarget).attr("jump-to") * 1;
            CMD.jumpTo.call(jPager, pn, true);
        });
        // 前/后页
        jPager.on("click", ".pg_btn [jump-off]", function(e){
            var off = $(e.currentTarget).attr("jump-off") * 1;
            CMD.jumpOff.call(jPager, off, true);
        });
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // 返回自身以便链式赋值
    return jPager;
}});
//...........................................................
})(window.jQuery, window.NutzUtil);

