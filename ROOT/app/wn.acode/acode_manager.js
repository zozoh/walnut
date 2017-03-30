(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/search/search',
    'ui/form/form',
    'ui/support/dom',
], function(ZUI, Wn, SearchUI, FormUI, DomUI){
//==============================================
var html = `
<div class="ui-arena acode-manager" ui-fitparent="yes">
    <div class="am-search" ui-gasket="search"></div>
    <div class="am-form"   ui-gasket="form"></div>
</div>`;
//==============================================
return ZUI.def("app.wn.acode_manager", {
    dom  : html,
    css  : "app/wn.acode/theme/acode_manager-{{theme}}.css",
    i18n : "app/wn.acode/i18n/{{lang}}.js",
    //...............................................................
    init : function(){
        var UI = this;

        UI.my_fields = [{
            hide : true,
            key : "id",
            title : "i18n:acode.ID",
            type : "string",
            editAs : "label",
        }, {
            hide : true,
            key : "buyer_id",
            title : "i18n:acode.buyer_id",
            type : "string",
            uiWidth : "all",
            editAs : "input",
        }, {
            key : "buyer_nm",
            title : "i18n:acode.buyer_nm",
            type : "string",
            uiWidth : "all",
            editAs : "input",
        }, {
            key : "ac_licence",
            title : "i18n:acode.ac_licence",
            type : "string",
            editAs : "droplist",
            uiConf : {
                items : function(params, callback){
                    Wn.fetchBy("obj ~/.licence -match '{tp:\"licence\"}' -sort {ct:1} -l -json", callback);
                },
                value : function(o){
                    return $z.getMajorName(o.nm);
                },
                text  : function(o, index, UI){
                    return $z.getMajorName(o.nm);
                },
                icon  : '<i class="fa fa-credit-card"></i>',
            }
        }, {
            key : "ac_day",
            title : "i18n:acode.ac_day",
            type : "int",
            dft : 30,
            editAs : "input",
        }, {
            key : "ac_expi",
            title : "i18n:acode.ac_expi",
            type : "datetime",
            editAs : "label",
        }, {
            hide : true,
            key : "ct",
            title : "i18n:acode.ct",
            type : "datetime",
            editAs : "label",
        }, {
            hide : true,
            key : "use_time",
            title : "i18n:acode.use_time",
            type : "datetime",
            editAs : "label",
        }, {
            hide : true,
            key : "ow_dmn_id",
            title : "i18n:acode.ow_dmn_id",
            type : "string",
            editAs : "label",
        }, {
            hide : true,
            key : "ow_dmn_nm",
            title : "i18n:acode.ow_dmn_nm",
            type : "string",
            editAs : "label",
        }];
    },
    //...............................................................
    redraw : function(){
        var UI = this;

        // 创建列表UI
        new SearchUI({
            parent : UI,
            gasketName : "search",
            menu : [{
                icon : '<i class="zmdi zmdi-flare"></i>',
                text : "i18n:acode.create",
                handler : function(){
                    UI.do_gen_acode();
                }
            }, "delete", "refresh"],
            edtCmdTmpl : {
                "delete"  : "rm -rf id:{{id}}",
            },
            data : "obj id:{{acodeHomeId}} -match '<%=match%>' -skip {{skip}} -limit {{limit}} -l -json -pager -sort 'ct:-1'",
            queryContext   : function(){
                return {
                    acodeHomeId : UI.__acode_home.id
                };
            },
            maskConf : {
                width  : 640,
                height : 600
            },
            formConf : {
                
            },
            list : {
                activable  : true,
                checkable  : true,
                fields     : UI.my_fields,
                on_actived : UI.on_active_acode,
                on_blur    : UI.empty_form,
                context    : UI
            },
            pager : {
                dft : {
                    pn   : 1,
                    pgsz : 100
                }
            }
        }).render(function(){
            UI.defer_report("search");
        });

        // 返回延迟加载
        return ["search"];
    },
    //...............................................................
    on_active_acode : function(o){
        var UI = this;

        // 仅更新
        if(UI.gasket.form && UI.gasket.form.uiName == "ui.form") {
            UI.gasket.form.setData(o);
        }
        // 重新加载
        else{
            new FormUI({
                parent : UI,
                gasketName : "form",
                title      : "i18n:acode.form_title",
                fields     : UI.my_fields,
                on_change  : UI.do_change_acode,
                context    : UI
            }).render(function(){
                this.setData(o);
            });
        }
    },
    //...............................................................
    empty_form : function(objs, jRows, nextObj, nextRow){
        var UI = this;
        if(!nextObj){
            new DomUI({
                parent : UI,
                gasketName : "form",
                dom : `<div class="am-empty">
                    <i class="fa fa-hand-o-left"></i> <b>{{acode.empty}}</b>
                </div>`
            }).render();
        }
    },
    //...............................................................
    do_gen_acode : function(){
        var UI = this;

        // 得到域
        var oDomain = Wn.fetch("/home/" + UI.__acode_home.d1);

        // 生成一个新对象
        var obj = {
            nm : "${id}.acode",
            buyer_id  : "-unknown-",
            buyer_nm  : "-unknown-",
            ow_dmn_id : oDomain.id,
            ow_dmn_nm : oDomain.nm,
        };

        // 准备命令上下文
        var cc = {
            acodeHomeId : UI.__acode_home.id,
            json        : $z.toJson(obj),
        };

        // 准备命令模板
        Wn.execf("obj id:{{acodeHomeId}} -new '<%=json%>' -o", cc, function(re){
            if(/^e./.test(re)) {
                alert(re);
            }
            else{
                var o = $z.fromJson(re);
                // 更新列表
                UI.subUI("search/list").add(o, 0, 1).setActived(0);
            }
        });
    },
    //...............................................................
    do_change_acode : function(key, val) {
        var UI  = this;
        var obj = UI.gasket.form.getData();

        // 客户信息
        if(/^buyer/.test(key)){
            var json;

            // 如果是客户 ID，自动更新名称
            if("buyer_id" == key) {
                json = Wn.exec("usrinfo id:"+val);
            }
            // 如果是客户名称，自动更新 ID
            else if("buyer_nm" == key) {
                json = Wn.exec("usrinfo "+val);
            }

            // 错误
            if(/^e./.test(json)) {
                UI.gasket.form.showPrompt(key, "warn");
            }
            // 执行更新
            else {
                UI.gasket.form.showPrompt("buyer_id", "spinning");
                UI.gasket.form.showPrompt("buyer_nm", "spinning");
                var usr = $z.fromJson(json);
                //console.log(usr)
                Wn.execf('obj id:'+obj.id+' -u \'buyer_id:"{{id}}", buyer_nm:"{{name}}"\' -o', usr, function(re){
                    if(/^e./.test(re)) {
                        UI.gasket.form.showPrompt(key, "warn");
                    }
                    else{
                        var o = $z.fromJson(re);
                        // 隐藏加载
                        UI.gasket.form.hidePrompt("buyer_id", "buyer_nm");
                        // 更新列表
                        UI.subUI("search/list").update(o);
                    }
                });
            }
        }
        // 其他的，就更新
        else {
            var str = val || null;
            if(_.isString(str))
                str = '"' + str.replace(/["'<>&*+#]/g, "") + '"';
            var cmdText = 'obj id:'+obj.id+" -u '" + key + ':' + str + "' -o";
            // 显示加载
            UI.gasket.form.showPrompt(key, "spinning");
            // 执行命令
            Wn.exec(cmdText, function(re){
                var o = $z.fromJson(re);
                // 隐藏加载
                UI.gasket.form.hidePrompt(key);
                // 更新列表
                UI.subUI("search/list").update(o);
            });
        }
    },
    //...............................................................
    // 这个木啥用了，就是一个空函数，以便 browser 来调用
    update : function(o) {
        var UI = this;
        // UI.arena.find(".pvg-users-menu .menu-item").first().click();
        UI.__acode_home = o;
        UI.gasket.search.refresh(function(acodes){
            if(acodes && acodes.length > 0)
                this.gasket.list.setActived(0);
            else
                UI.empty_form();
        });
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);