(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/layout/layout',
    'ui/thing3/support/th_util',
], function(ZUI, Wn, LayoutUI, Ths){
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

        // 加载配置文件

        // 加载主界面
        UI._bus = new LayoutUI({
            parent : UI,
            gasketName : 'main',
            layout : 'ui/thing3/layout/col3_md_ma.xml',
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
        }).render(function(){
            $z.doCallback(callback, [], UI);
        });

        // 表示自己是异步加载
        // 待加载完毕，需要主动调用回调
        return true;
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