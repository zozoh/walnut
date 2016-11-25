(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/menu/menu',
    'app/wn.hmaker2/support/hm__methods',
    'app/wn.hmaker2/support/hm_page_com_bar',
    'app/wn.hmaker2/support/hm_page_setup',
    'jquery-plugin/pmoving/pmoving',
    // 预先加载
    'app/wn.hmaker2/component/columns.js',
    'app/wn.hmaker2/component/image.js',
    'app/wn.hmaker2/component/text.js',
    'app/wn.hmaker2/component/objlist.js',
    'app/wn.hmaker2/component/objshow.js',
], function(ZUI, Wn, MenuUI, HmMethods, PageComBarUI, PageSetup){
//==============================================
var html_empty_prop = function(){/*
<div class="ui-arena">
    empty prop
</div>
*/};
var html = `
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
            <!--li ctype="imgslider"
                data-balloon="{{hmaker.com.imgslider.name}} : {{hmaker.com.imgslider.tip}}" 
                data-balloon-pos="left" data-balloon-length="medium">
                <%=hmaker.com.imgslider.icon%>
            </li-->
            <li ctype="objlist"
                data-balloon="{{hmaker.com.objlist.name}} : {{hmaker.com.objlist.tip}}" 
                data-balloon-pos="left" data-balloon-length="medium">
                <%=hmaker.com.objlist.icon%>
            </li>
            <li ctype="objshow"
                data-balloon="{{hmaker.com.objshow.name}} : {{hmaker.com.objshow.tip}}" 
                data-balloon-pos="left" data-balloon-length="medium">
                <%=hmaker.com.objshow.icon%>
            </li>
        </ul>
    </div></div>
</div></div>`;
//==============================================
return ZUI.def("app.wn.hmaker_page", {
    dom : html,
    //...............................................................
    init : function() {
        var UI = PageSetup(HmMethods(this));

        // 监听 Bus 的各种事件处理页面上的响应
        UI.listenBus("active:com",   UI.doActiveCom);
        UI.listenBus("active:page",  UI.doBlurActivedCom);
        UI.listenBus("change:site:skin", UI.doChangeSkin);
        
        UI.listenBus("change:block", function(mode, uiCom, block){
            if("page" == mode)
                return;
            //console.log("hm_page::on_change:block:", mode,uiCom.uiName, block);
            uiCom.applyBlock(block);
        });
        UI.listenBus("change:com", function(mode, uiCom, com){
            if("page" == mode)
                return;
            //console.log("hm_page::on_change:com:", mode, uiCom.uiName, com);
            
            // 移除旧皮肤
            if(com._skin_old) {
                uiCom.$el.removeClass(com._skin_old);
            }
            
            // 添加新皮肤
            if(com.skin) {
                uiCom.$el.addClass(com.skin);
            }
            
            // 绘制控件
            uiCom.paint(com);
        });

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
            // 得到组件的类型
            var ctype = $(e.currentTarget).attr("ctype");

            // 插入控件
            this.doInsertCom(ctype);
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
    bindComUI : function(jCom, callback) {
        var UI = this;
        
        // 确保控件内任意一个元素均等效
        jCom = jCom.closest(".hm-com");
        
        // 确保有组件序号
        UI.assignComSequanceNumber(jCom);

        // 看看是否之前就绑定过
        var uiId  = jCom.attr("ui-id");

        // 已经绑定了 UI，那么继续弄后面的
        if(uiId) {
            var uiCom = ZUI(uiId);
            $z.doCallback(callback, [uiCom], UI);
            return uiCom;
        }
        // 否则根据类型加载 UI 吧
        var ctype = jCom.attr("ctype");
        if(!ctype) {
            console.warn(ctype, jCom);
            throw "fail to found ctype from jCom";
        }
        
        // 执行加载
        seajs.use("app/wn.hmaker2/component/"+ctype, function(ComUI){
            jCom.css("visibility", "hidden");
            new ComUI({
                parent  : UI,
                $el     : jCom,
            }).render(function(){
                $z.doCallback(callback, [this], UI);
                jCom.css("visibility", "");
            })
        });
    },
    //...............................................................
    doInsertCom : function(ctype) {
        var UI = this;
        
        // 创建组件的 DOM
        var jCom = $('<div class="hm-com">').attr({
            "ctype"   : ctype
        }).appendTo(UI._C.iedit.$body);
        
        // 初始化 UI
        UI.bindComUI(jCom, function(uiCom){
            // 设置初始化数据
            var com   = uiCom.setData({}, true);
            var block = uiCom.setBlock({});
            
            // 通知激活控件
            uiCom.notifyActived();
            
            // 通知改动
            uiCom.notifyBlockChange(null, block);
            uiCom.notifyDataChange(null, com);
        });
    },
    //...............................................................
    doActiveCom : function(uiCom) {
        var UI   = this;
        var jCom = uiCom.$el;
        
        // 当前已经是激活
        if(jCom.attr("hm-actived"))
            return;
        
        // 取消其他激活的控件
        UI.doBlurActivedCom();
        
        // 激活自己
        jCom.attr("hm-actived", "yes");
    },
    //...............................................................
    doBlurActivedCom : function() {
        this._C.iedit.$body.find("[hm-actived]").removeAttr("hm-actived");
    },
    //...............................................................
    doChangeSkin : function(){
        var UI = this;

        // 得到皮肤的信息
        var oHome    = UI.getHomeObj();
        var skinName = oHome.hm_site_skin;
        var skinInfo = UI.getSkinInfo() || {};

        // 更新样式
        var jHead = UI._C.iedit.$head;
        UI._H(jHead, 'link[skin]', !skinName ? null
            : $z.tmpl('<link skin="yes" rel="stylesheet" type="text/css" href="/o/read/home/{{d1}}/.hmaker/skin/{{skinName}}/skin.css?aph=true">')({
                d1       : oHome.d1,
                skinName : skinName,
            }));

        // 确保样式加入到 body
        UI._C.iedit.$body.attr("skin", skinInfo.name || null);
    },
    //...............................................................
    setup_page_editing : function(){
        var UI = this;

        // 建立上下文: 这个过程，会把 load 的 iframe 内容弄到 edit 里
        UI._rebuild_context();

        //.......................... 下面的方法来自 support/hm_page_setup.js
        // 设置编辑区页面的 <head> 部分
        UI.__setup_page_head();

        // 设置编辑区的移动
        UI.__setup_page_moveresizing();

        // 监视编辑区，响应其他必要的事件处理
        UI.__setup_page_events();
        //.......................... 上面的方法来自 support/hm_page_setup.js

        // 处理所有的块显示
        UI._C.iedit.$body.find(".hm-com").each(function(){
            // 处理块中的组件
            var jCom = $(this);

            if(jCom.size()==0) {
                console.log("no jCom", jBlock.html());
            }

            // 绑定 UI，并显示
            UI.bindComUI(jCom);
        });

        // 应用网页显示样式
        UI.applyPageAttr();

        // 通知网页被加载
        UI.fire("active:page");

        // 模拟第一个块被点击
        window.setTimeout(function(){
            UI._C.iedit.$body.find(".hm-com").first().click();
        }, 500);
    },
    //...............................................................
    __after_iframe_loaded : function(name) {
        var UI = this;

        // 移除加载完毕的项目
        UI._need_load = _.without(UI._need_load, name);

        // 全部加载完毕了
        if(UI._need_load.length == 0){
            UI.setup_page_editing();
            
            // 显示 iFrame
            UI.arena.find(".hmpg-frame-edit").css("visibility", "");
        }
    },
    //...............................................................
    getActivedCom : function() {
        var UI = this;
        var jCom = UI._C.iedit.$body.find(".hm-com[hm-actived]");
        if(jCom.length > 0) {
            return ZUI(jCom);
        }
    },
    //...............................................................
    getCom : function(comId) {
        var jCom;
        if(_.isString(comId)){
            jCom = this._C.iedit.$body.find("#" + comId);
        }
        // 直接就是 DOM
        else if(_.isElement(comId) || $z.isjQuery(comId)){
            jCom = $(comId);
        }
        // 不支持
        else {
            throw "unsupport getCom by => " + comId;
        }
        // 得到组件
        if(jCom.length > 0) {
            return ZUI(jCom.closest(".hm-com"));
        }
    },
    //...............................................................
    deleteCom : function(uiCom) {
        if(uiCom) {
            uiCom.destroy();
            uiCom.$el.remove();
        }
    },
    //...............................................................
    // 得到本页所有布局控件的信息列表
    getLayoutComInfoList : function(){
        var UI = this;
        var _C = UI._C;

        // 准备返回值
        var re = [];

        // 找到所有的分栏控件
        _C.iedit.$body.find('.hm-com[ctype="rows"],.hm-com[ctype="columns"]')
            .each(function(){
                var jCom = $(this);
                re.push({
                    cid   : jCom.attr("id"),
                    ctype : jCom.attr("ctype")
                });
            });

        // 返回
        return re;
    },
    //...............................................................
    // 取得某个布局控件内部的可用区域（不包括子控件的区域）
    getLayoutComBlockDataArray : function(comId){
        var UI = this;
        var jCom  = UI.getComElementById(comId);
        var uiCom = UI.bindComUI(jCom);

        // 没有组件，返回就是空
        if(!uiCom)
            return [];

        // 返回
        return uiCom.getBlockDataArray();
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

        // 组件条
        new PageComBarUI({
            parent : UI,
            gasketName : "combar",
        }).render(function(){
            UI.defer_report("combar");
        });
       
        // 菜单条
        new MenuUI({
            parent : UI,
            gasketName : "pagebar",
            setup : [{
                icon : '<i class="zmdi zmdi-upload"></i>',
                text : 'i18n:hmaker.page.move_to_body',
                handler : function() {
                    var uiCom = UI.getActivedCom();
                    if(uiCom){
                        uiCom.appendToArea(null);
                    }
                }
            },{
                icon : '<i class="zmdi zmdi-long-arrow-up"></i>',
                tip  : 'i18n:hmaker.page.move_before',
                handler : function() {
                    var uiCom = UI.getActivedCom();
                    if(uiCom) {
                        var jPrev = uiCom.$el.prev();
                        if(jPrev.length > 0) {
                            uiCom.$el.insertBefore(jPrev);
                        }
                    }
                }
            },{
                icon : '<i class="zmdi zmdi-long-arrow-down"></i>',
                tip  : 'i18n:hmaker.page.move_after',
                handler : function() {
                    var uiCom = UI.getActivedCom();
                    if(uiCom) {
                        var jNext = uiCom.$el.next();
                        if(jNext.length > 0) {
                            uiCom.$el.insertAfter(jNext);
                        }
                    }
                }
            }]
        }).render(function(){
            UI.defer_report("pagebar");
        });
        
        // 返回延迟加载
        return ["combar", "pagebar"];
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
        jIfmEdit.css("visibility", "hidden");
        jIfmEdit.prop("src", "/a/load/wn.hmaker2/page_loading.html");

        var jIfmLoad = UI.arena.find(".hmpg-frame-load");
        jIfmLoad.prop("src", "/o/read/id:"+o.id);
    },
    //...............................................................
    $editBody : function() {
        return this._C.iedit.$body;
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
        C.iload.$body.attr({
            "pointer-moving-enabled" : null,
            "style" : null,
            "skin" : null
        });

        // 处理所有的控件，删掉临时属性和辅助节点
        C.iload.$body.find(".hm-com").each(function(){
            // 删掉临时的 style 样式
            var jCom = $(this).attr({
                "style" : null,
                "ui-id" : null,
                "hm-actived" : null
            });
            var jW   = jCom.children(".hm-com-W");
            
            jW.children(".ui-arena").removeAttr("style");
            
            // 删掉辅助节点
            jW.children(".hm-com-assist").remove();
            
            // 所有标识删除的节点也要删除
            jW.find(".hm-del-save, .ui-code-template").remove();
            
            // 删除所有临时属性
            jW.find('[del-attrs]').each(function(){
                var jq = $(this);
                var attrNames = jq.attr("del-attrs").split(",");
                console.log(attrNames)
                var subs = jq.find("*").andSelf();
                for(var attrName of attrNames) {
                    subs.removeAttr(attrName);
                }
            });
        });
        
        // 所有的分栏和组件前面都加入一个回车
        C.iload.$body.find(".hm-com, .hm-area").each(function(){
            this.parentNode.insertBefore(document.createTextNode("\n"),this);
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
        
        // 返回 HTML
        return '<!DOCTYPE html>\n<html>\n' + C.iload.$root.html() + '\n</html>\n';;
    },
    //...............................................................
    getActions : function(){
        return ["@::save_text",
                "::hmaker/hm_create", 
                "::hmaker/hm_delete",
                "~",
                "::view_text",
                "~",
                "::hmaker/pub_site",
                "::hmaker/pub_current_page",
                "~",
                "::hmaker/hm_site_conf",
                "~",
                "::zui_debug",
                "::open_console"];
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);