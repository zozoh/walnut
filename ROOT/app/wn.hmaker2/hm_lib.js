(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/support/hm__methods',
    'ui/list/list',
    'ui/o_edit_text/o_edit_text',
    'ui/support/dom'
], function(ZUI, Wn, HmMethods, ListUI, EditTextUI, DomUI){
//==============================================
var html = `
<div class="ui-arena hm-lib" ui-fitparent="yes">
    <header>
        <span class="lib"><%=hmaker.lib.icon%><b>{{hmaker.lib.title}}</b></span>
        <span class="item"></span>
    </header>
    <nav ui-gasket="list"></nav>
    <section ui-gasket="code"></section>
</div>`;
//==============================================
return ZUI.def("app.wn.hmaker_lib", {
    dom : html,
    //...............................................................
    init : function() {
        var UI = HmMethods(this);

        UI.listenBus("rename:libItem", UI.doRename);
    },
    //...............................................................
    redraw : function(){
        var UI = this;

        // 显示库列表
        new ListUI({
            parent : UI,
            gasketName : "list",
            escapeHtml : false,
            icon : UI.msg("hmaker.lib.icon_item"),
            text : function(o) {
                return '<b>' + o.nm + '</b>';
            },
            on_actived : function(o){
                UI._show_libItem(o);
            },
            on_blur : function(jItems, nextObj, nextItem){
                if(!nextObj){
                    UI._show_blank();
                    UI.arena.find(">header>.item").empty();
                }
            }
        }).render(function(){
            UI.defer_report("list");
        });

        // 显示空白
        UI._show_blank();

        // 返回以便延迟加载
        return ["list"];
    },
    //...............................................................
    _show_libItem : function(o) {
        var UI = this;
        // 加载代码编辑器
        if("ui.o_edit_text" != UI.gasket.code.uiName) {
            new EditTextUI({
                parent : UI,
                gasketName : "code",
                showMeta : false,
            }).render(function(){
                this.update(o);
            });
        }
        // 直接更新代码编辑器
        else {
            UI.gasket.code.update(o);
        }
        // 通知
        UI.fire("active:libItem", o);
    },
    //...............................................................
    _show_blank : function(){
        var UI = this;
        // 显示代码编辑器
        new DomUI({
            parent : UI,
            gasketName : "code",
            dom : `<div class="blank-tip">
                <i class="fa fa-hand-o-left"></i>
                <em>{{hmaker.lib.blank}}</em>
            </div>`
        }).render();
        // 通知
        UI.fire("blur:libItem");
    },
    //...............................................................
    doRename : function(){
        var UI = this;
        // 得到当前组件对象
        var oLib = UI.gasket.list.getActived();

        // 防错，但是其实没啥必要
        if(!oLib) {
            UI.alert("hmaker.lib.e_noselect");
            return;
        }

        // 获取新名字
        UI.prompt("hmaker.lib.rename_tip", {
            placeholder : oLib.nm,
            ok : function(str, callback) {
                //console.log(str)
                // var re = Wn.execf('hmaker lib id:{{homeId}} -get "{{libName}}" | json -out @{id}', {
                //     homeId  : UI.getHomeObjId(),
                //     libName : str
                // });
                var re = Wn.exec('hmaker lib id:'+UI.getHomeObjId()+' -get "'+str+'" | json -out @{id}');

                var err = $.trim(re);
                // 返回了 ID，那么说明存在 
                if(/^[0-9a-v]{26}$/.test(err)){
                    err = "hmaker.lib.nm_exists";
                }
                // 不存在，可以改
                else if(/^e.io.noexists/.test(err)){
                    err = null;
                }
                // 其他错误
                //console.log(re, err);
                // 处理回调的显示
                callback(err);
                // 如果没错误，继续处理
                if(!err) {
                    // 准备命令
                    var cmdText = $z.tmpl('hmaker lib id:{{homeId}} -rename "{{libName}}" "{{newName}}"')({
                        homeId  : UI.getHomeObjId(),
                        libName : oLib.nm,
                        newName : str,
                    });
                    // 执行命令
                    Wn.processPanel(cmdText, {
                        width  : 500,
                        height : 480,
                    }, function(res, jMsg){
                        jMsg.text(UI.msg("hmaker.lib.rename_ok"));
                        // 后台敲敲刷新页面
                        UI.refresh();
                    });
                }
            } // ~ end of ok
        });
    },
    //...............................................................
    doDelete : function(){
        var UI = this;
        // 得到当前组件对象
        var oLib = UI.gasket.list.getActived();

        // 防错，但是其实没啥必要
        if(!oLib) {
            UI.alert("hmaker.lib.e_noselect");
            return;
        }

        // 获取新名字
        UI.confirm("hmaker.lib.delete_tip", function(str, callback) {
            UI.showLoading();
            Wn.execf('hmaker lib id:{{homeId}} -del "{{libName}}"', {
                homeId  : UI.getHomeObjId(),
                libName : oLib.nm
            }, function(re) {
                UI.hideLoading();
                UI.refresh();
            });
        });
    },
    //...............................................................
    update : function(o) {
        var UI = this;

        // 通知其他部分激活某个对象的属性
        UI.fire("active:lib");

        // 更新显示对象 
        UI.refresh();
    },
    //...............................................................
    refresh : function(){
        var UI = this;
        
        // 准备命令
        var cmdText = 'hmaker lib id:'+UI.getHomeObjId()+' -list obj';
        //console.log(cmdText);

        // 更新显示对象 
        UI.showLoading();
        Wn.exec(cmdText, function(re){
            UI.hideLoading();

            var list = $z.fromJson(re);
            //console.log(list)
            UI.gasket.list.setData(list);

            if(!UI.gasket.list.hasActived()) {
                UI._show_blank();
            }
        });
    },
    //...............................................................
    getCurrentEditObj : function(){
        return $z.invoke(this.gasket.code, "getCurrentEditObj", []);
    },
    //...............................................................
    getCurrentTextContent : function(){
        return $z.invoke(this.gasket.code, "getCurrentTextContent", []);
    },
    //...............................................................
    getActions : function(){
        return ["@::hmaker/hm_save", 
                "@::refresh",
                "::hmaker/hm_delete_lib",
                "::hmaker/hm_rename_lib",
                "~",
                "::hmaker/hm_site_new",
                "::hmaker/hm_site_dup",
                "::hmaker/hm_site_del",
                "~",
                "::hmaker/pub_site",
                "~",
                "::hmaker/hm_site_conf",
                "~",
                "::zui_debug",
                "::open_console"];
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);