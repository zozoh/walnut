(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/hm__methods',
], function(ZUI, Wn, HmPanelMethods){
//==============================================
var html = '<div class="ui-arena hm-combar"></div>';
//==============================================
return ZUI.def("app.wn.hmpg_combar", {
    dom  : html,
    //...............................................................
    init : function() {
        var UI = HmPanelMethods(this);
        
        UI.listenBus("active:com",  UI.updateComPath);
        UI.listenBus("active:page", UI.emptyComPath);
    },
    //...............................................................
    events : {
        'click .hm-combar-item[parent]' : function(e){
            var UI = this;
            var comId  = $(e.currentTarget).attr("com-id");
            // 组件
            if(comId) {
                var jBlock = UI.pageUI().getBlockElementById(comId);
                UI.fire("active:block", jBlock);
            }
            // 页面
            else {
                UI.fire("active:page");
            }
        }
    },
    //...............................................................
    redraw : function(){
        this.emptyComPath();
    },
    //...............................................................
    emptyComPath : function() {
        var UI = this;
        UI.arena.empty().prepend(UI.compactHTML(`<div class="hm-combar-item" current="yes">
            <b>{{hmaker.page.body}}</b>
        </div>`));
    },
    //...............................................................
    __gen_com_html : function(jCom, current) {
        var UI = this;
        var jq = $(jCom);
        var comId = jq.attr("id");
        var ctype = jq.attr("ctype");

        var html = '<div class="hm-combar-item" com-id="'+comId+'"';
        if(current)
            html += ' current="yes">';
        else
            html += ' parent="yes">';
        html += UI.msg("hmaker.com."+ctype+".icon");
        html += '<b>'+UI.msg("hmaker.com."+ctype+".name")+'</b>';
        if(!current)
            html += '<i class="zmdi zmdi-chevron-right"></i>';
        //html += '<em>('+comId+')</em>';
        html += '</div>';

        return html;
    },
    //...............................................................
    updateComPath : function(uiCom) {
        var UI = this;
        jCom = uiCom.$el;

        // 清空
        this.arena.empty();

        // 根据组件路径绘制
        jCom.parents(".hm-com").each(function(){
            var html = UI.__gen_com_html(this);
            UI.arena.prepend(html);
        });

        // 绘制页面节点
        UI.arena.prepend(UI.compactHTML(`<div class="hm-combar-item" parent="yes">
            <b>{{hmaker.page.body}}</b>
            <i class="zmdi zmdi-chevron-right"></i>
        </div>`));

        // 最后绘制自己
        var html = UI.__gen_com_html(jCom, true);     
        UI.arena.append(html);
        
    },
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);