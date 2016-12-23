(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/list/list',
    'ui/menu/menu',
    'ui/mask/mask',
    'ui/pop/pop_browser'
], function(ZUI, Wn, ListUI, MenuUI, MaskUI, PopBrowser){
//==============================================
var html = `
<div class="ui-code-template">
    <div code-id="adduser" class="pvgau-mask">
        <header>{{pvg.user_add}}</header>
        <div class="pvgau-tip"><%=pvg.user_add_tip%></div>
        <section></section>
        <footer>
            <input placeholder="{{pvg.user_add_tipI}}">
            <b><i class="fa fa-plus"></i> {{add}}</b>
        </footer>
        <div class="cans"></div>
    </div>
</div>
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
</div>`;
//==============================================
return ZUI.def("app.wn.pvg", {
    dom  : html,
    css  : "theme/app/wn.pvg/pvg.css",
    i18n : "app/wn.pvg/i18n/{{lang}}.js",
    //...............................................................
    redraw : function(){
        var UI  = this;

        // 这里初始化一下 pvg 编辑控件的 HTML
        UI.pvgHTML = '<span class="pvg-edit">';
        UI.pvgHTML += '<span class="pvg-cus">'+UI.msg("pvg.mode_cus")+'</span>';
        UI.pvgHTML += '<span class="pvg-mode">';
        UI.pvgHTML += '<u mode="r" val="4">'+UI.msg("pvg.mode_r")+'</u>';
        UI.pvgHTML += '<u mode="w" val="2">'+UI.msg("pvg.mode_w")+'</u>';
        UI.pvgHTML += '<u mode="x" val="1">'+UI.msg("pvg.mode_x")+'</u>';
        UI.pvgHTML += '<u class="pvg-auto" data-balloon="'+UI.msg("pvg.mode_auto")+'" data-balloon-pos="down"><i class="zmdi zmdi-close"></i></u>';
        UI.pvgHTML += '</span>';
        UI.pvgHTML += '</span>';

        // 创建 MenuUI: users
        new MenuUI({
            parent : UI,
            gasketName : "usersMenu",
            setup : [{
                text : "i18n:pvg.user_add",
                handler : function(){
                    // 定义执行添加的函数
                    var on_add_user = function(e){
                        var uiMask = this;
                        var jInput = $(e.target).closest(".pvgau-mask footer").find("input");
                        var jMM    = jInput.closest(".pvgau-mask");
                        var jTip   = jMM.find(".pvgau-tip");
                        if(jInput.length > 0) {
                            var unm = $.trim(jInput.val()).replace(/"/g,"\\\"");
                            if(unm){
                                var cmdText = 'grp ' + Wn.app().session.grp + ' -a "'+unm+'" -role 10';
                                // 显示提示文字
                                jTip.html(UI.msg('pvg.user_add_ing'));
                                // 执行
                                Wn.exec(cmdText, function(re){
                                    // 失败
                                    if(/^e./.test(re)){
                                        jTip.html(UI.msg('pvg.user_add_fail',{msg:$.trim(re)}));
                                        // 闪一下提示文字
                                        $z.blinkIt(jTip);
                                    }
                                    // 成功
                                    else {
                                        jTip.html(UI.msg('pvg.user_add_ok'));
                                        // 创建一个新的行
                                        var html = '<div><i class="fa fa-user"></i> <b>' + unm + '</b>';
                                        var jUsr = $(html).appendTo(jMM.find("section"));
                                        $z.blinkIt(jUsr);
                                        // 去掉原来的文字
                                        jInput.val("");
                                        // 记录临时数据
                                        uiMask._unms = (uiMask._unms || []).concat(unm);
                                        // 移除对应的列表项
                                        $z.removeIt(uiMask.$main.find(".cans .can-u[unm="+unm+"]"));
                                    }
                                });
                            }
                        }
                    };
                    // 定义读取提示
                    var do_keyup_input = function(jInput){
                        var uiMask = this;
                        var jCans  = uiMask.$main.find(".cans");
                        var val = $.trim(jInput.val());
                        //console.log(val)

                        var usrs = UI.gasket.usersList.getData();
                        var unms = [].concat(uiMask._unms || []);
                        for(var i=0; i<usrs.length; i++)
                            unms.push(usrs[i].nm);
                        //console.log(unms);

                        // 准备请求数据
                        var params = {
                            nb : 10,
                            p  : val,
                            ignore : unms.join(",")
                        };

                        // 发送请求
                        $[params.ignore.length<512?"get":"post"]("/u/ajax/list", params, function(re){
                            var reo = $z.fromJson(re);
                            //console.log(reo)
                            // 清空
                            jCans.empty();

                            // 显示
                            if(reo && reo.ok && reo.data.length > 0) {
                                for(var i=0; i<reo.data.length; i++) {
                                    var ci  = reo.data[i];
                                    var jCi = $('<div class="can-u">')
                                        .attr("unm", ci.nm)
                                            .appendTo(jCans);
                                    $('<i>').css('background-image', 'url(/u/avatar/usr?nm='+ci.nm+')')
                                            .appendTo(jCi);
                                    $('<b>').text(ci.nm)
                                            .appendTo(jCi);
                                }
                            }
                            // 显示空
                            else {
                                jCans.text(UI.msg("pvg.user_nofound"));
                            }
                        });
                    };
                    // 打开遮罩
                    new MaskUI({
                        width  : 400,
                        height : 500,
                        on_close : function(){
                            UI.reloadUsers();
                        },
                        on_init : function(){
                            this.watchKey(13, on_add_user);
                        },
                        events : {
                            "click footer b"     : on_add_user,
                            "keyup footer input" : function(e) {
                                var uiMask  = this;
                                var jInput  = $(e.currentTarget);

                                var old_v   = jInput.attr("old-v");
                                var new_v   = $.trim(jInput.val());
                                if(new_v == old_v)
                                    return;
                                
                                var lastMs  = jInput.attr("last-ms") * 1 || -1;
                                var nowInMs = Date.now();
                                var duInMs  = nowInMs - lastMs;

                                //console.log(lastMs, nowInMs, duInMs);

                                if(duInMs > 800) {
                                    window.setTimeout(function(){
                                        jInput.attr("old-v", $.trim(jInput.val()))
                                                .removeAttr("last-ms");
                                        do_keyup_input.call(uiMask, jInput);
                                    }, 1000);
                                }

                                jInput.attr("last-ms", nowInMs);
                            },
                            "click .cans .can-u" : function(e){
                                var uiMask = this;
                                var unm    = $(e.currentTarget).find("b").text();
                                uiMask.$main.find("footer input").val(unm);
                            }
                        }
                    }).render(function(){
                        UI.ccode("adduser").appendTo(this.$main);
                    });
                }
            }, {
                text : "i18n:pvg.user_del",
                handler : function(){
                    // 得到选中的用户
                    var u = UI.gasket.usersList.getActived();
                    if(!u){
                        alert(UI.msg("pvg.user_del_none"));
                        return;
                    }

                    // 不能删除管理员
                    if(1 == u.role) {
                        alert(UI.msg("pvg.user_del_admin"));
                        return;
                    }

                    // 遍历右侧列表，依次删除路径中的权限设定
                    var oList = UI.gasket.pathsList.getData();
                    if(oList) {
                        for(var i=0; i<oList.length; i++) {
                            var o = oList[i];
                            UI.delPvg(o.id, u);
                        }
                    }

                    // 从组中移除选定用户
                    var cmdText = 'grp ' + Wn.app().session.grp + ' -d "id:'+u.id+'"';
                    Wn.exec(cmdText, function(re){
                        // 失败
                        if(/^e./.test(re)){
                            alert(re);
                        }
                        // 删除成功，修改界面显示
                        else {
                            var jN2 = UI.gasket.usersList.remove(u.id);
                            UI.gasket.usersList.setActived(jN2);
                        }
                    });
                }
            }, {
                icon : '<i class="fa fa-refresh"></i>',
                text : "i18n:refresh",
                handler : function(){
                    UI.reloadUsers();
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
            display : function(u){
                var html = '<span class="icon"><i class="uicon fa"></i></span>';
                html += '<b>' + u.nm + '</b>';
                html += '<em>' + UI.msg("pvg.role_"+u.roleName) + '</em>';
                html += UI.pvgHTML;
                return html;
            },
            on_draw_item : function(jItem, u){
                jItem.attr("role",u.roleName);
                // 管理员
                if(1 == u.role){
                    jItem.find("i.uicon").addClass("fa-user-secret");
                    jItem.find("span.pvg-edit").remove();
                }
                // 待定
                else if(100 == u.role){
                    jItem.find("i.uicon").addClass("fa-question");
                    jItem.find("span.pvg-edit").remove();
                }
                // 阻止
                else if(-1 == u.role){
                    jItem.find("i.uicon").addClass("fa-ban");
                    jItem.find("span.pvg-edit").remove();
                }
                // 默认是成员
                else{
                    jItem.find("i.uicon").addClass("fa-user");
                }
            },
            on_actived : function(u){
                UI.gasket.pathsList.setAllBlur();
                // 成员，定制 
                if(u.role == 10) {
                    UI.arena.find(".pvg-paths-list").attr("pvg-edit-on", "yes");
                    UI.updatePathsPvgSetting(u.id);
                }
                // 否则不显示
                else {
                    UI.arena.find(".pvg-paths-list").removeAttr("pvg-edit-on");   
                }
            },
            on_blur : function(){
                UI.arena.find(".pvg-paths-list").removeAttr("pvg-edit-on");
            },
            dom_events : {
                "click .lst-item .pvg-mode u[mode]" : function(e){
                    e.stopPropagation();
                    var jq  = $(this);
                    var u   = ZUI(jq).getData(jq);
                    var oid = UI.gasket.pathsList.getActivedId();
                    UI.setPvg(oid, u, jq);
                },
                "click .lst-item .pvg-mode u.pvg-auto" : function(e){
                    e.stopPropagation();
                    var jq  = $(this);
                    var u   = ZUI(jq).getData(jq);
                    var oid = UI.gasket.pathsList.getActivedId();
                    UI.delPvg(oid, u, jq);
                },
                "click .lst-item .pvg-cus" : function(e){
                    e.stopPropagation();
                    var jq  = $(this);
                    var u   = ZUI(jq).getData(jq);
                    var oid = UI.gasket.pathsList.getActivedId();
                    UI.addPvg(oid, u, jq);
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
                text : "i18n:pvg.items_add",
                handler : function(){
                    // 打开遮罩
                    new MaskUI({
                        width  : 400,
                        height : "80%",
                        arenaClass : "pvg-sideitem-mask",
                        events : {
                            "click .ui-mask-main item" : function(e) {
                                var uiMask = this;

                                // 执行添加
                                var jItem = $(e.currentTarget);
                                var o = Wn.fetch(jItem.attr("ph"));
                                var cmdText = 'obj id:'+o.id+" -u 'pvg:{}';\n";
                                Wn.exec(cmdText);

                                // 关闭弹出层
                                uiMask.close();

                                // 增加
                                if(!UI.gasket.pathsList.has(o.id)) {
                                    UI.gasket.pathsList.add(o, -1);
                                    // 清除缓存
                                    Wn.removeFromCache(o);
                                    // 闪一下
                                    $z.blinkIt(UI.gasket.pathsList.$item(-1))
                                }
                            }
                        }
                    }).render(function(){
                        // 得到全部侧边栏项目
                        this.$main.addClass('ui-oicon-16')
                            .html($('.obrowser-chute-sidebar .chute-wrapper').html());

                        // 移除那些已经添加过的
                        var dels = [];
                        this.$main.find("item").each(function(){
                            var o = Wn.fetch($(this).attr("ph"));
                            if(o.pvg){
                                dels.push(this);
                            }
                        });
                        $(dels).remove();
                    });
                }
            }, {
                text : "i18n:pvg.paths_add",
                handler : function(){
                    new PopBrowser({
                        title   : UI.msg("pvg.paths_add"),
                        lastObjId : "pvgAddPath",
                        objTagName : "SPAN",
                        filter  : function(o){
                            return 'DIR' == o.race;
                        },
                        canOpen : function(o){
                            return o.race == 'DIR';
                        },
                        on_ok : function(objs){
                            if(objs){
                                var cmdText = "";
                                // 显示路径
                                for(var i=0;i<objs.length;i++){
                                    var obj = objs[i];
                                    if(!UI.gasket.pathsList.has(obj.id)) {
                                        UI.gasket.pathsList.add(obj, -1);
                                        // 清除缓存
                                        Wn.removeFromCache(obj);
                                        cmdText += 'obj id:'+obj.id+" -u 'pvg:{}';\n";
                                    }
                                }
                                // 标识 pvg 元数据
                                if(cmdText)
                                    Wn.exec(cmdText);
                            }
                        }
                    }).render();
                }
            }, {
                text : "i18n:pvg.paths_del",
                handler : function(){
                    // 得到选中的路径
                    var oid = UI.gasket.pathsList.getActivedId();
                    if(!oid){
                        alert(UI.msg("pvg.path_del_none"));
                        return;
                    }

                    // 从组中移除选定用户
                    var cmdText = 'obj "id:'+oid+'" -u "pvg:null" -o';
                    //console.log(cmdText)
                    Wn.exec(cmdText, function(re){
                        // 失败
                        if(/^e./.test(re)){
                            alert(re);
                        }
                        // 删除成功，
                        else {
                            // 缓存数据
                            var o = $z.fromJson(re);
                            Wn.saveToCache(o);
                            // 修改界面显示
                            var jN2 = UI.gasket.pathsList.remove(oid);
                            UI.gasket.pathsList.setActived(jN2);
                        }
                    });
                }
            }, {
                icon : '<i class="fa fa-refresh"></i>',
                text : "i18n:refresh",
                handler : function(){
                    UI.reloadPaths();
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
                var html = '<span class="icon">' + Wn.objIconHtml(o) + '</span>';
                html += '<span>' + Wn.objDisplayPath(UI, o.ph, 2) + '</span>';
                html += UI.pvgHTML;
                return html;
            },
            on_actived : function(o){
                UI.gasket.usersList.setAllBlur();
                UI.arena.find(".pvg-users-list").attr("pvg-edit-on", "yes");
                UI.updateUsersPvgSetting(o.id);
            },
            on_blur : function(){
                UI.arena.find(".pvg-users-list").removeAttr("pvg-edit-on");
            },
            dom_events : {
                "click .lst-item .pvg-mode u[mode]" : function(e){
                    e.stopPropagation();
                    var jq  = $(this);
                    var u   = UI.gasket.usersList.getActived();
                    var oid = ZUI(jq).getData(jq).id;
                    UI.setPvg(oid, u, jq);
                },
                "click .lst-item .pvg-mode u.pvg-auto" : function(e){
                    e.stopPropagation();
                    var jq  = $(this);
                    var u   = UI.gasket.usersList.getActived();
                    var oid = ZUI(jq).getData(jq).id;
                    UI.delPvg(oid, u, jq);
                },
                "click .lst-item .pvg-cus" : function(e){
                    e.stopPropagation();
                    var jq  = $(this);
                    var u   = UI.gasket.usersList.getActived();
                    var oid = ZUI(jq).getData(jq).id;
                    UI.addPvg(oid, u, jq);
                }
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
    setPvg : function(oid, u, jq){
        var UI = this;

        if(!oid || !u)
            return;

        // 如果有激活的项目，那么修改它的 pvg 段
        var o = Wn.getById(oid);
        if(!o)
            return;

        // 标记模式
        jq.toggleClass("checked");

        // 计算权限码
        var jMode = jq.closest(".pvg-mode");
        var md = 0;
        jMode.find("u.checked").each(function(){
            md |= $(this).attr("val") * 1;
        });

        var pvg = {};
        pvg[u.id] = md;

        // 执行提交
        var map = {
            oid : oid,
            pvg : $z.toJson(pvg)
        };
        var cmdTmpl = 'obj id:{{oid}} -set \'pvg:<%=pvg%>\' -o';
        //console.log($z.tmpl(cmdTmpl)(map));
        Wn.execf(cmdTmpl, map, function(re){
            var obj = $z.fromJson(re);
            Wn.saveToCache(obj);

            // 界面上更新权限编辑控件
            var jSpan = jq.closest("span.pvg-edit");
            jSpan.attr("pvg", md);
        });
    },
    //...............................................................
    addPvg : function(oid, u, jq){
        var UI = this;
        
        if(!oid || !u)
            return;

        // 如果有激活的项目，那么修改它的 pvg 段
        var o = Wn.getById(oid);
        if(!o)
            return;

        // 计算权限
        var pvg = {};
        pvg[u.id] = 0;

        // 执行提交
        var map = {
            oid : oid,
            pvg : $z.toJson(pvg)
        };
        var cmdTmpl = 'obj id:{{oid}} -set \'pvg:<%=pvg%>\' -o';
        //console.log($z.tmpl(cmdTmpl)(map));
        Wn.execf(cmdTmpl, map, function(re){
            var obj = $z.fromJson(re);
            Wn.saveToCache(obj);

            // 界面上显示权限编辑控件
            var jSpan = jq.closest("span.pvg-edit");
            jSpan.attr("pvg", 0).find("u[mode]").removeClass("checked");
        });
    },
    //...............................................................
    // jq 参数可选，就是控制界面显示的
    delPvg : function(oid, u, jq){
        var UI = this;

        if(!oid || !u)
            return;

        // 如果有激活的项目，那么修改它的 pvg 段
        var o = Wn.getById(oid);
        if(!o)
            return;

        // 计算权限
        var pvg = {};
        pvg[u.id] = null;

        // 执行提交
        var map = {
            oid : oid,
            pvg : $z.toJson(pvg)
        };
        var cmdTmpl = 'obj id:{{oid}} -set \'pvg:<%=pvg%>\' -o';
        //console.log($z.tmpl(cmdTmpl)(map));
        Wn.execf(cmdTmpl, map, function(re){
            var obj = $z.fromJson(re);
            Wn.saveToCache(obj);

            // 界面上隐藏权限编辑控件
            if(jq){
                var jSpan = jq.closest("span.pvg-edit");
                jSpan.removeAttr("pvg").find("u[mode]").removeClass("checked");
            }
        });
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
            UI.__updatePvgSetting(jSpan, mode);
        });
    },
    //...............................................................
    updatePathsPvgSetting : function(uid) {
        var UI  = this;

        UI.arena.find(".pvg-paths-list .lst-item").each(function(){
            var jItem = $(this);
            var jSpan = jItem.find("span.pvg-edit");
            var oid   = jItem.attr("oid");
            var obj   = Wn.getById(oid);
            var pvg   = obj.pvg || {};
            var mode  = pvg[uid];
            UI.__updatePvgSetting(jSpan, mode);
        });
    },
    //...............................................................
    __updatePvgSetting : function(jSpan, mode) {
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
    },
    //...............................................................
    reloadUsers : function(callback){
        var UI  = this;
        var grp = Wn.app().session.grp;

        // 得到用户列表
        UI.gasket.usersList.showLoading();
        Wn.exec("grp-users -sort 'role:1' -json" + grp, function(re){
            UI.gasket.usersList.hideLoading();
            var list = $z.fromJson(re);
            UI.gasket.usersList.setData(list);
            UI.gasket.usersList.setAllBlur();
            UI.gasket.pathsList.setAllBlur();
            UI.arena.find("[pvg-edit-on]").removeAttr("pvg-edit-on");
            $z.doCallback(callback, [], UI);
        });
    },
    //...............................................................
    reloadPaths : function(callback) {
        var UI  = this;
        var grp = Wn.app().session.grp;

        // 得到路径列表
        UI.gasket.pathsList.showLoading();
        Wn.exec('obj -match \'d1:"'+grp+'", pvg:{"\\$ne":null}\' -json -l -P', function(re){
            UI.gasket.pathsList.hideLoading();
            var list = $z.fromJson(re);
            UI.gasket.pathsList.setData(list);
            UI.gasket.usersList.setAllBlur();
            UI.gasket.pathsList.setAllBlur();
            UI.arena.find("[pvg-edit-on]").removeAttr("pvg-edit-on");
            UI.gasket.usersList.setAllBlur();
            $z.doCallback(callback, [], UI);
        });
    },
    //...............................................................
    // 这个木啥用了，就是一个空函数，以便 browser 来调用
    update : function(o) {
        // var UI = this;
        // UI.arena.find(".pvg-users-menu .menu-item").first().click();
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);