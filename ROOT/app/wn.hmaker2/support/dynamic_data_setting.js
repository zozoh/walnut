(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/support/hm__methods_panel',
    'ui/form/form',
], function(ZUI, Wn, HmMethods, FormUI){
//==============================================
var html = `
<div class="ui-arena hm-dynamic-data-setting">
    <section ui-gasket="form"></section>
</div>`;
//==============================================
return ZUI.def("app.wn.hm_dynamic_data_setting", {
    dom  : html,
    //...............................................................
    init : function(opt){
        HmMethods(this);
    },
    //...............................................................
    events : {
        
    },
    //...............................................................
    redraw : function() {
        var UI  = this;
        var opt = UI.options;

        // 得到 API 的主目录
        var oApiHome = Wn.fetch("~/.regapi/api");

        // 创建表单
        new FormUI({
            parent     : UI,
            gasketName : "form",
            fitparent  : false,
            mergeData  : false,
            uiWidth    : "all",
            on_change  : function(key, val) {
                UI.__do_on_change(key, val);
            },
            fields : [{
                key    : "api",
                title  : "i18n:hmaker.dds.api",
                type   : "string",
                editAs : "droplist",
                uiConf : {
                    emptyItem : {},
                    items : "obj ~/.regapi/api/* -ExtendFilter -json -l",
                    icon  : '<i class="fa fa-plug"></i>',
                    text  : null,
                    value : function(o){
                        return "/" + Wn.getRelativePath(oApiHome, o);
                    },
                }
            }, {
                key    : "params",
                title  : "i18n:hmaker.dds.params",
                type   : "object",
                dft    : {},
                editAs : "pair",
            }, {
                key    : "template",
                title  : "i18n:hmaker.dds.template",
                type   : "string",
                editAs : "droplist",
                uiConf : {
                    emptyItem : {},
                    icon  : '<i class="fa fa-puzzle-piece"></i>',
                    items : function(params, callback){
                        // 得到站点皮肤
                        var skinInfo = UI.getSkinInfo();
                        var siTempl  = (skinInfo||{}).template || {}
                        // 得到模板列表
                        Wn.exec("obj ~/.hmaker/template/* -json -l", function(re){
                            var list  = $z.fromJson(re);
                            var items = [];
                            for(var o of list) {
                                var suffix = siTempl[o.nm] ? '' : ' !(' + UI.msg("hmaker.dds.tmpl_noskin") + ')';
                                items.push({
                                    text  : o.nm + suffix,
                                    value : o.nm
                                });
                            }
                            callback(items);
                        });
                    },
                }
            }, {
                key    : "options",
                title  : "i18n:hmaker.dds.options",
                type   : "object",
                dft    : {},
                editAs : "pair",
            }]
        }).render(function(){
            UI.defer_report("form");
        });

        // 返回延迟加载
        return ["form"];
    },
    //...............................................................
    __do_on_change : function(key, val){
        var UI  = this;
        var opt = UI.options;

        // 更新了 API -> 更新参数表
        if("api" == key) {
            var params = UI._eval_api_params(val);
            //console.log(params)
            UI.gasket.form.update("params", params);
        }
        // 更新了模板 -> 模板映射
        else if("template" == key) {
            var options = UI._eval_template_options(val);
            UI.gasket.form.update("options", options);
        }

        // 最后调用回调
        var com = UI.getData();
        //console.log("__do_on_change:", com);
        $z.invoke(opt, "on_change", [com], UI);
    },
    //...............................................................
    _eval_api_params : function(apiPath) {
        var UI = this;
        var re = {};
        if(apiPath) {
            // 得到 api 对象
            var oApi = Wn.fetch("~/.regapi/api" + apiPath);
            re = oApi.params || {};

            // 如果 API 定义了自己的参数表
            for(var key in re) {
                var val = UI.__params[key];
                if(!_.isUndefined(val))
                    re[key] = val;
            }
        }
        // 返回
        return re;
    },
    //...............................................................
    _eval_template_options : function(templateName) {
        var UI = this;
        var re = {}; 
        if(templateName) {
            // 加载模板
            var tmplInfo = UI.evalTemplate(templateName);

            // console.log(tmplInfo)

            // 更新模板映射的值
            for(var key in tmplInfo.options) {
                re[key] = UI.__options[key] || tmplInfo.options[key] || "";
            }
        }
        // 返回
        return re;
    },
    //...............................................................
    update : function(com) {
        var UI = this;

        // 记录原始的映射数据
        UI.__params  = com.params  || {};
        UI.__options = com.options || {};

        // 更新映射表
        com.params  = UI._eval_api_params(com.api);
        com.options = UI._eval_template_options(com.template);

        // 更新表单
        this.gasket.form.setData(com);
    },
    //...............................................................
    getData : function() {
        return this.gasket.form.getData();
    },
    //...............................................................
    setData : function(com) {
        this.update(com);
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);