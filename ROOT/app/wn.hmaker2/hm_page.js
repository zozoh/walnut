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
    <div code-id="ibar.ibox" class="hmpg-ibar-ibox"><div>
        <header>
            <b></b><em></em>
            <a class="hm-ireload">{{hmaker.page.ireload}}</a>
        </header>
        <section></section>
    </div></div>
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
        <h4 class="hm-ibtn"><i class="zmdi zmdi-plus"></i><span>{{hmaker.page.insert}}</span></h4>
        <ul>
            <li ctype="rows" icon="hmaker.com.rows.icon" 
                text="hmaker.com.rows.name" tip="hmaker.com.rows.tip"></li>
            <li ctype="columns" icon="hmaker.com.columns.icon" 
                text="hmaker.com.columns.name" tip="hmaker.com.columns.tip"></li>
            <li class="sep"></li>
            <li ctype="image" icon="hmaker.com.image.icon" tag-name="A"
                text="hmaker.com.image.name" tip="hmaker.com.image.tip"></li>
            <li ctype="navmenu" icon="hmaker.com.navmenu.icon" 
                text="hmaker.com.navmenu.name" tip="hmaker.com.navmenu.tip"></li>
            <li ctype="text" icon="hmaker.com.text.icon" 
                text="hmaker.com.text.name" tip="hmaker.com.text.tip"></li>
            <li ctype="htmlcode" icon="hmaker.com.htmlcode.icon"
                text="hmaker.com.htmlcode.name" tip="hmaker.com.htmlcode.tip"></li>
            <li class="sep"></li>
            <li ctype="dynamic" icon="hmaker.com.dynamic.icon"
                text="hmaker.com.dynamic.name" tip="hmaker.com.dynamic.tip"></li>
            <li ctype="searcher" icon="hmaker.com.searcher.icon"
                text="hmaker.com.searcher.name" tip="hmaker.com.searcher.tip"></li>
            <li ctype="filter" icon="hmaker.com.filter.icon"
                text="hmaker.com.filter.name" tip="hmaker.com.filter.tip"></li>
            <li ctype="sorter" icon="hmaker.com.sorter.icon"
                text="hmaker.com.sorter.name" tip="hmaker.com.sorter.tip"></li>
            <li ctype="pager" icon="hmaker.com.pager.icon"
                text="hmaker.com.pager.name" tip="hmaker.com.pager.tip"></li>
            <li class="sep"></li>
            <li ctype="objlist" icon="hmaker.com.objlist.icon" 
                text="hmaker.com.objlist.name" tip="hmaker.com.objlist.tip"></li>
            <!--li ctype="objshow" icon="hmaker.com.objshow.icon" 
                text="hmaker.com.objshow.name" tip="hmaker.com.objshow.tip"></li-->
            <li ctype="libitem" icon="hmaker.lib.icon" 
                text="hmaker.lib.item" tip="hmaker.lib.tip"></li>
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
    events : {
        // 修改插入条的宽高
        "click .hmpg-ibar .hm-ibtn" : function(){
            var UI = this;

            // 设置开关
            $z.toggleAttr(UI.arena, "full-ibar");

            // 本地记录状态
            UI.local("hm_ibar_full", UI.arena.attr("full-ibar") ? true : false);

            // 动画结束，确保 resize
            UI.arena.one("transitionend", function(){
                UI.resize(true);
            });
        },
        // 鼠标激活 ibar.ibox
        "mouseover .hmpg-ibar .hmpg-ibar-thumb" : function(e){
            var jq    = $(e.currentTarget);
            var jLi   = jq.closest("li");
            var jiBox = jLi.children(".hmpg-ibar-ibox");
            $z.dock(jq, jiBox, "V");
            this.doReloadIBarItem(jLi);
        },
        // 强制刷新 ibar 子项目
        "click .hmpg-ibar-ibox header .hm-ireload" : function(e){
            this.doReloadIBarItem($(e.currentTarget).parents("li"), true);
        },
        // 插入控件
        "click .hmpg-ibar .ibar-item" : function(e){
            var jItem   = $(e.currentTarget);
            var jLi     = jItem.parents("li");
            var ctype   = jLi.attr("ctype");
            var tagName = jLi.attr("tag-name") || 'DIV';
            this.doInsertCom(ctype, tagName, jItem.attr("val"));
        }
    },
    //...............................................................
    getPageAttr : function(includePageMeta){
        var UI   = this;
        var attr = $z.getJsonFromSubScriptEle(this._C.iedit.$body, "hm-page-attr");
        // 还要读取 page 的 title 属性
        if(includePageMeta) {
            attr.title = UI.getCurrentEditObj().title || null;
        }
        // 返回
        return attr;
    },
    setPageAttr : function(attr, merge){
        var UI = this;
        
        // 是不是与旧属性合并
        if(merge) {
            attr = _.extend(UI.getPageAttr(true), attr);
        }

        // 更新一下 page 的 title 属性
        var oPg = UI.getCurrentEditObj();
        if(oPg.title != attr.title) {
            Wn.execf('obj id:{{id}} -u \'title:"{{title}}"\' -o', {
                id    : oPg.id,
                title : attr.title || null
            }, function(re) {
                // 处理错误
                if(/^e./.test(re)) {
                    UI.alert(re);
                    return;
                }
                // 将最新结果计入缓存
                oPg = $z.fromJson(re);
                Wn.saveToCache(oPg);
                // 最后刷新一下资源
                UI.resourceUI().refresh();
            });
        }

        // 应用样式
        this.applyPageAttr(attr);

        // 保存到 DOM
        $z.setJsonToSubScriptEle(this._C.iedit.$body, 
            "hm-page-attr", 
            $z.pick(attr, "!^title$"), 
            true);
    },
    applyPageAttr : function(attr){
        var UI = this;
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 没指定 attr 就表示重新应用一下样式
        attr = attr || UI.getPageAttr();
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 挑选出 css 设置到 body 里
        var css = $z.pick(attr, "!^(title|links)$");
        UI._C.iedit.$body.css(css);
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~        
        // 最后同步一下页面的链接
        var links = attr.links || [];
        // 首先归纳
        var map = {};
        for(var i=0; i<links.length; i++) {
            var lnk = links[i];
            // 无视 js，因为编辑时不能加入
            if("js" == lnk.tp)
                continue;
            var aph = "/o/read"+lnk.ph+"?aph=true";
            map[aph] = lnk;
        }
        // 查看已有链接项目看看是否需要删除
        UI._C.iedit.$head.find('[page-link]').each(function(){
            var jq   = $(this);
            var href = jq.attr("href");
            // 不在需要链接这个资源
            if(!map[href]){
                jq.remove();
            }
            // 标记已存在
            else {
                map[href] = null;
            }
        });
        // 将剩余的项目添入 <head>
        for(var href in map) {
            if(map[href]) {
                $('<link page-link="yes">').attr({
                    "rel"  : "stylesheet",
                    "type" : "text/css",
                    "href" : href
                }).appendTo(UI._C.iedit.$head);
            }
        }
    },
    //...............................................................
    // 获取当前页面关联的所有 css 的可用选择器集合
    // 本函数会自动缓存，除非被调用 cleanCssSelectors
    // 否则在整个 PageUI 生命周期内，不会再次向服务器发送请求
    //
    // - flatThem 参数如果为 true 则会抚平集合，生成一个纯数组
    //
    // [{
    //    selector: "uuyytt",
    //    text: "哈哈哈",
    // }, {
    //    selector: "mmn-23-j_w",
    //    text: null,
    // }]
    //
    // 否则返回的格式为: 
    //
    //
    // {
    //     "css/abc.css" : [{
    //            selector: "uuyytt",
    //            text: "哈哈哈",
    //         }, {
    //            selector: "mmn-23-j_w",
    //            text: null,
    //         }],
    //     "css/bbb.css" : [..]
    // }
    //
    //
    getCssSelectors : function(flatThem){
        var UI = this;
        if(!UI.__css_selectors) {
            // 准备命令
            var cmdText = 'hmaker id:'+UI.getHomeObjId() + ' css';

            // 得到页面所有 css 的链接
            var attr = UI.getPageAttr();
            var lnks = attr.links || [];
            for(var i=0; i<lnks.length; i++){
                var lnk = lnks[i];
                if('css' == lnk.tp) {
                    cmdText += ' "' + lnk.rph + '"';
                }
            }
            
            // 执行命令
            var re = Wn.exec(cmdText);

            // 分析返回值
            UI.__css_selectors = $z.fromJson(re) || [];
        }

        // 抚平集合
        if(flatThem) {
            var re = [];
            for(var key in UI.__css_selectors) {
                var list = UI.__css_selectors[key];
                for(var i=0; i<list.length; i++){
                    re.push(_.extend({path:key}, list[i]));
                }
            }
            return re;
        }

        // 返回 
        return UI.__css_selectors;
    },
    //...............................................................
    // 获取当前页面关联的所有 css 的可用选择器集合
    // 得到的对象格式为:
    // {
    //      selector : Text
    // }
    // 本函数会缓存集合分析的结构，cleanCssSelectors 函数会清除这个缓存
    getCssSelectorMap : function() {
        var UI = this;
        // 分析
        if(!UI.__css_selector_map) {
            var ss = UI.getCssSelectors(true);
            UI.__css_selector_map = {};
            for(var i=0; i<ss.length; i++) {
                var so = ss[i];
                UI.__css_selector_map[so.selector] = so;
            }
        }
        // 返回 
        return UI.__css_selector_map;
    },
    // 根据一个 selector 返回其 text，
    // null 表示没声明，undefined 表示没有这个选择器
    getCssSelectorText : function(selector) {
        return (this.getCssSelectorMap()[selector] || {}).text;
    },
    // 根据一个 selector 返回其所在 css 文件路径,
    // null 或者 undefined 表示没有这个选择器的定义
    getCssSelectorPath : function(selector) {
        return (this.getCssSelectorMap()[selector] || {}).path;
    },
    cleanCssSelectors : function() {
        this.__css_selectors = null;
        this.__css_selector_map = null;
    },
    //...............................................................
    doReloadIBarItem : function(jLi, force) {
        var UI    = this;
        var jiBox = jLi.children(".hmpg-ibar-ibox");
        var jiSec = jiBox.find("section");

        // 开始读取
        if(force || jiSec.children().length == 0) {
            var ctype = jLi.attr("ctype");

            // 显示读取
            jiSec.html('<div class="loading">'+UI.msg("hmaker.page.ibarloading")+'</div>');

            // 组件库
            if("libitem" == ctype) {
                window.setTimeout(function(){
                    Wn.execf("hmaker id:{{homeId}} lib -list", {
                        homeId : UI.getHomeObjId()
                    }, function(re) {
                        var libNames = $z.fromJson(re);
                        // 空
                        if(libNames.length == 0) {
                            jiSec.find(".loading")
                                .html('<i class="zmdi zmdi-alert-circle-o"></i>'
                                     + UI.msg("hmaker.lib.empty"));
                        }
                        // 输出内容
                        else {
                            jiSec.empty();
                            for(var i=0; i<libNames.length; i++) {
                                var jDiv = $('<div class="ibar-item">');
                                jDiv.attr("val", libNames[i]);
                                jDiv.html(UI.msg("hmaker.lib.icon_item"));
                                jDiv.append($('<b>').text(libNames[i]));
                                jDiv.appendTo(jiSec);
                            }
                        }
                    });
                }, 200);
            }
            // 控件
            else {
                window.setTimeout(function(){
                    var skinList = UI.getSkinListForCom(ctype);
                    var icon = UI.msg(jLi.attr("icon"));
                    var text = UI.msg(jLi.attr("text"));

                    // 输出第一个项目
                    var list = [{
                        text : UI.msg("hmaker.com._.dft")+text,
                        selector : null
                    }].concat(skinList);

                    // 循环输出
                    jiSec.empty();
                    for(var i=0; i<list.length; i++) {
                        var li = list[i];
                        var jDiv = $('<div class="ibar-item">');
                        jDiv.attr("val", li.selector);
                        jDiv.html(icon);
                        jDiv.append($('<b>').text(li.text));
                        jDiv.appendTo(jiSec);
                    }
                }, 200);
            }
        }
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
    bindComUI : function(jCom, callback) {
        var UI = this;
        
        // 确保控件内任意一个元素均等效
        jCom = jCom.closest(".hm-com");

        // console.log(jCom[0])

        // 如果是无效控件，无视
        if(jCom.attr("invalid-lib")){
            // 显示
            jCom.css("visibility", "");
            // 回调
            $z.doCallback(callback, [this], UI);
            // 不在继续了
            return;
        }
        
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
                // 看看是否有父控件
                var jPCom  = jCom.parents(".hm-com");
                if(jPCom.length > 0) {
                    pUI = ZUI(jPCom);
                    //console.log(pUI, jPCom.attr("id"))
                    this.appendTo(pUI);
                }
                // 显示
                jCom.css("visibility", "");
                // 回调
                $z.doCallback(callback, [this], UI);
            })
        });
    },
    //...............................................................
    // ctype   : 控件类型，"libitem" 表示组件
    // tagName : 插入的元素名，默认 DIV
    // val     : 控件皮肤或者组件名
    doInsertCom : function(ctype, tagName, val) {
        var UI = this;

        // 创建新元素
        var eleCom = UI._C.iedit.doc.createElement(tagName || 'DIV');
        var jCom = $(eleCom).addClass("hm-com").appendTo(UI._C.iedit.$body);

        // 共享库组件
        if("libitem" == ctype) {
            if(!val) {
                UI.alert("hmaker.page.noLibName");
                return;
            }
            jCom.attr({"ctype":ctype, "lib":val});
            UI.reloadLibCode(jCom, function(uiCom){
                uiCom.notifyActived(null);
                UI.pageUI().invokeSkin("resize");
            });
        }
        // 普通控件
        else {
            jCom.attr({"ctype":ctype, "skin":val||null});
            // 初始化 UI
            UI.bindComUI(jCom, function(uiCom){
                // 设置初始化数据
                var com   = uiCom.setData({}, true);
                var block = uiCom.setBlock({});
                
                // 通知激活控件
                uiCom.notifyActived(null);

                // 通知控件更改
                uiCom.notifyBlockChange("panel");

                // 确保切换了皮肤
                uiCom.setComSkin(uiCom.getComSkin());

                // 通知皮肤
                this.invokeSkin("resize");
                
            });
        }
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

        // 移除所有的 ibar 项目
        UI.arena.find(".hmpg-ibar-ibox section").empty();

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
            // 建立上下文: 这个过程，会把 load 的 iframe 内容弄到 edit 里
            UI._rebuild_context();

            // 加载所有的组件
            //UI.__load_lib(UI._C.iedit.$body);

            // 设置可编辑
            UI.setup_page_editing();
            
            // 显示 iFrame
            UI.arena.find(".hmpg-frame-edit").css("visibility", "");
        }
    },
    //...............................................................
    // 在给定的范围内加载组件
    // __load_lib : function(jq) {
    //     var UI = this;

    //     // 缓存组件
    //     var cache  = {};
    //     var homeId = UI.getHomeObjId();

    //     // 在给定范围内查找所有的组件节点
    //     jq.find('.hm-com[lib]').each(function(){
    //         console.log(this)
    //         UI.reloadLibCode($(this), homeId, cache);
    //     });
    // },
    //...............................................................
    reloadLibCode : function(jCom, homeId, cache, callback) {
        var UI = this;

        // 接受快捷参数形式
        if(_.isFunction(homeId)){
            callback = homeId;
            homeId   = undefined;
            cache    = undefined;
        }

        homeId = homeId || UI.getHomeObjId(),
        cache  = cache  || {};
        //console.log(UI.parent.uiName)

        var libName = jCom.attr('lib');
        var comId   = UI.assignComId(jCom);
        // 得到组件的代码内容
        var html = cache[libName] || Wn.exec("hmaker id:"+homeId+" lib -read '"+libName+"'");
        cache[libName] = html;

        // 替换现有组件
        if(html && !/^e./.test(html)){
            // 老控件是否需要激活
            var comIsActived = jCom.attr("hm-actived") == "yes";
            var comUIbinded  = jCom.attr('ui-id') ? true : false;

            // 得到新的 COM DOM 结构
            var jCom2 = $(html).attr({
                "id"  : comId,
                "lib" : libName
            }).insertBefore(jCom);

            // 移除老的: 如果已经绑定了组件，注销组件
            if(comUIbinded){
                //console.log("destroy com:", jCom.attr('ui-id'));
                UI.deleteCom(ZUI(jCom));
            }
            // 否则直接移除就好
            else {
                //console.log("remove old");
                jCom.remove();
            }

            // 绑定控件
            var pageUI = UI.pageUI();
            pageUI.bindComUI(jCom2, function(uiCom){
                // 绑定所有子控件
                uiCom.$el.find(".hm-com").each(function(){
                    var jSubCom = $(this);
                    // 子组件
                    if(jSubCom.attr("lib")) {
                        UI.reloadLibCode(jSubCom, homeId, cache);
                    }
                    // 普通控件 
                    else {
                        pageUI.bindComUI(jSubCom);
                    }
                });
                // 调用回调
                $z.doCallback(callback, [uiCom, comIsActived], UI);
            });
        } 
        // 肯定有什么错误
        else {
            jCom.attr("invalid-lib", "yes")
                .html('<div class="invalid-lib-tip">'
                    + '<i class="zmdi zmdi-alert-polygon"></i>'
                    + "<b>" + UI.msg("hmaker.lib.e_load") + ":</b>"
                    + "<em>" + html + '</em>'
                    + '<u class="invalid-lib-del">'  + UI.msg("del") + '</u>'
                    + '</div>');
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
    // comId : 指定的控件 ID
    // returnType : "com|jQuery|Element" 默认是 "com" !大小写敏感
    getCom : function(comId, returnType) {
        var UI = this;
        var jCom;
        if(_.isString(comId)){
            jCom = UI._C.iedit.$body.find("#" + comId);
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
            jCom = jCom.closest(".hm-com");
            if("jQuery" == returnType)
                return jCom;
            if("Element" == returnType)
                return jCom[0];
            return ZUI(jCom);
        }
    },
    //...............................................................
    // 返回指定的类型的一组 COM 数组
    // filter : 过滤函数，则将参数为 F(Element):Boolean，返回true表示选中
    //          如果给定的是一个字符串，则表示指定控件的类型
    // returnType : "com|jQuery|Element" 默认是 "com" !大小写敏感
    getComList : function(filter, returnType) {
        var UI = this;
        var jComs;
        // 指定了类型
        if(_.isString(filter)){
            jComs = UI._C.iedit.$body.find('.hm-com[ctype="'+filter+'"]');
        }
        // 指定了过滤器
        else if(_.isFunction(filter)){
            var eles = [];
            UI._C.iedit.$body.find('.hm-com').each(function(){
                if(filter(this)){
                    eles.push(this);
                }
            });
            jComs = $(eles);
        }
        // 返回全部控件
        else {
            jComs = UI._C.iedit.$body.find('.hm-com');
        }
        // 返回
        var re = [];
        jComs.each(function(){
            if("jQuery" == returnType)
                re.push($(this));
            else if("Element" == returnType)
                re.push(this);
            else 
                re.push(ZUI(this));
        });
        return re;
    },
    //...............................................................
    //...............................................................
    deleteCom : function(uiCom, noResizeSkin) {
        if(uiCom) {
            uiCom.destroy();
            uiCom.$el.remove();

            // 重新应用皮肤
            if(!noResizeSkin)
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

        //console.log(layoutMap)

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
            //console.log(aid, mode)
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
        this.invokeSkin("resize");
    },
    //...............................................................
    redraw : function(){
        var UI  = this;

        // 更新 ibar 状态
        if(UI.local("hm_ibar_full")) {
            UI.arena.attr("full-ibar", true);
        }

        // 更新 ibar
        var jiBar = UI.arena.find(".hmpg-ibar");
        var jiUl  = jiBar.find(">.hm-W>ul");
        jiUl.children("li[ctype]").each(function(){
            var jLi  = $(this).empty();
            var icon = UI.msg(jLi.attr("icon"));
            var text = UI.msg(jLi.attr("text"));
            var tip  = UI.msg(jLi.attr("tip"));
            // 插入 ibox
            var jiBox = UI.ccode("ibar.ibox");
            jiBox.find("header b").text(text);
            jiBox.find("header em").text(tip);
            jiBox.appendTo(jLi);

            // 插入 thumb
            var jThumb = $('<div class="hmpg-ibar-thumb">').appendTo(jLi);
            jThumb.html(icon);
            $('<span>').text(text).appendTo(jThumb);
        });

        // 确保屏幕的模式
        UI.arena.find(".hmpg-screen").attr("mode", this.getScreenMode());

        // 开启动画
        window.setTimeout(function(){
            UI.arena.find(".hmpg-screen").attr("animat-on", "yes");
            UI.arena.attr("animat-on", "yes");
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
                icon : '<i class="fa fa-sign-out"></i>',
                tip  : 'i18n:hmaker.page.move_to_body',
                handler : function() {
                    var uiCom = UI.getActivedCom();
                    if(uiCom){
                        uiCom.appendToArea(null);
                        UI.invokeSkin("resize");
                    }
                }
            },{
                icon : '<i class="zmdi zmdi-arrow-right-top zmdi-hc-rotate-270"></i>',
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
                icon : '<i class="zmdi zmdi-arrow-left-bottom zmdi-hc-rotate-270"></i>',
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
        var UI = this;
        // 重新计算布局
        // var jiBar  = UI.arena.find(".hmpg-ibar");
        // var jsBar  = UI.arena.find(".hmpg-sbar");
        // var jStage = UI.arena.find(".hmpg-stage");

        // var iW = jiBar.outerWidth();
        // jsBar.css("left", iW);
        // jsBar.css("left", iW);

        // 调用皮肤的 resize
        UI.invokeSkin("resize");
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
    getCurrentTextContent : function(forSave) {
        return this.getHtml(null, null, forSave);
    },
    //...............................................................
    // comId 如果给定，表示获取指定控件的 outerHTML
    // 否则将返回整个网页的 HTML
    // forLib 如果为 true （仅在 comId 生效的情况下）
    // 将去掉 com 的 ID 等特殊属性
    getHtml : function(comId, forLib, forSave) {
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
            var jCom = $(this).removeClass("hm-pmv-hide").attr({
                "ui-id" : null,
                "c_seq" : null,
                "hm-actived" : null
            });
            var jW   = jCom.children(".hm-com-W");
            
            jW.children(".ui-arena").removeAttr("style");
            
            // 删掉辅助节点
            jW.children(".hm-com-assist").remove();

            // !!! 兼容老版本
            // 如果是 Image 控件，将 id:xxx 的 src 切换成相对站点的路径
            if("image" == jCom.attr("ctype")) {
                var com = $z.getJsonFromSubScriptEle(jCom, "hm-prop-com", {});
                if(/^id:[\w\d]+/.test(com.src)) {
                    var oImg  = Wn.getById(com.src.substring(3));
                    var oHome = UI.getHomeObj();
                    com.src   = "/" + Wn.getRelativePath(oHome, oImg);
                    $z.setJsonToSubScriptEle(jCom, "hm-prop-com", com, true);
                }
            }
        });
        
        // 所有标识删除的节点也要删除
        C.iload.$root.find(".hm-del-save, .ui-code-template, .ui-debug-mark, .ui-mask, .ui-loading, .pmv-mask")
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
        
        // 删除所有的皮肤以及页面动态添加的 css/js 节点
        C.iload.$head.find('[skin],[page-link]').remove();
        
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
                    return;
                }
                // 如果在两个 INLINE 的COM之间，也变成空串
                if(this.previousSibling
                    && this.previousSibling.nodeType == 1 
                    && /^(A|B|SPAN)$/.test(this.previousSibling.tagName)
                    && this.nextSibling
                    && this.nextSibling.nodeType == 1
                    && /^(A|B|SPAN)$/.test(this.nextSibling.tagName)) {
                    this.nodeValue = "";
                    return;
                }
                // 否则输出个回车
                this.nodeValue = "\n";
            }
        });

        // 处理组件的逻辑
        var _get_lib_code = function(jCom, forLib) {
            if(jCom.length == 0)
                return "";
            // 准备返回的 HTML
            var reHtml;
            // 移除作为组件时不必要的属性
            if(forLib){
                var comId   = jCom.attr("id")  || null;
                var libName = jCom.attr("lib") || null;
                jCom.attr({
                    "id"  : null,
                    "lib" : null,
                }).find(".hm-com")
                    .removeAttr("id");
                reHtml = jCom[0].outerHTML;
                // 恢复
                jCom.attr({
                    "id"  : comId,
                    "lib" : libName,
                });
            }
            // 直接使用
            else{
                reHtml = jCom[0].outerHTML;
            }
            // 返回自己的 HTML
            return reHtml;
        };

        // 返回某组件的 HTML
        if(comId) {
            return _get_lib_code(C.iload.$body.find("#"+comId), forLib);
        }

        // 定义存储组件的逻辑
        var _save_lib = function(jCom) {
            // 无视错误的组件
            if(jCom.attr("invalid-lib"))
                return;

            // 已经处理过了
            if(jCom.attr("lib-saved"))
                return;

            // 看看自己有木有子组件，如果有就先处理
            jCom.find(".hm-com[lib]").each(function(){
                _save_lib($(this));
            });

            // 保存到共享库, 无视错误的组件
            if(forSave) {
                var libName = jCom.attr('lib');
                var html    = _get_lib_code(jCom, true);
                Wn.execf("hmaker id:{{homeId}} lib -write {{libName}}", html, {
                    homeId  : oHomeId,
                    libName : libName
                });
            }

            // 然后标识一下,以便阻止重复保存 
            jCom.attr("lib-saved", "yes").html("loading...");
        };
        
        // 保存全部组件，并将组件内容置空
        var oHomeId = UI.getHomeObjId();
        C.iload.$body.find('.hm-com[lib]').each(function(){
            _save_lib($(this));
        });

        // 移除组件保存时的临时标记
        C.iload.$body.find('.hm-com[lib-saved]').removeAttr("lib-saved");

        // 返回 HTML
        return '<!DOCTYPE html>\n<html>\n' + C.iload.$root.html() + '\n</html>\n';;;
    },
    //...............................................................
    getActions : function(){
        return ["@::hmaker/hm_save",
                "::hmaker/hm_create", 
                "::hmaker/hm_delete",
                "~",
                "::hmaker/hm_site_new",
                "::hmaker/hm_site_dup",
                "::hmaker/hm_site_del",
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