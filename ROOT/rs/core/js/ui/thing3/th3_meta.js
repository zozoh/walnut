(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/support/dom',
    'ui/form/form',
], function(ZUI, Wn, DomUI, FormUI){
//==============================================
var html = function(){/*
<div class="ui-arena th3-meta"
    ui-fitparent="true" ui-gasket="form"></div>
*/};
//==============================================
return ZUI.def("ui.th3.meta", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "ui/thing/theme/thing-{{theme}}.css",
    i18n : "ui/thing/i18n/{{lang}}.js",
    //..............................................
    init : function(opt){
        var UI = this;
        UI.listenBus("obj:selected", UI.on_selected);
        UI.listenBus("obj:blur", UI.showBlank);
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