(function($z){
// ......................... 以下是帮助函数
function _H(jHead, selector, html) {
    if(jHead.children(selector).size() == 0){
        jHead.prepend($(html));
    }
}
// ......................... 以上是帮助函数
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/hm__methods',
    'ui/menu/menu',
    'ui/support/dom',
    'ui/form/form',
    'jquery-plugin/pmoving/pmoving',
    'jquery-plugin/moveresizing/moveresizing',
    // 预先加载
    'app/wn.hmaker2/component/columns.js',
    'app/wn.hmaker2/component/image.js',
    'app/wn.hmaker2/component/text.js',
    'app/wn.hmaker2/component/objlist.js',
    'app/wn.hmaker2/component/thingobj.js',
], function(ZUI, Wn, HmMethods, MenuUI){
//==============================================
var html_empty_prop = function(){/*
<div class="ui-arena">
    empty prop
</div>
*/};
var html = function(){/*
<div class="ui-code-template">
    <div code-id="block" class="hm-block">
        <div class="hmb-con">
            <div class="hmb-area"></div>
        </div>
    </div>
    <div code-id="drag_tip" class="hm-drag-tip">
        <i class="zmdi zmdi-arrows"></i> <b>{{hmaker.drag.hover}}</b>
    </div>
</div>
<div class="ui-arena hm-page" ui-fitparent="yes"><div class="hm-W">
    <iframe class="hmpg-frame-load"></iframe>
    <div class="hmpg-stage">
        <div class="hmpg-screen"><iframe class="hmpg-frame-edit"></iframe></div>
    </div>
    <div class="hmpg-sbar"><div class="hm-W">
        <div class="hmpg-sbar-com"  ui-gasket="combar"></div>
        <div class="hmpg-sbar-page" ui-gasket="pagebar"></div>
    </div></div>
    <div class="hmpg-ibar"><div class="hm-W">
        <h4>插</h4>
        <ul>
            <li ctype="rows"
                data-balloon="{{hmaker.com.rows.name}} : {{hmaker.com.rows.tip}}" 
                data-balloon-pos="left" data-balloon-length="medium">
                <%=hmaker.com.rows.icon%>
            </li>
            <li ctype="columns"
                data-balloon="{{hmaker.com.columns.name}} : {{hmaker.com.columns.tip}}" 
                data-balloon-pos="left" data-balloon-length="medium">
                <%=hmaker.com.columns.icon%>
            </li>
            <li ctype="navmenu"
                data-balloon="{{hmaker.com.navmenu.name}} : {{hmaker.com.navmenu.tip}}" 
                data-balloon-pos="left" data-balloon-length="medium">
                <%=hmaker.com.navmenu.icon%>
            </li>
            <li ctype="text"
                data-balloon="{{hmaker.com.text.name}} : {{hmaker.com.text.tip}}" 
                data-balloon-pos="left" data-balloon-length="medium">
                <%=hmaker.com.text.icon%>
            </li>
            <li ctype="image"
                data-balloon="{{hmaker.com.image.name}} : {{hmaker.com.image.tip}}" 
                data-balloon-pos="left" data-balloon-length="medium">
                <%=hmaker.com.image.icon%>
            </li>
            <li ctype="imgslider"
                data-balloon="{{hmaker.com.imgslider.name}} : {{hmaker.com.imgslider.tip}}" 
                data-balloon-pos="left" data-balloon-length="medium">
                <%=hmaker.com.imgslider.icon%>
            </li>
            <li ctype="objlist"
                data-balloon="{{hmaker.com.objlist.name}} : {{hmaker.com.objlist.tip}}" 
                data-balloon-pos="left" data-balloon-length="medium">
                <%=hmaker.com.objlist.icon%>
            </li>
            <li ctype="thingobj"
                data-balloon="{{hmaker.com.thingobj.name}} : {{hmaker.com.thingobj.tip}}" 
                data-balloon-pos="left" data-balloon-length="medium">
                <%=hmaker.com.thingobj.icon%>
            </li>
        </ul>
    </div></div>
</div></div>
*/};
//==============================================
return ZUI.def("app.wn.hmaker_page", {
    dom : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    init : function() {
        var UI = HmMethods(this);

        // 监听 Bus 的各种事件处理页面上的响应
        UI.listenBus("active:block", UI.doActiveBlock);
        UI.listenBus("change:block", UI.doChangeBlock);
        UI.listenBus("change:com",   UI.doChangeCom);
        UI.listenBus("active:page",  UI.doBlurAll);

        // 这里分配一个控件序号数组，采用 bitMap，序号从 0 开始一直排列
        UI._com_seq = [];
    },
    //...............................................................
    // 分配一个组件需要，并做记录
    assignComSequanceNumber : function(jCom) {
        if(!jCom.attr("c_seq")){
            var UI = this;
            var i  = 0;
            // 查找序号表
            for(;i<UI._com_seq.length;i++){
                if(!UI._com_seq[i]) {
                    break;
                }
            }
            // 增加一个记录
            UI._com_seq[i] = true;
            jCom.attr("c_seq", i).prop("id", "com"+i);
            // 返回这个序号
            return i;
        }
        // 返回已有序号
        return jCom.attr("c_seq") * 1;
    },
    //...............................................................
    events : {
        "click .hmpg-ibar li[ctype]" : function(e){
            var UI  = this;
            var jLi = $(e.currentTarget);

            // 首先插入一个块
            var jBlock = UI.doInsertBlock();
            var jArea = jBlock.find(".hmb-area").empty();

            // 得到组件的类型
            var ctype = jLi.attr("ctype");

            // 创建组件的 DOM
            var jCom = $('<div class="hm-com">').attr({
                "ctype"   : ctype
            }).appendTo(jArea);

            // 激活块
            UI.fire("active:block", jBlock);
        }
    },
    //...............................................................
    getPageAttr : function(){
        return $z.getJsonFromSubScriptEle(this._C.iedit.$body, "hm-page-attr");
    },
    setPageAttr : function(attr){
        $z.setJsonToSubScriptEle(this._C.iedit.$body, "hm-page-attr", attr, true);
        this.applyPageAttr(attr);
    },
    applyPageAttr : function(attr){
        var UI = this;
        attr = attr || UI.getPageAttr() || {};
        UI._C.iedit.$body.css(attr);
    },
    //...............................................................
    on_block_delete : function(e) {
        var UI = this;

        // 防止默认事件
        e.preventDefault();

        // 删除当前块
        var jBlock = UI.getActivedBlockElement();
        if(jBlock.size() > 0) {
            jBlock.remove();
            UI.fire("active:page");
        }
    },
    //...............................................................
    doInsertBlock : function() {
        var UI = this;

        if(!UI.nnn)
            UI.nnn = 1;

        var jBlock = UI.ccode("block").appendTo(UI._C.iedit.$body);
        UI.applyBlockProp(jBlock, UI.genBlockDefaultProp());

        UI._C.iedit.$body.moveresizing("format");

        return jBlock;
    },
    //...............................................................
    genBlockDefaultProp : function() {
        return {
            mode : "abs",
            posBy   : "top,left,width,height",
            posVal  : "10px,10px,300px,200px",
            width   : "auto",
            height  : "auto",
            padding : "10px",
            border : "" ,   // "1px solid #000",
            borderRadius : "",
            background : "rgba(40,40,40,0.3)",
            color : "",
            overflow : "",
            blockBackground : "",
        };
    },
    //...............................................................
    getBlockViewport : function(jBlock){
        jBlock = $(jBlock || this.getActivedBlockElement());
        var jArea = jBlock.closest(".hmb-area");
        if(jArea.size() > 0)
            return jArea;
        return jBlock.closest("body");
    },
    //...............................................................
    getBlockRectInCss : function(jBlock) {
        var UI = this;
        jBlock = jBlock || UI.getActivedBlockElement();
        var rect = $z.rect(jBlock);
        var viewport = $z.rect(UI.getBlockViewport(jBlock));
        return $z.rect_relative(rect, viewport, true);
    },
    //...............................................................
    getBlockProp : function(jBlock) {
        var UI = this;

        // 默认应用到激活的块
        jBlock = $(jBlock || UI.getActivedBlockElement());
        
        // 得到属性存放的 <Script>标签
        var prop = $z.getJsonFromSubScriptEle(jBlock, "hmc-prop-block");

        // 如果是相对定位，默认的宽高都是 auto
        if("inflow" == prop.mode) {
            $z.setUndefined(prop, "width",  "auto");
            $z.setUndefined(prop, "height", "auto");
        }

        return prop;
        // $z.setMeaningful(prop, "mode",         jBlock.attr("hmb-mode"));
        // $z.setMeaningful(prop, "posBy",        jBlock.attr("hmb-pos-by"));
        // $z.setMeaningful(prop, "posVal",       jBlock.attr("hmb-pos-val"));
        // $z.setMeaningful(prop, "width",        jBlock.attr("hmb-width"));
        // $z.setMeaningful(prop, "padding",      jBlock.attr("hmb-padding"));
        // $z.setMeaningful(prop, "border",       jBlock.attr("hmb-border"));
        // $z.setMeaningful(prop, "borderRadius", jBlock.attr("hmb-border-radius"));
        // $z.setMeaningful(prop, "background",   jBlock.attr("hmb-background"));
        // $z.setMeaningful(prop, "color",        jBlock.attr("hmb-color"));
    },
    //...............................................................
    applyBlockProp : function(jBlock, prop) {
        var UI = this;
        jBlock = jBlock || UI.getActivedBlockElement();
        var jCon  = jBlock.children(".hmb-con");
        var jArea = jCon.children(".hmb-area");
        //console.log("apply", prop)

        // 与旧属性合并
        prop = _.extend(UI.getBlockProp(jBlock), prop);

        // 更新块的熟悉
        jBlock.attr("hmb-mode", prop.mode);

        //-----------------------------------------------------
        // 对于绝对位置
        if("abs" == prop.mode) {
            // 如果绝对定位的块在一个 [hm-droppable] 内，将其移出到 body
            // TODO 厄，介个，要想想如何在对象内部还继续维持 absolute
            if(jBlock.parents("[hm-droppable]").length > 0) {
                jBlock.appendTo(UI._C.iedit.body);
            }

            // 确保经过格式化以便出现控制手柄
            UI._C.iedit.$body.moveresizing("format");

            // 嗯搞吧，先搞位置
            var pKeys = (prop.posBy||"").split(/\W+/);
            var pVals = (prop.posVal||"").split(/[^\dpx%.-]+/);
            
            // 块
            var css = _.object(pKeys,pVals);
            css.position = "absolute";
            jBlock.css(UI.formatCss(css, true));

            // area 的属性
            css = _.pick(prop,"padding","border","borderRadius","color","background","overflow","boxShadow");
            css.width  = "100%";
            css.height = "100%";
            css = UI.formatCss(css, true);
            //jArea.css(UI.formatCss(css, true));
            jArea.css(css);
        }
        //-----------------------------------------------------
        // 相对位置
        else {
            // 块
            jBlock.css(UI.getBaseCss());

            // area
            var css = _.pick(prop,"margin","width","height","padding","border","borderRadius","color","background","overflow","boxShadow");;
            jArea.css(UI.formatCss(css, true));
        }


        // 保存属性
        var jPropEle = $z.setJsonToSubScriptEle(jBlock, "hmc-prop-block", prop, true);
    },
    //...............................................................
    setup_page_editing : function(){
        var UI = this;

        // 建立上下文: 这个过程，会把 load 的 iframe 内容弄到 edit 里
        UI._rebuild_context();

        // 设置编辑区页面的 <head> 部分
        UI.__setup_page_head();

        // 设置编辑区的移动
        UI.__setup_page_moveresizing();

        // 监视编辑区，响应其他必要的事件处理
        UI.__setup_page_events();

        // 处理所有的块显示
        UI._C.iedit.$body.find(".hm-block").each(function(){
            var jBlock = $(this);
            var prop = UI.getBlockProp(jBlock, true);
            UI.applyBlockProp(jBlock, prop);

            // 处理块中的组件
            var jCom = UI.getComElement(jBlock);

            if(jCom.size()==0) {
                console.log("no jCom", jBlock.html());
            }

            // 绑定 UI，并显示属性
            UI.bindComUI(jCom, function(uiCom){
                // 得到组件的纯数据描述
                var com = uiCom.getData();
                // 修改控件的显示
                UI.doChangeCom(com, jCom);
            }, false);

        });

        // 应用网页显示样式
        UI.applyPageAttr();

        // 通知网页被加载
        UI.fire("active:page", UI._page_obj);

        // 模拟第一个块被点击
        // window.setTimeout(function(){
        //     UI._C.iedit.$body.find(".hm-block").first().click();
        // },500);
    },
    //...............................................................
    __setup_page_head : function() {
        var UI = this;

        // 首先清空
        var jHead = UI._C.iedit.$head.empty();

        // 链入固定的 CSS 
        _H(jHead, 'link[href*="normalize.css"]',
            '<link for-edit="yes" rel="stylesheet" type="text/css" href="/gu/rs/core/css/normalize.css">');
        _H(jHead, 'link[href*="font-awesome.css"]',
            '<link for-edit="yes" rel="stylesheet" type="text/css" href="/gu/rs/core/css/font-awesome-4.5.0/css/font-awesome.css">');
        _H(jHead, 'link[href*="material-design-iconic-font.css"]',
            '<link for-edit="yes" rel="stylesheet" type="text/css" href="/gu/rs/core/css/font-md/css/material-design-iconic-font.css">');
        _H(jHead, 'link[href*="hmaker_editing.css"]',
            '<link for-edit="yes" rel="stylesheet" type="text/css" href="/a/load/wn.hmaker2/hmaker_editing.css">');
        _H(jHead, 'link[href*="moveresizing.css"]',
            '<link for-edit="yes" rel="stylesheet" type="text/css" href="/theme/r/jqp/moveresizing/moveresizing.css">');

        _H(jHead, 'meta[name="viewport"]',
            '<meta name="viewport" content="width=device-width, initial-scale=1.0">');
        _H(jHead, 'meta[http-equiv="X-UA-Compatible"]',
            '<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">');
        _H(jHead, 'meta[charset="utf-8"]',
            '<meta charset="utf-8">');
    },
    //...............................................................
    __setup_page_events : function() {
        var UI = this;

        // 首先所有元素的点击事件，全部禁止默认行为
        UI._C.iedit.$root.on("click", "*", function(e){
            e.preventDefault();
            // console.log("hm_page.js: click", this.tagName, this.className);

            var jq = $(this);

            // 如果点在了块里，激活块，然后就不要冒泡了
            if(jq.hasClass("hm-block")){
                e.stopPropagation();
                if(!jq.attr("hm-actived"))
                    UI.fire("active:block", jq);
                // 确保控件扩展属性面板被隐藏
                UI.fire("hide:com:ele");
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
                UI.on_block_delete(e);
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

        // 通知移动的过程
        var notify_move_or_resize = function(rect) {
            var vals = UI.transRectToPosVal(rect, this.prop.posBy);

            // 更新
            this.prop.posVal = vals;

            // 通知
            UI.fire("change:block", this.prop);
        };

        // 监视控件的拖拽
        UI._C.iedit.$body.moveresizing({
            trigger : '.hm-block[hmb-mode="abs"]',
            findViewport : function(){
                return UI.getBlockViewport(this);
            },
            delay : 300,
            maskClass : "hm-page-move-mask",
            on_begin : function() {
                // 如果未被激活，激活当前的块
                if(!this.$trigger.closest(".hm-block").attr("hm-actived"))
                    UI.fire("active:block", this.$trigger);
                // 为所有的 drag 帮助类设置内容
                if(this.dropping)
                    for(var di of this.dropping) {
                        di.helper.append(UI.ccode("drag_tip"));
                    }
                // 记录 uiCom 以及初始的 prop 等信息
                this.$com  = UI.getComElement(this.$block);
                this.uiCom = ZUI(this.$com);
                this.prop  = UI.getBlockProp(this.$block);
            },
            on_change : notify_move_or_resize,
            updateBlockBy : null,
            findDropTarget : function(){
                // 如果是移动尺寸手柄，那么就不是拖动了
                if(this.$trigger.hasClass("mvrza-hdl")){
                    return;
                }
                // 寻找可以被放置的目标
                var re = [];
                var me = this.$trigger[0];
                this.$viewport.find("[hm-droppable]").each(function(){
                    if($(this).closest(me).length == 0){
                        re.push(this);
                    }
                });
                // 返回
                return $(re);
            },
            // on_dragenter : function(jq, helper) {
            //     helper.attr("drag-hover", "yes");
            // },
            // on_dragleave : function(jq, helper) {
            //     helper.removeAttr("drag-hover");
            // },
            on_drop : function(jq) {
                var jBlock = this.$block;
                var jCom   = this.$com;
                var uiCom  = this.uiCom;
                var prop   = this.prop;

                // 确保
                prop.mode = "inflow";

                // 将控件附加在放置区末尾
                jq.append(jBlock);

                // 格式化组件和块的尺寸
                var com = uiCom.getData();
                $z.invoke(uiCom, "formatSize", [prop, com, jBlock.attr("hmb-mode")]);

                // 通知 Block 更新: 确保组件的块不再是绝对位置
                uiCom.fire("change:block", prop);

                // 通知 COM 更新
                UI.fire("change:com", com);
            }
        });
    },
    //...............................................................
    bindComUI : function(jCom, callback, showProp) {
        var UI = this;
        jCom = jCom.closest(".hm-com");

        // 定义得到 COMUI 的后续处理
        var _do_com_ui = function(uiCom) {
            //console.log("bindCom _do_com_ui")
            // 确保有组件序号
            UI.assignComSequanceNumber(jCom);

            // 确保有组件属性存放的 <script>
            // var jPropEle = UI.$el.children("script.hmc-th-prop-ele");
            // if(jPropEle.size() == 0) {
            //     $('<script class="hmc-th-prop-ele">').prependTo(UI.$el);
            // }

            // 同时显示属性
            if(showProp) {
                // 得到属性编辑控件
                var PropComDef = $z.invoke(uiCom, "setupProp");

                // 如果没有属性，默认显示一个空面板
                if(!PropComDef) {
                    PropComDef = {
                        uiType : 'ui/support/dom',
                        uiConf : {
                            dom : $z.getFuncBodyAsStr(html_empty_prop.toString())
                        }
                    }
                }
                
                // 将 uiCom 与这个属性控件关联
                _.extend(PropComDef.uiConf, {
                    on_init : function(){
                        this.uiCom = uiCom;
                    },
                    on_change : function(key, val) {
                        //console.log(this.uiCom.uiName, key, val);
                        //UI.fire("change:com", $z.obj(key, val));
                        this.uiCom.notifyChange(key, val);
                    }
                });

                // 执行创建
                UI.parent.subUI("prop/edit").drawCom(PropComDef, function(){
                    $z.doCallback(callback, [uiCom], UI);
                });
            }
            // 不显示属性的话，就直接回调了
            else {
                $z.doCallback(callback, [uiCom], UI);
            }
        };

        // 看看是否之前就绑定过
        var uiId  = jCom.attr("ui-id");

        // 已经绑定了 UI，那么继续弄后面的
        if(uiId) {
            _do_com_ui(ZUI(uiId));
        }
        // 否则根据类型加载 UI 吧
        else {
            var ctype = jCom.attr("ctype");
            if(!ctype) {
                console.warn(ctype, jCom);
                throw "fail to found ctype from jCom";
            }
            seajs.use("app/wn.hmaker2/component/"+ctype, function(ComUI){
                new ComUI({
                    parent  : UI,
                    $el     : jCom,
                    keepDom : true
                }).render(function(){
                    _do_com_ui(this);
                })
            });
        }
    },
    //...............................................................
    // 找到当前的操作区，如果没有，那么默认为整个 body
    // 返回的是一个 jQuery 对象
    getActivedComElement : function() {
        return this.getComElement(this.getActivedBlockElement());
    },
    getActivedBlockElement : function() {
        return this._C.iedit.$body.find(".hm-block[hm-actived]");
    },
    getComElement : function(jBlock) {
        return jBlock.find(">.hmb-con>.hmb-area>.hm-com");
    },
    getBlockElement : function(jCom) {
        return jCom.closest(".hm-block");
    },
    //...............................................................
    doChangeBlock : function(prop) {
        var jBlock = this.getActivedBlockElement();
        this.applyBlockProp(jBlock, prop);
    },
    //...............................................................
    doChangeCom : function(com, jCom) {
        jCom = jCom || this.getActivedComElement();
        var uiCom = ZUI(jCom, true);
        uiCom.setData(com);
    },
    //...............................................................
    doActiveBlock : function(jq) {
        var UI = this;

        UI._C.iedit.$body.find(".hm-block[hm-actived]").removeAttr("hm-actived");
        var jBlock = jq.closest(".hm-block").attr("hm-actived", "yes");
        
        var jCom = UI.getComElement(jBlock);

        // 通知激活组件
        UI.fire("active:com", jCom);

        // 绑定 UI，并显示属性
        UI.bindComUI(jCom, function(uiCom){
            // 得到组件的纯数据描述
            var com = uiCom.getData();

            // 得到块的属性
            var prop = UI.getBlockProp(jBlock);

            // 格式化组件和块的尺寸
            $z.invoke(uiCom, "formatSize", [prop, com, jBlock.attr("hmb-mode")]);

            // 发出通知
            UI.fire("change:com", com, jCom);
        }, true);
    },
    //...............................................................
    doBlurAll : function() {
        this._C.iedit.$body.find("[hm-actived]").removeAttr("hm-actived");
    },
    //...............................................................
    __after_iframe_loaded : function(name) {
        var UI = this;

        // 移除加载完毕的项目
        UI._need_load = _.without(UI._need_load, name);

        // 全部加载完毕了
        if(UI._need_load.length == 0){
            UI.setup_page_editing();
        }
    },
    //...............................................................
    redraw : function(){
        var UI  = this;
        
        // 绑定隐藏 iframe onload 事件，这个 iframe 专门用来与服务器做数据交换的
        // var jIfmLoad = UI.arena.find(".hmpg-frame-load");
        // if(!jIfmLoad.attr("onload-bind")){
        //     jIfmLoad.bind("load", function(e){
        //         //console.log("hmaker_page: iframe onload", Date.now(), e);
        //         UI._C = UI._create_context();
        //         console.log(UI._C);

        //         UI.setup_page_editing();
        //     });
        //     jIfmLoad.attr("onload-bind", "yes");
        // }
        // 创建两个 iframe 一个负责编辑，一个负责加载
        // hmpg-frame-load
        var jW = UI.arena.find(">.hm-W");
        var jScreen = jW.find(".hmpg-screen");
        // var jIfmLoad = $('<iframe class="hmpg-frame-load">').appendTo(jW);
        // var jIfmEdit = $('<iframe class="hmpg-frame-edit" src="/a/load/wn.hmaker2/page_loading.html">').appendTo(jScreen);

        // var jIfmLoad = UI.arena.find(".hmpg-frame-load");
        // var jIfmEdit = UI.arena.find(".hmpg-frame-edit");

        var jIfmLoad = $(".hmpg-frame-load");
        var jIfmEdit = $(".hmpg-frame-edit");

        // 保存记录 
        UI._need_load = ["iedit", "iload"];

        // 监听加载完毕的事件
        jIfmLoad.one("load", function(e) {
            //console.log("AAAAA")
            UI.__after_iframe_loaded("iload");
        });

        jIfmEdit.one("load", function(e) {
            //console.log("BBBBBB")
            UI.__after_iframe_loaded("iedit");
        });
       
        // 菜单条
        new MenuUI({
            parent : UI,
            gasketName : "pagebar",
            setup : [{
                text : "Test",
                handler : function(){
                    UI._C.iload = UI._reset_context_vars(".hmpg-frame-load");
                    UI._C.iedit = UI._reset_context_vars(".hmpg-frame-edit");
                }
            },{
                icon : '<i class="zmdi zmdi-more-vert"></i>',
                items : [{
                    icon : '<i class="zmdi zmdi-settings"></i>',
                    text : 'i18n:hmaker.page.show_prop',
                    handler : function() {
                        UI.fire("active:page", UI._page_obj);
                    }
                }]
            }]
        }).render(); 
    },
    //...............................................................
    depose : function() {
        this.arena.find(".hmpg-frame-load").unbind();
    },
    //...............................................................
    update : function(o) {
        var UI = this;

        // 记录
        UI._page_obj = o;

        var jIfmEdit = UI.arena.find(".hmpg-frame-edit");
        jIfmEdit.prop("src", "/a/load/wn.hmaker2/page_loading.html");

        var jIfmLoad = UI.arena.find(".hmpg-frame-load");
        jIfmLoad.prop("src", "/o/read/id:"+o.id);
    },
    //...............................................................
    // 获取编辑操作时的上下文
    _rebuild_context : function() {
        var UI = this;

        // TODO 有必要是，来个 jIfmTmpl 用来加载模板文件
        var jIfmLoad = UI.arena.find(".hmpg-frame-load");
        var docLoad  = jIfmLoad[0].contentDocument;
        var rootLoad = docLoad.documentElement;

        var jIfmEdit = UI.arena.find(".hmpg-frame-edit");
        var docEdit  = jIfmEdit[0].contentDocument;
        var rootEdit = docEdit.documentElement;

        // 创建上下文对象
        var C = {
            $screen : UI.arena.find(".hmpg-screen"),
            $pginfo : UI.arena.find(".hmpg-sbar .hmpg-pginfo"),
        };

        // 设置 HTML 到编辑区
        rootEdit.innerHTML = rootLoad.innerHTML;

        // 重新索引快捷标记
        C.iload = UI._reset_context_vars(".hmpg-frame-load");
        C.iedit = UI._reset_context_vars(".hmpg-frame-edit");

        // 记录上下文
        UI._C = C;

        // 分析序号表
        UI._com_seq = [];
        C.iedit.$body.find(".hm-com").each(function(){
            var seq = $(this).attr("c_seq") * 1;
            if(_.isNumber(seq)){
                // 无效的或者已经存在的序号
                if(isNaN(seq) || UI._com_seq[seq]) {
                    $(this).removeAttr("c_seq");
                }
                // 记录序号
                else {
                    UI._com_seq[seq] = true;
                }
            }
        })
        // 为所有未分配序号的组件，分配
        .not("[c_seq]").each(function(){
            UI.assignComSequanceNumber($(this));
        });
    },
    //...............................................................
    _reset_context_vars : function(selector) {
        var UI = this;

        var ifrm = $(selector);
        var doc  = ifrm[0].contentDocument;

        var cobj = {
            doc  : doc,
            root : doc.documentElement,
            head : doc.head,
            body : doc.body,
        };
        cobj.$root = $(cobj.root);
        cobj.$head = $(cobj.head);
        cobj.$body = $(cobj.body);
        return cobj;
    },
    //...............................................................
    getCurrentEditObj : function() {
        return this._page_obj;
    },
    //...............................................................
    getCurrentTextContent : function() {
        var UI = this;
        var C  = UI._C;

        // 将 iedit 的内容复制到 iload 里面
        C.iload.root.innerHTML = C.iedit.root.innerHTML;
        C.iload = UI._reset_context_vars(".hmpg-frame-load");

        // 清空 iload 的头部
        C.iload.$head.empty();

        // 移除 body 的一些属性
        C.iload.$body
            .removeAttr("pointer-moving-enabled")
            .removeAttr("style");

        // 移除所有的辅助节点
        C.iload.$body.find(".mvrz-ass, .hm-del-save, .ui-code-template").remove();

        // 删除所有的块和控件的 CSS 渲染属性
        C.iload.$body.find(".hm-block,.hmb-con,.hmb-area,.hm-com,.ui-arena").each(function(){
            var jq = $(this).removeAttr("style");

            jq.attr({
                "mvrz-block" : null,
                "hm-actived" : null,
                "ui-id" : null,
            });

            // 所有的子
            jq.find("*").removeAttr("style");

        }).filter(".hm-com").each(function(){
            $(this).removeAttr("ui-id");
        });

        // 整理所有的空节点，让其为一个回车
        $z.eachTextNode(C.iload.$body, function(){
            var str = $.trim(this.nodeValue);
            // 处理空白节点
            if(0 == str.length) {
                // 如果之前的节点是个文本节点的话，那么自己就变成空字符串吧
                if(this.previousSibling && this.previousSibling.nodeType == 3) {
                    this.nodeValue = "";
                }
                // 否则输出个回车
                else {
                    this.nodeValue = "\n";
                }
            }
        });

        // 删除所有临时属性
        C.iload.$body.find('[del-attrs]').each(function(){
            var jq = $(this);
            var attrNames = jq.attr("del-attrs").split(",");
            console.log(attrNames)
            var subs = jq.find("*").andSelf();
            for(var attrName of attrNames) {
                subs.removeAttr(attrName);
            }
        });

        // 返回 HTML
        return '<!DOCTYPE html>\n<html>\n' + C.iload.$root.html() + '\n</html>\n';;
    },
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);