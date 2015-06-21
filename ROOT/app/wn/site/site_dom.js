(function($, $z, _){
// 帮助hanh
// 帮助函数: 获取两个路径的相对路径
function getRelativePath(base, path, baseIsFile) {
    var bb = $z.splitIgnoreEmpty(base, "/");
    if(baseIsFile && bb.length>0)
        bb = bb.slice(0, bb.length-1);
    var ff = $z.splitIgnoreEmpty(path, "/");
    var len = Math.min(bb.length, ff.length);
    var pos = 0;
    for (; pos < len; pos++)
        if (bb[pos] != ff[pos])
            break;

    if (len == pos && bb.length == ff.length)
        throw "o... it's impossiable: base: '"+base+"' path:'"+path+"'  baseIsFile"+baseIsFile;

    var re = $z.dupString("../", bb.length - pos);
    re += ff.slice(pos, ff.length).join("/");
    return re;
}
//--------------------------------------------------------------------
// 格式化值的函数, this 为 sc 对象
var func_eval = {
    "int"  : function(str){
        return parseInt(str*1);
    },
    "bool" : function(str){
        return /^true|on|yes|[1-9]+[0-9]*$/gi.test(str);
    },
    "float" : function(str){
        return str*1;
    },
    "link" : function(str){
        // 外部资源
        if(/^https?:\/\/.+$/i.test(str)){
            return str;
        }
        // 绝对路径
        if(/^\/.+$/.test(str)){
            return getRelativePath(this.rpath, str, true);
        }
        // 相对自身的路径
        return str;
    },
    "list" : function(str){
        return eval('('+str+')');
    },
    "obj" : function(str){
        return eval('('+str+')');
    },
    "markdown" : function(str){
        return str;
    },
    "html" : function(str){
        var sc = this;
        var jq = $("<div>" + str + "</div>");
        jq.find("img, a").each(function(){
            var me = $(this);
            // 图片
            if("IMG" == this.tagName){
                var src = me.attr("src");
                src = func_eval.link.call(sc, src);
                me.prop("src", src);
            }
            // 超链接
            else{
                var href = me.prop("href");
                href = func_eval.link.call(this, href);
                me.prop("href", href);
            }
        });
        return jq.html();
    }
};
//--------------------------------------------------------------------
var site_dom = {
    parseLibrary : function(libName){
        var jq = $(document.body).find("section.library>section[name="+libName+"]");
        if(jq.size()==0)
            throw "!!! lib nofound: " + libName;
        // 开始解析
        var lib = {name : libName};

        // 获取 DOM 模板
        var frag = jq.children(".lib-dom");
        lib.dom = $(frag).children().clone();

        // 解析所有的动态属性
        lib.props = {};
        jq.children(".lib-prop").each(function(){
            var me = $(this);
            // 属性的基本信息
            var prop = {
                name : me.attr("name"),
                type : me.attr("type")
            };
            // 得到值的处理器
            prop.fromString = func_eval[prop.type];
            if(typeof prop.fromString != "function"){
                throw "unsupport prop type : " + prop.type;
            }
            // 属性的 IDE 设置
            var jIDE = me.children(".IDE");
            if(jIDE.size()>0) {
                prop.ide = eval('(' + jIDE.html() + ')');
            }
            // 默认
            else{
                prop.ide = {
                    text : prop.name,
                    type : "input"
                };
            }
            // 属性的设置函数
            var jSetter = me.children(".SETTER");
            if(jSetter.size() == 0)
                throw "!!! lib '" + libName + "' without SETTER";
            prop.setter = eval('(' + jSetter.text() + ')');
            prop.selector = jSetter.attr("selector");
            // 计入属性列表
            lib.props[prop.name] = prop;
        });

        // 返回库
        return lib;
    },
    // 渲染组件，this 为 sc 对象
    renderComponent : function(jExt, jG){
        // 得到组件对象
        var sc = this;
        var libName = jExt.attr("apply");
        var lib = site_dom.parseLibrary(libName);
        
        // 对组件的 DOM 进行设值
        jExt.children("[prop]").each(function(){
            var pnm = $(this).attr("prop");
            var prop = lib.props[pnm];
            if(!prop)
                throw "prop nodefined : " + prop;
            var str = $.trim(this.innerHTML);
            var val = prop.fromString.call(sc, str);
            // 指定了某 selector
            if(prop.selector) {
                prop.setter.call(lib.dom.find(prop.selector), val);
            }
            // 采用顶级节点
            else{
                prop.setter.call(lib.dom, val);
            }
        });
        
        // 对组件进行深层展开
        jExt.children("section[apply]").each(function(){
            var jSubExt = $(this);
            var gasketName = jSubExt.attr("extend");
            var jSubG = $(lib.dom).find("[gasket="+gasketName+"]");
            site_dom.renderComponent.call(sc, jSubExt, jSubG);
        });

        // 将生成的 DOM 替换当前扩展点
        jG.append(lib.dom);
        jExt.remove();
    },
    renderGasket : function(jG){
        //console.log("======================== render: " + jG.attr("gasket"))
        var sc = this;
        jG.children("section[apply]").each(function(){
            site_dom.renderComponent.call(sc, $(this), jG);
        });
    },
    renderPage : function(){
        // 生成渲染上下文
        var jBody = $(document.body);
        var sc = {
            sitePath : jBody.attr("site-path"),
            pagePath : jBody.attr("page-path"),
            rpath    : jBody.attr("rpath")
        };
        // 遍历 body 下所有子节点
        jBody.children().each(function(){
            var me = $(this);
            // 忽略组件库 
            if(this.tagName=="section" && me.hasClass("library"))
                return;
            // 如果本身就是扩展点
            if(me.attr("gasket")){
                site_dom.renderGasket.call(sc, me);
            }
            // 否则在其内寻找扩展点
            else{
                me.find("[gasket]").each(function(){
                    site_dom.renderGasket.call(sc, $(this));
                });
            }
        });
        // 渲染完毕，删除 library 节点
        jBody.children("section.library").remove();

        // 整理扩展点的 theme 和 gasket 属性
        // 即， theme 变 className， gasket 删掉
        /*
        jBody.find("[theme]").each(function(){
            var me = $(this);
            me.addClass(me.attr("theme"))
                .removeAttr("theme")
                .removeAttr("gasket");
        });

        // 整理所有的 layout 属性，将其变成 class
        jBody.find("[layout]").each(function(){
            var me = $(this);
            me.addClass("layout-" + me.attr("layout"))
                .removeAttr("layout");
        });*/
    }
};
//--------------------------------------------------------------------
window.$site = _.extend(window.$site||{}, site_dom);
//--------------------------------------------------------------------
})(window.jQuery, window.$z, _);