(function($, $z){
//------------------------------------------------
var _invalid = function(jq, msg) {
    jq.$name.attr("mode", "invalid");
    jq.$tip.attr("mode","warn");
    jq.$tipInfo.text(msg);
    jq.$passwd.attr("mode", "hide");
    jq.$passre.attr("mode", "hide");
    jq.$vcode.removeAttr("show");
};
//------------------------------------------------
$(function(){
//------------------------------------------------
    $('.main').passwdform({
        domain : $(document.body).attr("domain"),
        scene  : "passwdback",
        on_blank : function(jq) {
            jq.$passwd.attr("mode", "hide");
            jq.$passre.attr("mode", "hide");
        },
        on_tooshort : function(jq, str) {
            _invalid(jq, "没有这个用户，你给的一定不是手机号或者邮箱地址");
        },
        on_invalid : function(jq, str) {
            _invalid(jq, "没有这个用户，请输入您账号关联的手机号或者邮箱地址");
        },
        on_error : function(jq, errMsg) {
            _invalid(jq, errMsg);
        },
        on_exists : function(jq, str, sendBy) {
            jq.$name.attr("mode", "ok");
            jq.$tip.attr("mode", "ok");
            jq.$passwd.removeAttr("mode");
            jq.$passre.removeAttr("mode");

            // 如果是手机号，启用验证码
            if("phone" == sendBy){
                jq.$tipInfo.text("用手机号找回密码");
                jq.show_vcode(str, "请输入手机验证码:", "6位数字");
            }
            // 如果是邮箱，启用验证码
            else if("email" == sendBy){
                jq.$tipInfo.text("用邮箱找回密码");
                jq.show_vcode(str, "请输入邮箱验证码:", "8位数字字母组合");
            }
            // 什么都不是，那么警告
            else {
                _invalid(jq, "请输入您账号关联的手机号或者邮箱地址，谢谢");
            }
        },
        on_noexists : function(jq, str, sendBy) {
            _invalid(jq, "其实这个" + ({
                "phone"   : "手机号",
                "email"   : "邮箱",
                "account" : "用户",
            })[sendBy] + "并未注册过 -_-!");
        },
        on_submit : function(jq, params, callback) {
            $.post("/u/do/passwd/reset/ajax", params, function(re){
                var reo = $z.fromJson(re);
                // 修改成功: 跳转到登录界面
                if(reo.ok) {
                    $z.openUrl("/u/h/login.html", "_self");
                }
                // 修改失败
                else {
                    // 验证码错误
                    if("e.usr.passwd.reset.vcode.invalid" == reo.errCode) {
                        jq.$vcode.attr("mode", "invalid");
                        jq.$tip.attr("mode","warn");
                        jq.$tipInfo.text("验证码错误");
                    }
                    // 密码不符合规范
                    else if("e.usr.passwd.reset.invalid" == reo.errCode) {
                        jq.$passwd.attr("mode", "invalid");
                    }
                    // 其他错误
                    else {
                        jq.$tip.attr("mode","warn");
                        jq.$tipInfo.text(reo.errCode + (reo.msg ? " : "+reo.msg : ""));
                    }
                    // 处理回调
                    callback();
                }
            });
        }
    });
//------------------------------------------------
});
//------------------------------------------------
})(window.jQuery, window.NutzUtil);


