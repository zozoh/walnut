(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/list/list',
    'ui/menu/menu',
    'ui/pop/pop_browser'
], function(ZUI, Wn, ListUI, MenuUI, PopBrowser){
//==============================================
var html = function(){/*
<div class="ui-arena pvg" ui-fitparent="yes" mode="inside">
    <div class="pvg-users-head pvg-head"><div>
        <div class="pvg-users-title pvg-title"><i class="fa fa-user"></i><b>{{pvg.users_title}}</b></div>
        <div class="pvg-users-menu pvg-menu" ui-gasket="usersMenu"></div>
    </div></div>
    <div class="pvg-users-list pvg-list" ui-gasket="usersList"></div>
    <div class="pvg-paths-head pvg-head"><div>
        <div class="pvg-paths-title pvg-title"><i class="fa fa-folder-o"></i><b>{{pvg.paths_title}}</b></div>
        <div class="pvg-paths-menu pvg-menu" ui-gasket="pathsMenu"></div>
    </div></div>
    <div class="pvg-paths-list pvg-list" ui-gasket="pathsList"></div>
</div>
*/};
//==============================================
return ZUI.def("app.wn.pvg", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "theme/app/wn.pvg/pvg.css",
    i18n : "app/wn.pvg/i18n/{{lang}}.js",
    //...............................................................
    events : {
        // 改变模式
        "click .pvg-users-list .lst-item .pvg-mode u[mode]" : function(e){
            var UI = this;
            var jq = $(e.currentTarget);

            // 得到当前激活的路径列表
            var oid = UI.gasket.pathsList.getActivedId();
            if(!oid)
                return;

            // 如果有激活的项目，那么修改它的 pvg 段
            var o = Wn.getById(oid);
            if(!o)
                return;

            // 标记模式
            jq.toggleClass("checked");

            // 不要冒泡了
            e.stopPropagation();

            // 得到用户的 ID
            var u = UI.gasket.usersList.getData(jq);

            // 计算权限码
            var jMode = jq.closest(".pvg-mode");
            var md = 0;
            jMode.find("u.checked").each(function(){
                md |= $(this).attr("val") * 1;
            });

            var pvg = o.pvg || {};
            pvg[u.id] = md;

            // 执行提交
            var map = {
                oid : oid,
                pvg : $z.toJson(pvg)
            };
            var cmdTmpl = 'obj id:{{oid}} -u \'pvg:<%=pvg%>\' -o';
            //console.log($z.tmpl(cmdTmpl)(map));
            Wn.execf(cmdTmpl, map, function(re){
                var obj = $z.fromJson(re);
                Wn.saveToCache(obj);

                // 界面上更新权限编辑控件
                var jSpan = jq.closest("span.pvg-edit");
                jSpan.attr("pvg", md);
            });
        },
        // 隐藏权限定制控件，同时删除权限
        "click .pvg-users-list .lst-item .pvg-mode u.pvg-auto" : function(e){
            var UI = this;
            var jq = $(e.currentTarget);

            // 得到当前激活的路径列表
            var oid = UI.gasket.pathsList.getActivedId();
            if(!oid)
                return;

            // 如果有激活的项目，那么修改它的 pvg 段
            var o = Wn.getById(oid);
            if(!o)
                return;

            // 不要冒泡了
            e.stopPropagation();

            // 得到用户的 ID
            var u = UI.gasket.usersList.getData(jq);

            var pvg = o.pvg || {};
            delete pvg[u.id];

            // 执行提交
            var map = {
                oid : oid,
                pvg : $z.toJson(pvg)
            };
            var cmdTmpl = 'obj id:{{oid}} -u \'pvg:<%=pvg%>\' -o';
            //console.log($z.tmpl(cmdTmpl)(map));
            Wn.execf(cmdTmpl, map, function(re){
                var obj = $z.fromJson(re);
                Wn.saveToCache(obj);

                // 界面上隐藏权限编辑控件
                var jSpan = jq.closest("span.pvg-edit");
                jSpan.removeAttr("pvg").find("u[mode]").removeClass("checked");
            });
        },
        // 显示权限定制控件
        "click .pvg-users-list .lst-item .pvg-cus" : function(e){
            var UI = this;
            var jq = $(e.currentTarget);

            // 得到当前激活的路径列表
            var oid = UI.gasket.pathsList.getActivedId();
            if(!oid)
                return;

            // 如果有激活的项目，那么修改它的 pvg 段
            var o = Wn.getById(oid);
            if(!o)
                return;

            // 不要冒泡了
            e.stopPropagation();

            // 得到用户的 ID
            var u = UI.gasket.usersList.getData(jq);

            var pvg = o.pvg || {};
            pvg[u.id] = 0;

            // 执行提交
            var map = {
                oid : oid,
                pvg : $z.toJson(pvg)
            };
            var cmdTmpl = 'obj id:{{oid}} -u \'pvg:<%=pvg%>\' -o';
            //console.log($z.tmpl(cmdTmpl)(map));
            Wn.execf(cmdTmpl, map, function(re){
                var obj = $z.fromJson(re);
                Wn.saveToCache(obj);

                // 界面上显示权限编辑控件
                var jSpan = jq.closest("span.pvg-edit");
                jSpan.attr("pvg", 0).find("u[mode]").removeClass("checked");
            });
        },
    },
    //...............................................................
    redraw : function(){
        var UI  = this;

        // 创建 MenuUI: users
        new MenuUI({
            parent : UI,
            gasketName : "usersMenu",
            setup : [{
                text : "i18n:pvg.user_add",
                handler : function(){

                }
            }, {
                text : "i18n:pvg.user_del",
                handler : function(){
                    
                }
            }]
        }).render(function(){
            UI.defer_report("usersMenu");
        });
        
        // 创建 ListUI: users
        new ListUI({
            parent : UI,
            gasketName : "usersList",
            activable  : true,
            checkable  : false,
            escapeHtml : false,
            display : function(o){
                var html = '<i class="uicon fa"></i>';
                html += '<b>' + o.nm + '</b>';
                html += '<em>' + UI.msg("pvg.role_"+o.roleName) + '</em>';
                html += '<span class="pvg-edit">';
                html += '<span class="pvg-cus">'+UI.msg("pvg.mode_cus")+'</span>';
                html += '<span class="pvg-mode">';
                html += '<u mode="r" val="4">'+UI.msg("pvg.mode_r")+'</u>';
                html += '<u mode="w" val="2">'+UI.msg("pvg.mode_w")+'</u>';
                html += '<u mode="x" val="1">'+UI.msg("pvg.mode_x")+'</u>';
                html += '<u class="pvg-auto" data-balloon="'+UI.msg("pvg.mode_auto")+'" data-balloon-pos="up"><i class="fa fa-close"></i></u>';
                html += '</span>';
                html += '</span>';
                return html;
            },
            on_draw_item : function(jItem, o){
                jItem.attr("role",o.roleName);
                // 管理员
                if(1 == o.role){
                    jItem.find("i.uicon").addClass("fa-user-secret");
                }
                // 待定
                else if(100 == o.role){
                    jItem.find("i.uicon").addClass("fa-question");
                    jItem.find("span.pvg-edit").remove();
                }
                // 阻止
                else if(-1 == o.role){
                    jItem.find("i.uicon").addClass("fa-ban");
                    jItem.find("span.pvg-edit").remove();
                }
                // 默认是成员
                else{
                    jItem.find("i.uicon").addClass("fa-user");
                }
            }
        }).render(function(){
            UI.reloadUsers(function(){
                UI.defer_report("usersList");
            });
        });

        // 创建 MenuUI: paths
        new MenuUI({
            parent : UI,
            gasketName : "pathsMenu",
            setup : [{
                text : "i18n:pvg.paths_add",
                handler : function(){
                    new PopBrowser({
                        title   : UI.msg("pvg.paths_add"),
                        lastObjId : "pvgAddPath",
                        filter  : function(o){
                            return 'DIR' == o.race;
                        },
                        canOpen : function(o){
                            return o.race == 'DIR';
                        },
                        on_ok : function(objs){
                            if(objs){
                                for(var i=0;i<objs.length;i++){
                                    var obj = objs[i];
                                    if(!UI.gasket.pathsList.has(obj.id)) {
                                        UI.gasket.pathsList.add(obj);
                                    }
                                }
                            }
                        }
                    }).render();
                }
            }, {
                text : "i18n:pvg.paths_del",
                handler : function(){
                    
                }
            }]
        }).render(function(){
            UI.defer_report("pathsMenu");
        });

        // 创建 ListUI: paths
        new ListUI({
            parent : UI,
            gasketName : "pathsList",
            escapeHtml : false,
            display : function(o){
                var html = '<i class="oicon" otp="'+Wn.objTypeName(o)+'"></i>';
                html += '<span>' + Wn.objDisplayPath(UI, o.ph) + '</span>';
                return html;
            },
            on_actived : function(o){
                UI.arena.find(".pvg-users-list").attr("pvg-edit-on", "yes");
                UI.updateUsersPvgSetting(o.id);
            },
            on_blur : function(){
                UI.arena.find(".pvg-users-list").removeAttr("pvg-edit-on");
            }
        }).render(function(){
            UI.reloadPaths(function(){
                UI.defer_report("pathsList");
            });
        });

        // 返回延迟加载列表
        return ["usersList", "usersMenu", "pathsList", "pathsMenu"];
    },
    //...............................................................
    updateUsersPvgSetting : function(oid) {
        var UI  = this;
        var obj = Wn.getById(oid);
        var pvg = obj.pvg || {};

        UI.arena.find(".pvg-users-list .lst-item").each(function(){
            var jItem = $(this);
            var jSpan = jItem.find("span.pvg-edit");
            var uid   = jItem.attr("oid");
            var mode  = pvg[uid];
            // 木定义了权限
            if(_.isUndefined(mode)) {
                jSpan.removeAttr("pvg").find("u[mode]").removeClass("checked");
            }
            // 定义了权限，分析权限码
            else {
                jSpan.attr("pvg", mode).find("u[mode]").each(function(){
                    var jU   = $(this);
                    var mask = jU.attr("val") * 1;
                    if((mode & mask) > 0) {
                        jU.addClass("checked");
                    }else{
                        jU.removeClass("checked");
                    }
                });
            }
        });
    },
    //...............................................................
    reloadUsers : function(callback){
        var UI  = this;
        var grp = Wn.app().session.grp;

        // 得到用户列表
        Wn.exec("grp-users -sort 'role:1' -json" + grp, function(re){
            var list = $z.fromJson(re);
            UI.gasket.usersList.setData(list);
            $z.doCallback(callback, [], UI);
        });
    },
    //...............................................................
    reloadPaths : function(callback) {
        var UI  = this;
        var grp = Wn.app().session.grp;

        // 得到路径列表
        Wn.exec('obj -match \'d1:"'+grp+'", pvg:{"\\$exists":true}\' -json -l -P', function(re){
            var list = $z.fromJson(re);
            UI.gasket.pathsList.setData(list);
            $z.doCallback(callback, [], UI);
        });
    },
    //...............................................................
    // 这个木啥用了，就是一个空函数，以便 browser 来调用
    update : function(o) {}
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);