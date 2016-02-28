(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/menu/menu'
], function(ZUI, Wn, MenuUI){
//==============================================
var html = function(){/*
<div class="ui-arena hmaker-page" ui-fitparent="yes">
    <div class="ue-bar1"><span>{{hmaker.comlib_add}}</span><span>{{hmaker.comlib_add_c}}</span></div>
    <div class="ue-shelf"></div>
    <div class="ue-bar2">
        <div class="ue-ssize">
            <input name="x"><em>x</em><input name="y">
            <span>
                <i class="fa fa-laptop highlight" val=""></i>
                <i class="fa fa-tablet" val="800x600"></i>
                <i class="fa fa-mobile" val="400x600"></i>
            </span>
        </div>
        <div class="ue-com-menu"></div>
    </div>
    <div class="ue-stage" mode="pc">
        <div class="ue-screen"><iframe src="/a/load/wn.hmaker/screen.html"></iframe></div>
    </div>
</div>
*/};
//==============================================
return ZUI.def("app.wn.hmaker_com_text", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    init : function(){
        var UI = this;
        // 增加一个 iframe 的回调
        if(!_.isFunction(window._hmaker_page_on_load)){
            window._hmaker_page_on_load = function(){
                UI.setup_page_editing();
            }
        }
    },
    //...............................................................
    redraw : function(){
        var UI  = this;

        // 读取加载项的内容
        UI.__reload_components(function(){
            UI.defer_report("coms");
        });

        // 标记延迟
        return ["coms"];
    },
    //...............................................................
    __reload_components : function(callback){
        var UI = this;
        // 加载插入项目
        var o = Wn.fetch("~/.hmaker/components.html", true);
        if(!o){
            alert(UI.msg("hmaker.page.e_nocoms"));
            return;
        }

        // 读取加载项的内容     
        Wn.read(o, function(html){
            // 将加载项目计入 DOM
            var jSh = UI.arena.find(".ue-shelf");
            jSh.empty().html(html);

            // 解析多国语言
            jSh.find("span,h4").each(function(){
                $(this).text(UI.text($(this).text()));
            });

            // 调用回调
            callback();
        }); 
    },
    //...............................................................
    update : function(oPg) {
        this.$el.attr("oid", oPg.id);
    },
    //...............................................................
    setup_page_editing : function(){
        var UI  = this;
        var oid = UI.$el.attr("oid");
        var oPg = Wn.getById(oid);

        // 读取 HTML
        var html = Wn.read(oPg);
        console.log("html:" + html);

        // 首先看看子页
        var ifrm = UI.arena.find(".ue-screen iframe")[0];
        var eDoc = ifrm.contentDocument;

        $(eDoc).find("head *").each(function(){
            console.log(this.tagName)
            if('LINK' == this.tagName)
                $(this).remove();
        })

    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);