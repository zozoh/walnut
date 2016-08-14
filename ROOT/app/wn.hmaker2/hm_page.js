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
    'app/wn.hmaker2/component/thingobj.js',
    'app/wn.hmaker2/component/thingset.js',
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
</div>
<div class="ui-arena hm-page" ui-fitparent="yes"><div class="hm-W">
    <!--iframe class="hmpg-frame-load" id="hmaker_page_loader" name="load"></iframe-->
    <div class="hmpg-stage">
        <div class="hmpg-screen"><!--iframe class="hmpg-frame-edit" id="hmaker_page_editor" name="edit"></iframe--></div>
    </div>
    <div class="hmpg-sbar"><div class="hm-W">
        <div class="hmpg-sbar-com"  ui-gasket="combar"></div>
        <div class="hmpg-sbar-page" ui-gasket="pagebar"></div>
    </div></div>
    <div class="hmpg-ibar"><div class="hm-W">
        <h4>插</h4>
        <ul>
            <li ctype="columns"
                data-balloon="{{hmaker.com.columns.name}} : {{hmaker.com.columns.tip}}" 
                data-balloon-pos="left" data-balloon-length="medium">
                <%=hmaker.com.columns.icon%>
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
            <li ctype="slider"
                data-balloon="{{hmaker.com.slider.name}} : {{hmaker.com.slider.tip}}" 
                data-balloon-pos="left" data-balloon-length="medium">
                <%=hmaker.com.slider.icon%>
            </li>
            <li ctype="thingset"
                data-balloon="{{hmaker.com.thingset.name}} : {{hmaker.com.thingset.tip}}" 
                data-balloon-pos="left" data-balloon-length="medium">
                <%=hmaker.com.thingset.icon%>
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

        // 监听键盘事件
        UI.watchKey(8,  UI.on_block_delete);
        UI.watchKey(46, UI.on_block_delete);
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
    on_block_delete : function(e) {
        var UI = this;

        console.log("haha", e.which)

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
        UI.applyBlockProp(jBlock, {}, true);

        UI._C.iedit.$body.moveresizing("format");

        return jBlock;
    },
    //...............................................................
    genBlockDefaultProp : function() {
        return {
            mode : "abs",
            posBy   : "top,left,width,height",
            posVal  : "10px,10px,300px,200px",
            padding : "10px",
            border : 0 ,   // "1px solid #000",
            borderRadius : "5px",
            background : "rgba(40,40,40,0.3)",
            color : "#000",
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
    // 参数 mergeDefault 会将当前块的属性与默认属性组合，即，保证了每个值都有值
    getBlockProp : function(jBlock, mergeDefault) {
        var UI = this;
        // 默认应用到激活的块
        if(!$z.isjQuery(jBlock)){
            mergeDefault = jBlock;
            jBlock = UI.getActivedBlockElement();
        }
        
        // 准备返回的属性
        var prop = mergeDefault ? UI.genBlockDefaultProp() : {};

        $z.setMeaningful(prop, "mode",         jBlock.attr("hmb-mode"));
        $z.setMeaningful(prop, "posBy",        jBlock.attr("hmb-pos-by"));
        $z.setMeaningful(prop, "posVal",       jBlock.attr("hmb-pos-val"));
        $z.setMeaningful(prop, "width",        jBlock.attr("hmb-width"));
        $z.setMeaningful(prop, "padding",      jBlock.attr("hmb-padding"));
        $z.setMeaningful(prop, "border",       jBlock.attr("hmb-border"));
        $z.setMeaningful(prop, "borderRadius", jBlock.attr("hmb-border-radius"));
        $z.setMeaningful(prop, "background",   jBlock.attr("hmb-background"));
        $z.setMeaningful(prop, "color",        jBlock.attr("hmb-color"));
        
        // 返回属性
        return prop;
    },
    //...............................................................
    // 参数 mergeDefault 会将当前块的属性与默认属性组合，即，保证了每个值都有值
    applyBlockProp : function(jBlock, prop, mergeDefault) {
        var UI = this;
        jBlock = jBlock || UI.getActivedBlockElement();
        //console.log("apply", prop)

        // 合并属性
        if(mergeDefault)
            prop = _.extend(UI.genBlockDefaultProp(), prop);

        var css;

        // 确定要修改位置方面的数值
        if(prop.mode || prop.posBy || prop.posVal) {
            // 计算 prop 的几个默认默认属性
            $z.setUndefined(prop, "mode", jBlock.attr("hmb-mode"));
            $z.setUndefined(prop, "posBy", jBlock.attr("hmb-pos-by"));

            // 准备 CSS
            css = {position:"", top:"",left:"",width:"",height:"",right:"",bottom:"",margin:""};

            // 处理位置
            // 绝对定位
            if("abs" == prop.mode) {
                css.position = "absolute";
                var pKeys = (prop.posBy||"").split(/\W+/);
                var pVals = (prop.posVal||"").split(/[^\dpx%.-]+/);
                if(pKeys.length==pVals.length && pKeys.length > 0) {
                    for(var i=0;i<pKeys.length;i++) {
                        css[pKeys[i]] = pVals[i];
                    }
                }
            }
            // 跟随
            else if("inflow" == prop.mode) {
                // 厄，啥都没必要做吧
            }
            // 居中
            else if("center" == prop.mode) {
                css.margin = "0 auto";
                css.width = prop.width;
            }
            // 修改位置 CSS
            jBlock.css(css);
        }

        // 循环处理其他属性
        css = {};
        for(var key in prop) {
            var val = prop[key];

            // 记录属性
            var attrName = "hmb-" + $z.lowerWord(key);
            jBlock.attr(attrName, val);


            // 位置信息，忽略
            if(/^(mode|pos)/.test(key))
                continue;

            // 其他的设置到 CSS 里
            css[key] = val || "";
        }

        //console.log(css)

        // 最后应用一下 CSS
        jBlock.children().css(css);
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

        // 通知网页被加载
        UI.fire("active:page", UI._page_obj);

        // 模拟第一个块被点击
        //window.setTimeout(function(){
            UI._C.iedit.$body.find(".hm-block").first().click();
        //},0);
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
                console.log("click block")
                e.stopPropagation();
                if(!jq.attr("hm-actived"))
                    UI.fire("active:block", jq);
                // 确保控件扩展属性面板被隐藏
                UI.fire("hide:com:ele");
            }
            // 如果点到了 body，那么激活页
            else if('BODY' == this.tagName){
                console.log("click body")
                UI.fire("active:page", UI._page_obj);
            }
        });

        // 截获所有的键事件，转发给 ZUI
        UI._C.iedit.$body.on("keydown", function(e){
            ZUI.on_keydown(e);
        });
    },
    //...............................................................
    __setup_page_moveresizing : function() {
        var UI = this;

        // 通知移动的过程
        var notify_move_or_resize = function(rect) {
            var vals = UI.transRectToPosVal(rect, this.$block.attr("hmb-pos-by"));
            UI.fire("change:block", {
                posVal : vals
            });
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
                if(!this.$trigger.closest(".hm-block").attr("hm-actived"))
                    UI.fire("active:block", this.$trigger);
            },
            on_change : notify_move_or_resize,
            updateBlockBy : null,
            // findDropTarget : ".canvas-wrapper>.moni:not(.moni-current)",
            // on_dragenter : function(jMoni, helper) {
            //     jMoni.attr("show-drop-pobj-tip", "yes");
            //     helper.append(UI.ccode("changeMoni"));
            // },
            // on_dragleave : function(jMoni, helper) {
            //     jMoni.removeAttr("show-drop-pobj-tip");
            //     helper.empty();
            // },
            // on_drop : function(jMoni) {
            //     // 得到对应的 pobj 元素
            //     var jPobj = this.$trigger.closest(".moni-pobj");

            //     // 切换监视器
            //     var moniIndex = jMoni.attr("moni-index") * 1;
            //     var pobj = jPobj.data("@POBJ");
            //     // 嗯果然换了，改吧
            //     if(pobj.args.monitor != moniIndex){                        
            //         pobj.args.monitor = moniIndex;
            //         pobj.style = this.pobj_origin_style;

            //         // 触发事件
            //         UI.parent.trigger("screen:pobj:style", pobj);

            //         // 更新自身显示
            //         UI.update();
            //     }

            // }
        });
    },
    //...............................................................
    bindComUI : function(jCom, callback, showProp) {
        var UI = this;

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

        // TODO 这里根据控件获取 UI
        var uiCom = ZUI(jCom);

        // 已经绑定了 UI，那么继续弄后面的
        if(uiCom) {
            _do_com_ui(uiCom);
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
    //...............................................................
    doChangeBlock : function(prop) {
        var jBlock = this.getActivedBlockElement();
        this.applyBlockProp(jBlock, prop, false);
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
            // 发出通知
            UI.fire("change:com", com);
        }, true);
    },
    //...............................................................
    doBlurAll : function() {
        this._C.iedit.$body.find("[hm-actived]").removeAttr("hm-actived");
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
        var jIfmLoad = $('<iframe class="hmpg-frame-load">').appendTo(jW);
        var jIfmEdit = $('<iframe class="hmpg-frame-edit" src="/a/load/wn.hmaker2/page_loading.html">').appendTo(jScreen);
        jIfmLoad.bind("load", function(e) {
            UI.setup_page_editing();
        });
       
        // 菜单条
        new MenuUI({
            parent : UI,
            gasketName : "pagebar",
            setup : [{
                text : "Test",
                handler : function(){
                    var rect = UI.getBlockRectInCss();
                    console.log(rect);
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

        var jIfmLoad = UI.arena.find(".hmpg-frame-load");
        jIfmLoad.prop("src", "/o/read/id:"+o.id);
    },
    //...............................................................
    // 获取编辑操作时的上下文
    _rebuild_context : function() {
        var UI = this;

        // TODO 有必要是，来个 jIfmTmpl 用来加载模板文件
        var jIfmLoad = UI.arena.find(".hmpg-frame-load");
        var jIfmEdit = UI.arena.find(".hmpg-frame-edit");

        // 创建上下文对象
        var C = {
            // 加载的 iFrame
            iload : {
                ifrm  : jIfmLoad[0],
                doc   : jIfmLoad[0].contentDocument,
                root  : jIfmLoad[0].contentDocument.documentElement,
            },
            // 编辑的 iframe
            iedit : {
                ifrm  : jIfmEdit[0],
                doc   : jIfmEdit[0].contentDocument,
                root  : jIfmEdit[0].contentDocument.documentElement,
            },
            $screen : UI.arena.find(".hmpg-screen"),
            $pginfo : UI.arena.find(".hmpg-sbar .hmpg-pginfo"),
        };

        // 确保加载区的 dom 合法


        // 设置 HTML 到编辑区
        C.iedit.root.innerHTML = C.iload.root.innerHTML;

        // 重新索引快捷标记
        UI._reset_context_vars(C.iload);
        UI._reset_context_vars(C.iedit);

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
    _reset_context_vars : function(cobj) {
        cobj.head  = cobj.doc.head;
        cobj.body  = cobj.doc.body;
        cobj.$root = $(cobj.root);
        cobj.$head = $(cobj.head);
        cobj.$body = $(cobj.body);
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
        UI._reset_context_vars(C.iload);

        // 清空 iload 的头部
        C.iload.$head.empty();
        C.iload.$body.removeAttr("pointer-moving-enabled");

        // 移除所有的辅助节点
        C.iload.$body.find(".mvrz-ass, .hm-del-save").remove();

        // 删除所有的块和控件的 CSS 渲染属性
        C.iload.$body.find(".hm-block,.hmb-con,.hmb-area,.hm-com,.ui-arena").each(function(){
            var jq = $(this).removeAttr("style");

            // 块
            if(jq.hasClass("hm-block")) {
                jq.removeAttr("mvrz-block");
                jq.removeAttr("hm-actived");
            }
            // 控件
            else if(jq.hasClass("hm-com")) {
                jq.removeAttr("ui-id");
            }
        }).filter(".hm-com").each(function(){
            $(this).removeAttr("ui-id");
        });

        // 最后整理所有的空节点，让其为一个回车
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

        // 返回 HTML
        return '<!DOCTYPE html>\n<html>\n' + C.iload.$root.html() + '\n</html>\n';;
    },
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);