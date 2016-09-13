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
        htmlToData : function(html){
            var jDiv = $("<div>");
            html = html.replace(/<\/p>/g, '')
                    .replace(/<p>/g, '\n\n')
                    .replace(/<br>/g, '\n');
            html = jDiv.text(html).text();
            jDiv.remove();
            return html;
        },
        dataToHtml : function(text){
           return text
                    .replace(/</g, "&lt;")
                    .replace(/>/g, "&gt;")
                    .replace(/(\r?\n){2,}/g, "<p>")
                    .replace(/\r?\n/g, '<br>'); 
        }
    },
    // HTML
    "html" : {
        type : 'html',
        icon : '<i class="fa fa-html5"></i>',
        htmlToData : function(html){
            return html;
        },
        dataToHtml : function(text){
           return text; 
        }
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

        UI.arena.find("textarea")[0].focus();
    },
    //...............................................................
    setData : function(content) {
        this.arena.find("> section > textarea").val(content || "");
    },
    //...............................................................
    getData : function() {
        return this.arena.find("> section > textarea").val();
    },
    //...............................................................
    __html_to_data : function(html) {
        // 不是字符串的话
        if(!_.isString(html)) {
            // 元素
            if(_.isElement(html))
                html = html.innerHTML;

            // jQuery
            else if($z.isjQuery(html))
                html = html.html();

            // 函数
            else if(_.isFunction(html))
                html = html();

            // 其他统统 toString
            else
                html = (html || "").toString();

        }
        // 设置
        return this._HD.htmlToData(escape_html_evil(html));
    },
    //...............................................................
    __data_to_html : function(data) {
        return this._HD.dataToHtml(escape_html_evil(data));
    },
    //...............................................................
    setHtml : function(html) {
        var data = this.__html_to_data(html);
        this.setData(data);
    },
    //...............................................................
    getHtml : function() {
        var data = this.getData();
        return this.__data_to_html(data);
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);