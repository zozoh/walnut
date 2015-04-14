(function(){
//===================================================================
var parse_dom = function(map, li, jq){
    li.$ele = jq;
    li.name = $.trim(jq.attr("layout-name")) || null;
    if(li.name) {
        map[li.name] = jq;
    }
    li.val  = $.trim(jq.attr("layout-val")) || null;
    if(li.val){
        // 百分比，不能超过 100%
        if(/^[0-9]+%$/.test(li.val)){
            li.val = li.val.substring(0,li.val.length-1) * 1 / 100;
            if(li.val>1) {
                console.log("invalid layout-val : '" + li.val*100 + "%'");
                li.val = 1.0;
            }
        }
        // 数字表示精确像素
        else if(/^[0-9]+$/.test(li.val)){
            li.val = li.val * 1;
        }
        // 其他必须是 *
        else if("*"!=li.val){
            console.log("invalid layout-val : '" + li.val + "'");
            li.val = "*";
        }
    }else{
        li.val = "$SELF";   // 表示保留元素原始的尺寸
    }

    // 递归 ...
    var mode = $.trim(jq.attr("layout-mode"));
    if(mode){
        li.mode = mode;
        li.children = [];
        jq.children().each(function(){
            var sub = {};
            var me = $(this);
            parse_dom(map, sub, me);
            li.children.push(sub);
            // 所有的区块外边距必须为 0
            me.css({
                "margin" : 0,
                "position" : "relative"
            });
            if("horizontal" == mode){
                me.css({
                    "top":0,
                    "position" : "absolute"
                });
            }
            if("$SELF" == sub.val){
                sub.val = "horizontal" == mode ? 
                            me.outerWidth()
                             : me.outerHeight();
            }
        });
    }
};
//===================================================================
var UI = {
    init : function(options) {
        //console.log("I am layout init");
        
        // 绑定监听事件
        this.listenModel("ui:ready", this.layout.on_ready);
        
        // 分析 DOM 得到布局信息
        this.regions = {}
        this.regions.$root = {};
        parse_dom(this.regions, this.regions.$root, this.arena);

    },
    //...............................................................
    events : {
        // "keypress .ui-console-inbox"     : on_keypress_at_inbox,
        // "keydown  .ui-console-inbox"     : on_keydown_at_inbox
    },
    //...............................................................
    // 递归调整自己的布局
    resize : function(W, H){
        this.layout._adjust_layout(this.regions.$root, W, H);
    },
    //...............................................................
    layout : {
        on_ready : function(){
            console.log("I am layout ready");
        },
        // 在一块区域内( W * H )，为 li.children 分配宽高
        _adjust_layout : function(li, W, H) {
            if(!li.children || li.children.length<=0)
                return;
            // 要分配的值
            var max = "vertical" == li.mode ? H : W;
            var remain = max;

            // 循环分配值
            var vals = [];
            var remainIndex = [];
            for(var i=0;i<li.children.length;i++){
                var sub = li.children[i];
                if("*" == sub.val){
                    vals[i] = 0;
                    remainIndex.push(i);
                } else if(_.isNumber(sub.val)){
                    if(sub.val<1){
                        vals[i] = parseInt(max * sub.val);
                        remain-=vals[i];
                    }else{
                        vals[i] = sub.val;
                        remain-=vals[i];
                    }
                } else {
                    throw "invalid sub.val : " + sub.val;
                }
            }
            // 分配剩余值
            if(remainIndex.length>0){
                var n = parseInt(remain/remainIndex.length);
                var i=0;
                for(;i<remainIndex.length;i++){
                    vals[remainIndex[i]] = n;
                }
                remain -= n*remainIndex.length;
                // 继续分配每个剩余的像素
                i = 0;
                while(remain>0){
                    var index = remainIndex[i++];
                    vals[index]++;
                    remain--;
                    if(i>=remainIndex.length)
                        i=0;
                }
            }

            // 垂直布局
            if("vertical" == li.mode){
                for(var i=0;i<li.children.length;i++){
                    var sub = li.children[i];
                    sub.area = {w:W, h:vals[i]};
                    sub.$ele.css({
                        "width"  : W, 
                        "height" : vals[i]
                    });
                }
            }
            // 水平布局
            else{
                var left = 0;
                for(var i=0;i<li.children.length;i++){
                    var sub = li.children[i];
                    sub.area = {w:vals[i], h:H};
                    sub.$ele.css({
                        "left"   : left,
                        "width"  : vals[i], 
                        "height" : H
                    });
                    left += sub.area.w;
                }
            }

            // 处理自己的下一层
            for(var i=0;i<li.children.length;i++){
                var sub = li.children[i];
                if(sub.mode) {
                    this._adjust_layout(sub, sub.area.w, sub.area.h);
                }
            }
        }
    }
    //...............................................................
};
//===================================================================
var ZLayout = {
    def : function(uiName, conf) {
        var options = _.extend({}, UI);
        // ....................................................
        ZUI.merge_funcs(conf, UI, "init");
        ZUI.merge_funcs(conf, UI, "resize");
        ZUI.merge_funcs(conf, UI, "depose");
        ZUI.merge_map(conf.events, UI.events);
        ZUI.merge_map(conf._ui, UI._ui);
        // ....................................................
        // 最后扩展
        $z.extend(options, conf);
        // 返回界面定义
        return ZUI.def(uiName, options)
    }
};
//===================================================================
// 添加到 window
window.ZLayout = ZLayout;
//===================================================================
// 支持 Require.js
if(require === requirejs) {
    define(['ui/zui'], function(ZUI){
        return ZLayout;
    });    
}
//===================================================================
})();