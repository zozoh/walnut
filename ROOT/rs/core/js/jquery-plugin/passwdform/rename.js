(function($){
//------------------------------------------------
// 绑定各种事件
$(function(){
    var jSec = $('section');
    // 一些关键 DOM 元素
    var jq = {
        $name          : jSec.find(".form-renm"),
        $nameInput     : jSec.find(".form-renm input"),
        $tip           : jSec.find('.form-tip'),
        $tipInfo       : jSec.find(".form-tip .re-info"),
        $passwd        : jSec.find(".form-passwd"),
        $passwdInput   : jSec.find(".form-passwd input"),
        $submit        : jSec.find(".form-btn"),
        $submitButton  : jSec.find(".form-btn button"),
    };
    //------------------------------------------------
    function sync_form_state(str) {
        var dis = false;
        // 名字必须正确
        if("ok" != jq.$name.attr("mode")) {
            dis = true;
        }
        // 如果显示了密码，则密码框必须有值
        else if("hide" != jq.$passwd.attr("mode") 
                && !$.trim(jq.$passwdInput.val())){
            dis = true;
        }
        // 修改按钮状态
        jq.$submitButton.prop("disabled", dis);
    }
    //------------------------------------------------
    // 监控用户名输入的改变
    jq.$nameInput.on("change", function(){
        // 强制小写字母
        var str = $.trim(jq.$nameInput.val()).toLowerCase();
        jq.$nameInput.val(str);

        // 恢复初始化状态
        jq.$name.removeAttr("mode");
        jq.$tip.removeAttr("mode");
        jq.$passwd.attr("mode", "hide");
        jq.$passwdInput.val("");
        sync_form_state();

        // 空字符串，啥也不做
        if(!str) {
            sync_form_state();
        }
        // 用户名非法
        else if(!/^[0-9a-zA-Z_]{4,}$/.test(str)){
            jq.$name.attr("mode", "invalid");
            jq.$tip.attr("mode", "warn");
            jq.$tipInfo.text("用户名，只能是数字，字母，下划线，且不能小于四位，一旦注册不能更改");
            $z.blinkIt(jq.$tipInfo);
        }
        // 否则检查是否存在
        else {
            $.get("/u/exists", {str:str}, function(re){
                var reo = $z.fromJson(re);

                // 错误
                if(!reo.ok) {
                    jq.$name.attr("mode", "invalid");
                    jq.$tip.attr("mode","warn");
                    jq.$tipInfo.text(reo.msg || re);
                }
                // 用户存在，则显示密码卡
                else if(reo.data){
                    jq.$name.attr("mode", "ok");
                    jq.$tip.attr("mode","show");
                    jq.$tipInfo.text("用户已存在，请输入密码以便合并登录信息");
                    jq.$passwd.removeAttr("mode");
                    jq.$passwdInput.focus();
                }
                // 没毛病
                else {
                    jq.$name.attr("mode", "ok");
                    jq.$tip.attr("mode", "ok");
                    jq.$tipInfo.text("这个用户名可以使用，请点击下方的「继续」按钮");
                }

                // 最后确保验证码部分的显示/隐藏状态正确
                $z.blinkIt(jq.$tipInfo);
                sync_form_state();
            });
        }
    });
    //------------------------------------------------
    // 监控密码输入的改变
    jq.$passwdInput.on("keyup", function(){
        sync_form_state();
    });
    //------------------------------------------------
    // 提交表单 
    $(".form-btn button").on("click", function(e){
        // 阻止默认行为
        e.preventDefault();

        // 得到表单对象
        var params = {
            nm : $.trim(jq.$nameInput.val()),
            passwd : $.trim(jq.$passwdInput.val()),
        };

        // 提交表单信息
        $.post("/u/do/rename/ajax", params, function(re){
            var reo = $z.fromJson(re);
            // 改名成功: 自动调整
            if(reo.ok) {
                $z.openUrl("/", "_self");
            }
            // 名称非法
            else if("e.u.rename.invalid" == reo.errCode) {
                jq.$tip.attr("mode","warn");
                jq.$tipInfo.text("用户名只能由数字，小写字母以及下划线组成,且不能小于四位");
            }
            // 已经存在
            else if("e.u.rename.exists" == reo.errCode) {
                jq.$tip.attr("mode","warn");
                jq.$tipInfo.text("这个用户已存在");
            }
            // 其他错误
            else {
                jq.$tip.attr("mode","warn");
                jq.$tipInfo.text(reo.msg);
            }
            $z.blinkIt(jq.$tipInfo);
        });
    });
    //------------------------------------------------
    // 让输入框获得焦点
    jq.$nameInput.focus();
    //------------------------------------------------
// 绑定各种事件
//------------------------------------------------
});
//------------------------------------------------
})(window.jQuery, window.NutzUtil);