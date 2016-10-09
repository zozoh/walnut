(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/search/search',
    'ui/form/form',
    'ui/support/dom'
], function(ZUI, Wn, SearchUI, FormUI, DomUI){
//==============================================
var html = function(){/*
<div class="ui-code-template">
    <div code-id="err.nothingjs" class="th-err">
        <i class="fa fa-warning"></i> <span>{{thing.err.nothingjs}}</span>
    </div>
    <div code-id="tip.blank">
        <div class="th-blank">
            <i class="fa fa-hand-o-left"></i> <span>{{thing.blank}}</span>
        </div>
    </div>
</div>
<div class="ui-arena thing" ui-fitparent="yes">
    <header>heading</header>
    <section class="th-search"><div class="th-con" ui-gasket="search"></div></section>
    <section class="th-form"><div class="th-con" ui-gasket="form"></div></section>
</div>
*/};
//==============================================
return ZUI.def("app.wn.thing", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "theme/app/wn.thing/thing.css",
    i18n : "app/wn.thing/i18n/{{lang}}.js",
    //...............................................................
    update : function(o) {
        var UI = this;
        UI.$el.attr("thing-set-id", o.id);

        // 更新标题
        var html = Wn.objIconHtml(o) + '<b>' + (o.title || o.nm) + '</b>';
        html += '<em>' + o.id +'</em>';
        UI.arena.children("header").html(html);

        // 重新加载数据 
        UI.reload(function(){
            UI.subUI("search/list").setActived(0);
        });
    },
    //...............................................................
    reload : function(callback){
        var UI  = this;
        var oid = UI.$el.attr("thing-set-id");

        // 读取所有的字段
        UI.showLoading();
        $z.loadResource("jso:///o/read/id:"+oid+"/thing.js", function(thConf){
            UI.hideLoading();
            // 加载 thing.js 失败，显示错误信息
            if(!thConf || (false === thConf.ok)){
                UI.ccode("err.nothingjs").appendTo(UI.arena.empty());
            }
            else if(thConf) {
                UI.__draw(thConf, callback);
            }
        });

    },
    //...............................................................
    showBlank : function(){
        new DomUI({
            parent : this,
            gasketName : "form",
            dom : this.ccode("tip.blank").html()
        }).render();
    },
    //...............................................................
    showThing : function(th) {
        var UI = this;
        var thConf = UI.thConf;

        th = th || UI.gasket.search.uiList.getActived();

        // 没东西，那么久显示空
        if(!th) {
            UI.showBlank();
            return;
        }
        // 已经是表单了，更新
        else if(UI.gasket.form.uiName == "ui.form") {
            UI.gasket.form.setData(th);
        }
        // 重新显示表单
        else {
            new FormUI({
                parent : this,
                gasketName : "form",
                uiWidth : "all",
                fields : UI.thConf.fields,
                on_update : function(data, fld) {
                    var th      = this.getData();
                    var oTS     = UI.getThingSetObj();
                    var json    = $z.toJson(data);
                    var cmdTmpl = UI.__cmd(thConf.updateBy, oTS.id, json);
                    var cmdText = $z.tmpl(cmdTmpl)(th);
                    console.log(cmdText)
                    // 执行命令
                    var uiForm = this;
                    this.showPrompt(fld.key, "spinning");
                    Wn.exec(cmdText, function(re) {
                        var newTh = $z.fromJson(re);
                        // 计入缓存
                        Wn.saveToCache(newTh);
                        // 更新左侧列表
                        UI.gasket.search.uiList.update(newTh);
                        // 隐藏提示
                        uiForm.hidePrompt(fld.key);
                    });
                    // var th = this.getData();
                    // UI.gasket.search.uiList.update(th);
                    // this.showPrompt(key, "spinning");
                    // window.setTimeout(function(){
                    //     UI.gasket.form.hidePrompt(key);
                    // }, 500);
                }
            }).render(function(){
                this.setData(th);
            });
        }
    },
    //...............................................................
    getThingSetObj : function(){
        var UI  = this;
        var oid = UI.$el.attr("thing-set-id");
        return Wn.getById(oid);
    },
    //...............................................................
    __cmd : function(cmd, TsId, str) {
        var re = cmd.replace(/<TsId>/g, TsId);
        if(str)
            return re.replace(/<str>/g, str.replace(/'/g, "\\'").replace(/"/g, "\\\""));
        return re;
    },
    //...............................................................
    __draw : function(thConf, callback) {
        var UI  = this;
        var oTS = UI.getThingSetObj();
        //console.log(thConf)
        // 保存 thConf (thing.js) 定义
        UI.thConf = thConf;

        // 定义默认命令模板
        $z.setUndefined(thConf, "updateBy", "thing <TsId> update {{id}} -fields '<str>'");
        $z.setUndefined(thConf, "queryBy" , "thing <TsId> query '<%=match%>' -skip {{skip}} -limit {{limit}} -json -pager -sort 'ct:-1'");
        $z.setUndefined(thConf, "createBy", "thing <TsId> create '<str>'");
        $z.setUndefined(thConf, "deleteBy", "thing <TsId> delete {{id}} -quiet");

        // 创建搜索条
        new SearchUI({
            parent : UI,
            gasketName : "search",
            menu : [{
                qkey  : "refresh",
                icon  : '<i class="zmdi zmdi-refresh"></i>',
            }, {
                text  : "i18n:thing.create",
                icon  : '<i class="zmdi zmdi-flare"></i>',
                handler : function() {
                    var objName = window.prompt(UI.msg("thing.create_tip"));
                    if(objName){
                        var cmdText = UI.__cmd(thConf.createBy, oTS.id, objName);
                        Wn.exec(cmdText, function(re){
                            var newObj = $z.fromJson(re);
                            UI.gasket.search.refresh(function(){
                                UI.subUI("search/list").setActived(newObj.id);
                            });
                        });
                    }
                }
            }, {
                qkey  : "delete",
                icon  : '<i class="zmdi zmdi-delete"></i>',
            }],
            data : UI.__cmd(thConf.queryBy, oTS.id),
            edtCmdTmpl : {
                "delete" : UI.__cmd(thConf.deleteBy, oTS.id),
            },
            list : {
                fields : thConf.fields,
                on_actived : function(th,jRow,prevTh) {
                    if(!prevTh || prevTh.id != th.id)
                        UI.showThing(th);
                },
                on_blur : function(objs, jRows, nextObj) {
                    if(!nextObj)
                        UI.showBlank();
                }
            },
            maskConf : {
                width  : 500,
                height : "90%"
            },
        }).render(function(){
            this.refresh(function(){
                $z.doCallback(callback, [oTS], UI);
            });
        });

        // 右侧显示空
        UI.showBlank();
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);