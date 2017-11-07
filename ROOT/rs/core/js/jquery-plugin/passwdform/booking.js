(function($, $z){
//-----------------------------------------
$(function(){
    // 获取模式缩放的阀值，就看 header 区的位置
    var vav = $(".main").outerHeight(true) - 120;

    // 根据锚点初始页面状态
    if("info" == $z.pageAnchor()){
        $(".main").css("height", $(".main").height());
        $(document.body).attr("show-sky", "yes");
        window.setTimeout(function(){
            $(window).scrollTop(vav + 1);
        }, 300)
    }
    // 监视滚动
    $(window).scroll(function(){
        var jBody = $(document.body);
        var sTop  = $(window).scrollTop();
        //console.log(sTop, vav)
        // 到顶了，移除标识
        if(sTop<=vav){
            $(".main").css("height", "");
            jBody.removeAttr("show-sky");
        }
        // 确保添加了标识，显示 sky 条
        else if(!jBody.attr("show-sky")){
            $(".main").css("height", $(".main").height());
            jBody.attr("show-sky", "yes");
        }
    });
    // 点击回到顶部 
    $(".sky a").click(function(){
        // 动画滚动
        if($(window).scrollTop()>(vav*1.5)) {
            $("html,body").animate({
                scrollTop : 0
            }, 500);
        }
        // 直接滚动
        else {
            $(window).scrollTop(0);
        }
    });
    // 点击了解更多
    $(".about a").click(function(){
        $("html,body").animate({
            scrollTop : vav + 1
        }, 300);
    });
    // 输入手机号
    $('.main').passwdform({
        domain : $(document.body).attr("domain"),
        scene  : "booking",
        url_exists : "/u/booking/exists",
        checkName : function(str) {
            return /^[0-9+-]{11}$/.test(str);
        },
        on_tooshort : function(jq, str) {
            jq.$name.attr("mode", "invalid");
            jq.$tip.attr("mode", "warn");
            jq.$tipInfo.text("我不认为你输入的是手机号，手机号啊啊啊啊！");
            jq.$vcode.removeAttr("show");
        },
        on_invalid : function(jq, str) {
            jq.$name.attr("mode", "invalid");
            jq.$tip.attr("mode", "warn");
            jq.$tipInfo.text("请输入一个合法的手机号");
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
            jq.$tipInfo.text("手机号 「" + str + "」已经被使用了，请换一个吧 ^_^!");
            jq.$vcode.removeAttr("show");
        },
        on_noexists : function(jq, str, sendBy) {
            if("phone" == sendBy) {
                jq.$name.attr("mode", "ok");
                jq.$tip.attr("mode", "ok");
                jq.$tipInfo.text("这个手机号可以申请");
                jq.show_vcode(str, "请输入手机验证码:", "6位数字");
            }
            // 否则显示错误（必然不是手机号）
            else {
                jq.$name.attr("mode", "invalid");
                jq.$tip.attr("mode", "warn");
                jq.$tipInfo.text("只能用手机号申请，请写手机号，请写手机号，请写手机号，谢谢 -_-!");
                jq.$vcode.removeAttr("show");
            }
        },
        on_submit : function(jq, params, callback) {
            console.log("on_submit", params);
            $.post("/u/do/booking/ajax", params, function(re){
                var reo = $z.fromJson(re);
                // 注册成功: 显示成功信息
                if(reo.ok) {
                    $z.openUrl("booking_ok.html", "_self");
                }
                // 注册失败
                else {
                    // 验证码错误
                    if("e.usr.vcode.invalid" == reo.errCode) {
                        jq.$vcode.attr("mode", "invalid");
                        jq.$tip.attr("mode","warn");
                        jq.$tipInfo.text("验证码错误");
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
});
//-----------------------------------------
})(window.jQuery, window.NutzUtil);
