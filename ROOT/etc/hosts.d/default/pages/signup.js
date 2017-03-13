(function($, $z){
//------------------------------------------------
// 绑定各种事件
$(function(){
    // 一些关键 DOM 元素
    var jForm  = $("form");
    var jVCode = $('.form-vcode');
    var jTip   = $('.form-checkre');
    //------------------------------------------------
    function sync_vcode(str) {
        // 如果是手机号，启用验证码
        if(/^[0-9+-]{11,20}$/.test(str)){
            $.get("/u/vcode/phone/get", {phone:str}, function(re){
                jVCode.find(".vcode-tip").text("验证码已经发出，请掏出手机查看:");
                jVCode.find(".vcode-tt").text("请输入手机验证码:");
                jVCode.find("input").attr("placeholder", "6位数字");
                jVCode.find("b").removeAttr("enabled");
                jVCode.find("b em").text(60);
                jVCode.attr("show", "yes");
            });
        }
        // 如果不是手机号，也不是邮箱，那么隐藏验证码
        else {
            jVCode.attr("show", null);
        }
    } 
    //------------------------------------------------
    // 监控用户名输入的改变
    $(".form-name input").on("change", function(){
        var str = $.trim($(this).val());
        // 如果空字符串，那么就都清空
        if(!str) {
            jTip.removeAttr("mode");
            jVCode.attr("show", null);
        }
        // 否则检查是否存在
        else {
            $.get("/u/exists", {str:str}, function(re){
                var reo = $z.fromJson(re);
                console.log(reo)
                // 用户名太短
                if("e.usr.loginstr.tooshort" == reo.errCode) {
                    jTip.attr("mode", "warn");
                    jTip.find(".re-info").text("用户名太短，不能少于4个字符！");
                }
                // 用户存在
                else if(reo.data){
                    jTip.attr("mode", "warn");
                    jTip.find(".re-info").text("用户已经存在！");
                }
                // 可用
                else {
                    jTip.attr("mode", "ok");
                    jTip.find(".re-info").text("用户未注册，可用");
                    sync_vcode(str);
                }
            });
        }
        
    });
    // 监控验证码获取的按钮状态
    var jVCodeEm = jVCode.find("b em");
    window.setInterval(function(){
        if(jVCode.attr("show")){
            var sec = jVCodeEm.text() * 1;
            if(sec > 0) {
                jVCodeEm.text(--sec);
            }
            if(sec == 0) {
                jVCode.find("b").attr("enabled", "yes");
            }
        }
    }, 1000);
});
//------------------------------------------------
})(window.jQuery, window.NutzUtil);