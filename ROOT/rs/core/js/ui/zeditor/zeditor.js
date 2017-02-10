(function($z){
$z.declare([
    'zui',
    'wn/util'
], function(ZUI, Wn){
//==============================================
var TYPES = {
    // 纯文本
    "text" : {
        type : 'text',
        icon : '<i class="fa fa-file-text-o"></i>',
    },
    // HTML
    "html" : {
        type : 'html',
        icon : '<i class="fa fa-html5"></i>',
    }
};
//==============================================
// 将 HTML 里面邪恶的东西都删掉！！！ 都删掉！！！
function escape_html_evil(html) {
    return html.replace(/(<)(script|link|style|object|html|body|head)/g, '&lt;$2');
}
//==============================================
return ZUI.def("ui.zeditor", {
    dom  : `
    <div class="ui-arena zeditor" ui-fitparent="yes">
        <header></header>
        <section>
            <textarea spellcheck="false"></textarea>
        </section>
    </div>`,
    //...............................................................
    css  : ['theme/ui/zeditor/zeditor.css'],
    i18n : "ui/zeditor/i18n/{{lang}}.js",
    //...............................................................
    init : function(opt) {
        var UI = this;

        // console.log("opt", opt.contentType)

        // 设置一系列默认值
        $z.setUndefined(opt, "contentType", "text");

        // 检查类型
        UI._HD = TYPES[opt.contentType];
        if(!UI._HD) {
            console.warn("no!", opt.contentType)
            UI._HD = TYPES[text];
        }
    },
    //...............................................................
    redraw : function() {
        var UI  = this;
        var opt = UI.options;

        var titleHtml =  opt.title || UI._HD.icon + UI.msg("zeditor.type." + UI._HD.type);
        UI.arena.children("header").html(titleHtml);
    },
    //...............................................................
    setData : function(content) {
        this.arena.find("> section > textarea")
            .val(content || "")
                .focus();
    },
    //...............................................................
    getData : function() {
        return this.arena.find("> section > textarea").val();
    },
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);