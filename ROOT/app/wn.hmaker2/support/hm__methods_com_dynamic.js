define(function (require, exports, module) {
var methods = {
    //...............................................................
    __check_mode : function(com) {
        var UI = this;
        var jMsg = UI.arena.find(">section.hmc-dds-msg").empty();

        // 确保有数据接口
        if(!com.api) {
            UI.ccode("noapi").appendTo(jMsg.show());
            UI.arena.find(">div").hide();
            return false;
        }

        // 确保有显示模板
        if(!com.template) {
            UI.ccode("notemplate").appendTo(jMsg.show());
            UI.arena.find(">div").hide();
            return false;
        }
        
        // 确保表示数据
        UI.$el.attr("dynamic-data", "yes")
            .find(".dynamic-keys,.dynamic-reload").remove();

        // 通过检查
        jMsg.hide();
        return true;
        
    },
    //...............................................................
    __paint_data : function(com, jP) {
        var UI = this;

        // 记录一下接口的特征，以防止重复加载
        var api_finger = $z.toJson(_.pick(com, "api", "params"));

        // 采用旧数据
        if(UI.__data_cache && api_finger == UI.__api_finger) {
            UI.__draw_data(UI.__data_cache, com);
        }
        // 重新加载
        else {
            UI.__reload_data(com, jP);
        }
    },
    __draw_dynamic_keys : function(jP) {
        if(jP.find(".dynamic-keys").length > 0)
            return;
            
        var dynamicKeys = this.__dynamicKeys;
        
        if(_.isArray(dynamicKeys) && dynamicKeys.length > 0) {
            var jUl = $('<div class="dynamic-keys"><ul></ul></div>')
                        .appendTo(jP).find("ul");
            for(var dKey of dynamicKeys)
                $('<li>').text(dKey).appendTo(jUl);
        }
    },
    __draw_dynamic_reload : function(jP) {
        if(jP.find(".dynamic-reload").length > 0)
            return;
            
        $('<div class="dynamic-reload"><b><i class="fa fa-refresh"></i></b></div>')
            .attr({
                "data-balloon" : this.msg("hmaker.dds.reload"),
                "data-balloon-pos"    : "left",
                "data-balloon-length" : "medium"
            })
            .appendTo(jP);
    },
    //...............................................................
    isDynamicButLackParams : function(){
        return this.__dynamicKeys 
                && this.__dynamicKeys.length > 0
                && this.__isLackParams;
    },
    //...............................................................
    __reload_data : function(com, jP){
        var UI = this;
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
        for(var key in pm_org) {
            var val = $.trim(pm_org[key]);

            // 请求参数
            m = /^@([\w\d_-]+)(<(.+)>)?$/.exec(val);
            if(m) {
                dynamicKeys.push(key);
                isLackParams = isLackParams || !m[3];
                params[key]  = m[3];
                continue;
            }
            // TODO Session 变量
            // TODO Cookie 的值
            params[key] = val;
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
        UI.ccode("api.loading").appendTo(jP.empty());

        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 向服务器请求
        $.post(apiUrl, params, function(re){
            // 请求成功后记录接口特征
            UI.__api_finger = api_finger;

            // api 返回错误
            if(/^e[.]/.test(re)){
                $('<div class="api-error">').text(re).appendTo(jP.empty());
                return;
            }

            // 试图解析数据
            try {
                // 记录数据
                UI.__data_cache = $z.fromJson(re);

                // 重绘项目
                UI.__draw_data(UI.__data_cache, com);
            }
            // 接口调用错误
            catch (errMsg) {
                $('<div class="api-error">').text(errMsg).appendTo(jP.empty());
            }
        });
        // 这个请求，显然是异步的
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    },
    //...............................................................
}; // ~End methods
//====================================================================
// 得到 HMaker 所有 UI 对象的方法
var HmMethods = require("app/wn.hmaker2/support/hm__methods_com");

//====================================================================
// 输出
module.exports = function(uiCom){
    return _.extend(HmMethods(uiCom), methods);
};
//=======================================================================
});