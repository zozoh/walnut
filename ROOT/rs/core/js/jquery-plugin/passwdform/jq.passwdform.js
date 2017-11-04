(function($, $z){
// 结束 jQuery 插件
/*------------------------------------------------
 * 插件: 弹出图形验证码，用法
 {
    domain  : "xxx"             // 提供验证码服务的域
    scene   : "signup",         // 场景，比如 signup
    account : "xxx"             // 要获取图形验证码的账号名
    title   : "xxx"             // 验证码输入框的标题文字
    placeholder : "xxx"         // 验证码输入框的占位符文字
    src : {c}F(account)         // 返回获取验证码图片的 URL
    url : {c}F(account, sendBy) // 返回提交验证码发送请求的 URL
    context : jVCode            // 回调函数的 this 默认为 jVCode
    holdSeconds : 60            // 多少秒后可以重新获取验证码
    on_change : {c}F(val)       // 验证码输入框发生改变
    on_before_send : {c}F()     // 验证之前调用
    on_ok    : {c}F(phone|email)   // 验证成功
    on_fail  : {c}F(phone|email)   // 验证失败，图片验证码输入错误
    on_error : {c}F(errMsg)     // 发生未知错误
 }
 */
$.fn.extend({"vcode": function(opt){
    var jVCode  = this;
    var context = opt.context || jVCode;

    // 监控函数的句柄
    var _H;
    //------------------------------------------------
    // 监控验证码获取的按钮状态
    function holdVCodeButton(){
        var jVCodeB    = jVCode.find("b").removeAttr("enabled");
        var jVCodeSpan = jVCodeB.find("span").text("秒后重新获取验证码");
        var jVCodeEm   = jVCodeB.find("em").text(opt.holdSeconds || 60);
        _H = window.setInterval(function(){
            // 进行判断
            if(_H){
                var sec = jVCodeEm.text() * 1;
                if(sec > 0) {
                    jVCodeEm.text(--sec);
                }
                else {
                    jVCode.find("b").attr("enabled", "yes");
                    jVCodeEm.empty();
                    jVCodeSpan.text("获取验证码");
                    window.clearInterval(_H);
                    _H = null;
                }
            }
        }, 1000);
    }

    // 首先生成 DOM 结构 
    var html = '<span class="fld-icon vcode-tt"></span>';
    html += '<input spellcheck="false" name="vcode"/>';
    html += '<b enabled="yes"><em></em><span>获取验证码</span></b>';
    jVCode.html(html);

    // 显示信息
    jVCode.find(".vcode-tt").text(opt.title);
    jVCode.find("input").attr("placeholder", opt.placeholder);

    // 清空事件监听
    jVCode.off();

    //------------------------------------------------
    // 显示图片验证码输入框
    jVCode.on("click", "b[enabled]", function(){
        var str = opt.account;

        // 显示图片验证码
        var html = '<div class="vcode-captcha">';
        html += '<div class="vc-main">';
        html += '<div class="vcm-img"><img></div>';
        html += '<div class="vcm-reload"><a href="javascript:void(0)">看不清换一张</a></div>';
        html += '<div class="vcm-input"><input spellcheck="false" placeholder="请输入图中的验证码"></div>';
        html += '<b class="vcm-do">继续</b>';
        html += '<a class="vcm-cancel" href="#">算了，我放弃</a></div>';
        html += '</div></div>';
        var jCaptcha = $(html).appendTo(document.body);

        // 显示图片验证码
        jCaptcha.find("img").attr("src", opt.src(str));
        jCaptcha.find("input").val("").focus();

        //------------------------------------------------
        // 事件:继续验证
        jCaptcha.on("click", '.vcm-do', function(){
            // 得到验证码
            var token = $.trim(jCaptcha.find('.vcm-input input').val());

            if(!token){
                alert("请输入验证码");
                return;
            }

            // 人家都要重新获取了，验证码输入框没必要标识错误了
            jVCode.removeAttr("mode");

            // 准备发送验证请求
            var sendBy;
            
            // 如果是手机号，启用验证码
            if(/^[0-9+-]{11,20}$/.test(str)){
                sendBy = "phone";
            }
            // 如果是邮箱，启用验证码
            else if(/^[0-9a-zA-Z_.-]+@[0-9a-zA-Z_.-]+.[0-9a-zA-Z_.-]+$/.test(str)){
                sendBy = "email";
            }
            // 什么都不是，那么隐藏验证码
            else {
                alert("又不是手机号，又不是邮箱，诡异呀！\n" + str);
                return;
            }

            // 隐藏图片验证码输入界面
            jCaptcha.remove();
            $z.invoke(opt, "on_before_send", [], context);

            // 延迟一段时间才能再获取验证码
            holdVCodeButton();

            // 发送请求
            $.get(opt.url(sendBy), {
                d : opt.domain,
                a : str,
                s : opt.scene,
                t : token
            }, function(re){
                var reo = $z.fromJson(re);

                // 验证出错
                if(!reo.ok) {
                    $z.invoke(opt, "on_error", 
                        [reo.errCode + (reo.msg ? " : "+reo.msg : "")], 
                        context);
                }
                // 验证失败
                else if(!reo.data) {
                    jVCode.find("b em").empty();
                    $z.invoke(opt, "on_fail", [sendBy], context);
                }
                // 验证成功
                else {
                    $z.invoke(opt, "on_ok", [sendBy], context);
                }

            });

        });  // ~ 事件:继续验证

        //------------------------------------------------
        // 刷新图片验证码
        jCaptcha.on("click", '.vcm-reload', function(){
            jCaptcha.find("img").attr("src", opt.src(str));
        });

        //------------------------------------------------
        // 放弃图片验证码
        jCaptcha.on("click", '.vcm-cancel', function(){
            jCaptcha.remove();
        });

    });  // ~ 显示图片验证码输入框

    //------------------------------------------------
    // 验证码输入框的输入
    jVCode.on("keyup", "input", function(){
        // 没必要再显示错误信息了
        if("invalid" == jVCode.attr("mode"))
            jVCode.removeAttr("mode");

        // 确保是去掉空白，并且小写的
        var jInput = $(this);
        var str = $.trim(jInput.val()).toLowerCase();
        jInput.val(str);

        // 回调通知验证码输入了内容
        $z.invoke(opt, "on_change", [str], jInput);
    });

}});
// 结束 jQuery 插件
/*------------------------------------------------
 * 插件: 控制注册/密码类表单主体，用法
 {
    domain : "site0",    // 指定的域
    scene  : "signup",   // 场景，比如 signup 
    context : jQuery     // 所有回调函数的 this，默认为当前选区元素 

    // 回调
    on_tooshort : {c}F(jq, str)   // 用户名太短
    on_invalid : {c}F(jq, str)    // 用户名非法
    on_error : {c}F(jq, str)      // 其他错误
    on_exists   : {c}F(jq, str, sendBy)   // 用户存在
    on_noexists : {c}F(jq, str, sendBy)   // 用户不存在
    on_submit : {c}F(jq, params, sendBy)  // 执行提交
 }
 其中 
 - sendBy 为 "account|email|phone"
 - params 格式为 
 {
    domain : "site0",   // 传入的 opt.domain
    str    : "xxxx",    // 账号（用户名，手机或邮箱）
    vcode  : "xxxx",    // 验证码
    passwd : "xxxx,     // 密码
 }
 - jq 为一个表单内关键元素的集合:
 {
    $form    : jQuery,       // 表单本身
    $name    : jQuery,       // 账户名的包裹 DIV
    $passwd  : jQuery,       // 密码的包裹 DIV
    $passre  : jQuery,       // 二次密码的包裹 DIV
    $vcode   : jQuery,       // 验证码的包裹 DIV
    $tip     : jQuery,       // 提示信息区的包裹 DIV
    $tipInfo : jQuery,       // 提示信息的文字区域元素
    $submit  : jQuery,       // 按钮区的包裹 DIV
    //..........................................
    // 显示验证码
    //  str         : 账号
    //  title       : 验证码提示文字
    //  placeholder : 验证码输入框占位文字 
    show_vcode(str, title, placeholder)
}
 */
$.fn.extend({"passwdform": function(opt){
    // 得到回调上下文
    var context = opt.context || this;

    // 设置默认值
    $z.setUndefined(opt, "url_exists", "/u/exists");

    // 一些关键 DOM 元素
    var jq = {
        $form    : this,
        $name          : this.find(".form-name"),
        $nameInput     : this.find(".form-name input"),
        $tip           : this.find('.form-tip'),
        $tipInfo       : this.find(".form-tip .re-info"),
        $vcode         : this.find('.form-vcode'),
        $passwd        : this.find(".form-passwd"),
        $passwdInput   : this.find(".form-passwd input"),
        $passre        : this.find(".form-passre"),
        $passreInput   : this.find(".form-passre input"),
        $submit        : this.find(".form-btn"),
        $submitButton  : this.find(".form-btn button"),
    };
    //------------------------------------------------
    function sync_form_state(str) {
        //...............................................
        // 默认不需要验证码的输入
        var vcodeIsOk = true;
        //...............................................
        // 验证码区域如果显示的话，则必须有值
        if(jq.$vcode.attr("show")){
            vcodeIsOk = $.trim(jq.$vcode.find("input").val()) ? true : false;
        }
        //...............................................
        // 控制提交按钮的状态
        if(vcodeIsOk
            && (jq.$name.length == 0   || "ok" == jq.$name.attr("mode"))
            && (jq.$passwd.length == 0 || "ok" == jq.$passwd.attr("mode"))
            && (jq.$passre.length == 0 || "ok" == jq.$passre.attr("mode"))) {
            jq.$submitButton.prop("disabled", false);
        }
        // 灰掉提交按钮
        else {
            jq.$submitButton.prop("disabled", true);
        }
    }
    //------------------------------------------------
    // 显示图片验证码
    jq.show_vcode = function(str, title, placeholder) {
        this.$vcode.attr("show", "yes").vcode({
            domain      : opt.domain,
            scene       : opt.scene,
            account     : str,
            title       : title,
            placeholder : placeholder,
            src : function(str){
                return "/u/vcode/captcha/get?d=" 
                        + opt.domain 
                        + "&a=" + str
                        + "&_=" + Date.now();
            },
            url : function(sendBy) {
                return "/u/vcode/" + sendBy + "/get";
            },
            on_change : function(){
                sync_form_state();
            },
            on_before_send : function(){
                jq.$tip.attr("mode","ing");
                jq.$tipInfo.text("正在获取验证码...");
            },
            on_ok : function(sendBy) {
                jq.$tip.attr("mode","ok");
                jq.$tipInfo.text(({
                    "phone" : "验证码已经发送您的手机，请掏出手机查看，通常1分钟内您会收到短信",
                    "email" : "验证码已经发送到您的邮箱，请登录您的邮箱查看，通常10分钟内您会收到邮件",
                })[sendBy]);
            },
            on_fail : function() {
                jq.$tip.attr("mode","warn");
                jq.$tipInfo.text("图片验证码输入失败");
            },
            on_error : function(errMsg){
                jq.$tip.attr("mode","warn");
                jq.$tipInfo.text(errMsg);
            }
        });
    };
    //------------------------------------------------
    // 检查二次密码框状态
    function check_passre(){
        var passwd = jq.$passwdInput.val();
        var passre = jq.$passreInput.val();
        
        // 空密码
        if(!passre) {
            jq.$passre.removeAttr("mode");
        }
        // 两次输入不一致
        else if(passwd != passre) {
            jq.$passre.attr("mode", "invalid");
        }
        // 没毛病
        else {
            jq.$passre.attr("mode", "ok");
        }

        // 最后同步一下表单状态
        sync_form_state();
    }
    //------------------------------------------------
    // 确保清除原先的事件监听
    jq.$form.off();

    //------------------------------------------------
    // 监控用户名输入的改变
    jq.$form.on("change", ".form-name input", function(){
        // 强制小写字母
        var jInput = $(this);
        var jName  = jInput.parent();
        var str = $.trim(jInput.val()).toLowerCase();
        $(this).val(str);
        // 首先恢复表单状态
        jq.$name.removeAttr("mode");
        jq.$tip.removeAttr("mode");
        jq.$vcode.removeAttr("show");
        $z.invoke(opt, "on_blank", [jq], context);
        sync_form_state();
        // 如果空字符串，啥也不做 
        if(!str) {
            $z.blinkIt(jq.$name);
        }
        // 否则检查是否存在
        else {
            $.get(opt.url_exists, {str:str}, function(re){
                var reo = $z.fromJson(re);

                // 判断账号的类型
                var sendBy;
                // 手机号
                if(/^[0-9+-]{11,20}$/.test(str)){
                    sendBy = "phone";
                }
                // 邮箱
                else if(/^[0-9a-zA-Z_.-]+@[0-9a-zA-Z_.-]+.[0-9a-zA-Z_.-]+$/
                        .test(str)){
                    sendBy = "email";
                }
                // 默认是普通账号
                else {
                    sendBy = "account";
                }

                //console.log(sendBy)

                // 用户名太短
                if("e.usr.loginstr.tooshort" == reo.errCode) {
                    $z.invoke(opt, "on_tooshort", [jq, str, sendBy], context);
                }
                // 用户名非法
                else if("e.usr.loginstr.invalid" == reo.errCode){
                    $z.invoke(opt, "on_invalid", [jq, str, sendBy], context);
                }
                // 其他错误
                else if(!reo.ok) {
                    $z.invoke(opt, "on_error", [jq, re], context);
                }
                // 用户存在
                else if(reo.data){
                    $z.invoke(opt, "on_exists", [jq, str, sendBy], context);
                }
                // 可用
                else {
                    $z.invoke(opt, "on_noexists", [jq, str, sendBy], context);
                }

                // 最后确保验证码部分的显示/隐藏状态正确
                sync_form_state();
            });
        }
    });
    
    //------------------------------------------------
    // 切换密码的隐藏显示
    jq.$form.on("click", ".form-passwd > span", function(){
        $z.toggleAttr(jq.$passwd, "eye-on", "yes");
        // 明文显示密码
        if(jq.$passwd.attr("eye-on")) {
            jq.$passwdInput.prop("type", "text");
            jq.$passreInput.prop("type", "text");
        }
        // 隐藏密码
        else {
            jq.$passwdInput.prop("type", "password");
            jq.$passreInput.prop("type", "password");
        }
    });

    //------------------------------------------------
    // 监控密码的修改
    jq.$form.on("keyup", ".form-passwd input", function(){
        var passwd = $(this).val();
        var level = $z.evalPassword(passwd, 6);

        // 空白密码
        if(level == 0) {
            jq.$passwd.removeAttr("mode");
        }
        // 密码太短
        else if(level == -1) {
            jq.$passwd.attr("mode", "short");
        }
        // 错误的密码
        else if(level < -1) {
            jq.$passwd.attr("mode", "invalid");
        }
        // 正确的密码，并显示指示器强度
        else {
            jq.$passwd.attr("mode", "ok");
            var jLi = jq.$passwd.find(".fpwd-level li")
                .removeAttr("on").removeAttr("on-mark");
            for(var i=0;i<level;i++){
                jLi.eq(i).attr("on", "yes");
            }
            jLi.eq(level-1).attr("on-mark", "yes");
        }

        // 最后同步二次密码框
        check_passre();
    });

    //------------------------------------------------
    // 监控二次输入的密码修改
    jq.$form.on("keyup", ".form-passre input", check_passre);

    //------------------------------------------------
    // 提交表单 
    jq.$form.on("click", ".form-btn button", function(e){
        // 阻止默认行为
        e.preventDefault();

        // 删除自己的错误标识
        jq.$submit.removeAttr("mode");

        // 得到表单对象
        var params = {
            domain : opt.domain,
            str    : $.trim(jq.$name.find('input').val()),
            vcode  : $.trim(jq.$vcode.find('input').val()),
        };
        if(jq.$passwd.length > 0) {
            params.passwd = $.trim(jq.$passwd.find('input').val())
        }
        //console.log(params)
        jq.$submitButton.prop("disabled", true);
        $z.invoke(opt, "on_submit", [jq, params, function(){
            jq.$submitButton.prop("disabled", false);
        }], context);
    });
}});
/**
 *------------------------------------------------
 * 登录插件
 {
    domain    : "site0",    // 指定的域
    context   : jQuery      // 所有回调函数的 this，默认为当前选区元素
    //.........................
    // 执行提交
    // 其中 callback 函数表示主动回调用来完成一次提交的后续处理
    // 其实就是用来恢复按钮状态
    on_submit : {c}F(jq, params, callback)
 }
 其中 
 - params 格式为 
 {
    nm     : "xxxx",    // 账号（用户名，手机或邮箱）
    passwd : "xxxx,     // 密码
 }
 - jq 为一个表单内关键元素的集合:
 {
    $form          : jQuery,       // 表单本身
    $name          : jQuery,       // 账户名的包裹 DIV
    $nameInput     : jQuery,       // 账户名的输入框
    $passwd        : jQuery,       // 密码的包裹 DIV
    $passwdInput   : jQuery,       // 密码的输入框
    $submit        : jQuery,       // 按钮区的包裹 DIV
    $submitButton  : jQuery,       // 提交按钮
    //..........................................
    // 得到表单的值
    getData() : {
        nm : "xxx",
        passwd : "xxx"
    }
}
*/
$.fn.extend({"login": function(opt){
    // 得到回调上下文
    var context = opt.context || this;
    // 得到关键的 DOM 元素 
    var jq = {
        $form          : this,
        $name          : this.find(".form-name"),
        $nameInput     : this.find(".form-name input"),
        $passwd        : this.find(".form-passwd"),
        $passwdInput   : this.find(".form-passwd input"),
        $tip           : this.find('.form-tip'),
        $tipInfo       : this.find(".form-tip .re-info"),
        $submit        : this.find(".form-btn"),
        $submitButton  : this.find(".form-btn button"),
        // 得到表单信息
        getData : function(){
            return {
                nm     : this.$nameInput.val(),
                passwd : this.$passwdInput.val(),
            };
        }
    };

    //------------------------------------------------
    // 确保清除原先的事件监听
    jq.$form.off();

    //------------------------------------------------
    // 监控输入的改变
    jq.$form.on("keyup", "input", function(e){
        var params = jq.getData();
        // 启用按钮
        if(params.nm && params.passwd) {
            jq.$submitButton.prop("disabled", false);
        }
        // 禁用按钮 
        else {
            jq.$submitButton.prop("disabled", true);
        }
        // 如果敲了回车，模拟点击
        if(13 == e.which) {
            jq.$submitButton.click();
            return;
        }
    });

    //------------------------------------------------
    // 提交注册请求
    jq.$form.on("click", ".form-btn button", function(e){
        // 阻止默认行为
        e.preventDefault();

        // 删除自己的错误标识
        jq.$passwd.removeAttr("mode");

        // 得到表单对象
        var params = jq.getData();

        // 标识
        jq.$submitButton.prop("disabled", true);
        jq.$tip.attr("mode","ing");
        jq.$tipInfo.text("正在登陆 ...")

        // 调用回调
        $z.invoke(opt, "on_submit", [jq, params, function(){
            jq.$submitButton.prop("disabled", false);
        }], context);
    });
}});
//------------------------------------------------
})(window.jQuery, window.NutzUtil);