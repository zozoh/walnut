(function($, $z){
//...........................................................
$.fn.extend({ "navmenuToggleArea" : function(opt){

    // 记录自己
    var jq = this;

    // 找到自己对应的区域
    var jLayout = $(opt.target);
    
    console.log("hahaha")
    
    // 如果找到了，就启用
    if(jLayout.length > 0) {
        // 找到自己当前高亮的项目
        var areaId = jq.find('li[toar-checked]').attr("toar-id");
        
        // 隐藏区域
        var jArena = jLayout.find('>.hm-com-W>.ui-arena');
        
        // 处理函数 
        var doToggle = function(areaId){
            jArena.children('[toggle-mode="show"]')
                .attr("toggle-mode","hide");
            jArena.children('[area-id="'+areaId+'"]')
                .attr("toggle-mode","show");
        };
        
        // 将高亮项目对应的区域显示
        if(areaId) {
            doToggle(areaId);
        }
        
        // 监听事件，以便切换项目
        jq.on("click", 'li[toar-id]', function(){
            jq.find('li').attr({
                "toar-checked" : null,
                "current"      : null,
            });
                
            var areaId = $(this).attr({
                "toar-checked" : "yes",
                "current"      : "yes",
            }).attr("toar-id")
            
            // 隐藏区域
            doToggle(areaId);
        });
    }
        
    // 返回自身以便链式赋值
    return this;
}});
//...........................................................
})(window.jQuery, window.NutzUtil);

