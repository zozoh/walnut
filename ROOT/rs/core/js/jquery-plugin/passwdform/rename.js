(function($){
//------------------------------------------------
// 绑定各种事件
$(function(){
    //------------------------------------------------
    // 提交表单 
    $(".form-btn button").on("click", function(e){
        // 阻止默认行为
        e.preventDefault();

        // 得到关键 DOM
        var jTip = $('.form-tip');
        var jTipInfo = jTip.find(".re-info");

        // 得到表单对象
        var params = {
            nm : $.trim($('.form-renm input').val()),
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
                jTip.attr("mode","warn");
                jTipInfo.text("用户名只能由数字，小写字母以及下划线组成,且不能小于四位");
            }
            // 已经存在
            else if("e.u.rename.exists" == reo.errCode) {
                jTip.attr("mode","warn");
                jTipInfo.text("这个用户已存在");
            }
            // 其他错误
            else {
                jTip.attr("mode","warn");
                jTipInfo.text(reo.errCode + (reo.msg ? " : "+reo.msg : ""));
            }
        });
    });
// 绑定各种事件
//------------------------------------------------
});
//------------------------------------------------
})(window.jQuery, window.NutzUtil);