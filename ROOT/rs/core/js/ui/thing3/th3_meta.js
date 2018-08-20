(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/support/dom',
    'ui/thing3/support/th3_methods',
    'ui/form/form',
], function(ZUI, Wn, DomUI, ThMethods, FormUI){
//==============================================
var html = function(){/*
<div class="ui-arena th3-meta"
    ui-fitparent="true" ui-gasket="form"></div>
*/};
//==============================================
return ZUI.def("ui.thing.th_obj_index_meta", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "ui/thing/theme/thing-{{theme}}.css",
    i18n : "ui/thing/i18n/{{lang}}.js",
    //..............................................
    init : function(opt){
        var UI = ThMethods(this);

        UI.listenBus("obj:selected", UI.on_selected);
        UI.listenBus("obj:blur", UI.showBlank);
    },
    //..............................................
    update : function() {
        var UI  = this;
        var man = UI.getMainData();
        var obj = man.currentId 
                        ? Wn.getById(man.currentId, true)
                        : null;

        // 有值
        if(obj) {
            UI.showForm(obj);
        }
        // 空
        else {
            UI.showBlank();
        }

    },
    //..............................................
    showForm : function(obj) {
        var UI   = this;
        var man  = UI.getMainData();
        var conf = man.conf;

        // 已经有了 form
        if(UI.gasket.form && UI.gasket.form.uiName == 'ui.form') {
            UI.gasket.form.setData(obj);
        }
        // 重新创建
        else {
            new FormUI(_.extend({
                parent : UI,
                gasketName : "form",
                fields  : conf.fields,
                uiWidth : "all",
                arenaClass : "obj-meta-form",
                on_change : function(key){
                    //console.log("update key=", key);
                    var uiForm = this;
                    var obj    = uiForm.getData();
                    obj.__force_update = true;
                    uiForm.showPrompt(key, "spinning");
                    UI.invokeConfCallback("meta", "update", [obj, key, function(){
                        uiForm.hidePrompt(key);
                        // 通知界面其他部分更新
                        UI.fire("change:meta", [obj, key]);
                    }, function(msg) {
                        uiForm.showPrompt(key, "warn", msg);
                    }]);
                }
            })).render(function(){
                this.setData(obj);
            });
        }
    },
    //..............................................
    showBlank : function() {
        var UI = this;

        // 替换掉索引项
        new DomUI({
            parent : UI,
            gasketName : "form",
            dom : `<div class="th-obj-blank">
                <i class="fa fa-hand-o-left"></i>
                {{thing.blank}}
            </div>`
        }).render();
    },
    //..............................................
    on_selected : function(eo) {
        this.setData.apply(this, eo.data);
    },
    //..............................................
    setData : function(objs) {
        var UI = this;
        // this.gasket.form.setData(o);
        // $z.doCallback(callback, [], this);
        console.log(objs)
        // 显示
        if(_.isArray(objs) && objs.length > 0) {
            var obj = objs[0];
            // TODO 多个对象应该显示模板
            if(objs.length > 1) {
                
            }
            // 更新表单
            this.showForm(obj);
        }
        // 显示空白
        else {
            UI.showBlank();
        }
    },
    //..............................................
});
//==================================================
});
})(window.NutzUtil);