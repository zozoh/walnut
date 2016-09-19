(function($z){
$z.declare([
    'zui',
    "ui/form/support/form_c_methods",
    "ui/mask/mask"
], function(ZUI, ParentMethods, MaskUI){
//==============================================
var html = `
<div class="ui-code-template">
    <div code-id="item" class="cf-item">
        <header></header>
        <section>
            <div class="wn-thumbnail" preview="yes"></div>
            <ul></ul>
        </section>
    </div>
    <div code-id="adder" class="cf-adder">
        <i class="zmdi zmdi-plus"></i>
    </div>
</div>
<div class="ui-arena com-file"></div>`;
//===================================================================
return ZUI.def("ui.form_com_file", {
    //...............................................................
    dom  : html,
    css  : "theme/ui/form/component.css",
    //...............................................................
    init : function(opt) {
        var UI = ParentMethods(this);

        // 最多可选择有多少个项目，max=1 表示大选，0 表示不限
        $z.setUndefined(opt, "max", 0);

        // 是多选模式
        $z.setUndefined(opt, "multi", opt.max > 1);

        // 模式可以是 icon | thumbnail
        $z.setUndefined(opt, "mode", "icon");

        // 生成图标的方法
        $z.setUndefined(opt, "icon", function(obj){
            return '<i class="zmdi zmdi-image-o"></i>';
        });

        // 生成项缩略图的方法
        $z.setUndefined(opt, "thumbnail", function(obj){
            return '<i class="zmdi zmdi-image cf-dft-thumbnail"></i>';
        });

        // 每个项目支持的命令，preview 和 remove 作为保留字
        $z.setUndefined("opt", "actions", {});
        _.extend(opt.actions, {
            "preview" : {
                text : "i18n:preview",
                handler : function(obj){

                }
            },
            "remove" : {
                text : "i18n:delete",
                handler : function(obj, jItem){

                }
            }
        })

        // 如果有完整内容预览的 HTML，则生成它
        $z.setUndefined(opt, "preview", function(obj){
            return "<h1> Dont't know how to preview</h1>";
        });

        // 生成一个新链接，打开该资源的唯一页面
        $z.setUndefined(opt, "url", function(obj){
            return '/a/open/browser?ph=id:' + obj.id;
        });

        // 如何选择一个对象
        $z.setUndefined(opt, "addBy", function(callback){
            callback({
                name : "fake object"
            });
        });
    },
    //...............................................................
    events : {
    },
    //...............................................................
    redraw : function() {
        var UI  = this;
        var opt = UI.options;

        // 标识模式等属性
        UI.arena.attr({
            "mode" : opt.mode
        });
    },
    //...............................................................
    __append_item : function(obj) {
        var UI  = this;
        var opt = UI.options;
        var context = opt.context || UI;

        // 生成项
        var jItem = UI.ccode("item");

        // 绘制 icon
        jItem.children("header").html(opt.icon.call(context, obj))

        // 绘制 thumbnail
        var jThumb = jItem.find(">section>.wn-thumbnail")
                        .html(opt.thumbnail.call(context, obj));

        // 无法预览做一下标识
        if(!opt.preview){
            jThumb.removeAttr("preview");
        }

        // 绘制扩展命令
        var jUl = jItem.find(">section>ul");
        for(var aKey in opt.actions) {
            // 无法预览，无视预览命令
            if(!opt.preview && "preview" == aKey)
                continue;

            // 得到命令项
            var a = opt.actions[aKey];

            // 绘制命令
            var jLi = $('<li><b>'+UI.text(a.text)+'</b>').attr("a", aKey);
            if(a.icon)
                $(a.icon).prependTo(jLi);
            jLi.appendTo(jUl);
        }

        // 最后计入 DOM
        jItem.appendTo(UI.arena).data("@OBJ", obj);
    },
    //...............................................................
    __draw_data : function(objList) {
        var UI  = this;
        var opt = UI.options;

        // 得到最大项目数量
        var maxNB = Math.min(opt.multi ? opt.max : 1, objList.length);

        // 清空
        UI.arena.empty();
        
        // 循环绘制
        var i = 0;
        for(; i<maxNB; i++) {
            UI.__append_item(objList[i]);
        }

        // 如果没超出限制，添加 adder
        if(i < maxNB) {
            UI.ccdoe("adder").appendTo(UI.arena);
        }
    },
    //...............................................................
    _set_data : function(objList) {
        var UI = this;

        // 必须是一个数组
        if(!objList){
            objList = [];
        }
        else if(!_.isArray(objList)){
            objList = [objList];
        }

        // 绘制数据
        UI.__draw_data(objList);
    },
    //...............................................................
    _get_data : function() {
        var UI  = this;
        var opt = UI.options;

        // 准备返回值
        var objList = [];

        // 搜索
        UI.arena.children(".cf-item").each(function(){
            objList.push($(this).data("@OBJ"));
        });

        // 返回
        if(opt.multi)
            return objList;

        return objList.length>0 ? objList[0] : null;

    },
    //...............................................................
    /*
    输入输出的数据格式:
    if multi : 
        [{..}, {..}] or []
    else :
        {..} or null
    */
    //...............................................................
    getData : function(){
        var UI = this;
        return UI.ui_format_data(function(opt){
            return UI._get_data();
        });
    },
    //...............................................................
    setData : function(val){
        var UI = this;
        this.ui_parse_data(val, function(data){
            UI._set_data(data);
        });
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);