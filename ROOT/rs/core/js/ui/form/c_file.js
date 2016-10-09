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
        <section><div>
            <div class="cf-ititle"></div>
            <div class="cf-preview-con"></div>
            <div class="cf-actions"><ul></ul></div>
        </div></section>
    </div>
    <div code-id="adder" class="cf-adder">
        <i class="zmdi zmdi-plus"></i>
    </div>
</div>
<div class="ui-arena com-file"></div>`;
//...............................................................
var do_on_preview = function(e) {
    var UI  = this;
    var opt = UI.options;
    var context = opt.context || UI;

    var jItem = $(e.currentTarget).closest(".cf-item");
    var obj   = jItem.data("@OBJ");
    opt.on_preview.call(context, obj, jItem, UI);
};
//===================================================================
return ZUI.def("ui.form_com_file", {
    //...............................................................
    dom  : html,
    css  : "theme/ui/form/component.css",
    //...............................................................
    init : function(opt) {
        var UI = ParentMethods(this);

        // 最多可选择有多少个项目，max=1 表示单选，0 表示不限
        $z.setUndefined(opt, "max", opt.multi ? 0 : 1);

        // 多选模式
        $z.setUndefined(opt, "multi", opt.max > 1);

        // 生成生成排排显示的缩略图的方法
        $z.setUndefined(opt, "thumbnail", function(obj, UI){
            return '<div class="cf-thumb" style="background-image: url(/o/thumbnail/id:'+obj.id+'?sh=32);"></div>';
        });

        // 生成比较大的预览图的方法 
        $z.setUndefined(opt, "preview", function(obj, UI){
            var url = '/o/thumbnail/id:'+obj.id+'?sh=128';
            if(/^image\//.test(obj.mime)){
                url = "/o/read/id:" + obj.id;
            }
            return '<div class="cf-preview" style="background-image: url('+url+');"></div>';
        });

        // 生成对象名称 
        $z.setUndefined(opt, "itemTitle", function(obj, UI){
            return $('<span>').text(obj.title || obj.name || obj.nm);
        });

        // 如何添加一个对象
        $z.setUndefined(opt, "on_add", function(callback, UI){
            alert("undefined on_add");
        });

        // 如何删除一个对象
        $z.setUndefined(opt, "on_remove", function(obj, jItem, UI){
            alert("undefined on_remove");
        });

        // 如果点击对象的预览图会发生什么
        $z.setUndefined(opt, "on_preview", function(obj, jItem, UI){
            $z.openUrl("/a/open/"+(window.wn_browser_appName||"wn.browser"), "_blank", "GET", {
                ph : "id:" + obj.id
            });
        });

        // 每个项目支持的扩展命令，其中 "remove" 保留，一定会被路由到 on_remove 回调里
        $z.setUndefined(opt, "actions", {});
        _.extend(opt.actions, {
            "remove" : {
                text : "i18n:delete",
                handler : opt.on_remove
            }
        })
    },
    //...............................................................
    events : {
        // 添加
        "click .cf-adder" : function(e){
            var UI  = this;
            var opt = UI.options;
            var context = opt.context || UI;

            var jAdd = $(e.currentTarget);

            opt.on_add.call(context, function(obj){
                var jItem = UI.__gen_item(objList[i]);
                jItem.insertBefore(jAdd);

                UI.showHideAdder();
            }, UI);
        },
        // 鼠标进入项目，显示详细面板
        "mouseenter .cf-item" : function(e){
            var jItem = $(e.currentTarget).attr("preview", "yes");
            var jHead = jItem.children("header");
            var jPreview = jItem.children("section");
            $z.dock(jHead, jPreview, "H");
        },
        // 鼠标离开项目，隐藏详细面板
        "mouseleave .cf-item" : function(e){
            $(e.currentTarget).removeAttr("preview");
        },
        // 点击项目动作连接
        "click .cf-item > section .cf-actions li" : function(e){
            var UI  = this;
            var opt = UI.options;
            var context = opt.context || UI;

            var jLi   = $(e.currentTarget);
            var aKey  = jLi.attr("a");
            var jItem = jLi.closest(".cf-item");
            var obj   = jItem.data("@OBJ");
            opt.actions[aKey].handler.call(context, obj, jItem, UI);
        },
        // 点击项目标题
        "click .cf-item > section .cf-ititle" : do_on_preview,
        // 点击项目预览图
        "click .cf-item > section .cf-preview" : do_on_preview,
    },
    //...............................................................
    redraw : function() {},
    //...............................................................
    __gen_item : function(obj) {
        var UI  = this;
        var opt = UI.options;
        var context = opt.context || UI;

        // 生成项
        var jItem = UI.ccode("item");

        // 绘制项目的快捷缩略图
        var jThumb = $(opt.thumbnail.call(context, obj, UI));
        jItem.children("header").append(jThumb);

        // 绘制 thumbnail
        var jItTitle = $(opt.itemTitle.call(context, obj, UI));
        var jPreview = $(opt.preview.call(context, obj, UI));
        jItem.find(">section .cf-ititle").append(jItTitle);
        jItem.find(">section .cf-preview-con").append(jPreview);

        // 绘制扩展命令
        var jUl = jItem.find(">section .cf-actions > ul");
        console.log(opt.actions)
        for(var aKey in opt.actions) {
            // 得到命令项
            var a = opt.actions[aKey];

            // 绘制命令
            var jLi = $('<li><b>'+UI.text(a.text)+'</b>').attr("a", aKey);
            if(a.icon)
                $(a.icon).prependTo(jLi);
            jLi.appendTo(jUl);
        }

        // 返回生成项目
        return jItem.data("@OBJ", obj);
    },
    //...............................................................
    __draw_data : function(objList) {
        var UI  = this;
        var opt = UI.options;

        console.log(objList)

        // 得到最大项目数量
        var maxNB = objList.length;
        if(!opt.multi){
            maxNB = Math.min(maxNB, 1);
        }
        else if(opt.max > 0){
            maxNB = Math.min(maxNB, opt.max);
        }

        // 清空
        UI.arena.empty();
        
        // 循环绘制
        var i = 0;
        for(; i<maxNB; i++) {
            var jItem = UI.__gen_item(objList[i]);
            UI.arena.append(jItem);
        }

        // 是否添加 adder
        UI.__showhide_adder(i);
    },
    //...............................................................
    __showhide_adder : function(nb) {
        var UI = this;
        var jAdd = UI.arena.find(".cf-adder");
        // 确保有
        if(UI._can_add_more(nb)){
            if(jAdd.length == 0)
                UI.ccode("adder").appendTo(UI.arena);
        }
        // 去除
        else {
            jAdd.remove();
        }
    },
    //...............................................................
    showHideAdder : function(){
        var UI = this;
        var objList = UI._get_data(true);
        UI.__showhide_adder(objList.length);
    },
    //...............................................................
    _can_add_more : function(nb) {
        var UI  = this;
        var opt = UI.options;

        return (opt.multi && (opt.max == 0 || nb<opt.max))
               || (!opt.multi && nb==0)
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
    _get_data : function(foreArray) {
        var UI  = this;
        var opt = UI.options;

        // 准备返回值
        var objList = [];

        // 搜索
        UI.arena.children(".cf-item").each(function(){
            objList.push($(this).data("@OBJ"));
        });

        // 只返回数组
        if(foreArray)
            return objList;

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