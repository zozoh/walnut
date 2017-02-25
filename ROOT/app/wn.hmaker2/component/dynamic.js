(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/support/hm__methods_com',
    '/gu/rs/ext/hmaker/hm_runtime.js'
], function(ZUI, Wn, HmComMethods){
//==============================================
var html = `<div class="hmc-dynamic ui-arena hm-del-save">
    <section></section>
</div>`;
//==============================================
return ZUI.def("app.wn.hm_com_dynamic", {
    dom     : html,
    keepDom : false,
    className : "!hm-com-dynamic",
    //...............................................................
    init : function(){
        var UI = HmComMethods(this);
    },
    //...............................................................
    events : {
        "click > .hm-com-W > .dynamic-reload" : function(e){
            this.__reload_data();
        }
    },
    //...............................................................
    getDataProp : function(){
        return {
            uiType : 'app/wn.hmaker2/com_prop/dynamic_prop',
            uiConf : {}
        };
    },
    //...............................................................
    paint : function(com) {
        var UI = this;
        var jW = UI.$el.find(">.hm-com-W");
        //console.log("I am dynamic paint");

        // 得到数据
        com = com || UI.getData();

        // 检查显示模式
        var oApi = UI.__check_mode(com);
        if(!oApi){
            return ;
        }

        // 记录一下接口的特征，以防止重复加载
        var api_finger = $z.toJson(_.pick(com, "api", "params"));

        // 采用旧数据
        if(UI.__data_cache && api_finger == UI.__api_finger) {
            UI.__clean_assists();
            UI.__draw_data(UI.__data_cache, com, oApi);
        }
        // 重新加载
        else {
            UI.pageUI().delayWhenReadyForEdit(function(){
                UI.__reload_data(com, oApi);
            });
        }

    },
    //...............................................................
    __reload_data : function(com, oApi){
        var UI = this;
        var jData = UI.arena.children("section").empty();

        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 确保有数据
        com = com || UI.getData();

        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 确保有数据接口
        oApi = oApi || UI.__check_mode(com);
        if(!oApi){
            return ;
        }

        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 清除动态标志
        UI.__clean_assists();

        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 检查一下是否 required 的字段都已经被设置了
        if(!_.isEmpty(oApi.params)) {
            var need_params = [];
            for(var key in oApi.params) {
                var pa = oApi.params[key];
                if(/^[*]/.test(pa)){
                    var val = com.params ? com.params[key] : undefined;
                    if(_.isUndefined(val)
                        || _.isNull(val)
                        || (_.isString(val) && $.trim(val).length == 0)){
                        need_params.push(key);
                    }
                }
            }
            if(need_params.length > 0) {
                UI.__tip("need_params : " + need_params.join(', '), "warn", jData);
                return;
            }
        }

        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 记录一下接口的特征，以防止重复加载
        var api_finger = $z.toJson(_.pick(com, "api", "params"));

        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 得到 api 的相关信息
        var apiUrl = UI.getHttpApiUrl(com.api);

        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 将参数处理成可向数据接口提交的形式
        var params;
        try{
            params = UI.__parse_params(com, oApi);
        }
        // 处理参数解析的错误
        catch(errMsg){
            UI.__tip(errMsg, "api-error", jData);
            throw errMsg;
        }
        //console.log(params)

        // 如果有动态参数，且缺少足够的默认参数，那么会直接让组件绘制 null
        if(UI.isDynamicButLackParams()) {
            UI.__draw_data(null, com, oApi);
            return;
        }

        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 显示正在加载
        UI.__tip("api_loading", "api-loading", jData);

        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 向服务器请求
        var method = com.api_method == "POST" ? "post" : "get";
        $[method](apiUrl, params, function(re){
            // 清除正在加载的显示
            jData.empty();
            
            // 请求成功后记录接口特征
            UI.__api_finger = api_finger;

            // api 返回错误
            if(/^e[.]/.test(re)){
                UI.__tip('<i class="zmdi zmdi-alert-triangle"></i>' + re, 
                    "api-error", jData);
                return;
            }

            // 试图解析数据
            var reo;
            try {
                // 记录数据
                reo = $z.fromJson(re);
                UI.__data_cache = reo;

                // 重绘项目
                UI.__draw_data(UI.__data_cache, com, oApi);
            }
            // 接口调用错误
            catch (errMsg) {
                // 显示错误
                UI.__tip('<i class="zmdi zmdi-alert-triangle"></i>' + errMsg, 
                    "api-error", jData);
                // 绘制重新加载按钮
                UI.__draw_dynamic_reload();
                // 抛出错误，不要继续了
                throw errMsg;
            }
        });
        // 这个请求，显然是异步的
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    },
    //...............................................................
    __parse_params : function(com, oApi){
        var UI = this;
        var PageUI = UI.pageUI();
        var oHome  = UI.getHomeObj();

        //console.log("before:", com.params);

        // 解析 result
        var re = HmRT.evalResult(com.params, {
            context : {
                siteName : oHome.nm,
                siteId   : oHome.id,
            },
            setting : HmRT.parseSetting(oApi.params || {}, true),
            getComValue : function(comId) {
                var uiComTa = PageUI.getCom(comId);
                if(!uiComTa) {
                    throw "e_nocom : " + comId; 
                }
                return $z.invoke(uiComTa, "getComValue", []);
            }
        });

        //console.log("re:", re);

        // 保存这个分析状态
        UI.__dynamicKeys = re.dynamicKeys;
        UI.__lackKeys    = re.lackKeys;

        // 返回参数
        return re.data;
    },
    //...............................................................
    __draw_data : function(data, com, oApi) {
        var UI = this;
        var jW = UI.$el.find(">.hm-com-W");
        var jData = UI.arena.children("section").empty();
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 确保有数据接口
        oApi = oApi || UI.__check_mode(com);
        if(!oApi){
            return ;
        }
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 确保有可绘制的数据
        com = com || UI.getData();
        
        // 绘制动态参数键列表
        UI.__draw_dynamic_keys(jW);

        // 绘制重新加载按钮
        UI.__draw_dynamic_reload(jW);
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 动态参数，但是缺少默认值，那么就没有足够的数据绘制了，显示一个信息吧
        if(UI.isDynamicButLackParams()) {
            UI.__tip("api_lack_params", "api-lack-params", jData);
            return;
        }
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 如果数据是翻页信息，那么还需要找到翻页控件，并更新它的值
        var PageUI = UI.pageUI();
        if("page" == oApi.api_return && data.pager && data.list) {
            //console.log(data.pager)
            // 先解析一下 API 的设置参数
            var setting = HmRT.parseSetting(oApi.params);
            for(var i=0; i<setting.length; i++) {
                // 寻找可以选用翻页条的项目: @com || @com:pager 
                var conf = setting[i];
                if(conf.type == "com" &&
                    (!conf.arg || conf.arg.indexOf("pager") >= 0)) {
                    // 在 com 中找到对应的翻页条并设置数据
                    var ta = $.trim((com.params||{})[conf.key]);
                    var m  = /^#<([^>]+)>$/.exec(ta);
                    if(m) {
                        var uiComTa = PageUI.getCom(m[1]);
                        if(uiComTa) {
                            $z.invoke(uiComTa, "setComValue", [data.pager]);
                        }
                    }
                }
            }
        }
        
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 加载模板
        var tmplInfo = UI.evalTemplate(com.template);

        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 转换数据
        //console.log("dynamic draw", data);
        var d2 = HmRT.convertDataForTmpl(data, tmplInfo.dataType);
        if(HmRT.isDataEmptyForTmpl(d2, tmplInfo.dataType)) {
            UI.__tip("api_empty", "api-no-data", jData);
            return;
        }
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 得到皮肤选择器
        var skinSelector = UI.getSkinForTemplate(com.template);

        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 准备绘制模板参数
        var tmplOptions = _.extend({}, com.options, {
            API : UI.getHttpApiUrl(),
        });
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 确保设置模板皮肤
        if(skinSelector)
            jData.prop("className", skinSelector);
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 调用模板的 jQuery 插件进行绘制
        jData[tmplInfo.name](d2, tmplOptions);
    },
    //...............................................................
    isDynamicButLackParams : function(){
        if(this.__dynamicKeys.length > 0){
            for(var i=0; i<this.__dynamicKeys.length; i++) {
                var dkey = this.__dynamicKeys[i];
                if(this.__lackKeys.indexOf(dkey)>=0)
                    return true;
            }
        }
        return false;
    },
    //...............................................................
    __draw_dynamic_keys : function(jW) {
        var dkeys = this.__dynamicKeys;

        if(!_.isArray(dkeys) || dkeys.length == 0)
            return;

        var jUl = $('<div class="dynamic-keys"><ul></ul></div>')
                    .appendTo(jW)
                        .find(">ul");
        for(var i=0; i<dkeys.length; i++) {
            $('<li>').text(dkeys[i]).appendTo(jUl);
        }
    },
    //...............................................................
    __draw_dynamic_reload : function(jW) {
        var UI = this;
        jW = jW || UI.$el.find(">.hm-com-W");

        // 有未设置默认值的动态参数，则没必要绘制重载按钮
        if(UI.isDynamicButLackParams())
            return;
            
        $('<div class="dynamic-reload"><b><i class="fa fa-refresh"></i></b></div>')
            .attr({
                "data-balloon" : UI.msg("hmaker.dds.reload"),
                "data-balloon-pos"    : "left",
                "data-balloon-length" : "medium"
            })
            .appendTo(jW);
    },
    //...............................................................
    // 清除 reload 按钮和动态参数标志
    __clean_assists : function() {
        var UI = this;
        var jW = UI.$el.find(">.hm-com-W");
        jW.find(">.dynamic-keys, >.dynamic-reload").remove();
    },
    //...............................................................
    __check_mode : function(com) {
        var UI = this;
        var jData = UI.arena.find(">section").empty();

        // 确保有数据接口
        if(!com.api) {
            UI.__tip("noapi", "warn", jData);
            return false;
        }

        // 确保有数据接口定义文件
        var oApi = Wn.fetch("~/.regapi/api" + com.api);
        if(!oApi) {
            UI.__tip("api_gone", "warn", jData);
            return;
        }

        // 确保有显示模板
        if(!com.template) {
            UI.__tip("notemplate", "warn", jData);
            return false;
        }
        
        // 通过检查
        return oApi;
        
    },
    //...............................................................
    __tip : function(key, mode, jData) {
        var UI = this;
        jData = jData || UI.arena.find(">section").empty();

        $('<aside class="dynamic-msg" m="'+mode+'">')
                .html(UI.msg("hmaker.com.dynamic." + key))
                    .appendTo(jData);
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);