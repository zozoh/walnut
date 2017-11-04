(function($, $z){
//-----------------------------------------
$(function(){
    // 监视滚动
    $(window).scroll(function(){
        var jBody = $(document.body);
        // 到顶了，移除标识
        if($(window).scrollTop()<=10){
            jBody.removeAttr("show-sky");
        }
        // 确保添加了标识，显示 sky 条
        else if(!jBody.attr("show-sky")){
            jBody.attr("show-sky", "yes");
        }
    });
    // 点击回到顶部 
    $(".sky a").click(function(){
        // 动画滚动
        if($(window).scrollTop()>($z.winsz().height/1.5)) {
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
            scrollTop : 60
        }, 500);
    });
})
//-----------------------------------------
})(window.jQuery, window.NutzUtil);
