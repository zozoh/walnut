(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker/component/hmc',
    'ui/menu/menu'
], function(ZUI, Wn, HMC, MenuUI){
//==============================================
var html = function(){/*
<textarea placeholder="{{hmaker.com.text.empty}}" spellcheck="false"></textarea>
*/};
//==============================================
return ZUI.def("app.wn.hmaker_com_text", {
    //...............................................................
    events : {
        "click .hmc-wrapper" : function(e){
            var jq   = $(e.currentTarget);
            var jCom = jq.closest(".hm-com");

            // 只有激活的控件才能编辑
            if(!jCom.attr("actived"))
                return;

            // 绝对位置
            if(jCom.attr("pos") == "absolute"){
                $z.editIt(jq);
            }
            // 相对位置的编辑
            else {
                $z.editIt(jq, {
                    multi : true,
                    extendHeight : true,
                    takePlace : true,
                });
            }
        }
    },
    //...............................................................
    checkDom : function(){
        var UI = this;
        var jW = UI.arena.find(".hmc-wrapper");
        if(!jW.text()){
            jW.text(UI.msg("hmaker.com.text.empty"));
        }
    },
    //...............................................................
    redraw : function(){
        var UI  = this;
        var opt = UI.options;

        // 确保 DOM 结构合法
        UI.checkDom();

        // 标题
        opt.$title.html(opt.titleHtml);

        // 属性
        console.log(UI.parent.uiName)
        
    },
    //...............................................................
    resize : function(){
        // var UI = this;

        // // 如果是固定尺寸，那就啥也别做了
        // if(UI.arena.attr("pos") == "absolute")
        //     return;

        // // 重新设定输出框的大小
        // var jq = this.arena.find("textarea");
        // console.log({
        //     "height" : jq.height(), 
        //     "outerHeight" : jq.outerHeight(),
        //     "scrollHeight" : jq[0].scrollHeight,
        //     "scrollTop" : jq.scrollTop(),
        //     "offsetHeight" : jq[0].offsetHeight,
        // })

        // jq.css("height", jq[0].scrollHeight + jq.scrollTop());
        // console.log({
        //     "after" : ":>",
        //     "height" : jq.height(), 
        //     "outerHeight" : jq.outerHeight()
        // })
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);