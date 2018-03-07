(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/pop/pop',
    'app/wn.hmaker2/support/hm__methods_panel',
    'ui/form/c_switch',
], function(ZUI, Wn, POP, HmMethods, SwitchUI ){
//==============================================
var html = `
<div class="ui-arena hmc-text-prop" ui-fitparent="yes">
    <header>
        <span ui-gasket="ttp"></span>
        <a>
            <i class="zmdi zmdi-window-maximize"></i> 
            <em>{{hmaker.com.text.open}}</em>
        </a>
    </header>
    <section>
        <textarea spellcheck="false"></textarea>
    </section>
    <footer>{{hmaker.com.text.edit_tip}}</footer>
</div>`;
//==============================================
return ZUI.def("app.wn.hm_com_text_prop", HmMethods({
    dom  : html,
    //...............................................................
    events : {
        // CTRL(Command)+Enter 快速应用修改
        "keydown textarea" : function(e) {
            if(13 == e.which && (e.metaKey || e.ctrlKey)) {
                this.applyContentText();
            }
        },
        // 应用修改
        "change textarea" : function(e) {
            this.applyContentText();
        },
        "click header a" : function(){
            var UI  = this;
            var opt = UI.options;
            var jT  = UI.arena.find("textarea");

            // 打开编辑器
            POP.openEditTextPanel({
                title       : "i18n:hmaker.com.text.edit_tt",
                contentType : "text",
                data : jT.val() || "",
                callback : function(data){
                    UI.applyContentText(data);
                }
            }, UI);
        }
    },
    //...............................................................
    redraw : function() {
        var UI = this;

        new SwitchUI({
            parent : UI, 
            gasketName : "ttp",
            on_change : function(v){
                UI.applyContentType(v);
            },
            items : [{
                text: 'i18n:hmaker.com.text.ttp_auto', value:"auto",
            }, {
                text: 'i18n:hmaker.com.text.ttp_text', value:"text",
            }, {
                text: 'i18n:hmaker.com.text.ttp_md',   value:"markdown",
            }]
        }).render(function(){
            UI.defer_report("ttp");
        })

        return ["ttp"];
    },
    //...............................................................
    applyContentType : function(ttp) {
        this.uiCom.saveData("panel", {contentType : ttp||"auto"}, true);
    },
    //...............................................................
    applyContentText : function(code) {
        var UI = this;
        var jT = UI.arena.find("textarea");

        // 读取
        if(_.isUndefined(code)) {
            code = jT.val();
        }
        // 写入
        else {
            jT.val(code);
        }
        
        // 通知
        this.uiCom.saveData("panel", {code : code}, true);
    },
    //...............................................................
    update : function(com) {
        var UI  = this;
        // 设置文本类型
        UI.gasket.ttp.setData(com.contentType || "auto");
        // 显示文本
        UI.arena.find("textarea").val(com.code || "");
    }
    //...............................................................
}));
//===================================================================
});
})(window.NutzUtil);