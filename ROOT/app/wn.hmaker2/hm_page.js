(function($z){
// ......................... 以下是帮助函数
var _H = function(jHead, selector, html) {
    if(jHead.children(selector).size() == 0){
        jHead.prepend($(html));
    }
};
// ......................... 以上是帮助函数
$z.declare([
    'zui',
    'wn/util',
    'ui/menu/menu',
    'jquery-plugin/pmoving/pmoving',
    'jquery-plugin/moveresizing/moveresizing',
], function(ZUI, Wn, MenuUI, TreeUI){
//==============================================
var html = function(){/*
<div class="ui-code-template">
    <div code-id="block" class="hm-block">
        <div class="hmb-con">
            <div class="hmb-area"></div>
        </div>
    </div>
    <div code-id="com" class="hm-com">
        <script type="text/x-template" class="hmc-prop"></script>
        <div class="ui-arena"></div>
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
            <li ctype="block" data-balloon="<%=hmaker.com.block.tip%>" data-balloon-pos="left" data-balloon-length="medium">
                <i class="zmdi zmdi-view-column"></i>
            </li>
            <li ctype="text" data-balloon="<%=hmaker.com.text.tip%>" data-balloon-pos="left" data-balloon-length="medium">
                <i class="fa fa-text-width"></i>
            </li>
            <li ctype="image" data-balloon="<%=hmaker.com.image.tip%>" data-balloon-pos="left" data-balloon-length="medium">
                <i class="fa fa-image"></i>
            </li>
            <li ctype="slider" data-balloon="<%=hmaker.com.slider.tip%>" data-balloon-pos="left" data-balloon-length="medium">
                <i class="zmdi zmdi-slideshow"></i>
            </li>
            <li ctype="thingSet" data-balloon="<%=hmaker.com.thingSet.tip%>" data-balloon-pos="left" data-balloon-length="medium">
                <i class="fa fa-cubes"></i>
            </li>
            <li ctype="thingObj" data-balloon="<%=hmaker.com.thingObj.tip%>" data-balloon-pos="left" data-balloon-length="medium">
                <i class="fa fa-cube"></i>
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
        var UI = this;

        // TODO 这里监听 com/block/area 各自被 actived 后的处理
        UI.listenParent("block:actived", UI.doActiveBlock);
        UI.listenParent("block:change",  UI.doUpdateBlock);
    },
    //...............................................................
    events : {
        "click .hmpg-ibar li[ctype]" : function(e){
            var UI  = this;
            var jLi = $(e.currentTarget);

            // 首先插入一个块
            var jBlock = UI.doInsertBlock();

            // 得到组件的类型
            var ctype = jLi.attr("ctype");

            // 根据类型(如果不是 block) 在块中创建对应的组件
            if("block" != ctype) {
                var jArea = jBlock.find(".hmb-area");
                // 创建组件的 DOM
                var jCom = UI.ccode("com").attr("ctype",ctype).appendTo(jArea);

                // 绑定组件的 UI 对象，并激活它
                UI.bindComUI(jCom, function(uiCom){
                    UI.fire("com:actived", uiCom);
                });
            }
            // 仅仅激活块
            else {
                console.log("fire!!!")
                UI.fire("block:actived", jBlock);
            }
        }
    },
    //...............................................................
    doInsertBlock : function() {
        var UI = this;
        var C  = UI.C();

        var jBlock = UI.ccode("block").appendTo(C.iedit.$body);
        UI.applyBlockProp(jBlock, {}, true);

        C.iedit.$body.moveresizing("format");

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
            background : "#CCC",
            color : "#F00",
        };
    },
    //...............................................................
    getBlockViewport : function(jBlock){
        jBlock = $(jBlock || this.getActivedBlock());
        var jArea = jBlock.closest(".hmb-area");
        if(jArea.size() > 0)
            return jArea;
        return jBlock.closest("body");
    },
    //...............................................................
    getBlockRectInCss : function(jBlock) {
        var UI = this;
        jBlock = jBlock || UI.getActivedBlock();
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
            jBlock = UI.getActivedBlock();
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
        jBlock = jBlock || UI.getActivedBlock();
        console.log("apply", prop)

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
            var pKey  = $z.lowerWord(key);
            css[pKey] = val;
        }

        // 最后应用一下 CSS
        jBlock.children().css(css);
    },
    //...............................................................
    setup_page_editing : function(){
        var UI = this;
        var C  = UI.C();

        // 读取加载的 HTML
        //console.log("setup_page_editing!");
        var html = C.iload.root.innerHTML;
        //console.log(html);

        // 设置到编辑区
        C.iedit.root.innerHTML = html;

        // 设置编辑区页面的 <head> 部分
        UI.__setup_page_head();

        // 设置编辑区的移动
        UI.__setup_page_moveresizing();

        // 循环整个编辑区，应用上控件
    },
    //...............................................................
    __setup_page_head : function() {
        var UI = this;
        var C  = UI.C();

        // 首先清空
        var jHead = C.iedit.$head.empty();

        // 链入固定的 CSS 
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
    __setup_page_moveresizing : function() {
        var UI = this;
        var C  = UI.C();

        // 通知移动的过程
        var notify_move_or_resize = function(rect) {
            var vals = UI.transRectToPosVal(rect, this.$block.attr("hmb-pos-by"));
            UI.fire("block:change", {
                posVal : vals
            });
        };

        // 监视控件的拖拽
        C.iedit.$body.moveresizing({
            trigger : '.hm-block[hmb-mode="abs"]',
            findViewport : function(){
                return UI.getBlockViewport(this);
            },
            delay : 50,
            maskClass : "hm-page-move-mask",
            on_begin : function() {
                
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
    bindComUI : function(jCom, callback) {
        var UI = this;

        // TODO 这里根据控件获取 UI

        $z.doCallback(callback, [uiCom], UI);
    },
    //...............................................................
    // 找到当前的操作区，如果没有，那么默认为整个 body
    // 返回的是一个 jQuery 对象
    getActivedCom : function() {
        return this.C().iedit.$body.find(".hm-com[hm-actived]");
    },
    getActivedArea : function() {
        return this.C().iedit.$body.find(".hmb-area[hm-actived]");
    },
    getActivedBlock : function() {
        return this.C().iedit.$body.find(".hm-block[hm-actived]");
    },
    //...............................................................
    doActiveBlock : function(jq) {
        var UI = this;
        var C  = UI.C();

        // 确定激活块
        var jBlock = jq.closest(".hm-block");
        //console.log("abc", UI.uiName)

        // 只有被激活的，才再次被激活
        if(jBlock.size()>0 && !jBlock.attr("hm-actived")) {
            // 取消其他的激活项目
            C.iedit.$body.find(".hm-block[hm-actived]").removeAttr("hm-actived");

            // 激活自身
            jBlock.attr("hm-actived", "yes");
        }
    },
    //...............................................................
    doUpdateBlock : function(prop) {
        var UI = this;
        var C  = UI.C();

        var jBlock = UI.getActivedBlock();
        UI.applyBlockProp(jBlock, prop, false);
    },
    //...............................................................
    redraw : function(){
        var UI  = this;
        
        // 绑定隐藏 iframe onload 事件，这个 iframe 专门用来与服务器做数据交换的
        var jIfmLoad = UI.arena.find(".hmpg-frame-load");
        if(!jIfmLoad.attr("onload-bind")){
            jIfmLoad.bind("load", function(e){
                //console.log("hmaker_page: iframe onload", Date.now(), e);
                UI.setup_page_editing();
            });
            jIfmLoad.attr("onload-bind", "yes");
        }
        
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

        var jIfmLoad = UI.arena.find(".hmpg-frame-load");
        jIfmLoad.prop("src", "/o/read/id:"+o.id);
    },
    //...............................................................
    // 获取编辑操作时的上下文
    C : function() {
        var UI = this;

        // TODO 有必要是，来个 jIfmTmpl 用来加载模板文件
        var jIfmLoad = UI.arena.find(".hmpg-frame-load");
        var jIfmEdit = UI.arena.find(".hmpg-frame-edit");

        return {
            // 加载的 iFrame
            iload : {
                ifrm  : jIfmLoad[0],
                doc   : jIfmLoad[0].contentDocument,
                root  : jIfmLoad[0].contentDocument.documentElement,
                $root : $(jIfmLoad[0].contentDocument.documentElement),
                $head : $(jIfmLoad[0].contentDocument.head),
                $body : $(jIfmLoad[0].contentDocument.body),
            },
            // 编辑的 iframe
            iedit : {
                ifrm  : jIfmEdit[0],
                doc   : jIfmEdit[0].contentDocument,
                root  : jIfmEdit[0].contentDocument.documentElement,
                $root : $(jIfmEdit[0].contentDocument.documentElement),
                $head : $(jIfmEdit[0].contentDocument.head),
                $body : $(jIfmEdit[0].contentDocument.body),
            },
            $screen : UI.arena.find(".hmpg-screen"),
            $pginfo : UI.arena.find(".hmpg-sbar .hmpg-pginfo"),
        };
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);