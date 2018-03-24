(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/support/dom',
    'ui/thing/support/th_methods',
    'ui/menu/menu',
    'ui/markdown/edit_markdown',
], function(ZUI, Wn, DomUI, ThMethods, MenuUI, EditMarkdownUI){
//==============================================
var html = function(){/*
<div class="ui-arena th-obj-index-detail" ui-fitparent="true">
    <section class="toid-content" ui-gasket="edit"></section>
    <section class="toid-brief">
        <h4>{{thing.key.brief}}</h4>
        <aside>
            <a><i class="zmdi zmdi-flash"></i> {{thing.detail.genbreif}}</a>
        </aside>
        <textarea spellcheck="false" placeholder="{{thing.detail.brief}}"></textarea>
    </section>
</div>
*/};
//==============================================
return ZUI.def("ui.th_obj_index_detail", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "ui/thing/theme/thing-{{theme}}.css",
    i18n : "ui/thing/i18n/{{lang}}.js",
    //..............................................
    init : function(opt){
        ThMethods(this);
    },
    //..............................................
    events : {
        'click .toid-brief aside a' : function(){
            var UI = this;
            var str = UI.gasket.edit.getData();
            var brief = $.trim(str||"")
                .replace(/[>+-`#\t\r\n ]/g, "")
                    .substring(0, 50);
            UI.arena.find(".toid-brief textarea").val(brief);
        }
    },
    //..............................................
    redraw : function(){
        var UI   = this;
        var conf = UI.getBusConf();
       
        new EditMarkdownUI({
            parent : UI,
            gasketName : "edit",
            menu : [{
                icon : '<i class="fa fa-save"></i>',
                text : "i18n:thing.detail.save",
                asyncIcon : '<i class="zmdi zmdi-settings zmdi-hc-spin"></i>',
                asyncText : "i18n:thing.detail.saving",
                asyncHandler : function(jq, mi, callback) {
                    var obj = UI.__OBJ;
                    // 防守一下吧，虽然不太可能会发生
                    if(!obj) {
                        UI.alert("thing.detail.noobj", "warn");
                        return;
                    }
                    // 读取数据 
                    var det = {
                        tp : "md",
                        brief : $.trim(UI.arena.find(">.toid-brief textarea").val()),
                        content : this.getData()
                    };
                    // 执行保存
                    //conf.detail.save(obj, det, callback);
                    UI.invokeConfCallback("detail", "save", [obj, det, function(re){
                        // 这里主动调用一下异步函数回调
                        // 以便恢复按钮状态
                        $z.doCallback(callback);
                        // 处理任务回调
                        UI.doActionCallback(re, function(newObj){
                            // 通知界面其他部分更新
                            UI.fire("change:meta", [newObj]);
                        });
                    }]);
                }
            }, {type : "separator"}, "preview"],
            preview : conf.detail.markdown || {
                media : function(src) {
                    var m = /^(media|attachment)\/(.+)$/.exec(src);
                    console.log(m)
                    if(m) {
                        return ($z.tmpl("/o/read/id:{{thset}}/data/{{th_id}}/{{cate}}/{{path}}"))({
                            thset : UI.getHomeObjId(),
                            th_id : UI.__OBJ.id,
                            cate  : m[1],
                            path  : m[2]
                        });
                    }
                    return src;
                }
            },
            defaultMode : UI.local("markdown-mode"),
            on_mode : function(m) {
                UI.local("markdown-mode", m);
            }
        }).render(function(){
            UI.defer_report("edit");
        });

        return ["edit"];
    },
    //..............................................
    update : function(o, callback) {
        var UI  = this;
        var conf = UI.getBusConf();
        UI.__OBJ = o;

        this.invokeConfCallback("detail", "read", [o, function(str){
            UI.gasket.edit.setData(str);
            $z.doCallback(callback, [str], UI);  
        }]);

        // 更新摘要
        UI.arena.find(">.toid-brief textarea").val(o.brief || "");
    },
    //..............................................
    resize : function() {
        var UI = this;
        var jBreif   = UI.arena.find(">.toid-brief");
        var jContent = UI.arena.find(">.toid-content");
        jContent.css({
            "height": UI.arena.height() - jBreif.outerHeight()
        });
    }
    //..............................................
});
//==================================================
});
})(window.NutzUtil);