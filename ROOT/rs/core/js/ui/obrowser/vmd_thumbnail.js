(function($z){
$z.declare(['zui', 'wn/util'], function(ZUI, Wn){
//==============================================
var html = function(){/*
<div class="ui-arena obrowser-vmd-thumbnail wn-thumbnail" ui-fitparent="yes">I am thumbnail</div>
*/};
//==============================================
return ZUI.def("ui.obrowser_vmd_thumbnail", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //..............................................
    // init : function(){
    //     var UI = this;
    //     UI.on("ui:display", function(){
    //         UI.arena.find(".wnobj-nm-con").each(function(){
    //             // 溢出了，处理一下
    //             if(this.scrollHeight > this.offsetHeight){
    //                 $(this).addClass("wnobj-nm-overflow")
    //             }
    //         });
    //     });
    // },
    //..............................................
    events : {
        "dblclick .wnobj-thumbnail" : function(e){
            var UI = this;
            // 如果支持单击就打开就啥也不做了，因为单击会打开
            if(UI.options.singleClickOpen){
                return;
            }
            var o = this.getData(e.currentTarget);
            UI.browser.setData("id:"+o.id);
        },
        "click .wnobj-thumbnail" : function(e){
            var UI = this;
            var jq = $(e.currentTarget);
            // 如果支持单击就打开 ...
            if(UI.options.singleClickOpen){
                var o = this.getData(e.currentTarget);
                if(UI.browser.canOpen(o)){
                    UI.browser.setData("id:"+o.id);
                    return;
                }
            }
            // 否则表示选中
            UI.__set_actived(jq, e.shiftKey, $z.os.mac?e.metaKey:$z.os.ctrlKey);
        },
        "click .wnobj-nm" : function(e){
            var UI = this;
            var jq = $(e.currentTarget);
            var ctrlIsOn = $z.os.mac?e.metaKey:$z.os.ctrlKey;
            // 如果是选中的，那么就改名
            if(UI.isActived(jq) && !ctrlIsOn){
                UI.browser.rename();
            }
            // 否则表示选中
            else{
                UI.__set_actived(jq, e.shiftKey, ctrlIsOn);
            }
        },
        // 取消全部选择
        "click .ui-arena" : function(e){
            var UI = this;
            if($(e.target).hasClass("ui-arena")){
                UI.arena.find(".wnobj")
                    .removeClass("wnobj-actived")
                    .removeClass("wnobj-checked");
                // 发出通知
                UI.__notify_footer();
            }
        }
    },
    //..............................................
    __set_actived : function(ele, shiftIsOn, ctrlIsOn) {
        var UI   = this;
        var jObj = $(ele).closest(".wnobj");

        // 得到原来的激活项
        var jA   = UI.arena.find(".wnobj-actived").not(jObj);

        // 单个多选
        if(ctrlIsOn && UI.browser.options.multi){
            //console.log("haha",jObj.size())
            jA.removeClass("wnobj-actived");
            jObj.toggleClass("wnobj-actived")
                .toggleClass("wnobj-checked");
        }
        // 如果是多选
        else if(shiftIsOn && UI.browser.options.multi){
            // 没的激活，激活自己
            if(jA.size() == 0){
                jA = UI.arena.find(".wnobj:first-child");
                jObj.addClass("wnobj-actived wnobj-checked");
            }

            // 寻找开始结束
            var pos_a = jA.index();
            var pos_o = jObj.index();
            var jq
            
            // 如果相等就高亮一个
            if(pos_a == pos_o){
                jq = jA;
            }
            // jObj 在前面
            else if(pos_a > pos_o){
                jq = jObj.nextUntil(jA).addBack().add(jA);
            }
            // jObj 在后面
            else {
                jq = jObj.prevUntil(jA).addBack().add(jA);
            }
            jq.addClass("wnobj-checked");
        }
        // 改变激活项目
        else{
            jA.removeClass("wnobj-actived");
            UI.arena.find(".wnobj-checked").removeClass("wnobj-checked");
            jObj.addClass("wnobj-actived wnobj-checked");
        }
        // 发出通知
        UI.__notify_footer();
    },
    //..............................................
    __notify_footer : function(){
        var UI = this;
        UI.browser.trigger("browser:info", UI.msg("obrowser.selectNobj", {n:UI.getChecked().length}));
    },
    //..............................................
    update : function(o, callback){
        var UI = this;


        // 显示正在加载
        UI.showLoading();

        // 得到当前所有的子节点
        Wn.getChildren(o, UI.browser.options.filter, function(objs){
            UI.hideLoading();
            
            // 清空
            UI.arena.empty();

            // 循环在选区内绘制图标
            objs.forEach(function(obj){
                Wn.gen_wnobj_thumbnail(UI, obj, 
                    'span',
                    UI.browser.options.thumbnail
                ).appendTo(UI.arena);
            });

            // 调用回调
            $z.doCallback(callback, [objs]);
        }, true);
    },
    //..............................................
    $item : function(it){
        var UI = this;
        // 默认用更新
        if(_.isUndefined(it)){
            return UI.arena.find(".wnobj-actived");
        }
        // 如果是字符串表示 ID
        else if(_.isString(it)){
            return UI.arena.find(".wnobj[oid="+it+"]");
        }
        // 本身就是 dom
        else if(_.isElement(it) || $z.isjQuery(it)){
            return $(it).closest(".wnobj");
        }
        // 数字
        else if(_.isNumber(it)){
            return $z.jq(UI.arena, it, ".wnobj");
        }
        // 靠不晓得了
        else {
            throw "unknowns row selector: " + row;
        }
    },
    //..............................................
    getData : function(arg){
        return this.browser.getById($(arg).closest(".wnobj").attr("oid"));
    },
    //..............................................
    // 修改激活项目的名称
    rename : function(){
        var UI = this;
        var jq = UI.arena.find(".wnobj-actived");
        if(jq.size()==0)
            return;

        // 得到对象
        var o = Wn.getById(jq.attr("oid"));

        // 如果是可编辑的，显示编辑框
        if(Wn.isObjNameEditable(UI, o)) {
            var jNmCon = jq.find(".wnobj-nm-con");
            var jNm = jNmCon.children(".wnobj-nm");
            $z.editIt(jNmCon, {
                multi : true,
                text  : o.nm,
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
                    // 执行改名
                    Wn.exec('mv -T id:'+o.id+' "id:'+o.pid+'/'+newName+'" -o', function(re){
                        var newObj = $z.fromJson(re);
                        Wn.saveToCache(newObj);
                    });
                    // 修改显示
                    jNm.text(newName);
                }
            });
        }
    },
    //..............................................
    isActived : function(ele){
        return $(ele).closest(".wnobj").hasClass("wnobj-actived");
    },
    //..............................................
    getActived : function(){
        var UI = this;
        var jq = UI.arena.find(".wnobj-actived");
        if(jq.size()==0)
            return null;
        return UI.browser.getById(jq.attr("oid"));
    },
    setActived : function(arg){
        var UI = this;
        var jq = UI.$item(arg);
        if(jq.size() > 0) {
            UI.__set_actived(jq);
        }
    },
    //..............................................
    getChecked : function(){
        var UI = this;
        var re = [];
        this.arena.find(".wnobj-checked, .wnobj-actived").each(function(){
            re.push(UI.browser.getById($(this).attr("oid")));
        });
        return re;
    }
    //..............................................
});
//==================================================
});
})(window.NutzUtil);