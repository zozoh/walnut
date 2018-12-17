(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/thing3/support/th3_methods',
    'ui/form/form',
], function(ZUI, Wn, ThMethods, FormUI){
//==============================================
var html = function(){/*
<div class="ui-arena th3-obj-create" ui-fitparent="true">
    <header>head</header>
    <section ui-gasket="form"></section>
    <footer>
        <b a="do-obj-create">{{th3.create_do}}</b>
        <a a="do-cancel">{{cancel}}</a>
    </footer>
</div>
*/};
//==============================================
return ZUI.def("ui.thing.th_obj_create", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "ui/thing3/theme/th3-{{theme}}.css",
    i18n : "ui/thing3/i18n/{{lang}}.js",
    //..............................................
    init : function(opt){
        var UI = ThMethods(this);

        // 监听 Esc
        this.watchKey(27, function(e){
            this.bus().hideArea('create');
        });
    },
    //..............................................
    events : {
        'click footer a[a="do-cancel"]' : function(){
            this.bus().hideArea('create');
        },
        'click footer b[a="do-obj-create"]' : function(){
            var UI = this;
            UI.gasket.form.checkData({
                ok : function(obj) {
                    //console.log("OK:!!!")
                    var home = UI.getHomeObj();
                    var cmdText = "thing "+home.id+" create ";
                    var json = $z.toJson(obj).replace(/'/g, "");
                    cmdText += "-fields '" + json + "'";
                    // 执行命令
                    UI.showLoading();
                    Wn.exec(cmdText, function(re) {
                        UI.doActionCallback(re, {
                            ok : function(newTh){
                                UI.hideLoading();
                                UI.fireBus('list:add', newTh);
                                UI.fireBus('obj:selected', [newTh]);
                                UI.bus().hideArea('create');
                            },
                            fail : function(errMsg){
                                UI.hideLoading();
                            }
                        });
                    });
                },
                fail : function(){
                    UI.alert('th3.create_do_lack', 'warn');
                }
            });
        }
    },
    //..............................................
    redraw : function() {
        var UI  = this;
        var man = UI.getMainData();
        var conf = man.conf;

        // 绘制标题
        UI.arena.find('>header').text(UI.msg('th3.create_tt', {
            text : UI.getHomeOneObjTitle()
        }));

        // 编制一下唯一性索引的键表
        var ukMap = {};
        //console.log(conf.uniqueKeys)
        if(_.isArray(conf.uniqueKeys)) {
            for(var i=0; i<conf.uniqueKeys.length; i++) {
                var uk = conf.uniqueKeys[i];
                if(_.isArray(uk.name))
                    for(var x=0; x<uk.name.length; x++) {
                        ukMap[uk.name[x]] = true;
                    }
            }
        }

        // 准备表单字段
        var fields = [];
        for(var i=0; i<conf.fields.length; i++) {
            var fld = conf.fields[i];
            // 本身标记了 required
            // 或者在唯一性索引里
            if(fld.required || ukMap[fld.key]) {
                fields.push(_.extend({},fld,{required:true}));
            }
        }

        // 如果没有必要字段。。。 那么试图找到名称字段
        if(fields.length == 0) {
            for(var i=0; i<conf.fields.length; i++) {
                var fld = conf.fields[i];
                if(/^(th_nm|nm)$/.test(fld.key)) {
                    fields.push(_.extend({},fld,{required:true}));
                    break;
                }
            }
        }

        // 什么！？还没有，那么就找第一个文本字段吧
        if(fields.length == 0) {
            for(var i=0; i<conf.fields.length; i++) {
                var fld = conf.fields[i];
                if(!fld.type || 'string' == fld.type) {
                    fields.push(_.extend({},fld,{required:true}));
                    break;
                }
            }
        }

        // 所有只读的文本节点，显示一个输入框
        for(var i=0; i<fields.length; i++) {
            var fld = fields[i];
            if(fld.editAs == "label") {
                fld.editAs = "input"
            }
            else if(fld.uiType == "@label") {
                fld.uiType = "@input"
            }
        }

        // 绘制表单
        new FormUI({
            parent : UI,
            gasketName : "form",
            mergeData : false,
            uiWidth   : "all",
            fields    : fields,
            displayMode : "compact"
        }).render(function(){
            UI.defer_report("form");
        });
        
        // 返回以便延迟加载
        return ["form"];
    },
    //..............................................
    update : function() {
        //console.log("I am update", this.cid)
        this.gasket.form.setData({});
    }
    //..............................................
});
//==================================================
});
})(window.NutzUtil);