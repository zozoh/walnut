(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/form/form',
    'ui/thing3/support/th3_methods'
], function(ZUI, Wn, FormUI, ThMethods){
//==============================================
var html = function(){/*
<div class="ui-arena th3-meta"
    ui-fitparent="true" ui-gasket="form"></div>
*/};
//==============================================
return ZUI.def("ui.th3.meta", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "ui/thing3/theme/th3-{{theme}}.css",
    i18n : "ui/thing3/i18n/{{lang}}.js",
    //..............................................
    init : function(opt){
        var UI = ThMethods(this);
        UI.listenBus("obj:selected", UI.on_selected);
        UI.listenBus("obj:blur", UI.showBlank);
        UI.listenBus("meta:updated", UI.on_selected);
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
                    console.log("update key=", key);
                    // 准备数据
                    var uiForm = this;
                    var obj = uiForm.getData();

                    // 标识执行中
                    uiForm.showPrompt(key, "spinning");
                    
                    // 执行命令
                    var json = $z.toJson(obj);
                    Wn.execf('thing {{th_set}} update {{id}} -fields', json, obj, function(re){
                        // 错误
                        if(/^e\./.test(re)) {
                            uiForm.showPrompt(key, "warn", re);
                        }
                        // 成功后：通知界面其他部分更新
                        else {
                            uiForm.hidePrompt(key);
                            var newTh = $z.fromJson(re);
                            Wn.saveToCache(newTh);
                            UI.fireBus("meta:updated", [newTh, key]);
                        }
                    });
                }
            })).render(function(){
                this.setData(obj);
            });
        }
    },
    //..............................................
    showBlank : function() {
        var UI = this;

        UI.__show_blankUI("form", {
            icon : '<i class="fa fa-hand-o-left"></i>',
            text : 'i18n:th3.blank'
        });
    },
    //..............................................
    on_selected : function(eo) {
        console.log(eo.type, eo)
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
    }
    //..............................................
});
//==================================================
});
})(window.NutzUtil);