(function($, $z){
//..........................................................
var IS_RUNTIME = $('html[hmaker-runtime]').size() > 0;
//..........................................................
$.fn.extend({ "_std_th_show_obj" : function(obj, opt){
    var jData = this;

    //console.log(obj, opt)
    
    // 首先得到布局
    var str = opt.layout;

    // 默认布局
    if(!str) {
        str = ".th_nm";
    }

    // 解析布局
    var layout = HmRT.parseLayout(str);

    // 指定链接（包括封面图片和标题）
    var href = HmRT.explainHref(opt.href, obj);
    
    // 渲染
    HmRT.renderLayout(opt, jData, layout, obj, href);

    // 事件
    HmRT.setupLayoutEvents(opt, jData[0].ownerDocument.defaultView);

    // 模拟第一次点击
    jData.find(".wn-obj-layout ul[li-target]").each(function(){
        $(this).find("li").first().click();
    });
    
    // 返回自身以便链式赋值
    return this;
}});
//..........................................................
})(window.jQuery, window.NutzUtil);