(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/hm__methods_com',
    'ui/form/c_droplist',
], function(ZUI, Wn, HmComMethods, DroplistUI){
//==============================================
var PARAMS_DOM = {
    // 复杂的参数行
    "complex"  : function(jDiv, val){
        // base
        var base = "";
        if(val.base)
            base = $z.toJson(val.base);
        jDiv.find('[nm="base"] input').val(base);

        // from
        jDiv.find('[nm="from"] input').val(val.from || "");

        // key
        jDiv.find('[nm="key"] .ui-checkbox').attr("checked", val.key ? "yes" : null);
        jDiv.find("input").prop("disabled", val.key ? false : true);

        // merge
        jDiv.find('[nm="merge"] .ui-checkbox').attr("checked", val.merge ? "yes" : null);

    },
    // 简单的参数行
    "simple"  : function(jDiv, val){
        jDiv.find('input').val(val);
    },
};
//==============================================
var html = `
<div class="ui-code-template">
    <div code-id="complex" class="pv-complex">
        <div nm="base"><input></div>
        <div nm="from"><input></div>
        <div nm="key"><span class="ui-checkbox"></span><em>{{hmaker.dds.allkeys}}</em><input></div>
        <div nm="merge"><span class="ui-checkbox"></span><em>{{hmaker.dds.merge}}</em></div>
    </div>
    <div code-id="simple" class="pv-simple">
        <input>
    </div>
</div>
<div class="ui-arena hm-dynamic-data-setting">
    <section part="api">
        <dl>
            <dt>{{hmaker.dds.api}}</dt>
            <dd ui-gasket="api"></dd>
            <dd class="refresh">
                <i class="zmdi zmdi-refresh-sync"></i>
                <i class="zmdi zmdi-rotate-right zmdi-hc-spin"></i>
            </dd>
        </dl>
    </section>
    <section part="params"><table><tbody></tbody></table></section>
    <section part="template">
        <dl>
            <dt>{{hmaker.dds.template}}</dt>
            <dd ui-gasket="template"></dd>
            <dd class="refresh">
                <i class="zmdi zmdi-refresh-sync"></i>
                <i class="zmdi zmdi-rotate-right zmdi-hc-spin"></i>
            </dd>
        </dl>
    </section>
</div>`;
//==============================================
return ZUI.def("app.wn.hm_dynamic_data_setting", {
    dom  : html,
    //...............................................................
    init : function(){
        HmComMethods(this);
    },
    //...............................................................
    events : {
        // 刷新数据接口
        'click .refresh' : function(e) {
            var UI = this;
            var jq = $(e.currentTarget);
            var part = jq.closest("section").attr("part");
            jq.attr("loading", "yes");
            var uiDroplist = UI.gasket[part];

            var val = uiDroplist.getData();
            
            window.setTimeout(function(){
                uiDroplist.refresh(function(){
                    jq.removeAttr("loading");
                    uiDroplist.setData(val);
                });
            }, 800);
        }
    },
    //...............................................................
    redraw : function() {
        var UI = this;

        // 得到 API 的主目录
        var oApiHome = Wn.fetch("~/.regapi/api");

        // API
        new DroplistUI({
            parent : UI,
            gasketName : "api",
            items : "obj ~/.regapi/api/* -ExtendFilter -json -l",
            icon  : '<i class="fa fa-plug"></i>',
            text  : null,
            on_change : function(apiPath) {
                UI.fire("change:com", {api: apiPath});
                UI._reload_params();
            },
            value : function(o){
                return "/" + Wn.getRelativePath(oApiHome, o);
            }
        }).render(function(){
            UI.defer_report("api");
        });

        // Template
        new DroplistUI({
            parent : UI,
            gasketName : "template",
            items : "obj ~/.hmaker/template/* -json -l",
            icon  : '<i class="fa fa-puzzle-piece"></i>',
            text  : null,
            value : function(o){
                return o.nm;
            }
        }).render(function(){
            UI.defer_report("template");
        });


        // 返回延迟加载
        return ["api", "template"];
    },
    //...............................................................
    _reload_params : function(com) {
        var UI = this;
        com = com || UI.uiCom.getPropFromDom();

        // 如果没有声明 params
        com.params = com.params || {
            id  : null,
            cnd   : {base:{},     from:"filter", key:null   , merge : true },
            limit : {base:50,     from:"pager" , key:"limit", merge : false},
            skip  : {base:0 ,     from:"pager" , key:"skip" , merge : false},
            sort  : {base:{nm:1}, from:"sorter", key:null   , merge : false},
        };

        // 开始绘制
        var jTable = UI.arena.find('[part="params"] table tbody').empty();
        for(var key in com.params) {
            var val = com.params[key];

            // 准备绘制行
            var jTr = $('<tr><td class="pkey"></td><td class="pval"></td></tr>').appendTo(jTable);
            jTr.children(".pkey").text(key);

            var jTd = jTr.children(".pval");
            var valMode = val && _.isObject(val) ? "complex" : "simple";
            var jDiv = UI.ccode(valMode).appendTo(jTd);
            PARAMS_DOM[valMode](jDiv, val);
        }
    },
    //...............................................................
    update : function(com) {
        var UI = this;
        
        console.log("I am update")

        // 更新 API
        UI.gasket.api.setData(com.api);

        // 更新参数表
        UI._reload_params(com);

        // 最后在调用一遍 resize
        UI.resize(true);
    },
    //...............................................................
    resize : function() {
        var UI = this;

    }
});
//===================================================================
});
})(window.NutzUtil);