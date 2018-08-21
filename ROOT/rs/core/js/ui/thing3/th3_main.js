(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/layout/layout',
    'ui/thing3/support/th3_methods',
    'ui/thing3/support/th3_util',
], function(ZUI, Wn, LayoutUI, ThMethods, Ths){
//==============================================
var html = function(){/*
<div class="ui-arena th3-main" ui-fitparent="true" ui-gasket="main">
</div>
*/};
//==============================================
return ZUI.def("ui.th3.th_main", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "ui/thing3/theme/th3-{{theme}}.css",
    i18n : "ui/thing3/i18n/{{lang}}.js",
    //..............................................
    update : function(oDir, callback) {
        var UI  = this;
        var opt = UI.options;
        //console.log(opt)

        // 准备主界面布局对象
        UI._bus = new LayoutUI({
            parent : UI,
            gasketName : 'main',
            layout : 'ui/thing3/layout/col3_md_ma.xml',
            on_before_init : function(){
                ThMethods(this);
            },
            setup :{
                "search"  : 'ui/thing3/th3_search',
                "meta"    : 'ui/thing3/th3_meta',
                "content" : 'ui/thing3/th3_content',
                "media"   : {
                    uiType :'ui/thing3/th3_media',
                    uiConf : {folderName:"media"}
                },
                "attachment"  : {
                    uiType :'ui/thing3/th3_media',
                    uiConf : {folderName:"attachment"}
                }
            }
        });

        // 监听各个区域
        UI._bus.listenSelf("area:ready", function(eo) {
            console.log("area:ready", eo, eo.UI.getMainData());
            $z.invoke(eo.UI, "update");
        });
        UI._bus.listenSelf("do:create", function(){
            this.showArea("create");
        });

        // 加载配置文件
        var oThConf = Wn.fetch("id:"+oDir.id+"/thing.js");
        Wn.read(oThConf, function(json) {
            // 格式化配置对象
            var conf = $z.fromJson(json);
            Ths.evalConf(UI, conf, opt, oDir);

            // 初始化本地数据
            UI.__main_data = {
                home      : oDir,
                conf      : conf,
                currentId : UI.local('th3_last_actived_id_'+oDir.id)
            };
            
            // 加载主界面
            UI._bus.render(function(){
                // 调用回调，以便调用者知道异步加载已经完成
                $z.doCallback(callback, [], UI);
            });
        });

        // 表示自己是异步加载
        // 待加载完毕，需要主动调用回调
        return true;
    },
    //..............................................
    setCurrentObj : function(obj) {
        var man = this.__main_data;
        man.currentId = obj ? obj.id : null;
        // 本地记录一下
        this.local('th3_last_actived_id_'+man.home.id, man.currentId);
    },
    //..............................................
    createObj : function(callback){
        var UI = this;
        var bus = UI.bus();
        var conf = UI.getBusConf();

        // 准备标题
        var oHome = UI.getHomeObj();
        var title = UI.msg("thing.create_tip2", {
            text : UI.text(oHome.title || oHome.nm)
        });

        // 编制一下唯一性索引的键表
        var ukMap = {};
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
                fields.push(_.extend({},fld,{rquired:true}));
            }
        }

        // 直接弹出一个表单收集新对象信息
        if(fields.length > 0) {
            POP.openFormPanel({
                title : title,
                width : 640,
                height: "61.8%",
                form : {
                    mergeData : false,
                    uiWidth   : "all",
                    fields    : fields,
                    data : {},
                },
                autoClose : false,
                callback : function(obj) {
                    var pop = this;
                    UI.invokeConfCallback("actions", "create", [obj, function(newObj){
                        var jItem = UI.addObj(newObj);
                        UI.gasket.main.uiList.setActived(jItem);
                        $z.doCallback(callback, [newObj], UI);
                        pop.uiMask.close();
                    }, function(){
                        pop.jBtn.removeAttr("btn-ing");
                        pop.uiMask.is_ing = false;
                    }]);
                },
                errMsg : {
                    "lack" : "e.thing.fld.lack"
                }
            }, UI);
        }
        // 否则如果为空，那么仅仅弹出一个询问框
        else {
            UI.prompt(title, {
                icon  : oHome.icon || '<i class="fa fa-plus"></i>',
                btnOk : "thing.create_do",
                ok : function(str){
                    str = $.trim(str);
                    if(!str) {
                        UI.alert('e.thing.fld.lack', 'warn');
                        return;
                    }
                    UI.invokeConfCallback("actions", "create", [str, function(newObj){
                        var jItem = UI.addObj(newObj);
                        UI.gasket.main.uiList.setActived(jItem);
                        $z.doCallback(callback, [newObj], UI);
                    }]);                
                }
            });
        }
    }, 
    //..............................................
});
//==================================================
});
})(window.NutzUtil);