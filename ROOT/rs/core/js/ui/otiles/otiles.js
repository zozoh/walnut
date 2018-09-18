(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/support/list_methods'
], function(ZUI, Wn, ListMethods){
//==============================================
var html = '<div class="ui-arena otiles wn-thumbnail" ui-fitparent="true"></div>';
//==============================================
return ZUI.def("ui.otiles", {
    dom  : html,
    css  : ["ui/otiles/theme/otiles-{{theme}}.css",
            "ui/support/theme/thumbnail-{{theme}}.css"],
    //..............................................
    init : function(opt){
        var UI  = ListMethods(this);
        // 父类
        UI.__setup_options(opt);

        // 默认配置 
        $z.setUndefined(opt, "on_open", function(obj){
            var url = "/a/open/"+(window.wn_browser_appName||"wn.browser");
            $z.openUrl(url, "_blank", "GET", {
                "ph" : "id:" + obj.id
            });
        });
    },
    //..............................................
    events : {
        // 点击激活或者选中项目 
        "click .list-item .wnobj-thumbnail, .list-item .wnobj-nm" : function(e){
            this._do_click_list_item(e);
        },
        // 移动触摸设备，单击表示打开
        "touchend .list-item .wnobj-thumbnail, .list-item .wnobj-nm" : function(e) {
            var UI  = this;
            var opt = UI.options;
            var context = opt.context || UI;

            var jq  = $(e.currentTarget);
            var obj = this.getData(jq);

            $z.invoke(opt, "on_open", [obj, context]);
        },
        // 点击空白区域
        "click .ui-arena" : function(e){
            var UI  = this;
            var opt = UI.options;

            // 如果点击到了选中的项目的有效区域里，则啥都不做
            if($(e.target).closest('.list-item[li-actived]').length > 0){
                if($(e.target).closest(".wnobj-thumbnail, .wnobj-nm").length > 0)
                    return;
            }

            // 否则取消所有项目选中
            if($(e.target).closest(".list-item").length == 0) {
                this.setAllBlur();
            }
        },
        // 改名
        "click .list-item[li-actived] .wnobj-nm" : function(e) {
            this.rename(e.currentTarget);
        },
        // 打开
        "dblclick .wnobj-thumbnail .img" : function(e){
            var UI  = this;
            var opt = UI.options;
            var context = opt.context || UI;

            var jq  = $(e.currentTarget);
            var obj = this.getData(jq);

            $z.invoke(opt, "on_open", [obj, context]);
        }
    },
    //...............................................................
    redraw : function() {
        var UI = this;
        var opt = UI.options;

        // 启用拖拽
        if(opt.drag) {
            UI.arena.moving({
                trigger    : '.wnobj[li-checked]',
                maskClass  : 'wn-dragging',
                // target     : function() {
                //     console.log(this.$selection.find(".wnobj[li-checked]"))
                //     return this.$selection.find(".wnobj[li-checked]");
                // },
                sensors  : function() {
                    var ing = this;
                    // 准备自己的内部可 drop 目标
                    var conf = _.extend({ignoreAncestorMap:{}}, opt.drag);
                    if(_.isArray(ing.data)) {
                        for(var i=0; i<ing.data.length; i++) {
                            var obj = ing.data[i];
                            conf.ignoreAncestorMap[obj.id] = obj;
                        }
                    }
                    else if(ing.data) {
                        conf.ignoreAncestorMap[ing.data.id] = ing.data;
                    }
                    var senList = UI.getDropSensors(conf);

                    // 合并外部的 sensor
                    if(_.isArray(opt.drag.moreDragSensors)){
                        senList = senList.concat(opt.drag.moreDragSensors);
                    }
                    // 动态调用的
                    else if(_.isFunction(opt.drag.moreDragSensors)){
                        var sl2 = opt.drag.moreDragSensors.call(ing, UI);
                        if(_.isArray(sl2) && sl2.length>0) {
                            senList = senList.concat(sl2);
                        }
                    }

                    // 最后返回
                    return senList;
                },
                sensorFunc : {
                    "drag" : {
                        "enter" : function(sen){this.dropInSensor = sen;},
                        "leave" : function(sen){this.dropInSensor = null;}
                    }
                },
                on_begin : function() {
                    var ing   = this;
                    // 记录正在拖拽的数据
                    ing.data = UI.getChecked();

                    // 复制对象以便显示拖拽
                    UI.$checked().each(function(index){
                        // 最多弄六个就成了吧
                        if(index >= 6) 
                            return;
                        // 复制一下目标
                        $(this).clone().css({
                            "position" : "absolute",
                            "width"    : "100%",
                            "height"   : "100%",
                            "top"      : index * 1,
                            "left"     : index * 4,
                            "opacity"  : (1 - index * 0.1),
                        }).prependTo(ing.mask.$target);
                    });

                    // 拖拽目标不要裁掉
                    ing.mask.$target.css("overflow", "visible");

                    // 调用回调
                    $z.invoke(opt.drag, "on_begin", [], ing);
                },
                on_ready : function(){
                    $z.invoke(opt.drag, "on_ready", [], this);
                },
                on_end : function() {
                    var ing   = this;

                    //console.log(ing.dropInSensor);
                    var args = [];
                    if(ing.dropInSensor) {
                        args.push(ing.dropInSensor.data);
                        args.push(ing.dropInSensor.$ele);
                    }

                    // 调用回调
                    $z.invoke(opt.drag, "on_end", args, ing);
                }
            });
        }
    },
    //...............................................................
    /* 获取自己可以被 drop 的传感器
     - conf: 传感器的配置 : {
            name  : "drag",
            rect  : 1,
            scope : null,           // null 表示自动判断

            // 拖拽传感器类型
            drag_sen_type : "folder"

            // true 表示忽略所有选中的项目
            ignoreChecked : true,
            
            // true 表示忽略所有叶子节点
            ignoreLeaf : true;
            
            // 表示过滤方法, 返回 false 表示无视
            filter : F(o, jItem):Boolean 

            // 表示这些节点不许移动
            ignoreIdMap : {ID : {...}, ..},
            
            // 表示这些节点以及其下节点下都不许移动
            ignoreAncestorMap : {ID : {...}, ..},

            // 表示一个虚的根节点
            oRoot : {..}

            // 表示虚的根节点对应的 DOM
            $root : jQuery
        }                       
    */
    getDropSensors : function(conf) {
        var UI = this;

        // 准备默认值
        conf = conf || {};
        $z.setUndefined(conf, "ignoreChecked", true);
        $z.setUndefined(conf, "ignoreLeaf",    true);

        // 准备返回值
        var senList = [];

        // 增加根
        if(conf.oRoot) {
            senList.push({
                drag_sen_type : conf.drag_sen_type || "folder",
                name : "drag",
                rect : 1,
                text : "HOME",
                $ele : conf.$root || UI.$el,
                data : conf.oRoot,
            });
        }

        // 搜索自己的 sensor
        UI.findItem().each(function(){
            var jItem  = $(this);
            var jThumb = jItem.find(".wnobj-thumbnail");
            var obj    = UI.getObj(jThumb);

            var disabled;

            // 叶子节点要搞一下
            if(conf.ignoreLeaf && 'DIR' != obj.race){
                disabled = true;
            }
            // 自定义函数了
            else if(_.isFunction(conf.filter) 
                && !conf.filter(obj, jItem)){
                disabled = true;
            }
            // 自己不能在选中的 ID 里
            else if(conf.ignoreChecked && UI.isChecked(jItem)) {
                disabled = true;
            }
            // 防止自己
            else if(conf.ignoreIdMap && conf.ignoreIdMap[obj.id]) {
                disabled = true;
            }
            // 防止祖先
            else if(conf.ignoreAncestorMap && !_.isEmpty(conf.ignoreAncestorMap)) {
                // 首先自己不能在列表中
                if(conf.ignoreAncestorMap[obj.id]){
                    disabled = true;
                }
                // 找到自己所有的祖先，也都不能在选中的 ID 里
                else {
                    var ans = Wn.getAncestors(obj);
                    for(var i=0; i<ans.length; i++) {
                        var an = ans[i];
                        if(conf.ignoreAncestorMap[an.id]) {
                            disabled = true;
                            break;
                        }
                    }
                }
            }
            // 推入传感器
            senList.push({
                    drag_sen_type : conf.drag_sen_type || "folder",
                    name  : conf.name || "drag",
                    rect  : _.isNumber(conf.rect) ? conf.rect : 1,
                    scope : conf.scope,
                    disabled : disabled,
                    text : obj.nm,
                    $ele : jThumb,
                    data : obj,
                });
        });

        // 返回
        //console.log(senList)
        return senList;
    },
    //...............................................................
    depose : function() {
        var UI = this;
        var opt = UI.options;

        // 启用拖拽
        if(opt.drag) {
            UI.arena.moving("destroy");
        }
    },
    //...............................................................
    rename : function(it){
        var UI    = this;
        var opt   = UI.options;
        var jItem = UI.$item(it);

        if(opt.renameable && jItem.length > 0){
            var jCon  = jItem.find(".wnobj-nm-con");
            var jNm   = jCon.find(".wnobj-nm");
            var obj   = UI.getData(jCon);
            $z.editIt(jCon, {
                text  : obj.nm,
                multi : true,
                enterAsConfirm : true,
                extendWidth  : false,
                extendHeight : false,
                after : function(newval, oldval){
                    var newName = $.trim(newval);
                    // 重名的话，就不搞了
                    if(newval == oldval){
                        return;
                    }
                    // 不能为空
                    if(!newName) {
                        alert(UI.msg("e.fnm.blank"));
                        return;
                    }
                    // 不支持特殊字符
                    if(/['"\\\\\/$%*]/.test(newval)) {
                        alert(UI.msg("e.fnm.invalid"));
                        return;
                    }
                    // 显示改名中
                    jNm.html('<i class="zmdi zmdi-rotate-right zmdi-hc-spin">');
                    // 执行改名
                    Wn.exec('mv -T id:'+obj.id+' "id:'+obj.pid+'/'+newName+'" -o', function(re){
                        var newObj = $z.fromJson(re);
                        Wn.saveToCache(newObj);
                        UI.update(newObj);
                        $z.invoke(opt, "on_rename", [newObj], UI);
                    });
                }
            });
        }
    },
    //...............................................................
    $listBody : function(){
        return this.arena;
    },
    //...............................................................
    $createItem : function(){
        return $('<div class="wnobj">');
    },
    //...............................................................
    _draw_item : function(jItem, obj) {
        var UI  = this;
        var opt = UI.options;
        //console.log(obj)
        jItem.html(Wn.gen_wnobj_thumbnail_html(this.options.renameable ? 'B' : (opt.objTagName || 'A')));
        Wn.update_wnobj_thumbnail(UI, obj, jItem, opt.evalThumb, opt.nmMaxLen);
    },
    //...............................................................
    showProgress : function(it, pe) {
        var UI = this;
        var jItem = UI.$item(it).attr("show-progress", "yes");
        var str = _.isNumber(pe) ? $z.toPercent(pe, 2) : pe;
        jItem.find(".wnobj-ing-nb").text(str);
        jItem.find(".wnobj-ing-bar>span").css({
            "width" : str
        });
    },
    //...............................................................
    hideProgress : function(it) {
        this.$item(it).removeAttr("show-progress");
    },
    //...............................................................
});
//==================================================
});
})(window.NutzUtil);