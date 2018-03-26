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
                maskClass  : 'wn-thumbnail',
                // target     : function() {
                //     console.log(this.$selection.find(".wnobj[li-checked]"))
                //     return this.$selection.find(".wnobj[li-checked]");
                // },
                sensors  : function() {
                    // 准备自己的内部可 drop 目标
                    var senList = UI.getDropSensors(opt.drag);

                    // 合并外部的 sensor
                    if(_.isArray(opt.drag.sensors)){
                        senList = senList.concat(opt.drag.sensors);
                    }
                    // 动态调用的
                    else if(_.isFunction(opt.drag.sensors)){
                        var sl2 = opt.drag.sensors.call(UI);
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
                    //console.log("hahahah", ing.$target.length, ing.$trigger.length)

                    // 将选中的对象复制，然后变虚
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
                    }).css("opacity", 0.1);

                    // 修改拖拽目标样式
                    ing.mask.$target.css("overflow", "visible");

                    // 调用回调
                    $z.invoke(opt.drag, "on_begin", [], UI);
                },
                on_end : function() {
                    var ing   = this;
                    // 恢复原始对象
                    UI.$checked().css("opacity", ""); 

                    //console.log(ing.dropInSensor);
                    var args = [];
                    if(ing.dropInSensor) {
                        args.push(ing.dropInSensor.data);
                        args.push(ing.dropInSensor.$ele);
                    }

                    // 调用回调
                    $z.invoke(opt.drag, "on_end", args, UI);
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

            // true 表示忽略所有选中的项目
            ignoreChecked : true,
            
            // true 表示忽略所有叶子节点
            ignoreLeaf : true;
            
            // 表示过滤方法, 返回 false 表示无视
            filter : F(o, jItem):Boolean 
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

        // 搜索自己的 sensor
        UI.findItem(function(o, jItem){
            if(conf.ignoreChecked && UI.isChecked(o))
                return false;

            if(conf.ignoreLeaf && 'DIR' != o.race)
                return false;

            if(_.isFunction(conf.filter))
                return conf.filter(o, jItem);
            
            return true;
        }).each(function(){
            var jThumb = $(this).find(".wnobj-thumbnail");
            var obj    = UI.getObj(jThumb);
            senList.push(_.extend({
                    name  : conf.name || "drag",
                    rect  : _.isNumber(conf.rect) ? conf.rect : 1,
                    scope : conf.scope,
                }, {
                    text : obj.nm,
                    $ele : jThumb,
                    data : obj,
                }));
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