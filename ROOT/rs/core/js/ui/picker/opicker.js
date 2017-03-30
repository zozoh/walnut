(function($z){
$z.declare([
    'zui', 
    'wn/util',
    'ui/pop/pop'
], 
function(ZUI, Wn, POP){
//==============================================
var html = function(){/*
<div class="ui-code-template">
    <span code-id="obj" class="picker-obj">
        <a target="_blank"></a>
    </span>
</div>
<div class="ui-arena picker opicker">
    <div class="picker-box"></div>
    <div class="picker-btn picker-choose">{{choose}}</div>
    <div class="picker-btn picker-clear">{{clear}}</div>
</div>
*/};
//==============================================
return ZUI.def("ui.picker.opicker", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "ui/picker/theme/picker-{{theme}}.css",
    //...............................................................
    init : function(opt) {
        $z.setUndefined(opt, "mustInBase", true);
        $z.setUndefined(opt, "setup", {});
        $z.setUndefined(opt, "clearable", true);
        $z.setUndefined(opt, "keeyAppend", true);
    },
    //...............................................................
    events : {
        // 选择对象
        "click .picker-choose" : function(){
            var UI    = this;
            var opt   = UI.options;
            var conf  = opt.setup || {};

            // 确定基础目录
            var base = opt.base || Wn.fetch("~");

            // 准备数据，没有的话，采用上一次本对话框选择的数据
            var o = UI.getObj();
            //console.log(o)

            if(!o && opt.lastObjKey) {
                var lastObjPath = UI.local(opt.lastObjKey);
                if(lastObjPath)
                    o = Wn.fetch(lastObjPath);
            }
            // 如果要求选择的对象在不给定目录内，相当于 null
            if(opt.mustInBase && o) {
                var rph = Wn.getRelativePath(base, o);
                if(!Wn.isInDir(base, o))
                    o = null;
            }
            // 根据对象重新定义 base
            if(o) {
                // 主目录
                if("./" == Wn.getRelativePathToHome(o)) {
                    base = o;
                }
                // 其他目录显示上一级
                else {
                    base = "id:"+o.pid;
                }
            }

            // 打开对话框
            POP.browser(UI.text(opt.title) || "", _.extend({
                checkable : false,
                sidebar   : false,
                objTagName : 'SPAN',
                width  : "80%",
                height : "80%",
                base   : base,
                canOpen : function(o){
                    return o.race == 'DIR';
                },
                on_ok : function(objs){
                    if(objs && objs.length > 0){
                        //console.log(objs)
                        // 记录第一个对象为上次打开对象
                        if(opt.lastObjKey) {
                            UI.local(opt.lastObjKey, objs[0].ph);
                        }
                        // 执行更新
                        UI._update(objs);
                    }
                }
            }, conf), function(){
                if(o)
                    this.setActived(o);
            });
        },
        // 清除
        "click .picker-clear" : function(){
            this._update();
        }
    },
    //...............................................................
    redraw : function(){
        var UI   = this;
        var opt  = UI.options;
        // 不用取消
        if(!opt.clearable){
            UI.arena.find(".picker-clear").remove();
        }
        // 标记多选
        if(opt.setup.checkable){
            UI.arena.addClass("picker-multi");
        }
        // 标记折行
        if(opt.wrapButton)
            UI.arena.attr("wrap-button", "yes");
    },
    //...............................................................
    setData : function(obj){
        this.ui_parse_data(obj, function(o){
            this._update(o, true);
        });
    },
    //...............................................................
    // 接受标准的 WnObj
    _update : function(o, quit){
        var UI  = this;
        var opt = UI.options;
        var jBox = UI.arena.find(".picker-box");

        // 清除数据
        if(!o){
            jBox.empty();
        }
        // 多选
        else if(opt.setup.checkable) {
            // 确保是数组
            if(!_.isArray(o)){
                o = [o];
            }
            // 是否保持原来的数据
            if(!opt.keeyAppend)
                jBox.empty();
            // 循环添加
            for(var i=0; i<o.length; i++){
                UI.__append_item(o[i], jBox);
            }
        }
        // 单选
        else{
            // 确保不是数组
            if(_.isArray(o)){
                o = o.length > 0 ? o[0] : undefined;
            }
            // 清除数据
            jBox.empty();
            
            // 添加
            UI.__append_item(o, jBox);
        }
        // 回调事件
        if(!quit)
            UI.__on_change();
    },
    //...............................................................
    __on_change : function(){
        var UI  = this;
        var opt = UI.options;
        var context = opt.context || UI;
        var v = UI.getData();
        $z.invoke(opt, "on_change", [v], context);
        UI.trigger("change", v);
    },
    //...............................................................
    __append_item : function(o, jBox){
        var UI  = this;
        var opt = UI.options;

        jBox = jBox || UI.arena.find(".picker-box")
        var jq = UI.ccode("obj").data("@OBJ", o)
                    .prepend(Wn.objIconHtml(o));
        var jA = jq.find("a").prop("href", "/a/open/"
                                    + (window.wn_browser_appName||"wn.browser")
                                    + "?ph=id:"+o.id);

        // 显示名称
        if(opt.showPath) {
            var html = Wn.objDisplayPath(UI, o.ph, 
                    opt.showPath.offset,
                    opt.showPath.wrapper || null, 
                    opt.showPath.sep || null);
            jA.html( html || UI.msg("home"));
        } else {
            jA.text(Wn.objDisplayName(UI, o, 18));
        }
        
        // 加入 DOM
        jBox.append(jq);
    },
    //...............................................................
    getObj : function(opt){
        var UI  = this;
        var opt = opt || this.options;

        var re  = [];
        UI.arena.find(".picker-obj").each(function(){
            re.push($(this).data("@OBJ"));
        });
        if(!opt.setup.checkable){
            re = re.length > 0 ? re[0] : null;
        }
        return re;
    },
    //...............................................................
    getData : function(){
        var UI = this;
        return UI.ui_format_data(function(opt){
            return UI.getObj(opt);
        });
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);