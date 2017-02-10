(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/support/hm__methods_panel',
], function(ZUI, Wn, HmMethods, FormUI){
//==============================================
var html = `
<div class="ui-arena hmc-htmlcode-prop" ui-fitparent="yes">
    <header>
        <span>{{hmaker.com.htmlcode.tt}}</span>
        <a>
            <i class="zmdi zmdi-window-maximize"></i> 
            {{hmaker.com.htmlcode.open}}
        </a>
    </header>
    <section><textarea spellcheck="false"></textarea></section>
    <footer>{{hmaker.com.htmlcode.edit_tip}}</footer>
</div>`;
//==============================================
return ZUI.def("app.wn.hm_com_htmlcode_prop", HmMethods({
    dom  : html,
    //...............................................................
    events : {
        // CTRL(Command)+Enter 快速应用修改
        "keydown textarea" : function(e) {
            if(13 == e.which && (e.metaKey || e.ctrlKey)) {
                this.applyHtmlCode();
            }
        },
        // 应用修改
        "change textarea" : function(e) {
            this.applyHtmlCode();
        }
    },
    //...............................................................
    applyHtmlCode : function() {
        var code = this.arena.find("textarea").val();
        this.uiCom.saveData("panel", {code : code}, true);
    },
    //...............................................................
    update : function(com) {
        this.arena.find("textarea").val(com.code || "");
    }
    //...............................................................
}));
//===================================================================
});
})(window.NutzUtil);