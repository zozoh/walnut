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

        // 启用插入条的拖拽
        UI.__setup_ibar_drag_and_drop();

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
                //console.log(index, jCom.attr("id"));
                // 初始化属性
                jCom.removeAttr("hm-actived").attr("hm-blur", "yes");
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
        window.setTimeout(function(){
            UI._C.iedit.$body.find(".hm-com").first().click();
        }, 200);
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
        UI._H(jHead, 'link[href*="hmaker_editing"]',
            UI.getCssTheme('<link rel="stylesheet" type="text/css" class="hm-del-save" href="/a/load/wn.hmaker2/theme/hmaker_editing-{{theme}}.css">'));

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
        UI._C.iedit.$body.on("click", "*", function(e){
            e.preventDefault();
        })
        // 如果点在了控件里，激活
        .on("click", ".hm-com", function(e){
            e.stopPropagation();

            // 被拖拽禁止了点击
            if(window.__forbid_click)
                return;

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

            // 调用控件的激活事件
            $z.invoke(uiCom, "on_actived");
        })
        // 激活页面
        .on("click", function(e){
            // 被拖拽禁止了点击
            if(window.__forbid_click)
                return;
            // 点在 body 里面则激活页面属性面板
            if(e.target === UI._C.iedit.body 
                || e.target === UI._C.iedit.root) {
                UI.fire("active:page", UI._page_obj);
            }

            // 不管怎样，模拟一下父框架页面的点击
            // $(document.body).click();
        });

        // 捕获窗口滚动事件
        UI._C.iedit.$win.on("scroll", function(e){
            UI.updateComScrollInfo();
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
                UI.invokeSkin("ready");
                UI.invokeSkin("resize");
            });
        }
    },
    //...............................................................
    invokeSkin : function(method){
        var UI = this;
        //console.log("invokeSkin", method, UI._C ? UI._C.SkinJS : "!No UI._C");
        if(UI._C && UI._C.SkinJS && UI._C.iedit.doc && UI._C.iedit.doc.defaultView){
            // 准备上下文
            var skinContext = {
                mode   : "IDE",
                doc    : UI._C.iedit.doc,
                win    : UI._C.iedit.doc.defaultView,
                root   : UI._C.iedit.root,
                jQuery : window.jQuery,
            };

            // 清除 rootElement 的 fontsize
            if("off" == method) {
                $(skinContext.root).css("fontSize", "");
            }
            // 重新计算 rootElement 的 fontsize
            else {
                $z.do_change_root_fontSize(skinContext);
            }

            // 调用皮肤
            $z.invoke(UI._C.SkinJS, method, [], skinContext);
        }
    },
    //...............................................................
    __setup_ibar_drag_and_drop : function() {
        var UI = this;

        // 启用拖拽
        UI.arena.find(".hmpg-ibar").moving({
            trigger    : '.ibar-item',
            maskClass  : 'hm-page-move-mask',
            scrollSensor : {x:"10%", y:30},
            client : function(){
                return UI._C.iedit.$body;
            },
            clientRect : function(){
                return UI.get_edit_win_rect();
            },
            on_begin : function(){
                var ing = this;
                var opt = ing.options;

                // 得到感应器的设定
                var senSetup = UI.getDragAndDropSensors(true);
                opt.sensors    = senSetup.sensors;
                opt.sensorFunc = senSetup.sensorFunc;

                // 去掉插入条的标识
                ing.$trigger.closest('li[ctype]').removeAttr("enter");
                
                // 标识一下移动组件的 helper
                ing.mask.$target.attr("md", "drag");

                // 修改 target 副本的显示样式
                ing.mask.$target.html(ing.$target.html());

                // 标记页面其他元素的样式
                UI._C.iedit.$body.children().addClass("hm-pmv-hide");
            },
            // 移动结束，保存 Block 信息
            on_end : function() {
                var ing = this;
                //console.log(_.isElement(ing.drop_in_area), $z.isjQuery(ing.drop_in_area));
                // 加入到页面
                if(ing.drop_in_area && ing.drop_in_area.length>0){
                    var jItem   = ing.$target;
                    var jLi     = jItem.closest("li[ctype]");
                    var ctype   = jLi.attr("ctype");
                    var tagName = jLi.attr("tag-name") || 'DIV';
                    var val     = jItem.attr("val");
                    
                    // 插入
                    var jCom = UI.doInsertCom(ctype,tagName,val,ing.drop_in_area);

                    // 滚动到显示
                    if(ing.drop_in_area[0] == UI._C.iedit.body
                        && jCom.attr("hmc-mode") == "inflow"){
                        //UI._C.iedit.body.scrollTop = UI._C.iedit.$body.height();
                        jCom[0].scrollIntoView();
                    }
                    // 闪一下 ^_^
                    $z.blinkIt(jCom);
                }
                //......................................
                // 去掉其他元素的标识
                UI._C.iedit.$body.find(".hm-pmv-hide")
                    .removeClass("hm-pmv-hide");
            },
        });
        
    },
    //...............................................................
    __setup_page_moveresizing : function() {
        var UI = this;
        UI._C.iedit.$body.moving({
            trigger    : '.hm-com',
            maskClass  : 'hm-page-move-mask',
            clientRect : function(){
                return UI.get_edit_win_rect();
            },
            viewport : function(){
                var ing = this;
                var jAreaCon = ing.jCom.closest(".hm-area-con");
                if(jAreaCon.length>0){
                    ing.is_in_area = true;
                    return jAreaCon;
                }
                return ing.$selection;
            },
            viewportRect : function(){
                var jAreaCon = this.$trigger.closest(".hm-area-con");
                var editBody = UI.get_edit_win_rect();
                // 在 hm-area 里，则用它
                if(jAreaCon.length>0) {
                    return $D.rect.gen(jAreaCon, {
                        boxing   : "content",
                        scroll_c : true,
                        viewport : editBody,
                        overflow : true,
                        overflowEle : jAreaCon,
                    });
                }
                // 否则用 body
                return editBody;
            },
            scrollSensor : {x:"10%", y:30},
            init : function(){
                var ing = this;
                var jq  = $(ing.Event.target);
                //......................................
                // 得到组件顶部节点元素，并记录到上下文
                ing.jCom  = ing.$trigger.closest(".hm-com");
                ing.uiCom = ZUI(ing.jCom);
                ing.comBlock = ing.uiCom.getBlock();
                ing.comMeasureConf = 
                    ing.uiCom.getMeasureConf(ing.comBlock.measureBy);
            },
            target : function(){
                var ing = this;
                var jq  = $(ing.Event.target);
                //......................................
                // 无视错误的组件
                if(ing.jCom.attr("invalid-lib"))
                    return null;

                // 辅助节点
                var jAi = jq.closest(".hmc-ai");
                if(jAi.length > 0)
                    return jAi;

                // 普通移动: inflow 的不能移动
                if('inflow' == ing.jCom.attr("hmc-mode")){
                    return null;
                }

                // 默认移动组件本身
                return ing.jCom;
            },
            on_begin : function(){
                var ing   = this;
                var opt   = ing.options;
                var jCom  = ing.$target.closest(".hm-com");
                var uiCom = ing.uiCom;
                //......................................
                // 确保这个控件是激活的
                if(!jCom.attr("hm-actived")){    
                    ing.uiCom.notifyActived("page");
                }
                //......................................
                // 根据控件的定位顶点，设置移动组件的 cssBy
                // 以便确定要更新哪四个属性
                opt.cssBy = UI.comBlockModeToKeys(ing.comBlock.posBy);
                //......................................
                // 是控制柄
                if(ing.$target.hasClass("hmc-ai")){
                    var jAi = ing.$target;
                    var m   = jAi.attr("m");
                    //..................................
                    // 改变组件层级关系的拖拽柄
                    if("H" == m) {
                        // 标识移动是为了拖拽
                        ing.__is_for_drop = true;
                        // 标识一下移动组件的 helper
                        ing.mask.$target.attr("md", "drag");

                        // 拖拽期间，组件不显示
                        jCom.css("visibility", "hidden");

                        // 得到拖拽感应器设置
                        var senSetup = UI.getDragAndDropSensors(jCom, ing.$viewport);
                        opt.sensors    = senSetup.sensors;
                        opt.sensorFunc = senSetup.sensorFunc;

                        // 修改 trigger 的显示样式
                        ing.mask.$target.html(uiCom.getIconHtml());

                        // 标记页面其他元素的样式
                        UI._C.iedit.$body.children().addClass("hm-pmv-hide");
                    }
                    //..................................
                    // 改变控件大小的八个柄
                    else {
                        // 显示块面板
                        UI.fire("show:prop", "block");
                        // 标识一下突出显示视口
                        ing.mask.$viewport.attr("in-area", ing.is_in_area?"yes":null);
                        // 得到组件的矩形信息
                        ing.rect.com = $D.rect.gen(jCom, {
                            scroll_c : true,
                            viewport : ing.rect.client
                        });
                        // 准备计算函数
                        var keys = ({
                            NW : ["left", "top"],
                            W  : ["left"],
                            SW : ["left", "bottom"],
                            N  : ["top"],
                            S  : ["bottom"],
                            NE : ["right", "top"],
                            E  : ["right"],
                            SE : ["right", "bottom"]
                        })[m];
                        //console.log("keys:", keys)
                        // 四个方向限制一下移动
                        if(m.length == 1) {
                            opt.mode = /^[NS]$/.test(m) ? "y" : "x";
                        }
                        // 设置回调函数
                        opt.on_ing = function(){
                            // 计算 com 的绝对矩形
                            _.extend(this.rect.com, $z.pick(this.rect.current, keys));
                            $D.rect.count_tlbr(this.rect.com);
                            // 转换成 css
                            var css = $D.rect.relative(
                                this.rect.com, 
                                this.rect.viewport, 
                                true, {
                                    x : this.$viewport[0].scrollLeft,
                                    y : this.$viewport[0].scrollTop,
                                });
                            this.uiCom.formatBlockDimension(css, ing.comMeasureConf);
                            // 更新控件
                            _.extend(this.comBlock, {
                                top:"",left:"",right:"",bottom:"",width:"",height:""
                            }, $z.pick(css, opt.cssBy));
                            // 通知修改
                            this.uiCom.notifyBlockChange(null, this.comBlock);
                        }
                    }
                }
                //......................................
                // 那么就是移动组件本身
                else {
                    // 显示块面板
                    UI.fire("show:prop", "block");
                    // 标识一下突出显示视口
                    ing.mask.$viewport.attr("in-area", ing.is_in_area?"yes":null);
                    // 标识一下移动组件的 helper
                    ing.mask.$target.attr("md", "target");
                    // 动态设置一下参考线
                    opt.assist = {
                        axis : [],
                        axisFullScreen : $D.rect.gen(UI.arena.find('.hmpg-frame-edit'), {
                            boxing   : "content",
                            scroll_c : true,
                        })
                    };
                    opt.assist.axis[0] = opt.cssBy.indexOf("left")>=0?"left":"right";
                    opt.assist.axis[1] = opt.cssBy.indexOf("top")>=0?"top":"bottom";
                    // 准备执行函数
                    opt.on_ing = function(){
                        // 转换
                        var css = _.extend({}, this.css.current);
                        ing.uiCom.formatBlockDimension(css, ing.comMeasureConf);
                        // 更新控件
                        _.extend(this.comBlock, {
                            top:"",left:"",right:"",bottom:"",width:"",height:""
                        }, css);
                        // 通知修改
                        this.uiCom.notifyBlockChange(null, this.comBlock);
                    }
                }
            },
            // 移动结束，保存 Block 信息
            on_end : function() {
                var ing  = this;
                var jCom = ing.$target.closest(".hm-com");
                //......................................
                // 如果已经被认为是开始拖拽
                if(ing.uiCom) {
                    // 拖拽的话，看看落在哪个感应器里了
                    if(ing.__is_for_drop) {
                        if(ing.drop_in_area) {
                            var jAreaCon = ing.drop_in_area;
                            //console.log("drop in", ing.drop_in_area);
                            if(jAreaCon[0] == UI._C.iedit.body){
                                jAreaCon = null;
                            }
                            ing.uiCom.appendToArea(jAreaCon);
                            // 滚动到显示
                            if(!jAreaCon){
                                UI._C.iedit.body.scrollTop = 
                                    UI._C.iedit.body.clientHeight;
                                $z.blinkIt(jCom);
                            }
                        }

                        // 恢复组件显示
                        jCom.css("visibility", "");
                    }
                    // 否则，是位置及大小的改动，那么最后再保存一下位置 
                    else {
                        ing.uiCom.setBlock(ing.comBlock);
                    }
                    // 重新应用皮肤
                    UI.invokeSkin("ready");
                    UI.invokeSkin("resize");
                }
                //......................................
                // 去掉其他元素的标识
                UI._C.iedit.$body.find(".hm-pmv-hide")
                    .removeClass("hm-pmv-hide");
            },
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