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
var JS_LIB = { 
    "@jquery"       : '/gu/rs/core/js/jquery/jquery-2.1.3/jquery-2.1.3.min.js',
    "@underscore"   : '/gu/rs/core/js/backbone/underscore-1.8.2/underscore.js',
    "@backbone"     : '/gu/rs/core/js/backbone/backbone-1.1.2/backbone.js',
    "@vue"          : '/gu/rs/core/js/vue/vue.js',
    "@alloy_finger" : '/gu/rs/core/js/alloy_finger/alloy_finger.js',
    "@zutil"        : '/gu/rs/core/js/nutz/zutil.js',
    "@dateformat"   : '/gu/rs/core/js/ui/dateformat.js',
};
//==============================================
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
        <div class="hmpg-screen" mode="pc"><iframe class="hmpg-frame-edit"></iframe></div>
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
            <li ctype="image" tag-name="A"
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
            // 绘制控件
            uiCom.paint(com);

            // 重新应用皮肤
            UI.invokeSkin("resize");
        });

        // 这里分配一个控件序号数组，采用 bitMap，序号从 0 开始一直排列
        UI._com_seq = [];
    },
    //...............................................................
    // 分配一个组件需要，并做记录
    assignComId : function(jCom) {
        var comId = jCom.attr("id");
        if(!comId){
            var ctype = jCom.attr("ctype");
            // 遍历所有同类控件，找到最大的那个 ID 序号
            var seq   = 0;
            var regex = new RegExp("^"+ctype+"_([\\d]+)$");
            this._C.iedit.$body.find('.hm-com[ctype="'+ctype+'"]').each(function(){
                var theId = this.id;
                var m = regex.exec(theId);
                if(m) {
                    seq = Math.max(seq, m[1] * 1);
                }
            });
            // 嗯，这应该就是新控件的序号
            var comId = ctype + "_" + (++seq);
            // 为了保险，再做个判断
            while(this._C.iedit.$body.find("#"+comId).length > 0) {
                comId = ctype + '_' + (++seq);
            }
            // 设置新 ID
            jCom.attr("id",comId);
        }
        // 返回
        return comId;
    },
    //...............................................................
    events : {
        // 插入控件
        "click .hmpg-ibar li[ctype]" : function(e){
            var jLi   = $(e.currentTarget);
            this.doInsertCom(jLi.attr("ctype"), jLi.attr("tag-name"));
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
        UI.assignComId(jCom);

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
    doInsertCom : function(ctype, tagName) {
        var UI = this;
        
        // 创建组件的 DOM
        var eleCom = UI._C.iedit.doc.createElement(tagName || 'DIV');
        var jCom = $(eleCom).addClass("hm-com")
                        .attr("ctype", ctype)
                            .appendTo(UI._C.iedit.$body);
        
        // 初始化 UI
        UI.bindComUI(jCom, function(uiCom){
            // 设置初始化数据
            var com   = uiCom.setData({}, true);
            var block = uiCom.setBlock({});
            
            // 通知激活控件
            uiCom.notifyActived(null);

            // 通知皮肤
            this.invokeSkin("resize");
            
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
        var prevCom = UI.doBlurActivedCom(uiCom);
        
        // 激活自己
        jCom.attr("hm-actived", "yes");
        $z.invoke(uiCom, "on_actived", [prevCom]);
    },
    //...............................................................
    doBlurActivedCom : function(nextCom) {
        var re = [];
        this._C.iedit.$body.find("[hm-actived]").each(function(){
            var jCom  = $(this).removeAttr("hm-actived");
            var uiCom = ZUI(jCom);
            $z.invoke(uiCom, "on_blur", [nextCom]);
            re.push(uiCom);
        });

        // 应用皮肤
        this.invokeSkin("resize");

        return re.length > 0 ? re[0] : null;
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
    
        // 释放之前的皮肤 JS
        UI.invokeSkin("off");
    
        // 加载皮肤声明了 JS
        if(skinInfo.js) {
            var src = $z.tmpl('/o/read/home/{{d1}}/.hmaker/skin/{{skinName}}/skin.js?aph=true')({
                d1       : oHome.d1,
                skinName : skinName,
            });
            seajs.use(src, function(SkinJS){
                // 记录这个皮肤JS
                UI._C.SkinJS = SkinJS;
                // 应用
                UI.invokeSkin("on");
                UI.invokeSkin("resize");
            });
        }

        // 确保样式加入到 body
        UI._C.iedit.$root.attr("skin", skinInfo.name || null);
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

            // 重新应用皮肤
            this.invokeSkin("resize");
        }
    },
    //...............................................................
    /* 
    收集一下所有的区域显示菜单，对应的区域，返回
    {
        "布局组件ID" : {
            "area-id" : true | false
        }
    }
    */
    getToggleAreaMap : function(){
        var layoutMap = {};

        // 找 ...
        this._C.iedit.$body.find('.hm-com[navmenu-atype="toggleArea"]')
        .each(function(){
            var uiCom = ZUI(this);
            var com   = uiCom.getData();
            // 关联的某个布局 ...
            if(com.layoutComId) {
                var map = uiCom.joinToggleAreaMap(layoutMap[com.layoutComId], com);
                layoutMap[com.layoutComId] = map;
            }
        });

        return layoutMap;
    },
    //...............................................................
    cleanToggleArea : function() {
        var UI = this;

        // 菜单们都关联了哪些区域
        var layoutMap = UI.getToggleAreaMap();

        console.log(layoutMap)

        // 找到所有的布局控件
        UI._C.iedit.$body.find('.hm-layout').each(function(){
            var jLayout = $(this);
            // 如果标识了被关联，那么看看是否在索引中
            if(jLayout.attr("toggle-on")) {
                var comId = jLayout.attr("id");
                // 不在的话，去掉关联标识
                if(!layoutMap[comId]){
                    jLayout.removeAttr("toggle-on");
                }
            }

            // 如果没有被关联，确保所有的子区域，显示属性被清除
            if(!jLayout.attr("toggle-on")) {
                jLayout.find(">.hm-com-W>.ui-arena>.hm-area")
                    .removeAttr("toggle-mode");
            }
        });
    },
    //...............................................................
    // 指定的一个布局控件，将其关联为区域显示
    // 给定了 areaMap，表示这个表里面的区域才会被关联
    // 其他的统统无关
    toggleLayout : function(layoutId, areaMap) {
        var UI = this;
        // console.log("haha", areaMap, layoutId)
        UI._C.iedit.$body.find("#" + layoutId).attr({
            "toggle-on" : "yes" 
        }).find(">.hm-com-W>.ui-arena>.hm-area").each(function(){
            var jArea = $(this);
            var aid   = jArea.attr("area-id");
            var mode  = areaMap[aid];
            // console.log(aid, mode)
            // 关联区域: 显示
            if("yes" == mode) {
                jArea.attr("toggle-mode", "show")
            }
            // 关联区域: 不显示
            else if(mode) {
                jArea.attr("toggle-mode", "hide")
            }
            // 否则无关
            else {
                jArea.attr("toggle-mode", "ignore")
            }
        });
    },
    //...............................................................
    setToggleArea : function(layoutId, areaId) {
        var UI = this;
        UI._C.iedit.$body.find("#" + layoutId + '>.hm-com-W>.ui-arena>.hm-area')
            .each(function(){
                var jArea = $(this);
                var aid   = jArea.attr("area-id");
                if(jArea.attr("toggle-mode") != "ignore"){
                    jArea.attr("toggle-mode", aid == areaId ? "show" : "hide");
                }
            });
    },
    //...............................................................
    // 标识当前菜单对应的 toggle 区域
    setToggleCurrent : function(layoutId) {
        var UI = this;
        // 先取消了
        UI._C.iedit.$body.find(".hm-layout[toggle-current]")
            .removeAttr("toggle-current");
        // 给了 ID 就标识一下
        if(layoutId) {
            UI._C.iedit.$body.find("#" + layoutId)
                .attr("toggle-current", "yes");
        }
    },
    //...............................................................
    // 得到本页所有布局控件的信息列表
    getLayoutList : function(){
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
    getLayoutAreaList : function(comId){
        if(!comId)
            return [];

        var UI = this;
        var uiCom = UI.getCom(comId);

        // 没有组件，返回就是空
        if(!uiCom)
            return [];

        // 返回
        return $z.invoke(uiCom, "getAreaObjList", []) || [];
    },
    //...............................................................
    isAssistedOff : function() {
        return this.local("assisted_off");
    },
    // 设置是否显示辅助线 
    // isOff :  true 表隐藏辅助线, 
    setAssistedOff : function(isOff){
        this.local("assisted_off", isOff);
        this.syncAssistedMark();
    },
    //...............................................................
    getScreenMode : function(){
        return this.local("screen_mode");
    },
    setScreenMode : function(mode) {
        this.local("screen_mode", mode);
        this.arena.find(".hmpg-screen").attr("mode", mode);
    },
    //...............................................................
    redraw : function(){
        var UI  = this;

        // 确保屏幕的模式
        UI.arena.find(".hmpg-screen").attr("mode", this.getScreenMode());
        window.setTimeout(function(){
            UI.arena.find(".hmpg-screen").attr("animat-on", "yes");
        }, 0);
        
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
                        UI.invokeSkin("resize");
                    }
                }
            },{
                type : "separator"
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
                        $z.blinkIt(uiCom.$el);
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
                        $z.blinkIt(uiCom.$el);
                    }
                }
            },{
                type : "separator"
            },{
                key      : 'assisted_showhide',
                tip      : "i18n:hmaker.page.assisted_showhide",
                type     : "boolean",
                icon_on  : '<i class="zmdi zmdi-border-all"></i>',
                icon_off : '<i class="zmdi zmdi-border-clear"></i>',
                on_change : function(isOn) {
                    UI.setAssistedOff(!isOn);
                },
                init : function(mi){
                    mi.on = !UI.isAssistedOff();
                }
            },{
                type : "separator"
            },{
                key  : 'screen_mode',
                type : "status",
                status : [{
                    icon : '<i class="zmdi zmdi-laptop"></i>',
                    val  : 'pc'
                }, {
                    icon : '<i class="zmdi zmdi-smartphone-android"></i>',
                    val  : 'phone'
                }],
                on_change : function(mode){
                    UI.setScreenMode(mode);
                },
                init : function(mi){
                    var mode = UI.getScreenMode();
                    mi.status.forEach(function(si){
                        si.on = (si.val == mode);
                    });
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
    resize : function() {
        this.invokeSkin("resize");
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
        
        // 清空编辑区
        $(rootEdit).empty();

        // 设置 HTML 到编辑区
        rootEdit.innerHTML = rootLoad.innerHTML;

        // 重新索引快捷标记
        C.iload = UI._reset_context_vars(".hmpg-frame-load");
        C.iedit = UI._reset_context_vars(".hmpg-frame-edit");

        // 记录上下文
        UI._C = C;
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
        // 需要将所有的可运行的 script 都删掉
        C.iload.root.innerHTML = C.iedit.root.innerHTML;
        C.iload = UI._reset_context_vars(".hmpg-frame-load");

        // 清空 iload 的头部
        // C.iload.$head.empty();

        // 移除 body 的一些属性
        C.iload.$body.attr({
            "pointer-moving-enabled" : null,
            "style" : null
        });

        // 删除全部的 style 属性
        C.iload.$body.find("[style]").removeAttr("style");

        // 处理所有的控件，删掉临时属性和辅助节点
        C.iload.$body.find(".hm-com").each(function(){
            // 删掉临时的 style 样式
            var jCom = $(this).attr({
                "ui-id" : null,
                "c_seq" : null,
                "hm-actived" : null
            });
            var jW   = jCom.children(".hm-com-W");
            
            jW.children(".ui-arena").removeAttr("style");
            
            // 删掉辅助节点
            jW.children(".hm-com-assist").remove();
        });
        
        // 所有标识删除的节点也要删除
        C.iload.$root.find(".hm-del-save, .ui-code-template, .ui-debug-mark")
            .remove();
        
        // 删除所有临时属性
        C.iload.$root.find('[del-attrs]').each(function(){
            var jq = $(this);
            var attrNames = jq.attr("del-attrs").split(",");
            var subs = jq.find("*").andSelf();
            for(var attrName of attrNames) {
                subs.removeAttr(attrName);
            }
        });
        
        // 删除所有的皮肤动态添加的节点
        C.iload.$head.find('[skin]').remove();
        
        // 所有的分栏和组件前面都加入一个回车
        C.iload.$root.find(".hm-com, .hm-area, meta, link, body").each(function(){
            if(this.firstChild)
                this.insertBefore(document.createTextNode("\n"),this.firstChild);
        });
        
        // 整理所有的空节点，让其为一个回车
        $z.eachTextNode(C.iload.$root, function(){
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
                "@::hmaker/pub_current_page",
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