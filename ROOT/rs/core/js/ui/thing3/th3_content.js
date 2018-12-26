(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/menu/menu',
    'ui/markdown/edit_markdown',
    'ui/thing3/support/th3_methods'
], function(ZUI, Wn, MenuUI, EditMarkdownUI, ThMethods){
//==============================================
var html = function(){/*
<div class="ui-arena th3-content" ui-fitparent="true">
    <section class="toid-content" ui-gasket="edit"></section>
    <section class="toid-brief">
        <h4>{{th3.key.brief}}</h4>
        <aside>
            <a><i class="zmdi zmdi-flash"></i> {{th3.content.genbreif}}</a>
        </aside>
        <textarea spellcheck="false" placeholder="{{th3.content.brief}}"></textarea>
    </section>
</div>
*/};
//==============================================
return ZUI.def("ui.th3.content", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "ui/thing3/theme/th3-{{theme}}.css",
    i18n : "ui/thing3/i18n/{{lang}}.js",
    //..............................................
    init : function(opt){
        var UI = ThMethods(this);
        UI.listenBus("obj:selected", UI.on_selected);
        UI.listenBus("obj:blur", UI.showBlank);
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
    update : function() {
        var UI  = this;
        var man = UI.getMainData();
        var obj = Wn.getById(man.currentId, true);
        UI.setData(obj);
    },
    //..............................................
    showEditing : function(obj) {
        var UI   = this;
        var man  = UI.getMainData();
        var conf = man.conf;

        // 重新获取数据
        obj = Wn.getById(man.currentId);

        // 更新摘要
        UI.arena.find('.toid-brief').show();
        UI.arena.find(">.toid-brief textarea").val(obj.brief || "");

        // 读取数据
        Wn.read(obj, function(str){
            // 已经有了 form
            if(UI.gasket.edit && UI.gasket.edit.uiName == 'ui.edit-markdown') {
                UI.gasket.edit.setData(str);
            }
            // 重新创建
            else {
                new EditMarkdownUI({
                    parent : UI,
                    gasketName : "edit",
                    menu : [{
                        icon : '<i class="fa fa-save"></i>',
                        text : "i18n:th3.content.save",
                        asyncIcon : '<i class="zmdi zmdi-settings zmdi-hc-spin"></i>',
                        asyncText : "i18n:th3.content.saving",
                        asyncHandler : function(jq, mi, callback) {
                            // 重新获取数据
                            var obj = Wn.getById(man.currentId);
                            // 防守一下吧，虽然不太可能会发生
                            if(!obj) {
                                UI.alert("th3.content.noobj", "warn");
                                return;
                            }
                            // 读取数据 
                            var det = {
                                tp : "md",
                                brief : $.trim(UI.arena.find(">.toid-brief textarea").val()),
                                content : this.getData()
                            };
                            // 执行保存
                            var cmdText = $z.tmpl('thing {{th_set}} detail {{id}} -content')(obj);
                            if(det.tp)
                                cmdText += ' -tp ' + det.tp;
                            // 更新摘要和类型
                            if(!_.isUndefined(det.brief)) {
                                cmdText += ' -brief "' + det.brief||"null" + '"'; 
                            }
                            // 更新
                            Wn.exec(cmdText, det.content||"", function(re){
                                // 回调更新菜单按状态
                                $z.doCallback(callback);
                                // 处理回调
                                UI.doActionCallback(re, function(newTh){
                                    Wn.saveToCache(newTh);
                                    UI.fireBus("meta:updated", [newTh]);
                                });
                            });
                        }
                    }, {type : "separator"}, "preview"],
                    preview : {
                        media : function(src) {
                            var m = /^(media|attachment)\/(.+)$/.exec(src);
                            // console.log(m)
                            if(m) {
                                return ($z.tmpl("/o/read/id:{{thset}}/data/{{th_id}}/{{cate}}/{{path}}"))({
                                    thset : UI.getHomeObjId(),
                                    th_id : obj.id,
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
                    this.setData(str);
                });
            }
        }, true);  // ~ Wn.read(obj, function(str){
    },
    //..............................................
    showBlank : function() {
        var UI = this;

        UI.__show_blankUI("edit", {
            text : 'i18n:th3.nocontent'
        });

        UI.arena.find('.toid-brief').hide();
    },
    //..............................................
    on_selected : function(eo) {
        this.setData.apply(this, [eo.data]);
    },
    //..............................................
    setData : function(objs) {
        var UI = this;
        // this.gasket.form.setData(o);
        // $z.doCallback(callback, [], this);
        // console.log(objs)
        // 格式化数据
        if(!_.isArray(objs) && objs) {
            objs = [objs];
        }

        // 显示
        if(objs && objs.length > 0) {
            var obj = objs[0];
            // TODO 多个对象应该显示模板
            if(objs.length > 1) {
                
            }
            // 更新表单
            this.showEditing(obj);
        }
        // 显示空白
        else {
            UI.showBlank();
        }
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