define(function (require, exports, module) {
var methods = {
    //...............................................................
    // 添加 CSS
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
    syncAssistedMark : function(){
        if(this.isAssistedOff()){
            this._C.iedit.$body.attr({
                "assisted-off" : "yes",
                "assisted-on"  : null,
            });
        }else{
            this._C.iedit.$body.attr({
                "assisted-off" : null,
                "assisted-on"  : "yes",
            });
        }
    },
    //...............................................................
    setup_page_editing : function(){
        var UI = this;
        
        // 标识网页的编辑器模式
        UI._C.iedit.$root.attr({
            "hmaker"     : "2.0",
            "hmaker-ide" : "yes",
        });

        // 设置辅助线模式
        UI.syncAssistedMark();

        // 清理不必要的元素
        UI._C.iedit.$root
            .find(".hm-del-save, .ui-code-template, .ui-debug-mark, .ui-mask, .ui-loading")
                .remove();

        //.......................... 下面的方法来自 support/hm_page_setup.js
        // 设置编辑区页面的 <head> 部分
        UI.__setup_page_head();

        // 应用网页显示样式
        UI.applyPageAttr();

        // 设置编辑区的移动
        UI.__setup_page_moveresizing();

        // 监视编辑区，响应其他必要的事件处理
        UI.__setup_page_events();
        //.......................... 上面的方法来自 support/hm_page_setup.js

        // 处理所有的块显示
        // UI._C.iedit.$body.find(".hm-com").each(function(){
        //     // 处理块中的组件
        //     var jCom = $(this);
        //     // 绑定 UI，并显示
        //     UI.bindComUI(jCom);
        // });
        // 缓存组件
        var cache  = {};
        var homeId = UI.getHomeObjId();
        // 寻找组件列表
        var jComs = UI._C.iedit.$body.find(".hm-com");
        var i = 0;
        var do_bind_com = function(index) {
            if(index < jComs.length) {
                var jCom = $(jComs[index]);
                // 加载共享库组件
                if(jCom.attr("lib")){
                    UI.reloadLibCode(jCom, homeId, cache, function(uiCom){
                        do_bind_com(index + 1);
                    });
                } 
                // 普通控件
                else {
                    UI.bindComUI(jCom, function(){
                        do_bind_com(index + 1);
                    });
                }
            }
            // 如果全部的控件都加载完毕了，做一下标识，会自动执行延迟的处理
            else {
                UI.markReadyForEdit(true);
                // 对所有的组件，重新应用一遍 block
                UI._C.iedit.$body.find(".hm-com").each(function(){
                    ZUI(this).applyBlock();
                });
            }
        };
        do_bind_com(0);

        // 通知网页被加载
        UI.fire("active:page");

        // 更新皮肤
        UI.doChangeSkin();

        // 模拟点击
        // window.setTimeout(function(){
        //     UI._C.iedit.$body.find(".hm-com").first().click();
        // }, 500);
    },
    //...............................................................
    // 标识自己是否可以被编辑（即所有的组件都加载完毕）
    // 一般来说，是加载一个网页前，要标识 false
    // 所有组件预处理完毕后，标识 true
    // 标识 false 的时候，会显示一个罩罩，把网页遮住，这样可以掩盖一些丑丑的中间过程
    // 嗯，就是这样
    markReadyForEdit : function(is_ready_for_edit) {
        var UI = this;
        UI.__is_ready_for_edit = is_ready_for_edit ? true : false;

        // 标识完成了的话 ...
        if(is_ready_for_edit) {
            //console.log("done!!!!");
            // 执行后续
            if(_.isArray(UI.__delay_queue))
                for(var i=0; i<UI.__delay_queue.length; i++) {
                    UI.__delay_queue[i]();
                }
            // 最后移除页面的标志
            window.setTimeout(function(){
                // 移除标志
                UI.arena.find(".hmpg-stage").removeAttr("hm-page-preparing");

                // 确保通知一遍所有控件的 resize
                UI.resize(true);
            }, 0);
        }
        // 标识正在准备页面控件
        else {
            UI.arena.find(".hmpg-stage").attr("hm-page-preparing", true);
        }
    },
    isReadyForEdit : function(){
        return this.__is_ready_for_edit;
    },
    //...............................................................
    // 给入一个函数，如果当前页面已经准备完成了，就立即执行，否则推入暂存堆栈
    // 等页面预处理完毕后再执行
    delayWhenReadyForEdit : function(callback) {
        var UI = this;
        if(UI.isReadyForEdit()){
            callback();
        }
        // 记入读取延迟队列
        else {
            UI.__delay_queue = UI.__delay_queue || [];
            UI.__delay_queue.push(callback);
        }

    },
    //...............................................................
    // 添加 JS
    // _HScript(doc, src, attrs) {
    //     var eScript = doc.createElement("script");
    //     eScript.type = "text/javascript";
    //     eScript.src = src;
    //     if(attrs){
    //         $(eScript).attr(attrs);
    //     }
    //     doc.body.appendChild(eScript);
    // },
    //...............................................................
    __setup_page_head : function() {
        var UI = this;

        // 首先清空
        var jHead = UI._C.iedit.$head;

        // 头部元数据
        UI._H(jHead, 'meta[charset="utf-8"]',
            '<meta charset="utf-8">');
        UI._H(jHead, 'meta[name="viewport"]',
            '<meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=0, minimum-scale=1.0, maximum-scale=1.0">');
        UI._H(jHead, 'meta[http-equiv="X-UA-Compatible"]',
            '<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">');
        

        // 链入固定的 CSS 
        UI._H(jHead, 'link[href*="balloon.min.css"]',
            '<link rel="stylesheet" type="text/css" class="hm-del-save" d href="/gu/rs/core/css/balloon.min.css">');
        UI._H(jHead, 'link[href*="normalize.css"]',
            '<link rel="stylesheet" type="text/css" class="hm-del-save" href="/gu/rs/core/css/normalize.css">');
        UI._H(jHead, 'link[href*="font-awesome.css"]',
            '<link rel="stylesheet" type="text/css" class="hm-del-save" href="/gu/rs/core/css/font-awesome-4.5.0/css/font-awesome.css">');
        UI._H(jHead, 'link[href*="material-design-iconic-font.css"]',
            '<link rel="stylesheet" type="text/css" class="hm-del-save" href="/gu/rs/core/css/font-md/css/material-design-iconic-font.css">');
        UI._H(jHead, 'link[href*="hmaker_editing.css"]',
            '<link rel="stylesheet" type="text/css" class="hm-del-save" href="/a/load/wn.hmaker2/hmaker_editing.css">');
        UI._H(jHead, 'link[href*="moveresizing.css"]',
            '<link rel="stylesheet" type="text/css" class="hm-del-save" href="/theme/r/jqp/moveresizing/moveresizing.css">');

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
        })
        // 如果点在了控件里，激活
        .on("click", ".hm-com", function(e){
            e.stopPropagation();
            var jCom = $(this);
            // 如果点在了控件里，激活
            //console.log("isCom!!!", jCom)
            // !!!
            // 错误的组件
            if(jCom.attr("invalid-lib")) {
                var comId   = jCom.attr("id");
                var libName = jCom.attr("lib");
                var msg = UI.msg("hmaker.lib.confirm_del_invalid") 
                            + ':<em>' + libName + '</em>'
                            + ' <b>(#' + comId + ')</b>'

                // 询问用户
                UI.confirm(msg, function(){
                    jCom.remove();
                })
                return;
            }
            
            // 已经激活就不再激活了
            if(jCom.attr("hm-actived"))
                return;
            
            // 如果控件再一个 highlight-mode 的布局里面
            var jHMLayout = jCom.closest(".hm-layout[highlight-mode]");
            if(jHMLayout.length > 0) {
                // 控件不在高亮区域内
                // 那么应该激活这个布局，
                if(jCom.closest('.hm-area[highlight]').length == 0)
                    jCom = jHMLayout;
            }
            
            // 得到组件的 UI
            var uiCom = ZUI(jCom);
            
            //console.log("uiCom", uiCom.uiName, uiCom.$el);
            
            // 快速切换页面的时候会出现异步的问题
            // 防守一道
            if(!uiCom)
                return;
                        
            // 通知激活控件
            uiCom.notifyActived("page");
        })
        // 激活页面
        .on("click", function(e){
            if(e.target === UI._C.iedit.body 
                || e.target === UI._C.iedit.root) {
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

        // 阻止页面的 form 提交
        UI._C.iedit.$body.on("submit", "form", function(e){
            e.preventDefault();
        });

        // 绑定 resize 事件
        if(!UI._C.iedit.doc.defaultView.hm_resize_binded){
            UI._C.iedit.doc.defaultView.hm_resize_binded = true;
            $(UI._C.iedit.doc.defaultView).resize(function(){
                UI.invokeSkin("resize");
            });
        }
    },
    //...............................................................
    invokeSkin : function(method){
        var UI = this;
        //console.log("invokeSkin", method, UI._C ? UI._C.SkinJS : "!No UI._C");
        if(UI._C && UI._C.SkinJS){
            $z.invoke(UI._C.SkinJS, method, [], {
                mode   : "IDE",
                doc    : UI._C.iedit.doc,
                win    : UI._C.iedit.doc.defaultView,
                root   : UI._C.iedit.root,
                jQuery : window.jQuery,
            });
        }
    },
    //...............................................................
    __setup_page_moveresizing : function() {
        var UI = this;
        
        UI._C.iedit.$body.pmoving({
            trigger    : '.hm-com',
            maskClass  : 'hm-page-move-mask',
            autoUpdateTriggerBy : null,
            sensorSize : 30,
            compactDropsRect : "NE",
            findTrigger : function(e) {
                var jq    = $(e.target);
                
                // 无视错误的组件
                if(jq.closest(".hm-com").attr("invalid-lib"))
                    return null;

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
            findViewport : function($context, e) {
                var jVP = this.closest(".hm-area-con");
                if(jVP.length > 0)
                    return jVP;
                return $context;
            },
            findDropTarget : function(e) {
                // 只有拖动手柄的时候才会触发
                var jAi  = this.$trigger;
                
                if("H" == jAi.attr("m")) {
                    // 当前控件所在的区域
                    var jMyArea = jAi.closest(".hm-area");
                    var eMyArea = jMyArea.length > 0 ? jMyArea[0] : null;
                    
                    // 当前控件（如果是布局控件）包含的区域先找一下
                    // 这些区域也要过滤到
                    var jCom = jAi.closest(".hm-com");
                    var jSubAreas = jCom.find(".hm-area");
                    var eSubs = Array.from(jSubAreas);
                    
                    // 准备要返回的区域列表
                    var eles = [];
                    
                    // 挨个查找：叶子区域，且不包含当前控件的，统统列出来
                    UI._C.iedit.$body.find(".hm-area").each(function(){
                        if(eMyArea != this
                           && eSubs.indexOf(this) < 0
                           && $(this).find(".hm-area").length == 0) {
                            eles.push(this);
                        }
                    });
                    
                    // 返回
                    return $(eles);
                }
            },
            on_begin : function(e) {
                //......................................
                // 得到组件顶部节点元素
                var jCom  = this.$trigger.closest(".hm-com");
                var uiCom = ZUI(jCom);
                //...........................................................
                this.__hm_check_viewport = function(){
                    // 如果视口不是 body，那么在遮罩层做特殊标识
                    if(this.$context[0] !== this.$viewport[0]) {
                        var gRect = $z.rect(this.$context);
                        var vpCss = $z.rect_relative(this.rect.viewport, gRect, true);
                        $('<div class="pmv-viewport">')
                            .css(vpCss)
                            .appendTo(this.$mask.attr("in-area", "yes"));
                    }
                };
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
                    if("H" == m && this.drops) {
                        this.__is_for_drop = true;
                        this.__do_ing = function(pmvc) {
                            //console.log("drag", pmvc.rect.inview)
                            return true;
                        };
                        
                        // 处理每个拖放的目标的内容显示
                        var pmvc = this;
                        this.$mask.find(".pmv-dropi").each(function(index){
                            var jDrop = $(this);
                            var as    = jDrop.attr("d-as");
                            var index = jDrop.attr("d-index") * 1;
                            var di    = pmvc.drops[as][index];
                            var jArea  = di.$ele;
                            var areaId = jArea.attr("area-id");
                            //console.log(areaId)
                            $(`<div class="di-area"></div>`)
                                .text(areaId).appendTo(jDrop);
                        });
                        
                        // 修改 trigger 的显示样式
                        this.$helper.html(uiCom.getIconHtml());

                        // 标记页面其他元素的样式
                        this.$mask.prevAll().addClass("hm-pmv-hide");
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
                        
                        // 检查 viewport 的模式
                        // this.__hm_check_viewport();
                        
                    }
                }
                //......................................
                // 没有触发到特殊手柄，那么就表示移动自身
                else {
                    // 处理移动时回调
                    this.__do_ing = function(pmvc){
                        pmvc.$mask.attr("mmode", "move")
                        pmvc.rect.com = pmvc.rect.trigger;
                    };
                    
                    // 检查 viewport 的模式
                    this.__hm_check_viewport();
                }
                //......................................
                this.uiCom = uiCom;
                this.comBlock = this.uiCom.getBlock();
                this.rect.com = $z.rect(jCom, false, true);
                //......................................
                // 确保这个控件是激活的
                if(!jCom.attr("hm-actived")){    
                    this.uiCom.notifyActived("page");
                }
            },
            on_ing : function() {
                // 计算，如果返回 true 表示不要更新块的位置大小
                if(_.isFunction(this.__do_ing)
                   && !this.__do_ing(this)){
                    // 计算控件相对于 viewport 的全本 CSS
                    var comCss = $z.rect_relative(this.rect.com,
                                                this.rect.viewport,
                                                true,
                                                this.$viewport);
                    _.extend(this.comBlock, {
                        top:"",left:"",bottom:"",right:"",width:"",height:""
                    }, UI.pickCssForMode(comCss, this.comBlock.posBy))
                    
                    // 通知修改，在 on_end 的时候会保存位置的
                    this.uiCom.notifyBlockChange(null, this.comBlock);
                }
            },
            // 移动结束，保存 Block 信息
            on_end : function() {
                // 如果已经被认为是开始拖拽
                if(this.uiCom) {
                    // 这个拖动是修改位置，保存最后的位置
                    if(!this.__is_for_drop) {
                        this.uiCom.setBlock(this.comBlock);
                    }
                    // 重新应用皮肤
                    UI.invokeSkin("resize");
                }

                // 去掉其他元素的标识
                this.$mask.prevAll().removeClass("hm-pmv-hide");
            },
            // 拖拽到了一个目标，执行修改
            on_drop : function(jAreaCon) {
                //console.log("drop to ", jAreaCon);
                this.uiCom.appendToArea(jAreaCon);
                this.uiCom.$el.removeClass("hm-pmv-hide");
                this.uiCom.el.scrollIntoView();
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