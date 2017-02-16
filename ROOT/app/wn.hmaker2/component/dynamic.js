(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/support/hm__methods_com'
], function(ZUI, Wn, HmComMethods){
//==============================================
var html = `<div class="hmc-dynamic ui-arena hm-del-save">
    <section></section>
</div>`;
//==============================================
return ZUI.def("app.wn.hm_com_dynamic", {
    dom     : html,
    keepDom : false,
    //...............................................................
    init : function(){
        var UI = HmComMethods(this);
    },
    //...............................................................
    events : {
        "click > .hm-com-W > .dynamic-reload" : function(){
            var UI = this;
            var jW = UI.$el.find(".hm-com-W");

            // 加载前固定高度
            jW.css("height", jW.outerHeight());

            // 加载后移除固定的高度
            UI.__reload_data(function(){
                jW.css("height", "");
            });
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
        var jW = UI.$el.find(".hm-com-W")

        // 得到数据
        com = com || UI.getData();

        // 检查显示模式
        if(!UI.__check_mode(com)){
            return ;
        }

        // 记录一下接口的特征，以防止重复加载
        var api_finger = $z.toJson(_.pick(com, "api", "params"));

        // 采用旧数据
        if(UI.__data_cache && api_finger == UI.__api_finger) {
            UI.__draw_data(UI.__data_cache, com);
        }
        // 重新加载
        else {
            UI.__reload_data(com);
        }

    },
    //...............................................................
    __reload_data : function(com, callback){
        var UI = this;
        var jData = UI.arena.children("section");

        // 支持直接给入 callback 的方式
        if(_.isFunction(com)) {
            callback = com;
            com = undefined;
        }

        // 确保有数据
        com = com || UI.getData();

        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 记录一下接口的特征，以防止重复加载
        var api_finger = $z.toJson(_.pick(com, "api", "params"));

        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 得到 api 的相关信息
        var apiUrl = UI.getHttpApiUrl(com.api);
        var pm_org = _.extend({}, com.params);

        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 处理动态参数 (来自请求参数，Session变量，Cookies 里面的值等)
        var params = {};
        var m;
        var dynamicKeys  = [];     // 参数是否有动态参数
        var isLackParams = false;  // 是否所有的动态参数都有默认值

        // 得到站点名称等动态值的上下文
        var pc = {
            siteName : UI.getHomeObjName(),
        };

        // 循环处理 ...
        for(var key in pm_org) {
            var val = $.trim(pm_org[key]);

            // 进行标准替换
            var v2 = $z.tmpl(val, {
                escape: /\$\{([\s\S]+?)\}/g
            })(pc);
            //console.log(key, val, v2);

            // 请求参数
            m = /^@([\w\d_-]+)(<(.+)>)?$/.exec(v2);
            if(m) {
                dynamicKeys.push(key);
                isLackParams = isLackParams || !m[3];
                params[key]  = m[3];
                continue;
            }
            // TODO Session 变量
            // TODO Cookie 的值

            // 记录参数
            params[key] = v2;
        }
        // 保存这个分析状态
        UI.__dynamicKeys  = dynamicKeys;
        UI.__isLackParams = isLackParams;

        // 如果有动态参数，且缺少足够的默认参数，那么会直接让组件绘制 null
        if(UI.isDynamicButLackParams()) {
            UI.__draw_data(null, com);
            return;
        }

        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 显示正在加载
        $('<div class="dynamic-msg" m="api-loading">')
            .html(UI.msg("hmaker.com.dynamic.api_loading"))
                .appendTo(jData.empty());

        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 向服务器请求
        var method = com.api_method == "POST" ? "post" : "get";
        $[method](apiUrl, params, function(re){
            // 请求成功后记录接口特征
            UI.__api_finger = api_finger;

            // api 返回错误
            if(/^e[.]/.test(re)){
                $('<div class="dynamic-msg" m="api-error">')
                    .html('<i class="zmdi zmdi-alert-triangle"></i>' + re)
                        .appendTo(jData.empty());
                return;
            }

            // 试图解析数据
            var reo;
            try {
                // 记录数据
                reo = $z.fromJson(re);
                UI.__data_cache = reo;

                // 重绘项目
                UI.__draw_data(UI.__data_cache, com);
            }
            // 接口调用错误
            catch (errMsg) {
                $('<div class="dynamic-msg" m="api-error">')
                    .html('<i class="zmdi zmdi-alert-circle"></i>' + errMsg)
                        .appendTo(jData.empty());
                throw errMsg;
            }
            // 最后要调用回调
            finally {
                //console.log("do Callback");
                $z.doCallback(callback, [re, reo], UI);
            }
        });
        // 这个请求，显然是异步的
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    },
    //...............................................................
    __draw_data : function(obj, com) {
        var UI = this;
        var jW = UI.$el.find(".hm-com-W");
        var jData = UI.arena.children("section").empty();

        // 确保有可绘制的数据
        com = com || UI.getData();
        
        // 绘制动态参数键列表
        UI.__draw_dynamic_keys(jW);

        // 绘制重新加载按钮
        UI.__draw_dynamic_reload(jW);

        // 动态参数，但是缺少默认值，那么就没有足够的数据绘制了，显示一个信息吧
        if(UI.isDynamicButLackParams()) {
            $('<div class="dynamic-msg" m="api-lack-params">')
                .html(UI.msg("hmaker.com.dynamic.api_lack_params"))
                    .appendTo(jData);
            return;
        }

        console.log("dynamic draw", obj);

        // 如果木有数据，就显示空
        if(!obj || (_.isArray(obj) && obj.length == 0)) {
            $('<div class="dynamic-msg" m="api-no-data">')
                .html(UI.msg("hmaker.com.dynamic.api_empty"))
                    .appendTo(jData);
            return;
        }

        // console.log(list)

        // 加载模板
        var tmplInfo = UI.evalTemplate(com.template);

        // 得到皮肤选择器
        var skinSelector = UI.getSkinForTemplate(com.template);

        // 准备绘制模板参数
        var tmplOptions = _.extend({}, com.options, {
            API : UI.getHttpApiUrl(),
            OBJ_TYPE : com.api_return,
        });

        // 确保设置模板皮肤
        if(skinSelector)
            jData.addClass(skinSelector);

        // 调用模板的 jQuery 插件进行绘制
        jData[tmplInfo.name](obj, tmplOptions);
    },
    //...............................................................
    isDynamicButLackParams : function(){
        return this.__dynamicKeys 
                && this.__dynamicKeys.length > 0
                && this.__isLackParams;
    },
    //...............................................................
    __draw_dynamic_keys : function(jW) {
        if(jW.find(">.dynamic-keys").length > 0)
            return;
            
        var dynamicKeys = this.__dynamicKeys;
        
        if(_.isArray(dynamicKeys) && dynamicKeys.length > 0) {
            var jUl = $('<div class="dynamic-keys"><ul></ul></div>')
                        .appendTo(jW)
                            .find(">ul");
            for(var dKey of dynamicKeys)
                $('<li>').text(dKey).appendTo(jUl);
        }
    },
    //...............................................................
    __draw_dynamic_reload : function(jW) {
        if(jW.find("> .dynamic-reload").length > 0)
            return;
            
        $('<div class="dynamic-reload"><b><i class="fa fa-refresh"></i></b></div>')
            .attr({
                "data-balloon" : this.msg("hmaker.dds.reload"),
                "data-balloon-pos"    : "left",
                "data-balloon-length" : "medium"
            })
            .appendTo(jW);
    },
    //...............................................................
    __check_mode : function(com) {
        var UI = this;
        var jData = UI.arena.find(">section").empty();

        // 确保有数据接口
        if(!com.api) {
            $('<div class="dynamic-msg" m="warn">')
                .html(UI.msg("hmaker.com.dynamic.noapi"))
                    .appendTo(jData);
            return false;
        }

        // 确保有显示模板
        if(!com.template) {
            $('<div class="dynamic-msg" m="warn">')
                .html(UI.msg("hmaker.com.dynamic.notemplate"))
                    .appendTo(jData);
            return false;
        }
        
        // 通过检查
        return true;
        
    },
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);