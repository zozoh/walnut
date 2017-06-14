(function($){
//------------------------------------------------
// 绑定插件
$(function(){
    $('.main').login({
        domain : $(document.body).attr("domain"),
        on_submit : function(jq, params, callback){
            $.post("/u/do/login/ajax", params, function(re){
                var reo = $z.fromJson(re);
                // 登录成功: 自动跳转
                if(reo.ok) {
                    jq.$tip.attr("mode","ok");
                    jq.$tipInfo.text("登录成功，正在跳转 ...");
                    $z.openUrl("/", "_self");
                }
                // 登录失败 
                else {
                    
                        jq.$tip.attr("mode","warn");
                        jq.$tipInfo.text(reo.msg || reo.errCode);

                    // 处理回调
                    callback();
                }
            });
        }
    });
});
// 绑定各种事件
//------------------------------------------------
})(window.jQuery, window.NutzUtil);