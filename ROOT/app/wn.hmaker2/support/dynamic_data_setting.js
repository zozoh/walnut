(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/hm__methods',
    'ui/form/c_droplist',
], function(ZUI, Wn, HmMethods, DroplistUI){
//==============================================
var _PARAMS = {
    get : function(jDiv){
        var re = {};

        // base
        re.base = $z.fromJson(jDiv.find('[nm="base"] input').val());

        // from
        re.from = $.trim(jDiv.find('[nm="from"] input').val()) || null;

        // key
        var jKey = jDiv.find('[nm="key"]');
        var jKeyInput = jKey.find("input");
        if(jKey.attr("for-all")) {
            re.key = null;
        }
        // ! nokey
        else {
            re.key = $.trim(jKeyInput.val());
            jKeyInput.attr("old-val", re.key);
        }

        // merge
        re.merge = jDiv.find('[nm="merge"] .ui-checkbox').attr("checked") ? true : false;

        // 返回
        return re;
    },
    set : function(jDiv, val){
        val = val || {};
        // base
        var base = "";
        if(!_.isUndefined(val.base) && !_.isNull(val.base))
            base = $z.toJson(val.base);
        jDiv.find('[nm="base"] input').val(base);

        // from
        jDiv.find('[nm="from"] input').val(val.from || "");

        // key
        var jKey = jDiv.find('[nm="key"]');
        // ! nokey
        if(_.isNull(val.key) || _.isUndefined(val.key)) {
            jKey.attr("for-all", "yes")
                .find('.ui-checkbox').removeAttr("checked");
            jKey.find("input").prop("disabled", true).val("");
        }
        // has key
        else {
            jKey.removeAttr("for-all")
                .find('.ui-checkbox').attr("checked", "yes");
            jKey.find("input").prop("disabled", false).val(val.key);
        }

        // merge
        jDiv.find('[nm="merge"] .ui-checkbox').attr("checked", val.merge ? "yes" : null);

    }
};
//==============================================
var html = `
<div class="ui-code-template">
    <div code-id="paramItem" class="param-item">
        <div nm="base"><em>{{hmaker.dds.param_base}}</em><input></div>
        <div class="dynamic">
            <div nm="from">
                <em>{{hmaker.dds.param_from}}</em>
                <input list="param_item_from">
            </div>
            <div nm="key"><span class="ui-checkbox"></span><em>{{hmaker.dds.param_key}}</em><input></div>
            <div nm="merge"><span class="ui-checkbox"></span><em>{{hmaker.dds.param_merge}}</em></div>
        </div>
    </div>
</div>
<div class="ui-arena hm-dynamic-data-setting">
    <datalist id="param_item_from">
        <option value="filter">
        <option value="pager">
        <option value="sorter">
        <option value="HTTP_GET">
        <option value="HTTP_COOKIE">
    </datalist>
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
        HmMethods(this);
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

                    // 调用回调
                    UI.__do_on_change();
                });
            }, 800);
        },
        // 复杂参数:开启/关闭键的输入
        'click .pval .ui-checkbox' : function(e) {
            var UI = this;
            var jq = $(e.currentTarget);
            var jDiv = jq.closest(".param-item");
            var val  = _PARAMS.get(jDiv);

            console.log("clicked")
            
            // toggle 键
            var jKey = jq.closest('[nm="key"]');
            if(jKey.length > 0) {
                val.key = _.isString(val.key) ? null : (jKey.find("input").attr("old-val") || "");
            }
            // toggle marget
            else if(jq.closest('[nm="merge"]').length > 0) {
                val.merge = !val.merge;
            }

            _PARAMS.set(jDiv, val);

            // 调用回调
            UI.__do_on_change();
        },
        // 复杂参数:开启/关闭合并选项

    },
    //...............................................................
    __do_on_change : function(){
        var UI  = this;
        var opt = UI.options;

        var com = UI.uiCom.getData();
        console.log("__do_on_change:", com)
        $z.invoke(opt, "on_change", [com], UI);
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
                var com = UI._eval_params_by_api({api:apiPath});
                UI.fire("change:com", com);
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
            on_change : function(template) {
                UI.fire("change:com", {template:template});
            },
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
    _eval_params_by_api : function(com) {
        if(com.api) {
            // TODO 这里根据 api 的 params 元数据生成默认参数表
            com.params = {
                id    : null,
                cnd   : {base:{},     from:"filter", key:null   , merge : true },
                limit : {base:50,     from:"pager" , key:"limit", merge : false},
                skip  : {base:0 ,     from:"pager" , key:"skip" , merge : false},
                sort  : {base:{nm:1}, from:"sorter", key:null   , merge : false},
            };
        }
        return com;
    },
    //...............................................................
    _reload_params : function(com) {
        var UI = this;

        // 看看是否指定了参数表
        var paramNames = Object.keys(com.params || {});

        // 开始绘制
        var jTable = UI.arena.find('[part="params"] table tbody').empty();
        for(var key of paramNames) {
            var val = com.params[key];

            // 准备绘制行
            var jTr = $('<tr><td class="pkey"></td><td class="pval"></td></tr>').appendTo(jTable);
            jTr.children(".pkey").text(key);

            var jTd = jTr.children(".pval");
            var jDiv = UI.ccode("paramItem").appendTo(jTd);
            _PARAMS.set(jDiv, val);
        }
    },
    //...............................................................
    update : function(com) {
        var UI = this;

        // 更新 API
        if(com.api)
            UI.gasket.api.setData(com.api);

        // 更新参数表
        if(com.params)
            UI._reload_params(com);

        // 更新模板
        if(com.template)
            UI.gasket.template.setData(com.template);
    },
    //...............................................................
    getData : function() {
        var UI = this;
        var com = {
            api      : UI.gasket.api.getData(),
            params   : {},
            template : UI.gasket.template.getData
        };

        UI.arena.find('section[part="params"] tr').each(function(){
            var jTr  = $(this);
            var jDiv = jTr.find(".pval");
            var key  = jTr.find(".pkey").text();
            com.params[key] = _PARAMS.get(jDiv);
        });

        return com;
    },
    //...............................................................
    setData : function(com) {
        var UI = this;

        // 更新 API
        UI.gasket.api.setData(com.api);

        // 更新参数表
        UI._reload_params(com);

        // 更新模板
        UI.gasket.template.setData(com.template);
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);