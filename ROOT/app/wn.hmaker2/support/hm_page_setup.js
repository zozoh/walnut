define(function (require, exports, module) {
var methods = {
    //...............................................................
    _H(jHead, selector, html) {
        var jq = jHead.children(selector);
        // 确保存在
        if(html){
            if(jq.size() == 0){
                jHead.prepend($(html));
            }
        }
        // 确保删除
        else {
            jq.remove();
        } 
    },
    //...............................................................
    __setup_page_head : function() {
        var UI = this;

        // 首先清空
        var jHead = UI._C.iedit.$head.empty();

        // 头部元数据
        UI._H(jHead, 'meta[charset="utf-8"]',
            '<meta charset="utf-8">');
        UI._H(jHead, 'meta[name="viewport"]',
            '<meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=0, minimum-scale=1.0, maximum-scale=1.0">');
        UI._H(jHead, 'meta[http-equiv="X-UA-Compatible"]',
            '<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">');
        

        // 链入固定的 CSS 
        UI._H(jHead, 'link[href*="balloon.min.css"]',
            '<link rel="stylesheet" type="text/css" href="/gu/rs/core/css/balloon.min.css">');
        UI._H(jHead, 'link[href*="normalize.css"]',
            '<link rel="stylesheet" type="text/css" href="/gu/rs/core/css/normalize.css">');
        UI._H(jHead, 'link[href*="font-awesome.css"]',
            '<link rel="stylesheet" type="text/css" href="/gu/rs/core/css/font-awesome-4.5.0/css/font-awesome.css">');
        UI._H(jHead, 'link[href*="material-design-iconic-font.css"]',
            '<link rel="stylesheet" type="text/css" href="/gu/rs/core/css/font-md/css/material-design-iconic-font.css">');
        UI._H(jHead, 'link[href*="hmaker_editing.css"]',
            '<link rel="stylesheet" type="text/css" href="/a/load/wn.hmaker2/hmaker_editing.css">');
        UI._H(jHead, 'link[href*="moveresizing.css"]',
            '<link rel="stylesheet" type="text/css" href="/theme/r/jqp/moveresizing/moveresizing.css">');

        // _H(jHead, 'script[src*="zutil.js"]',
        //     '<script src="/gu/rs/core/js/nutz/zutil.js"></script>');
        // _H(jHead, 'script[src*="seajs"]',
        //     '<script src="/gu/rs/core/js/seajs/seajs-2.3.0/sea-debug.js" id="seajsnode"></script>');
        // _H(jHead, 'script[src*="jquery-2.1.3"]',
        //     '<script src="/gu/rs/core/js/jquery/jquery-2.1.3/jquery-2.1.3.min.js"></script>');

        // 设置皮肤
        UI.doChangeSkin();
    },
    //...............................................................
    __setup_page_events : function() {
        var UI = this;

        // 首先所有元素的点击事件，全部禁止默认行为
        UI._C.iedit.$root.on("click", "*", function(e){
            e.preventDefault();
            //console.log("hm_page.js: click", this.tagName, this.className, e.target);

            var jq = $(this);

            // 如果点在了块里，激活块，然后就不要冒泡了
            if(jq.hasClass("hm-com")){
                e.stopPropagation();
                
                // 已经激活就不再激活了
                if(jq.attr("hm-actived"))
                    return;
                
                // 得到组件的 UI
                var uiCom = ZUI(jq);
                
                console.log("uiCom", uiCom);
                
                // 快速切换页面的时候会出现异步的问题
                // 防守一道
                if(!uiCom)
                    return;
                            
                // 通知激活控件
                uiCom.notifyActived();
                
                // 通知改动
                uiCom.notifyBlockChange("page", uiCom.getBlock());
                uiCom.notifyDataChange("page", uiCom.getData());
            }
            // 如果点到了 body，那么激活页
            else if('BODY' == this.tagName){
                UI.fire("active:page", UI._page_obj);
            }

            // 不管怎样，模拟一下父框架页面的点击
            // $(document.body).click();
        });

        // 截获所有的键事件
        UI._C.iedit.$body.on("keydown", function(e){
            // 删除
            if(8 == e.which || 46 == e.which) {
                UI.deleteCom(UI.getActivedCom());
                UI.fire("active:page", UI._page_obj);
            }
        });
        UI._C.iedit.$body.on("keyup", function(e){
            // Shift 将表示开关移动遮罩的  no-drag
            if(16 == e.which) {
                var mvMask = UI._C.iedit.$body.children(".pmv-mask");
                if(mvMask.length > 0) {
                    $z.toggleAttr(mvMask, "no-drag", "yes");
                }
            }
        });
    },
    //...............................................................
    __setup_page_moveresizing : function() {
        var UI = this;
        
        UI._C.iedit.$body.pmoving({
            trigger    : '.hm-com',
            maskClass  : 'hm-page-move-mask',
            autoUpdateTriggerBy : null,
            findTrigger : function(e) {
                var jq    = $(e.target);
                // 辅助节点
                var jAi   = jq.closest(".hmc-ai");
                if(jAi.length > 0)
                    return jAi;
                // inflow 的不能移动
                if('inflow' == jq.closest(".hm-com").attr("hmc-mode"))
                    return null;
                // 可以移动
                return $(this);
            },
            findDropTarget : function(e) {
                // 只有拖动手柄的时候才会触发
                var jAi  = this.$trigger;
                
                if("H" == jAi.attr("m")) {
                    var jAreaCons = UI._C.iedit.$body.find(".hm-area-con");
                                        
                    // 当前控件所在的区域
                    var jMyArea = jAi.closest(".hm-area");
                    var eMyArea = jMyArea.length > 0 ? jMyArea[0] : null;
                    
                    // 当前控件（如果是布局控件）包含的区域先找一下
                    // 这些区域也要过滤到
                    var jCom = jAi.closest(".hm-com");
                    var jSubAreas = jCom.find(".hm-area-con");
                    var eSubs = Array.from(jSubAreas);
                    
                    // 准备要返回的区域列表
                    var eles = [];
                    
                    // 挨个查找：叶子区域，且不包含当前控件的，统统列出来
                    UI._C.iedit.$body.find(".hm-area-con").each(function(){
                        if(eMyArea != this
                           && eSubs.indexOf(this) < 0
                           && $(this).find(".hm-area-con").length == 0) {
                            eles.push(this);
                        }
                    });
                    
                    // 返回
                    return $(eles);
                }
            },
            on_begin : function(e) {
                // 得到组件顶部节点元素
                var jCom  = this.$trigger.closest(".hm-com");
                var uiCom = ZUI(jCom);
                //...........................................................
                // 这个对象描述了手柄模式的计算方式
                var HDLc = {
                    NW : ["left", "top"],
                    W  : ["left"],
                    SW : ["left", "bottom"],
                    N  : ["top"],
                    S  : ["bottom"],
                    NE : ["right", "top"],
                    E  : ["right"],
                    SE : ["right", "bottom"]
                };
                //......................................
                // 根据触发点类型不同，为上下文设置不同的处理函数
                if(this.$trigger.hasClass("hmc-ai")) {
                    var jAi = this.$trigger;
                    
                    // 得到辅助柄的类型
                    var m   = jAi.attr("m");
                    this.$mask.attr("mmode", m);
                    
                    // 移动控件的树的层级
                    if("H" == m && this.$drops) {
                        this.__is_for_drop = true;
                        this.__do_ing = function(pmvc) {
                            //console.log("drag", pmvc.rect.inview)
                        };
                        
                        // 处理每个拖放的目标的内容显示
                        var pmvc = this;
                        this.$drops.children().each(function(index){
                            var di = pmvc.dropping[index];
                            var jArea  = di.$ele.parents(".hm-area");
                            var areaId = jArea.attr("area-id");
                            $(`<div class="di-area"></div>`)
                                .text(areaId).appendTo(this);
                        });
                        
                        // 修改 trigger 的显示样式
                        this.$helper.html(uiCom.getIconHtml());
                    }
                    // 改变控件大小
                    else {
                        // 得到模式
                        var hdl_mode = HDLc[m];
                    
                        // 设置回调
                        this.__do_ing = function(pmvc) {
                            // 计算顶点
                            _.extend(pmvc.rect.com, 
                                $z.rectObj(pmvc.rect.trigger, hdl_mode));

                            // 重新计算矩形其他尺寸
                            $z.rect_count_tlbr(this.rect.com);
                        };
                    }
                }
                //......................................
                // 没有触发到特殊手柄，那么就表示移动自身
                else {
                    this.__do_ing = function(pmvc){
                        pmvc.$mask.attr("mmode", "move")
                        pmvc.rect.com = pmvc.rect.trigger;
                    };
                }
                //......................................
                this.uiCom = uiCom;
                this.comBlock = this.uiCom.getBlock();
                this.rect.com = $z.rect(jCom);
                //......................................
                // 确保这个控件是激活的
                if(!jCom.attr("hm-actived")){    
                    // 通知激活控件
                    this.uiCom.notifyActived();
                    this.uiCom.notifyBlockChange("page", this.uiCom.getBlock());
                    this.uiCom.notifyDataChange("page",  this.uiCom.getData());
                }
            },
            on_ing : function() {
                // 计算，如果返回 true 表示不要更新块的位置大小
                if(_.isFunction(this.__do_ing)
                   && !this.__do_ing(this)){
                    // 得到改变
                    var rect = $z.rect_relative(this.rect.com, 
                                                this.rect.viewport,
                                                true);
                    _.extend(this.comBlock, {
                        top:"",left:"",bottom:"",right:"",width:"",height:""
                    }, UI.pickCssForMode(rect, this.comBlock.posBy))
                    
                    // 通知修改，在 on_end 的时候会保存位置的
                    this.uiCom.notifyBlockChange(null, this.comBlock);
                }
            },
            // 移动结束，保存 Block 信息
            on_end : function() {
                // 这个拖动是修改位置，保存最后的位置
                if(!this.__is_for_drop) {
                    this.uiCom.setBlock(this.comBlock);
                }
            },
            // 拖拽到了一个目标，执行修改
            on_drop : function(jAreaCon) {
                console.log("drop to ", jAreaCon)
            }
        });
        
    },
    //...............................................................
}; // ~End methods
//====================================================================
// 输出
module.exports = function(uiCom){
    return _.extend(uiCom, methods);
};
//=======================================================================
});