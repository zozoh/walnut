(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/support/hm__methods_com',
], function(ZUI, Wn, HmComMethods){
//==============================================
//==============================================
var html = `<div class="ui-arena hmc-pager">
    <div class="pg_btn"><a key="first"></a><a key="prev"></a></div>
    <div class="pg_nbs"><a>1</a><a>2</a><b>3</b><a>4</a><a>5</a><a>6</a></div>
    <div class="pg_btn"><a key="next"></a><a key="last"></a></div>
    <div class="pg_brief"></div>
</div>`;
//==============================================
return ZUI.def("app.wn.hm_com_pager", {
    dom     : html,
    keepDom : true,
    //...............................................................
    init : function(){
        HmComMethods(this);
    },
    //...............................................................
    paint : function(com) {
        var UI = this;
        
        // 设置属性开关
        UI.arena.attr({
            "pager-type" : com.pagerType || "button",
            "free-jump"  : com.freeJump  ?  "yes" : null,
            "show-brief" : com.showBrief ?  "yes" : null,
            "show-first-last" : com.showFirstLast ?  "yes" : null,
        });

        // 设置按钮文本
        UI.arena.find('.pg_btn a[key="first"]').text(com.btnFirst);
        UI.arena.find('.pg_btn a[key="prev"]').text(com.btnPrev);
        UI.arena.find('.pg_btn a[key="next"]').text(com.btnNext);
        UI.arena.find('.pg_btn a[key="last"]').text(com.btnLast);

        // 设置消息文本
        var brief = $z.tmpl(com.briefText||"No Brief")({
            pn:3, pgnb:6, sum: com.dftPageSize * 6, pgsz: com.dftPageSize
        });
        UI.arena.find(".pg_brief").html(brief);
    },
    //...............................................................
    getBlockPropFields : function(block) {
        return [block.mode == 'inflow' ? "margin" : null,
                "padding","border","borderRadius",
                "color", "background",
                "boxShadow","overflow"];
    },
    //...............................................................
    getDefaultData : function(){
        return {
            pagerType    : "button",
            freeJump     : true,
            dftPageSize  : 50,
            showFirstLast: true,
            btnFirst     : "|<<",
            btnPrev      : "<",
            btnNext      : ">",
            btnLast      : ">>|",
            showBrief    : true,
            briefText    : this.msg("hmaker.com.pager.brief_dft"),
        };
    },
    //...............................................................
    // 返回属性菜单， null 表示没有属性
    getDataProp : function(){
        return {
            uiType : 'app/wn.hmaker2/com_prop/pager_prop',
            uiConf : {}
        };
    }
});
//===================================================================
});
})(window.NutzUtil);