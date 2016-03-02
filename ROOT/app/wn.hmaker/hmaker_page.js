(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/menu/menu'
], function(ZUI, Wn, MenuUI){
//==============================================
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
<div class="ui-code-template" common-class="ui-console-block">
    <div code-id="hcm.assist" class="hcm-assist">
        <div class="hcm-pos-hdl" h-type="NW"></div>
        <div class="hcm-pos-hdl" h-type="NE"></div>
        <div class="hcm-pos-hdl" h-type="SW"></div>
        <div class="hcm-pos-hdl" h-type="SE"></div>
        <div class="hcm-pos-hdl" h-type="N"></div>
        <div class="hcm-pos-hdl" h-type="S"></div>
        <div class="hcm-pos-hdl" h-type="E"></div>
        <div class="hcm-pos-hdl" h-type="W"></div>
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
            <div class="ue-com-menu"></div>
        </div>
        <div class="ue-stage" mode="pc">
            <div class="ue-screen"><iframe></iframe></div>
        </div>
    </div></div>
    <div class="hmaker-deta"><div class="hmaker-deta-wrapper">
        <div class="ue-com-title"></div>
        <div class="ue-com-prop"></div>
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
    getPageId : function(){
        return this.$el.attr("oid");
    },
    getCurrentObj : function(){
        return Wn.getById(this.getPageId());
    },
    getCurrentTextContent : function(){
        var UI   = this;
        var ifrm = UI.arena.find(".ue-screen iframe")[0];
        // 移除所有的 UI ID
        var jHtm = $(ifrm.contentDocument.documentElement);
        jHtm.find("[ui-id]").removeAttr("ui-id");

        // 移除所有的代码帮助节点
        jHtm.find(".hcm-assist, .hmc-rm-when-save").remove();

        // 移除页面最开始的文本节点空白
        var jBody   = jHtm.find("body");
        var eBody   = jBody[0];
        var ndFirst = eBody.firstChild;
        if(ndFirst && ndFirst.nodeType == 3){
            console.log(ndFirst)
            ndFirst.textContent = "\n";
        }

        // 移除页面最后面的文本节点空白
        var ndLast = eBody.lastChild;
        if(ndLast && ndLast.nodeType == 3){
            console.log(ndLast)
            ndLast.textContent = "\n";
        }

        // 返回 HTML
        return '<!DOCTYPE html>\n<html>\n' + jHtm.html() + '\n</html>\n';
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
                    parent : UI,
                    $el    : jDiv,
                    $menu  : UI.arena.find(".ue-com-menu"),
                    $title : UI.arena.find(".ue-com-title"),
                    $prop  : UI.arena.find(".ue-com-prop"),
                    titleHtml : titleHtml
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
            jProp = $('<script type="text/x-template" class="hmc-prop">').prependTo(jCom);
        }

        // 如果是绝对位置，增加标识

        // 确保有 hcm-assist，如果已经存在了，替换掉
        // 这样，以后可以随时添点嘛儿
        jCom.children(".hcm-assist").remove();
        var jAss = UI.ccode("hcm.assist").insertAfter(jProp);

        // 确保有 .hmc-wrapper
        var jW = jCom.children(".hmc-wrapper");
        if(jW.size() == 0){
            jW = $('<div class="hmc-wrapper">').insertAfter(jAss);
        }
    },
    //...............................................................
    setActived : function(ele) {
        var UI   = this;
        var jCom = $(ele).closest(".hm-com");

        // 本身就是激活对象，那就啥也不做
        if(jCom.attr("actived") || jCom.size() == 0)
            return;

        // 移除其他的激活对象
        var jBody = jCom.closest("body");
        jBody.find("div.hm-com[actived]").removeAttr("actived");

        // 激活自身
        jCom.attr("actived", "yes");

        // 应用组件
        UI._apply_com(jCom).render();
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
            UI._apply_com(jActivedCom).render();

    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);