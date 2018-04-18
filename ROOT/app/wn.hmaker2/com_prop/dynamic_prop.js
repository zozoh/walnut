(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/support/hm__methods_panel',
    'ui/form/form',
    'ui/form/c_droplist',
    '/gu/rs/ext/hmaker/hm_runtime.js'
], function(ZUI, Wn, HmMethods, FormUI, DroplistUI){
//==============================================
var html = `
<div class="ui-arena hmc-dynamic-prop" ui-fitparent="yes">
    <h4 l-key="hide_part_api_info">{{hmaker.com.dynamic.api}}</h4>
    <header  class="api" ui-gasket="api"></header>
    <aside class="api-info">
        <span>{{hmaker.com.dynamic.params}}</span>
        <b></b>
    </aside>
    <section class="api-params" ui-gasket="params"></section>
    <h4 l-key="hide_part_tmpl_opt">{{hmaker.com.dynamic.template}}</h4>
    <!--header  class="tmpl" ui-gasket="template"></header-->
    <section class="tmpl-options" ui-gasket="options"></section>
</div>`;
//==============================================
return ZUI.def("app.wn.hm_com_dynamic_prop", {
    dom  : html,
    //...............................................................
    init : function(){
        HmMethods(this);
    },
    //...............................................................
    events : {
        "click .hmc-dynamic-prop > h4" : function(e){
            this.showHidePart($(e.currentTarget));
        }
    },
    //...............................................................
    showHidePart : function(jH4, asHidden) {
        if(!_.isBoolean(asHidden)) {
            asHidden = jH4.attr("hide-part") ? false : true;
        }
        // 记录本地状态
        this.local(jH4.attr("l-key"), asHidden);
        // 切换显示模式
        var jPart = jH4.nextUntil("h4[l-key]");
        jH4.attr("hide-part", asHidden ? "yes" : null);
        jPart.attr("hide-me", jH4.attr("hide-part") || null);
        this.resize(true);
    },
    //...............................................................
    redraw : function() {
        var UI = this;

        // 初始化两个部分的本地状体
        var jH4Api = UI.arena.find('> h4[l-key="hide_part_api_info"]');
        var jH4Opt = UI.arena.find('> h4[l-key="hide_part_tmpl_opt"]');
        UI.showHidePart(jH4Api, UI.local(jH4Api.attr("l-key"))? true : false);
        UI.showHidePart(jH4Opt, UI.local(jH4Opt.attr("l-key"))? true : false);

        // 得到 API 的主目录
        var oApiHome = Wn.fetch("~/.regapi/api");

        // 数据接口列表
        new DroplistUI({
            parent : UI,
            gasketName : "api",
            emptyItem : {},
            items : [],
            itemData : function(o) {
                var ph = "/" + Wn.getRelativePath(oApiHome, o);
                return {
                    icon  : '<i class="fa fa-plug"></i>',
                    text  : o.title,
                    value : ph,
                    tip   : ph,
                };
            },
            on_change : function(v){
                UI.uiCom.saveData(null, {api:v, params:{}}, true);
            }
        }).render(function(){
            UI.defer_report("api");
        });
        
        // 返回延迟加载
        return ["api"];
    },
    //...............................................................
    isAsyncUpdate : function(){
        return true;
    },
    //...............................................................
    update : function(com, callback) {
        var UI = this;
        var jApiInfo = UI.arena.find(">.api-info");
        var jParams  = UI.arena.find(">.api-params");
        var jOptions = UI.arena.find(">.tmpl-options");

        //console.log("prop update", UI.uiCom.getComSkin(), com)

        // 如果发现属性有不正确的，会标记这个变量，以便通知组件重绘
        var raw_com_json = $z.toJson(com);
        
        //-----------------------------------------------------
        // 根据皮肤得到显示模板
        com.template = UI.uiCom.getMyTemplateName();

        // // 更新 template
        // UI.gasket.template.setData(com.template);

        // // 重新读取一下模板，这样，不在列表里的模板设置将会被设置成空值
        // com.template = UI.gasket.template.getData();

        // 试图寻找 template 对应的信息
        var tmplInfo = com.template ? UI.evalTemplate(com.template, false)
                                    : null;

        // 如果没有将 template 置空
        //  || !HmRT.isMatchDataType(oApi.api_return, tmplInfo.dataType)
        if(!tmplInfo) {
            com.template = "";
            jOptions.hide();
        }
        // 更新 options 的 form
        // 并设置 data
        else {
            jOptions.show();
            var fields = UI.__eval_form_fields_by(tmplInfo.options);
            //console.log(fields)
            UI.__draw_form({
                cacheKey : "_finger_tmpl_options",
                finger   : $z.toJson(tmplInfo.options),
                gasketName : "options",
                $pel   : jOptions,
                fields : fields
            }, com.options);
        }

        //-----------------------------------------------------
        // 根据皮肤更新一下 API 列表
        var oApiList = UI.getHttpApiList(function(oApi){
            //console.log(oApi.ph)
            if(!tmplInfo || !oApi)
                return false;
            return HmRT.isMatchDataType(oApi.api_return, tmplInfo.dataType);
        });
        UI.gasket.api.setItems(oApiList);

        //-----------------------------------------------------
        // 首先确保 api 在列表中
        UI.gasket.api.setData(com.api);
        com.api = UI.gasket.api.getData();

        // 得到 api 对象
        var oApi = com.api ? Wn.fetch("~/.regapi/api" + com.api)
                           : null;

        // 根据 API 里面的设定设置 params
        if(oApi) {
            oApi.params = oApi.params || {};
        }
        // 如果没找到，将 api 置空
        else {
            com.api = "";
        }

        // 更新 api
        UI.gasket.api.setData(com.api);

        // 显示 API
        var jB = jApiInfo.children("b");
        if(oApi) {
            jApiInfo.removeAttr("is-empty");
            jParams.removeAttr("is-empty");
            // 更新 api info 部分
            //jApiInfo.show();
            jB.empty();
            $('<u>').text(oApi.api_method  || "GET").appendTo(jB);
            $('<em>').text(oApi.api_return || "obj").appendTo(jB);

            // 更新 params 的 form
            // 并设置 data
            //jParams.show();
            var fields = UI.__eval_form_fields_by(oApi.params);
            UI.__draw_form({
                cacheKey : "_finger_api_form",
                finger   : $z.toJson(oApi.params),
                gasketName : "params",
                $pel   : jParams,
                fields : fields
            }, com.params);

        }
        // 没有参数，清空显示
        else {
            jApiInfo.attr("is-empty", "yes");
            jParams.attr("is-empty", "yes");
        }

        //-----------------------------------------------------
        // 看看是否需要通知控件重绘
        if(raw_com_json != $z.toJson(com)) {
            UI.uiCom.saveData("panel", com, true);
        }

        // 最后调用回调
        $z.doCallback(callback, []);
    },
    /*...............................................................
    setting : {  // 表单配置信息
        cacheKey : "_finger_api_form",
        finger   : JSON String 
        $pel     : 插入点的 DOM (jQuery),
        fields   : [..]
    }
    data : 要绘制的表单数据
    */
    __draw_form : function(setting, data) {
        var UI = this;

        // 得到之前 form 的 ID
        var uiid   = setting.$pel.children().attr("ui-id");
        var uiForm = uiid ? ZUI(uiid) : null;

        // 如果没有字段，显示空
        if(setting.fields.length == 0) {
            // 销毁表单控件
            if(uiForm)
                uiForm.destroy();

            // 清空缓存指纹
            UI[setting.cacheKey] = null;

            // 显示无参数
            setting.$pel.attr("no-setting","yes")
                .html(UI.msg("hmaker.com._.no_setting"));
            return;
        }

        // 无需创建 form
        if(uiForm && UI[setting.cacheKey] == setting.finger) {
            uiForm.setData(data);
        }
        // 创建表单并赋值
        else {
            if(!uiForm)
                setting.$pel.removeAttr("no-setting").empty();
            new FormUI({
                //parent     : UI,
                //gasketName : setting.gasketName,
                $pel       : setting.$pel,
                mergeData  : false,
                fitparent  : false,
                uiWidth :"all",
                fields     : setting.fields,
                on_update  : function(){
                    var data = this.getData();
                    UI.uiCom.saveData("panel", $z.obj(setting.gasketName, data), true);
                }
            }).render(function(){
                this.setData(data);
            });
            // 更新缓存指纹
            UI[setting.cacheKey] = setting.finger;
        }
        
    },
    //...............................................................
    // 根据一个 JSON 对象，来生成一个 form 控件的字段配置信息
    // 具体支持什么格式的参数，文档上有描述 hmc_dynamic.md
    __eval_form_fields_by : function(setting) {
        var UI = this;
        var re = [];

        // 解析参数
        var flds = HmRT.parseSetting(setting || {});

        // 循环输出表单字段配置信息
        for(var i=0; i<flds.length; i++) {
            var F = flds[i];
            //console.log(F)
            // 准备字段
            var fld = {
                key      : F.key,
                title    : F.title || F.key,
                tip      : F.tip,
                dft      : F.dft,
                required : F.required,
                uiWidth  : F.uiWidth,
            };

            // 默认增加 key 的说明
            if(!fld.key_tip && F.key != F.title) {
                fld.key_tip = F.key;
            }

            // 字段: thingset
            if("thingset" == F.type) {
                fld.uiWidth = "all";
                fld.uiType = "@droplist";
                fld.uiConf = {
                    emptyItem : {},
                    items : "obj -mine -match \"tp:'thing_set'\" -json -l -sort 'nm:1' -e '^(id|nm|title)'",
                    icon  : '<i class="fa fa-cubes"></i>',
                    text  : function(o){
                        return o.title || o.nm;
                    },
                    value : function(o){
                        return o.id;
                    }
                };
            }
            // 字段: TSS
            else if("TSS" == F.type) {
                fld.uiWidth = "all";
                fld.uiType = "@droplist";
                fld.uiConf = {
                    multi : true,
                    items : "obj -mine -match \"tp:'thing_set'\" -json -l -sort 'nm:1' -e '^(id|nm|title)'",
                    icon  : '<i class="fa fa-cubes"></i>',
                    text  : function(o){
                        return o.title || o.nm;
                    },
                    value : function(o){
                        return o.id;
                    },
                    parseData : function(ids) {
                        return ids ? ids.split(/ *[, ] */g) : [];
                    }
                };
            }
            // 字段: sites
            else if("site" == F.type) {
                fld.uiWidth = "all";
                fld.uiType = "@droplist";
                fld.valueKey = F.arg;
                fld.uiConf = {
                    emptyItem : {
                        icon  : '<i class="zmdi zmdi-flash"></i>',
                        text  : "i18n:auto",
                        value : "id" == F.arg ? "${siteId}" : "${siteName}",
                    },
                    items : "obj -mine -match \"tp:'hmaker_site', race:'DIR'\" -json -l -sort 'nm:1'",
                    icon  : '<i class="fa fa-sitemap"></i>',
                    text  : function(o){
                        return o.title || o.nm;
                    },
                    value : function(o){
                        return o[this.valueKey || "nm"];
                    }
                };
            }
            // 字段: com
            else if("com" == F.type) {
                fld.uiWidth = "all";
                fld.uiType = "@droplist";
                fld.uiConf = {
                    emptyItem : {
                        text : "i18n:hmaker.com.dynamic.com_none",
                    },
                    itemArgs  : F.arg,
                    items : function(ctype, callback){
                        callback(UI.pageUI().getComList(ctype));
                    },
                    itemData : function(uiCom) {
                        if(uiCom) {
                            return {
                                icon  : uiCom.getIconHtml(),
                                text  : uiCom.getComDisplayText(false),
                                value : "#<" + uiCom.getComId() + ">",
                                tip   : "#" + uiCom.getComId(),
                            }
                        }
                    },
                };
            }
            // 字段: link
            else if("link" == F.type) {
                fld.uiType = "app/wn.hmaker2/support/c_edit_link";
                fld.uiConf = {
                    emptyItem : {
                        icon  : '<i class="zmdi zmdi-flash-auto"></i>',
                        text  : '<span>@auto</span><em>{{hmaker.link.auto}}</em>',
                        value : '@auto'
                    }
                }
            }
            // 字段: 映射表
            else if("mapping" == F.type) {
                fld.uiWidth = "all";
                fld.type = "object";
                fld.uiType = "@pair";
                fld.uiConf = {
                    mergeWith   : true,
                    templateAsDefault : false,
                    objTemplate : F.mapping || {}
                };
            }
            // 字段: 字段列表
            else if("fields" == F.type) {
                fld.uiWidth = "all";
                fld.type = "object";
                fld.uiType = "@text";
                fld.uiConf = {
                    height : 100,
                    formatData : function(s){
                        var re = null;
                        s = $.trim(s);
                        if(s) {
                            re = {};
                            var lines = s.split(/(\r?\n)+/g);
                            for(var i=0; i<lines.length; i++) {
                                var line = $.trim(lines[i]);
                                if(line) {
                                    var ss = line.split(/[ \t]*[:：][ \t]*/);
                                    var key = ss[0];
                                    var val = ss.length > 1 ? ss[1] : null;
                                    re[key] = val;
                                }
                            }
                        }
                        return re;
                    },
                    parseData : function(obj) {
                        var re = "";
                        for(var key in obj) {
                            var val = obj[key];
                            re += key;
                            if(val) {
                                re += " : " + val + "\n";
                            }
                        }
                        return re;
                    }
                };
            }
            // 字段：字段布局
            else if("layout" == F.type) {
                fld.uiWidth = "all";
                fld.type = "string";
                fld.uiWidth = "all";
                fld.uiType = "@text";
                fld.uiConf = F.arg ? $z.fromJson('{'+F.arg+'}') : {
                    height: 230
                };
            }
            // 字段：开关
            else if("toggle" == F.type) {
                fld.type = /^true(\/false)?/.test(F.arg)
                                ? "boolean"
                                : "string";
                fld.uiType = "@toggle";
                fld.uiConf = {
                    values : ({
                        "yes/no" : ["no", "yes"],
                        "yes"    : [null, "yes"],
                        "on/off" : ["off", "on"],
                        "on"     : [null, "on"],
                        "true/false" : [false, true],
                        "true"       : [false, true],
                    })[F.arg]
                }
            }
            // 字段：切换开关
            // 其 F.arg 格式为 "文字[=值]?", 譬如
            //  "黑色=black,白色=white"
            else if("switch" == F.type || "droplist" == F.type) {
                fld.type = "string";
                fld.uiType = "@" + F.type;
                fld.uiConf = {
                    items : UI.parseStringItems(F.arg)
                }
            }
            // 字段：多行文本
            else if("text" == F.type) {
                fld.type = "string";
                fld.uiWidth = "all";
                fld.uiType = "@text";
                fld.uiConf = F.arg ? $z.fromJson('{'+F.arg+'}') : {};
            }
            // 字段：JSON
            else if("json" == F.type) {
                fld.type = "object";
                fld.uiWidth = "all";
                fld.uiType = "@text";
                fld.uiConf = _.extend({
                        height: 90
                    }, (F.arg ? $z.fromJson('{'+F.arg+'}') : {}), {
                        formatData : function(val) {
                            return $z.map(val);
                        },
                        parseData : function(val) {
                            return $z.toJson(val, null, '  ');
                        }
                    });
            }
            // 直接使用
            else {
                _.extend(fld, F);
                // input 作为默认选项
                $z.setUndefined(fld, "type", "string");
                $z.setUndefined(fld, "uiType", "@input");
                $z.setUndefined(fld, "uiWidth", "all");
            }

            // 默认uiWidth为 auto
            $z.setUndefined(fld, "uiWidth", "auto");

            // 计入结果
            re.push(fld);
        }

        // 返回
        return re;
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);