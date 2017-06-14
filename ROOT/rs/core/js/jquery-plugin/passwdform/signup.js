(function($, $z){
//------------------------------------------------
$(function(){
//------------------------------------------------
    $('.main').passwdform({
        domain : $(document.body).attr("domain"),
        scene  : "signup",
        on_tooshort : function(jq, str) {
            jq.$name.attr("mode", "invalid");
            jq.$tip.attr("mode", "warn");
            jq.$tipInfo.text("用户名太短，不能少于4个字符！");
            jq.$vcode.removeAttr("show");
        },
        on_invalid : function(jq, str) {
            jq.$name.attr("mode", "invalid");
            jq.$tip.attr("mode","warn");
            jq.$tipInfo.text("用户名，只能是数字，字母，下划线，且不能小于四位，一旦注册不能更改");
            jq.$vcode.removeAttr("show");
        },
        on_error : function(jq, str) {
            jq.$name.attr("mode", "invalid");
            jq.$tip.attr("mode","warn");
            jq.$tipInfo.text(re);
            jq.$vcode.removeAttr("show");
        },
        on_exists : function(jq, str, sendBy) {
            jq.$name.attr("mode", "invalid");
            jq.$tip.attr("mode", "warn");
            jq.$tipInfo.text(({
                "phone"   : "手机号",
                "email"   : "邮箱",
                "account" : "用户",
            })[sendBy] + "已经被注册！");
            jq.$vcode.removeAttr("show");
        },
        on_noexists : function(jq, str, sendBy) {
            jq.$name.attr("mode", "ok");
            jq.$tip.attr("mode", "ok");

            // 如果是手机号，启用验证码
            if("phone" == sendBy){
                jq.$tipInfo.text("这个手机号可以注册");
                jq.show_vcode(str, "请输入手机验证码:", "6位数字");
            }
            // 如果是邮箱，启用验证码
            else if("email" == sendBy){
                jq.$tipInfo.text("这个邮箱可以注册");
                jq.show_vcode(str, "请输入邮箱验证码:", "8位数字字母组合");
            }
            // 什么都不是，那么隐藏验证码区域
            else {
                jq.$tipInfo.text("这个用户名可以注册");
                jq.$vcode.removeAttr("show");
            }
        },
        on_submit : function(jq, params, callback) {
            $.post("/u/do/signup/ajax", params, function(re){
                var reo = $z.fromJson(re);
                // 注册成功: 自动执行登录
                if(reo.ok) {
                    $z.openUrl("/u/do/login", "_self", "POST", {
                        nm : params.str,
                        passwd : params.passwd
                    });
                }
                // 注册失败
                else {
                    // 验证码错误
                    if("e.usr.vcode.invalid" == reo.errCode) {
                        jq.$vcode.attr("mode", "invalid");
                        jq.$tip.attr("mode","warn");
                        jq.$tipInfo.text("验证码错误");
                    }
                    // 密码不符合规范
                    else if("e.usr.pwd.invalid" == reo.errCode) {
                        jq.$passwd.attr("mode", "invalid");
                    }
                    // 其他错误
                    else {
                        jq.$form.find('.form-btn').attr("mode", "invalid");
                        jq.$form.find('.form-submit-error span')
                            .text(reo.errCode + (reo.msg ? " : "+reo.msg : ""));
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


