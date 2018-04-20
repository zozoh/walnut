/**
Navmenu 控件的运行时行为:

opt : {
    // 来自编辑器
    "atype": "link|toggleArea",
    "layoutComId": null,
    "autoCurrent": true,
    "autoShowSub": true,

    // 下面是服务器端转换增加的属性
    "srcPath" : "/index",
    "srcName" : "index",
}
*/
(function($, $z){
//...........................................................
// 设置区域切换模式的行为
function setup_toggle(opt) {
    // 当前区域
    var jq = this;

    // 得到对应布局的控件元素
    var jLayout = $(opt.layoutComId);

    // 必须对应一个布局元素
    if(!jLayout.hasClass("hm-layout"))
        return;

    // 预先得到布局元素全部区域
    var jArena = jLayout.find('>.hm-com-W>.ui-arena');
    
    // 处理函数 
    var doToggle = function(areaId){
        jArena.children('[toggle-mode="show"]')
            .attr("toggle-mode","hide");
        jArena.children('[area-id="'+areaId+'"]')
            .attr("toggle-mode","show");
    };

    // 找到自己当前高亮的项目
    var areaId = jq.find('li[current]').attr("href");
        
    // 将高亮项目对应的区域显示
    if(areaId) {
        doToggle(areaId);
    }
    
    // 监听事件，以便切换项目
    jq.on("click", 'li[href]', function(){
        // 移除其他区域
        jq.find('li').removeAttr("current");
        
        // 显示自己对应的区域
        var areaId = $(this).attr("current", "yes").attr("href");
        doToggle(areaId);
    });
}
//...........................................................
// 命令模式
var CMD = {
    show_sub : function(jLi){
        // 得到菜单条顶级元素
        var jq = jLi.closest(".hmc-navmenu");
        var autoDock = jq.attr("auto-dock");

        // 移除其他菜单项的显示
        jq.find("li[open-sub]").not("[is-mouse-in]").removeAttr("open-sub");

        // 显示自己
        var jAns = jLi.parentsUntil(".ul-top", "li").andSelf();
        jAns.attr("open-sub", "yes");

        // 标识自己的父为高亮
        //jq.find("li[current]").removeAttr("current");
        //jLi.closest(".li-top").attr("current", "yes");

        // 自动停靠
        if(autoDock){
            console.log("haha")
            // 得到子菜单项
            var jUl = jLi.children("ul");

            // 除非明确指定，否则默认顶级菜单停靠在水平边
            if(jLi.hasClass("li-top") && /^H/.test(autoDock)){
                $z.dockIn(jLi, jUl, autoDock, true);
            }
            // 其他子菜单一律停靠在垂直边
            else {
                $z.dockIn(jLi, jUl, "V", true);
            }
        }
    },
    hide_sub : function(jLi){
        // 延迟 500 毫秒再做判断
        window.setTimeout(function(){
            if(!jLi.attr("is-mouse-in")){
                jLi.removeAttr("open-sub");
            }
        }, 100);
    }
};
//...........................................................
$.fn.extend({ "hmc_navmenu" : function(opt){
    // 得到自己所在控件
    var jq = this.closest(".hm-com-navmenu");
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~    
    // 区域切换模式
    if("toggleArea" == opt.atype) {
        setup_toggle.call(jq, opt);
    }
    // 默认是链接模式，则看看是否需要自动高亮
    else {
        if(opt.autoCurrent) {
            jq.find('li[current]').removeAttr("current");
            jq.find('li[href="'+opt.srcPath+'"]')
                .closest(".li-top").attr("current", "yes");
        }
    }
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // 监听事件
    if(opt.autoShowSub) {
        jq.on("mouseenter", "li[sub-item-nb]", function(){
            // 标识自己
            $(this).attr("is-mouse-in", "yes");
            // 执行显示
            CMD.show_sub($(this));
        });
        jq.on("mouseleave", "li[sub-item-nb]", function(e){
            // 标识自己
            $(this).removeAttr("is-mouse-in");
            // 执行隐藏
            CMD.hide_sub($(this));
        });
    }
    // 点击切换子菜单显示隐藏
    else {
        jq.on("click", "li[sub-item-nb]", function(e){
            if(!$(this).attr("open-sub")){
                e.stopPropagation();
                CMD.show_sub($(this));
            }
        });
        // 如果点击事件冒到了页面上, 关闭自己所有的菜单项
        $(document.documentElement).click(function(e){
            jq.find("li[open-sub]").removeAttr("open-sub");
        });
    }
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // 返回自身以便链式赋值
    return jq;
}});
//...........................................................
})(window.jQuery, window.NutzUtil);

