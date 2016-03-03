(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/menu/menu'
], function(ZUI, Wn, MenuUI){
//==============================================
// .........................  控件们的通用属性
// 这是一个 form UI 的配置项
var comGeneralProp = {
    title     : 'i18n:hmaker.cprop_general',
    className : "hmaker-prop-general",
    cols   : 2,
    autoLineHeight : true,
    fields : [{
        key    : "ID",
        title  : "i18n:hmaker.cprop.ID",
        type   : "string",
        span   : 2
    }, {
        key    : "position",
        title  : "i18n:hmaker.cprop.position",
        type   : "string",
        dft    : "relative",
        span   : 2,
        editAs : "switch",
        uiConf : {
            items : [{
                val  : 'relative',
                text : 'i18n:hmaker.cprop.pos_relative'
            },{
                val  : 'absolute',
                text : 'i18n:hmaker.cprop.pos_absolute'
            }]
        }
    }, {
        key    : "top",
        title  : "i18n:hmaker.cprop.top",
        type   : "int",
        uiConf : {unit : "px"}
    }, {
        key    : "left",
        title  : "i18n:hmaker.cprop.left",
        type   : "int",
        uiConf : {unit : "px"}
    }, {
        key    : "width",
        title  : "i18n:hmaker.cprop.width",
        type   : "int",
        uiConf : {unit : "px"}
    }, {
        key    : "height",
        title  : "i18n:hmaker.cprop.height",
        type   : "int",
        uiConf : {unit : "px"}
    }, {
        key    : "padding",
        title  : "i18n:hmaker.cprop.padding",
        type   : "int",
        uiConf : {unit : "px"}
    }, {
        key    : "magin",
        title  : "i18n:hmaker.cprop.margin",
        type   : "int",
        uiConf : {unit : "px"}
    }]
};
// .........................  控件 ID 计数器
var COM_SEQ = 0;
// ......................... 帮助函数
var _H = function(jHead, selector, html) {
    if(jHead.children(selector).size() == 0){
        jHead.prepend($(html));
    }
};
// ......................... DOM
var html = function(){/*
<div class="ui-code-template">
    <div code-id="hmc.assist" class="hmc-assist">
        <div class="hmca-grp" md="L">
            <div class="hmca-hdl" hd="NW"></div>
            <div class="hmca-hdl" hd="W"></div>
            <div class="hmca-hdl" hd="SW"></div>
        </div>
        <div class="hmca-grp" md="C">
            <div class="hmca-hdl" hd="N"></div>
            <div class="hmca-hdl" hd="S"></div>
        </div>
        <div class="hmca-grp" md="R">
            <div class="hmca-hdl" hd="NE"></div>
            <div class="hmca-hdl" hd="E"></div>
            <div class="hmca-hdl" hd="SE"></div>
        </div>
    </div>
</div>
<div class="ui-arena hmaker-page" ui-fitparent="yes">
    <div class="hmaker-view"><div class="hmaker-view-warpper">
        <div class="ue-bar1"><span>{{hmaker.comlib_add}}</span><span>{{hmaker.comlib_add_c}}</span></div>
        <div class="ue-shelf"></div>
        <div class="ue-bar2">
            <div class="ue-ssize">
                <input name="x"><em>x</em><input name="y">
                <span>
                    <i class="fa fa-desktop highlight" val=""></i>
                    <i class="fa fa-tablet" val="800x600"></i>
                    <i class="fa fa-mobile" val="400x600"></i>
                </span>
            </div>
            <div class="ue-com-menu" ui-gasket="menu"></div>
        </div>
        <div class="ue-stage" mode="pc">
            <div class="ue-screen"><iframe></iframe></div>
        </div>
    </div></div>
    <div class="hmaker-deta"><div class="hmaker-deta-wrapper">
        <div class="ue-com-title" ui-gasket="ctitle"></div>
        <div class="ue-com-prop"  ui-gasket="prop"></div>
    </div></div>
</div>
*/};
//==============================================
return ZUI.def("app.wn.hmaker_page", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    events : {
        "click .hmaker-components [ctype]" : function(e){
            this._insert_com($(e.currentTarget).attr("ctype"));
        }
    },
    //...............................................................
    redraw : function(){
        var UI  = this;

        // 绑定 iframe onload
        UI.arena.find(".ue-screen iframe").bind("load", function(){
            //console.log("hmaker_page: iframe onload");
            UI.setup_page_editing();
        });

        // 读取加载项的内容
        UI.__reload_components(function(){
            UI.defer_report("coms");
        });

        // 标记延迟
        return ["coms"];
    },
    //...............................................................
    depose : function(){
        //console.log("hmaker_page: depose iframe onload")
        this.arena.find(".ue-screen iframe").unbind();
    },
    //...............................................................
    __reload_components : function(callback){
        var UI = this;
        // 加载插入项目
        var o = Wn.fetch("~/.hmaker/components.html", true);
        if(!o){
            alert(UI.msg("hmaker.page.e_nocoms"));
            return;
        }

        // 读取加载项的内容     
        Wn.read(o, function(html){
            // 将加载项目计入 DOM
            var jSh = UI.arena.find(".ue-shelf");
            jSh.empty().html(html);

            // 解析多国语言
            jSh.find("span,h4").each(function(){
                $(this).text(UI.text($(this).text()));
            });

            // 依次加载组件项
            var uiComs = [];
            jSh.find("li[ctype]").each(function(){
                uiComs.push("app/wn.hmaker/component/hmc_" + $(this).attr("ctype"));
            });

            // 加载 & 调用回调
            seajs.use(uiComs, function(){
                callback();
            });
        }); 
    },
    //...............................................................
    update : function(oPg) {
        var UI = this;

        // 记录 ID
        UI.$el.attr("oid", oPg.id);

        // 加载页面，完毕后，页面的脚步会调用本类的 setup_page_editing 函数
        var ifrm = UI.arena.find(".ue-screen iframe")[0];
        ifrm.src = "/o/read/id:"+encodeURIComponent(oPg.id)+"?t="+Date.now();
    },
    //...............................................................
    $com : function(ele){
        var jCom;

        if(ele){
            jCom = $(ele).closest(".hm-com");
        }

        // 获取激活控件
        if(jCom && jCom.size() == 0){
            jCom = this.arena.find(".hm-com[actived]");
        }

        // 靠，指定是有啥问题
        if(!jCom || jCom.size() == 0)
            throw "Can not found jCom by : " + ele;

        return jCom;
    },
    //...............................................................
    // 重新持久化以便给定组件的样式
    syncComponentInfo : function(ele){
        var UI   = this;
        if(UI.gasket.prop){
            var jCom = UI.$com(ele);
            var info = UI.gasket.prop.getData();
            info     = UI.setComponentInfo(jCom, info);
            UI._apply_com(jCom).updateStyle(info);
        }
    },
    //...............................................................
    // 获取组件的属性信息
    // 如果没有给定 ele，则试图用当前激活的控件，如果没有控件激活，呵呵，返回 null
    getComponentInfo : function(ele){
        var UI   = this;
        var jCom = UI.$com(ele);

        // 总会返回点信息嘛
        var json = $.trim(jCom.children("script.hmc-prop").html());
        var re = json ? $z.fromJson(json) : {};

        // 补上这个控件的 ID
        re.ID = jCom.prop("id");

        // 嗯，就这样吧
        return re;
    },
    //...............................................................
    // 设置组件的属性信息，会将信息保存在 .hmc-prop 里面
    setComponentInfo : function(ele, info){
        var jCom  = $(ele);
        
        // 确保是有 ID 的
        if(!info.ID)
            info.ID = jCom.attr("ctype") + (++COM_SEQ);

        // 处理一下 ID
        var oldID = jCom.prop("id");
        if(oldID != info.ID){
            // 因为切换 ID 的缘故，先删掉头部就的那个声明
            $(jCom[0].ownerDocument.documentElement)
                .find('head>style[for-com="'+oldID+'"]')
                    .remove();

            // 切换 ID
            jCom.prop("id", info.ID);
        }

        // 处理位置，绝对位置的话，必须要有尺寸
        if(info.position == "absolute"){
            jCom.attr("pos", "absolute");
            info.top    = info.top    || 0;
            info.left   = info.left   || 0;
            info.width  = info.width  || 300;
            info.height = info.height || 300;
        }
        // 相对位置的话，尺寸啥的就没用了
        else {
            jCom.attr("pos", "relative");
            delete info.top;
            delete info.left;
            delete info.width;
            delete info.height;
        }

        // 保存前，去掉 ID
        var info2 = _.extend({}, info);
        delete info2.ID;

        // 来吧，存吧
        jCom.children("script.hmc-prop").html($z.toJson(info2,null,'    '));

        // 返回整理后的信息
        return info;
    },
    //...............................................................
    REG_px : /^(top|left|width|height|padding|margin|lineHeight|fontSize|letterSpacing)/,
    //...............................................................
    // 看看规则是不是需要用 px 作为后缀, 返回 undefined 表示『我不管』
    gen_rule_item : function(key, val){
        // 需要添加 px 为单位的
        if(_.isNumber(val) && this.REG_px.test(key)){
            return  $z.lowerWord(key, "-") + ":" + val + "px;";
        }
        // 默认
        return  $z.lowerWord(key, "-") + ":" + val + ";";
    },
    REG_ge : /^(position|top|left|width|height|padding|margin)$/,
    //...............................................................
    // 控件会用这个方法来看看是不是这个规则是归顶层来管的, 返回 undefined 表示『我不管』
    gen_rules : function(ID, styleRules, info){
        var UI = this;

        // 不处理的规则放这里返回
        var reInfo = {};

        var outer = [];
        var inner = [];

        // 依次处理
        for(var key in info){
            var val = info[key];
            // 要我处理的
            if(UI.REG_ge.test(key)){
                // 处理内边距: .hmc-wrapper 的内边距
                if("padding" == key){
                    inner.push("padding:"+val+"px;");
                }
                // 处理外边距: .hm-com 的内边距
                else if("margin" == key){
                    outer.push("padding:"+val+"px;");
                }
                // 只有绝对定位才处理
                else if("position" == key){
                    if("absolute" == val){
                        outer.push("position:absolute;");
                    }
                }
                // 其他的都放到外面去
                else{
                    outer.push(UI.gen_rule_item(key, val));
                }
            }
            // 我不处理的
            else{
                reInfo[key] = val;
            }
        }

        // 记录到规则列表里
        if(outer.length > 0) {
            styleRules.push({
                selector : "#"+ID,
                items    : outer
            })
        }
        if(inner.length > 0) {
            styleRules.push({
                selector : "#"+ID+" .hmc-main",
                items    : innter
            })
        }

        // 返回剩下的信息
        return reInfo;
    },
    //...............................................................
    // 子控件整理过信息后，生成 CSS 的 rule，本函数将更新页面头部的声明
    updateComStyle : function(ele, styleRules){
        var jCom  = $(ele);
        var jHtm  = $(jCom[0].ownerDocument.documentElement);
        var jHead = jHtm.children("head");
        var ID   = jCom.prop("id");
        var cssText;

        // 变成 css 文本
        if(_.isArray(styleRules) && styleRules.length>0){
            for(var i=0;i<styleRules.length;i++){
                var rule = styleRules[i];
                if(rule.items.length>0){
                    var str  = rule.selector + "{\n";
                    str += "  " + rule.items.join("\n  ");
                    str += '\n}\n';
                    styleRules[i] = str;
                }else{
                    styleRules[i] = "";
                }
            }
            cssText = styleRules.join("\n");
        }
        // 本来就是文本了
        else if(_.isString(styleRules)){
            cssText = styleRules;
        }
        // 都不是，当做删除吧
        else {
            // TODO 嘿嘿 
        }

        // 找到 style 标签
        var jStyle = jHead.children('style[for-com="'+ID+'"]');
        if(cssText){
            // 木有的话就创建一个
            if(jStyle.size() == 0){
                jStyle = $('<style for-com="'+ID+'">').appendTo(jHead);
            }
            // 写入
            jStyle.html("\n" + cssText);
        }
        // 移除
        else if(jStyle.size()>0){
            jStyle.remove();
        }

    },
    //...............................................................
    // 获取当前编辑页面对象的 id (WnObj.id)
    getPageId : function(){
        return this.$el.attr("oid");
    },
    // 获取当前页面对应的 WnObj
    getCurrentObj : function(){
        return Wn.getById(this.getPageId());
    },
    // 获取当前编辑的内容的文本形式
    getCurrentTextContent : function(){
        var UI   = this;
        var ifrm = UI.arena.find(".ue-screen iframe")[0];
        // 移除所有的 UI ID
        var jHtm = $(ifrm.contentDocument.documentElement);
        jHtm.find("[ui-id]").removeAttr("ui-id");

        // 移除所有的代码帮助节点
        jHtm.find(".hmc-assist").remove();

        // 移除页面最开始的文本节点空白
        var jBody   = jHtm.find("body");
        var eBody   = jBody[0];
        var ndFirst = eBody.firstChild;
        if(ndFirst && ndFirst.nodeType == 3){
            //console.log(ndFirst)
            ndFirst.textContent = "\n";
        }

        // 移除页面最后面的文本节点空白
        var ndLast = eBody.lastChild;
        if(ndLast && ndLast.nodeType == 3){
            //console.log(ndLast)
            ndLast.textContent = "\n";
        }

        // 得到要返回 HTML
        var re = '<!DOCTYPE html>\n<html>\n' + jHtm.html() + '\n</html>\n';

        // 再把所有的代码帮助节点加回来
        if(UI.children)
            for(var i=0; i<UI.children.length; i++){
                var uiCom = UI.children[i];
                uiCom.$el.attr("ui-id", uiCom.cid);
                UI._check_com_dom(uiCom.$el);
                $z.invoke(uiCom, "checkDom");
            }

        // 返回
        return re;
    },
    //...............................................................
    // 因为组件已经是提前加载过的 (在 __reload_components 里)
    // 这里肯定不会发起请求了，个个控件就检查 .hmc-wrapper 里面的东东合不合心意吧
    // 同时各个控件的 checkDom 也会根据属性生成一个 <style> 查到文档的 <head> 里
    _apply_com : function(jDiv){
        var UI    = this;
        var ctype = jDiv.attr("ctype");

        // 如果已经声明了组件，看看是不是直接就有实例可用
        var uiCom;
        var uiid = jDiv.attr("ui-id");
        if(uiid){
            uiCom = ZUI(uiid);
        }

        // 没有的话，创建一个 UI 的实例
        if(!uiCom){
            // 准备一个默认的标题 HTML
            var titleHtml = UI.arena.find('.ue-shelf li[ctype="'+ctype+'"]').html();

            // 创建组件
            seajs.use("app/wn.hmaker/component/hmc_"+ctype, function(ComUI){
                uiCom = new ComUI({
                    parent    : UI,
                    $el       : jDiv,
                    $menu     : UI.arena.find(".ue-com-menu"),
                    $title    : UI.arena.find(".ue-com-title"),
                    $prop     : UI.arena.find(".ue-com-prop"),
                    titleHtml : titleHtml,
                    propConf  : comGeneralProp
                });
            });
        }

        // 返回实例
        return uiCom;
    },
    //...............................................................
    _insert_com : function(ctype){
        var UI   = this;
        var ifrm = UI.arena.find(".ue-screen iframe")[0];
        var jCom = $('<div class="hm-com">').addClass("hmc-"+ctype).attr("ctype", ctype);
        
        // 分配 ID
        jCom.prop("id", ctype + (++COM_SEQ));

        // 加入到编辑文档中
        $(ifrm.contentDocument.body).append(jCom);

        // 检查总体结构
        UI._check_com_dom(jCom);

        // 激活控件，启用控件绘制
        UI.setActived(jCom);
    },
    //...............................................................
    _check_com_dom : function(jCom){
        var UI    = this;
        var ctype = jCom.attr("ctype");

        // 更新控件最后的序号
        var cid = jCom.prop("id");
        var m = new RegExp("^("+ctype+")(\\d+)$").exec(cid);
        if(m)
            COM_SEQ = Math.max(COM_SEQ, m[2]*1);

        // 确保有 script
        var jProp = jCom.children("script.hmc-prop");
        if(jProp.size() == 0) {
            jProp = $('<script type="text/x-template" class="hmc-prop">').appendTo(jCom);
        }

        // 确保有 .hmc-wrapper
        var jW = jCom.children(".hmc-wrapper");
        if(jW.size() == 0){
            jW = $('<div class="hmc-wrapper">').appendTo(jCom);
        }

        // 如果是绝对位置，增加标识

        // 确保有 hmc-assist，如果已经存在了，替换掉
        // 这样，以后可以随时添点嘛儿
        jW.children(".hmc-assist").remove();
        var jAss = UI.ccode("hmc.assist").prependTo(jW);

        // 确保有 .hmc-main
        var jM = jW.children(".hmc-main");
        if(jM.size() == 0){
            jM = $('<div class="hmc-main">').appendTo(jW);
        }

        
    },
    //...............................................................
    setActived : function(ele, force) {
        var UI   = this;
        var jCom = UI.$com(ele);

        // 本身就是激活对象，那就啥也不做
        if(!force)
            if(jCom.attr("actived") || jCom.size() == 0)
                return;

        // 移除其他的激活对象
        var jBody = jCom.closest("body");
        jBody.find("div.hm-com[actived]").removeAttr("actived");

        // 激活自身
        jCom.attr("actived", "yes");

        // 应用组件
        UI._apply_com(jCom).render(function(){
            if(UI.gasket.prop)
                UI.gasket.prop.hmakr_com_type = jCom.attr("ctype");
        });
    },
    //...............................................................
    _move_com : function(UI, jCom, e){
        var jBody = $(jCom[0].ownerDocument.body);

        // 已经无效了 ...
        if(!jCom.attr("actived") || !jCom.attr("mouse-noup")){
            jBody.removeAttr("noselect");
            return;
        }

        // 建立遮罩
        var jMM   = $('<div class="hmaker-mouse-mask">').appendTo(jBody);
        jCom.attr("mouse-moving", "yes");

        // 记录自己的相对点击位置
        var rect = $z.rect(jCom);
        var mouse_x = e.pageX - rect.left;
        var mouse_y = e.pageY - rect.top;

        // 鼠标移动的时候，修改大小
        jMM.on("mousemove", function(e){
            var css = {
                left : e.pageX - mouse_x,
                top  : e.pageY - mouse_y
            };
            // 即时更新状态
            jCom.css(css);
            // 更新属性表单
            if(UI.gasket.prop)
                UI.gasket.prop.update(css);
        });

        // 鼠标抬起的时候，释放
        jBody.one("mouseup", function(){
            // 持久化样式
            UI.syncComponentInfo(jCom);

            // 移除临时的 style
            jCom.css({width:"",height:"",top:"",left:""});
            jCom.removeAttr("mouse-moving");

            // 移除遮罩
            jMM.remove();

            // 延迟移除禁止选择
            window.setTimeout(function(){
                jBody.removeAttr("noselect");
            }, 1000);
        });
    },
    //...............................................................
    _resize_com : function(UI, jCom, rszType){
        var jBody = $(jCom[0].ownerDocument.body);

        // 已经无效了 ...
        if(!jCom.attr("actived") || !jCom.attr("mouse-noup")){
            jBody.removeAttr("noselect");
            return;
        }

        // 建立遮罩
        var jMM   = $('<div class="hmaker-mouse-mask">').appendTo(jBody);
        
        // 得到位置信息
        var rect = $z.rect(jCom);

        // 根据图标处理
        var handler;
        if("S" == rszType){
            handler = function(e){
                return {height : e.pageY - rect.top, bottom: e.pageY};
            }
        }
        else if("E" == rszType){
            handler = function(e){
                return {width : e.pageX - rect.left, right:e.pageX};
            }
        }
        else if("W" == rszType){
            handler = function(e){
                return {left: e.pageX, width : rect.right - e.pageX};
            }
        }
        else if("N" == rszType){
            handler = function(e){
                return {top: e.pageY, height : rect.bottom - e.pageY};
            }
        }
        else if("SE" == rszType){
            handler = function(e){
                return {width : e.pageX - rect.left, height : e.pageY - rect.top,
                        right : e.pageX, bottom: e.pageY};
            }
        }
        else if("NE" == rszType){
            handler = function(e){
                return {width : e.pageX - rect.left, height : rect.bottom - e.pageY,
                        right : e.pageX, top: e.pageY};
            }
        }
        else if("SW" == rszType){
            handler = function(e){
                return {width : rect.right - e.pageX, height : e.pageY - rect.top,
                        left : e.pageX, bottom: e.pageY};
            }
        }
        else if("NW" == rszType){
            handler = function(e){
                return {width : rect.right - e.pageX, height : rect.bottom - e.pageY,
                        left : e.pageX, top: e.pageY};
            }
        }

        // 鼠标移动的时候，修改大小
        jMM.on("mousemove", function(e){
            var css = handler(e);
            // 全局记录
            _.extend(rect, css);
            // 即时更新状态
            jCom.css(css);
            // 更新属性表单
            if(UI.gasket.prop)
                UI.gasket.prop.update(css);
        });

        // 鼠标抬起的时候，释放
        jBody.one("mouseup", function(){
            // 持久化样式
            UI.syncComponentInfo(jCom);

            // 移除临时的 style
            jCom.css({width:"",height:"",top:"",left:""});

            // 移除遮罩
            jMM.remove();

            // 延迟移除禁止选择
            window.setTimeout(function(){
                jBody.removeAttr("noselect");
            }, 1000);
        });
    },
    //...............................................................
    setup_page_editing : function(){
        var UI  = this;
        // 首先看看子页
        var ifrm  = UI.arena.find(".ue-screen iframe")[0];
        var jDoc  = $(ifrm.contentDocument);
        var jHtm  = $(ifrm.contentDocument.documentElement);
        var jHead = jHtm.children("head");
        var jBody = jHtm.children("body");

        // 绑定通用事件
        jBody.on("click", "div.hm-com", function(e){
            UI.setActived(this);
        });

        // 通用的移动
        jBody.on("mousedown", "div.hm-com[actived]", function(e){
            // 只监视左键
            if(e.which != 1){
                return;
            }
            var jCom = $(this).attr("mouse-noup", "yes");
            jBody.attr("noselect", "yes");
            //console.log(jCom.attr("mouse-noup"))
            window.setTimeout(UI._move_com, 100, UI, jCom, e);
        });
        jBody.on("mouseup", function(e){
            // 只监视左键
            if(e.which != 1){
                return;
            }
            console.log("mouse up")
            $(this).find("[mouse-noup]").removeAttr("mouse-noup");
        });

        // 通用的 resize
        jBody.on("mousedown", "div.hm-com[actived] .hmca-hdl", function(e){
            // 只监视左键
            if(e.which != 1){
                return;
            }
            e.stopPropagation();
            jBody.attr("noselect", "yes");
            var jHdl = $(this);
            var jCom = jHdl.closest(".hm-com").attr("mouse-noup", "yes");
            window.setTimeout(UI._resize_com, 100, UI, jCom, jHdl.attr("hd"));
        });


        // 控件计数归零
        COM_SEQ = 0;

        // 初始化页面的头
        _H(jHead, 'link[href*="hmpg_editing.css"]', '<link for-edit="yes" rel="stylesheet" type="text/css" href="/a/load/wn.hmaker/hmpg_editing.css">');
        _H(jHead, 'link[href*="hmc.css"]', '<link rel="stylesheet" type="text/css" href="/a/load/wn.hmaker/component/hmc.css">');
        _H(jHead, 'link[href*="page.css"]', '<link rel="stylesheet" type="text/css" href="/a/load/wn.hmaker/page/page.css">');
        _H(jHead, 'script[src*="underscore.js"]', '<script src="/gu/rs/core/js/backbone/underscore-1.8.2/underscore.js">');
        _H(jHead, 'script[src*="jquery"]', '<script src="/gu/rs/core/js/jquery/jquery-2.1.3/jquery-2.1.3.min.js">');
        _H(jHead, 'meta[name="viewport"]', '<meta name="viewport" content="width=device-width, initial-scale=1.0">');
        _H(jHead, 'meta[http-equiv="X-UA-Compatible"]', '<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">');
        _H(jHead, 'meta[charset="utf-8"]', '<meta charset="utf-8">');

        // 依次用各个组件检查一下 DOM 是否有问题
        jBody.children("div").each(function(){
            var jDiv  = $(this); 
            
            // 检查总体结构
            UI._check_com_dom(jDiv);

            // 检查每个控件的 DOM 结构是否合意
            UI._apply_com(jDiv).checkDom();
        });

        // 如果有激活的组件，激活它
        var jActivedCom = jBody.children('div.hm-com[actived]');
        if(jActivedCom.size()>0)
            UI.setActived(jActivedCom, true);

    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);