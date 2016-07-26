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
        UI.reload();
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
        th = th || UI.gasket.search.uiList.getActived();
        // 没东西，那么久显示空
        if(!th) {
            UI.showBlank();
            return;
        }
        // 显示表单
        new FormUI({
            parent : this,
            gasketName : "form",
            uiWidth : "all",
            fields : UI.thConf.fields,
            on_change : function(key, val) {
                var data  = {};
                data[key] = val;
                var json  = $z.toJson(data);
                var cmdText = "thing " + th.id + " update -fields '"+json+"'";
                console.log(cmdText)
                // 执行命令
                var uiForm = this;
                this.showPrompt(key, "spinning");
                Wn.exec(cmdText, function(re) {
                    var newTh = $z.fromJson(re);
                    // 计入缓存
                    Wn.saveToCache(newTh);
                    // 更新左侧列表
                    UI.gasket.search.uiList.update(newTh);
                    // 隐藏提示
                    uiForm.hidePrompt(key);
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
    },
    //...............................................................
    getThingSetObj : function(){
        var UI  = this;
        var oid = UI.$el.attr("thing-set-id");
        return Wn.getById(oid);
    },
    //...............................................................
    __draw : function(thConf, callback) {
        var UI  = this;
        var oTS = UI.getThingSetObj();
        //console.log(thConf)
        // 保存 thConf (thing.js) 定义
        UI.thConf = thConf;

        // 创建搜索条
        new SearchUI({
            parent : UI,
            gasketName : "search",
            menu : [{
                qkey  : "refresh",
                icon  : '<i class="zmdi zmdi-refresh"></i>',
            }, {
                qkey  : "create",
                icon  : '<i class="zmdi zmdi-flare"></i>',
            }, {
                qkey  : "delete",
                icon  : '<i class="zmdi zmdi-delete"></i>',
            }],
            data : "thing "+oTS.id+" query '<%=match%>' -skip {{skip}} -limit {{limit}} -json -pager -sort 'lm:1'",
            edtCmdTmpl : {
                "create" : "thing "+oTS.id+" create -fields '<%=json%>'",
                "delete" : "thing {{id}} delete -quiet",
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