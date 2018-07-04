(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/pop/pop',
    'ui/list/list',
    'ui/thing/support/th_design_command_list'
], function(ZUI, Wn, POP, ListUI, ThDesignCommandListUI){
//==============================================
var html = function(){/*
<div class="ui-arena th-design-commands" ui-fitparent="yes">
    <section class="thdc-actions">
        <div class="thdca-list" ui-gasket="list"></div>
        <div class="thdca-opt">
            <ul>
                <li><a m="add">添加函数集</a></li>
                <li><a m="del">移除函数集</a></li>
            </ul>
        </div>
    </section>
    <section class="thdc-search" ui-gasket="search"></section>
    <section class="thdc-obj" ui-gasket="obj"></section>
</div>
*/};
//==============================================
return ZUI.def("app.wn.thdesign_commands", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    events : {
        // 添加函数集
        'click .thdca-opt a[m="add"]' : function(){
            var UI = this;

            // 得到上一次打开的地址
            var lastPath = UI.local('thdc-last-open') || "~";
            var base = Wn.get(lastPath);

            // 打开浏览器
            POP.browser({
                base : base,
                setup : {
                    filter : function(o) {
                        if('DIR' == o.race)
                            return true;
                        return 'js' == o.tp;
                    }
                },
                ok : function(o) {
                    var objs   = this.getChecked();
                    console.log(objs)
                    if(objs && objs.length > 0) {
                        // 存储第一个对象所在的路径
                        UI.local('thdc-last-open', objs[0].ph);
                        // 将所有的对象设置到路径里面
                        UI.__add_objs(objs);
                    }
                }
            }, UI);
        },
        // 移除函数集
        'click .thdca-opt a[m="del"]' : function(){
            this.gasket.list.remove();
            // 通知修改
            this.notifyChanged();
        }
    },
    //...............................................................
    redraw : function() {
        var UI  = this;       
        
        // 列表
        new ListUI({
            parent : UI,
            gasketName : "list",
            escapeHtml : false,
            icon : '<i class="zmdi zmdi-language-javascript"></i>',
            text : function(o) {
                return o.ph;
            }

        }).render(function(){
            UI.defer_report("list");
        });

        // 搜索命令菜单
        new ThDesignCommandListUI({
            parent : UI,
            gasketName : "search",
            title : '列表菜单扩展命令'
        }).render(function(){
            UI.defer_report("search");
        });

        // 对象命令菜单
        new ThDesignCommandListUI({
            parent : UI,
            gasketName : "obj",
            title : '对象菜单扩展命令'
        }).render(function(){
            UI.defer_report("obj");
        });
        
        // 返回延迟加载
        return ["list", "search", "obj"];
    },
    //...............................................................
    __add_objs : function(objs) {
        var UI = this;
        if(_.isArray(objs) && objs.length > 0) {
            var list = UI.gasket.list.getData() || [];
            // 定义一个判断对象是否在列表里的函数
            var _is_in_list = function(o) {
                if(!_.isArray(list) || list.length == 0)
                    return false;
                for(var i=0; i<list.length; i++)
                    if(list[i].id == o.id)
                        return true;
                return false;
            }
            // 逐个添加
            for(var i=0; i<objs.length; i++) {
                var o = objs[i];
                if(!_is_in_list(o))
                    list.push(o);
            }
            // 添加完毕后，显示列表
            UI.gasket.list.setData(list);
            // 通知修改
            UI.notifyChanged();
        }
    },
    //...............................................................
    getData : function() {
        var UI = this;
        
        // 准备返回数据
        var data = {
            actions : [],
            search  : [],
            obj : []
        };

        // 得到扩展命令集合的对象列表
        var oHome = Wn.getHome();
        var oCmds = UI.gasket.list.getData();
        for(var i=0; i<oCmds.length; i++) {
            var oCmd = oCmds[i];
            var aph = oCmd.ph;
            if(Wn.isInDir(oHome, oCmd))
                aph = "~/" + Wn.getRelativePathToHome(oCmd);
            data.actions.push(aph);
        }

        // 得到搜索列表的扩展命令菜单
        data.search = UI.gasket.search.getData();

        // 得到对象的扩展命令菜单
        data.obj = UI.gasket.obj.getData();

        // 嗯返回吧
        return {
            extendCommand: data
        };
    },
    //...............................................................
    setData : function(thConf) {
        var UI = this;

        thConf = thConf || {};

        // 确保是对象
        if(_.isString(thConf))
            thConf = $z.fromJson(thConf);

        // 设置扩展命令
        var extcmds = thConf.extendCommand || {};

        // 设置命令集
        var actions = [];
        if(_.isArray(extcmds.actions)) {
            for(var i=0; i<extcmds.actions.length; i++) {
                var aph = extcmds.actions[i];
                var oCmd = Wn.fetch(aph, true);
                if(oCmd) {
                    actions.push(oCmd);
                }
            }
        }
        UI.gasket.list.setData(actions);

        // 设置数据
        UI.gasket.search.setData(extcmds.search);
        UI.gasket.obj.setData(extcmds.obj);
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);