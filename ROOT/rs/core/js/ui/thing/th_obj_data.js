(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/support/dom',
    'ui/menu/menu',
    'ui/thing/support/th_methods',
    'ui/thing/th_obj_data_media',
], function(ZUI, Wn, DomUI, MenuUI, ThMethods, ThObjMediaUI){
//==============================================
var html = function(){/*
<div class="ui-arena th-obj-data th-obj-pan" ui-fitparent="true">
    <header><div>
        <div class="top-tabs">
            <ul>
                <li m="media">{{thing.data.media}}</li>
                <li m="attachment">{{thing.data.attachment}}</li>
            </ul>
        </div>
        <div class="top-menu" ui-gasket="menu"></div>
    </div></header>
    <section ui-gasket="main"></section>
    <div class="hide-file-selector">
        <input type="file" multiple>
    </div>
</div>
*/};
//==============================================
return ZUI.def("ui.th_obj_data", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "ui/thing/theme/thing-{{theme}}.css",
    i18n : "ui/thing/i18n/{{lang}}.js",
    //..............................................
    init : function(opt){
        ThMethods(this);
    },
    //..............................................
    events : {
        // 切换标签
        'click .top-tabs li[m]' : function(e) {
            var UI = this;
            var jq = $(e.currentTarget);
            if('media' == jq.attr("m")) {
                UI.showMedia();
            } else {
                UI.showAttachment();
            }
        },
        // 监控隐藏的上传按钮
        'change input[type="file"]' : function(e) {
            var UI   = this;
            var eleF = e.currentTarget;

            // 关闭菜单
            UI.gasket.menu.closeGroup();

            // 执行上传
            UI.gasket.main.upload(e.currentTarget.files);

            // 清空
            $(eleF).val("");
        }
    },
    //..............................................
    redraw : function(){
        var UI   = this;
        var conf = UI.getBusConf();
        var jTabs = UI.arena.find(">header .top-tabs");

        // 都有
        if(conf.media && conf.attachment) {
            UI.__show_main(function(){
                UI.defer_report("main");
            });
        }
        // 仅有媒体
        else if(conf.media) {
            jTabs.find('li[m="attachment"]').hide();
            UI.showMedia(function(){
                UI.defer_report("main");
            });
        }
        // 那么就仅有元数据咯
        else if(conf.attachment) {
            jTabs.find('li[m="media"]').hide();
            UI.showAttachment(function(){
                UI.defer_report("main");
            });
        }
        // 总得有点啥吧
        else {
            throw "not setup media or attachment!";
        }

        // 显示菜单
        new MenuUI({
            parent : UI,
            gasketName : "menu",
            setup : [{
                type : "group",
                icon : '<i class="zmdi zmdi-more-vert"></i>',
                items : [{
                    icon : '<i class="zmdi zmdi-refresh"></i>',
                    text : 'i18n:refresh',
                    handler : function(){
                        UI.gasket.main.refresh();
                    }
                }, {
                    icon : '<i class="fa fa-trash"></i>',
                    text : 'i18n:thing.data.remove',
                    handler : function(){
                        UI.gasket.main.removeCheckedItems();
                    }
                }, {
                    icon : '<i class="zmdi zmdi-upload"></i>',
                    text : 'i18n:thing.data.upload',
                    handler : function(){
                        UI.arena.find('input[type="file"]').click();
                    }
                }]
            }]
        }).render(function(){
            UI.defer_report("menu");
        });

        // 返回延迟加载
        return ["main", "menu"];
    },
    //..............................................
    _fill_context : function(uiSet) {
        uiSet.data = this;
    },
    //..............................................
    update : function(o, callback) {
        // 记录当前对象
        this.__OBJ = o;
        this.gasket.main.update(o, callback);
        // TODO 同时也要更新对象菜单吧
    },
    //..............................................
    __show_main : function(callback){
        var UI = this;
        // 显示主界面界面
        if("media" == UI.local("th_obj_data_current_tab")){
            UI.showMedia(callback);
        }
        // 默认显示元数据界面
        else {
            UI.showAttachment(callback);
        }
    },
    //..............................................
    showMedia : function(callback) {
        var UI  = this;
        var bus = UI.bus();
        var jTabs = UI.arena.find(">header .top-tabs");

        // 修改文件过滤器
        UI.arena.find('input[type="file"]').attr({
            "accept" : "image/png,image/jpeg,image/gif"
        });

        new ThObjMediaUI({
            parent : UI,
            gasketName : "main",
            bus : bus,
            folderName : "media",
        }).render(function(){
            UI.local("th_obj_data_current_tab", "media");
            jTabs.find('li').removeAttr("current")
                .filter('[m="media"]').attr("current", "yes");
            if(UI.__OBJ) {
                this.update(UI.__OBJ, callback);
            } else {
                $z.doCallback(callback, [this], UI);
            }
        });
    },
    //..............................................
    showAttachment : function(callback) {
        var UI  = this;
        var bus = UI.bus();
        var jTabs = UI.arena.find(">header .top-tabs");

        // 修改文件过滤器
        UI.arena.find('input[type="file"]').attr({
            "accept" : null
        });

        new ThObjMediaUI({
            parent : UI,
            gasketName : "main",
            bus : bus,
            folderName : "attachment",
        }).render(function(){
            UI.local("th_obj_data_current_tab", "attachment");
            jTabs.find('li').removeAttr("current")
                .filter('[m="attachment"]').attr("current", "yes");
            if(UI.__OBJ) {
                this.update(UI.__OBJ, callback);
            } else {
                $z.doCallback(callback, [this], UI);
            }
        });
    },
    //..............................................
});
//==================================================
});
})(window.NutzUtil);