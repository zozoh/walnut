(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/obrowser/support/browser__methods',
    'ui/menu/menu', 
    'jquery-plugin/folder/folder'
], function(ZUI, Wn, BrowserMethods, MenuUI){
//==============================================
var html = function(){/*
<div class="ui-code-template">
    <div code-id="me.info">
        <span></span><b></b><a>{{exit}}</a>
    </div>
    <div code-id="crumb.item" class="citem ui-clr">
        <b></b><span class="ochild"><i class="fa fa-caret-down"></i></span>
    </div>
</div>
<div class="ui-arena obrowser-sky ui-clr" ui-fitparent="true">
    <div class="obrowser-crumb"></div>
    <div class="obrowser-me"></div>
    <div class="obrowser-menu" ui-gasket="menu"></div>
</div>
*/};
//==============================================
return ZUI.def("ui.obrowser_sky", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //..............................................
    init : function(){
        var UI = BrowserMethods(this);
        // 注册全局事件，控制子菜单的关闭
        var on_hide_drop_menu = function(e){
            var jq = $(e.target);
            // 只要不点击展开按钮，就关闭菜单
            if(jq.closest(".folder-item").size()==0){
                UI.hideFolderDropMenu();
            }
            // 只要不是点击打开按钮，就关闭子节点菜单
            if(jq.closest(".ochild").size() == 0){
                UI.hideItemDropMenu();
            }
        };
        var on_edit_address = function(e){
            e.preventDefault();
            UI.arena.find(".obrowser-crumb").click();
        };
        UI.watchMouse("click", on_hide_drop_menu);
        UI.watchKey(27, on_hide_drop_menu);
        UI.watchKey(76, "alt", on_edit_address);
        UI.watchKey(76, "meta", on_edit_address);
    },
    //..............................................
    events : {
        "click .obrowser-crumb .folder-item" : function(e){
            var jFolder = $(e.currentTarget);
            var jBtn  = jFolder.children().eq(0);
            var jDrop = jFolder.children().eq(1);
            $z.dock(jBtn, jDrop.fadeIn(300));
        },
        "click .obrowser-crumb" : function(e){
            var UI  = this;
            var opt = UI.browser().options;
            var jq  = $(e.target);
            var jItem = jq.closest(".citem");
            // 点击空白处，编辑路径
            if(jItem.size() == 0 
                && jq.closest(".folder-item").size()==0){
                UI.editPath();
            }
            // 显示自己的子对象列表
            else if(jq.closest(".ochild").size()>0){
                // 禁止主目录点击
                var ph = UI.getPath(jItem);
                if(opt.forbidClickHomeInCrumb && "~" == ph){
                    return;
                }

                // 嗯准备展开项目吧
                var oid = jItem.attr("oid");
                var o   = Wn.getById(oid);
                var children = Wn.getChildren(o, "DIR");
                var jDrop = jItem.find(".citem-drop");
                // 已经展开了，就啥也不做了
                if(jDrop.size()>0)
                    return;

                // 移除其他的下拉项
                UI.hideItemDropMenu();

                // 创建下来列表
                jDrop = $('<div class="citem-drop">').appendTo(jItem);
                if(children.length>0){
                    for(var i=0;i<children.length;i++){
                        UI._append_crumb_item(jDrop, children[i], "tail");
                    }
                }
                // 没有子，就显示空
                else{
                    jDrop.html('<div class="citem-drop-empty">'+UI.msg("empty")+'</div>');
                }
                // 停靠再选择节点周围
                $z.dock(jItem, jDrop);
                
            }
            // 切换到自己所在的路径
            else if(jq.closest(".folder-btn").size()==0){
                var ph = UI.getPath(jItem);
                // 禁止主目录点击
                if(opt.forbidClickHomeInCrumb && "~" == ph){
                    return;
                }

                //console.log("::", ph);
                UI.browser().setData(ph);
            }
        }
    },
    //..............................................
    editPath : function(){
        var UI = this;
        var jCrumb = UI.arena.find(".obrowser-crumb");

        $z.editIt(jCrumb, {
            copyStyle : false,
            text      : UI.getPath(),
            after     : function(newval, oldval){
                if(newval != oldval){
                    UI.browser().setData(newval);
                }
            }
        });
    },
    //..............................................
    // 得到当前正在编辑的路径
    getPath : function(ele){
        var UI  = this;
        var nms = [];
        UI.getData(ele).forEach(function(o){
            nms.push(o.nm);
        });
        // 在主目录内
        if("~" == nms[0])
            return nms.join("/");
        // 否则拼上绝对路径
        else
            return "/" + nms.join("/");
    },
    //..............................................
    // 返回显示中的（包括折叠）的每个对象的 id 和 nm
    // 其中，如果是主目录，用 "~" 表示 nm
    getData : function(ele){
        var UI = this;
        var objs = [];
        var foldered_oid;
        var jq;
        // 给定一个面包屑项，那就是获取它的路径
        if(ele){
            var jItem = $(ele).closest(".citem");
            var jFolder = jItem.closest(".folder-item");
            var jDrop   = jItem.closest(".citem-drop");
            // 在折叠中，标记有效的 foldered_oid
            if(jFolder.size()>0){
                foldered_oid = jItem.attr("oid");
                jq = jFolder.prevAll().addBack();
            }
            // 在下拉子节点菜单中
            else if(jDrop.size()>0){
                jq = jDrop.closest(".citem").prevAll().addBack().add(jItem);
            }
            // 好吧就是普通路径咯
            else{
                jq = jItem.prevAll().addBack();
            }
        }
        // 默认的，就用全部面包屑项目
        else{
            jq = UI.arena.find(".obrowser-crumb").children();
        }

        // 进行迭代
        jq.each(function(){
            var jItem = $(this);
            // 折叠部分，反序查找
            if(jItem.hasClass("folder-item")){
                jItem.children(".folder-drop").children().get().reverse()
                .forEach(function(ele){
                    // 表示已经超过给定项目，就都忽略吧
                    if(foldered_oid===true){
                        return;
                    }
                    // 如果遇到了给定的项目，标记一下，以便忽略后面
                    var oid = $(ele).attr("oid");
                    if(foldered_oid===oid){
                        foldered_oid = true;
                    }
                    // 添加到返回列表
                    objs.push({id:oid, nm:$(ele).attr("onm")});
                });
            }
            // 其他的，直接添加
            else{
                objs.push({id:jItem.attr("oid"), nm:jItem.attr("onm")});
            }
        });
        return objs;
    },
    //..............................................
    hideFolderDropMenu : function(){
        var jFolder = this.arena.find(".obrowser-crumb .folder-item");
        var jDrop = jFolder.children().eq(1);
        jDrop.fadeOut(200);
    },
    //..............................................
    hideItemDropMenu : function(){
        this.arena.find(".obrowser-crumb .citem-drop").remove();
    },
    //..............................................
    update : function(o, asetup){
        var UI  = this;
        var opt = UI.browser().options;

        // 标识禁止主目录点击
        if(opt.forbidClickHomeInCrumb){
            UI.arena.find(".obrowser-crumb").attr("forbid-click-home","yes");
        }

        // 记录对象的路径
        UI.arena.attr("PWD", o.ph);
        
        // 绘制面包屑道航
        UI._draw_crumb(o);

        // 绘制用户信息
        var jMe = UI.arena.find(".obrowser-me").empty();
        if(opt.myInfo) {
            var jMyInfo = UI.ccode("me.info").appendTo(jMe);
            // 名称
            jMyInfo.find("b").text(opt.myInfo.name);
            // 头像
            if(opt.myInfo.avata){
                jMyInfo.find("span").html(opt.myInfo.avata);
            }else{
                jMyInfo.find("span").remove();
            }

            // 登出链接
            if(opt.myInfo.logout)
                jMyInfo.find("a").prop("href", opt.myInfo.logout);
            else
                jMyInfo.find("a").remove();
        }
        // 否则删除
        else {
            jMe.remove();
        }

        // 绘制右侧动作菜单
        // UI._draw_menu(UIBrowser, o, asetup);

        // 最后重新计算一下尺寸
        UI.resize();

        // 触发事件
        UI.fire("browser:current", o);
        UI.invokeCallback("on_current", [o], UI);
    },
    //..............................................
    updateMenu : function(menuSetup, menuContext){
        this._draw_menu(menuSetup, menuContext);
        this.resize();
    },
    //..............................................
    _draw_menu : function(menuSetup, menuContext){
        var UI = this;
        var jMenu = UI.arena.find(".obrowser-menu");

        // 没有菜单
        if(!_.isArray(menuSetup) || menuSetup.length == 0){
            jMenu.hide();
            return;
        }

        // 显示菜单
        jMenu.show();

        // 调用菜单控件显示
        new MenuUI({
            parent       : UI,
            gasketName   : "menu",
            tipDirection : "left",
            setup        : menuSetup,
            context      : menuContext || UI.browser()
        }).render(function(){
            $z.defer(function(){
                UI.resize();
            });
        });
    },
    //..............................................
    _draw_crumb : function(o){
        var UI = this;
        // 开始创建面包屑道航条
        var jCrumb = UI.arena.find(".obrowser-crumb").empty();
        var list = Wn.getAncestors(o, true);

        // 查找主目录
        var i=0;
        var homePath = UI.app.session.envs.HOME;
        //console.log("home:", homePath);

        for(;i<list.length;i++){
            // console.log(i, list[i].ph);
            if(list[i].ph == homePath){
                UI._append_crumb_item(jCrumb, list[i], "home");
                break;
            }
        }

        // 不在主目录之中
        if(i == list.length){
            var lastIndex = list.length-1;
            for(i=0;i<list.length;i++){
                UI._append_crumb_item(jCrumb, list[i], i==lastIndex?"tail":"");
            }
        }
        // 在主目录中，绘制剩下的路径
        else{
            var lastIndex = list.length-1;
            for(++i;i<list.length;i++){
                UI._append_crumb_item(jCrumb, list[i], i==lastIndex?"tail":"");
            }
        }
    },
    _append_crumb_item : function(jCrumb, o, itype){
        var UI  = this;
        var opt = UI.browser().options;
        var jq = UI.ccode("crumb.item");
        jq.attr("oid", o.id);

        // 主目录特殊显示
        if("home" == itype){
            jq.attr("onm", "~").attr("home", "yes");
            // 如果禁止主目录点击，那么就不是下拉箭头了
            if(opt.forbidClickHomeInCrumb) {
                jq.find(".ochild").html('<i class="fa fa-angle-right"></i>')
            }
            // 显示图标
            $(Wn.objIconHtml(o)).prependTo(jq);
            // 显示文字
            jq.find("b").text(UI.msg("home"));
        }
        // 其他项目
        else{
            jq.attr("onm", o.nm);
            $(Wn.objIconHtml(o)).attr("mime",o.mime).prependTo(jq);
            // 指定了标题
            if(o.title) {
                jq.find("b").text(o.title);
            }
            // 显示名称
            else {
                jq.find("b").text(Wn.objDisplayName(UI, o.nm));
            }
        }

        // 移除最后的展开符
        if(o.race!='DIR' || "tail"==itype){
            jq.addClass("citem-tail");
        }

        // 标记隐藏对象
        if(/^[.]/.test(o.nm)){
            jq.addClass("wnobj-hide");
        }

        return jq.appendTo(jCrumb);
    },
    //..............................................
    resize : function(){
        var UI = this;
        var jCrumb = UI.arena.find(".obrowser-crumb");
        // var jMe = UI.arena.find(".obrowser-me");
        // var jMenu  = UI.arena.find(".obrowser-menu:visible");
        // jCrumb.css({
        //     "width"  : UI.arena.width() -  jMe.outerWidth(true) -jMenu.outerWidth(true),
        //     "height" : UI.arena.height()
        // });
        jCrumb.folder({
            dmode : "tail",
            keep : 1
        });
    }
    //..............................................
});
//==================================================
});
})(window.NutzUtil);