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
    //...............................................................
    __reload_data : function(com, jP){
        var UI = this;
        com = com || UI.getData();

        // 记录一下接口的特征，以防止重复加载
        var api_finger = $z.toJson(_.pick(com, "api", "params"));

        // 得到 api 的相关信息
        var apiUrl = UI.getHttpApiUrl(com.api);
        var params = com.params || {};
        // 显示正在加载
        UI.ccode("api.loading").appendTo(jP.empty());
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
    },
    //...............................................................
}; // ~End methods
//====================================================================
// 得到 HMaker 所有 UI 对象的方法
var HmMethods = require("app/wn.hmaker2/hm__methods_com");

//====================================================================
// 输出
module.exports = function(uiCom){
    return _.extend(HmMethods(uiCom), methods);
};
//=======================================================================
});