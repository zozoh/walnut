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
    css  : ["theme/ui/otiles/otiles.css",
            "theme/ui/support/thumbnail.css"],
    //..............................................
    init : function(opt){
        var UI  = ListMethods(this);
        // 父类
        UI.__setup_options(opt);
    },
    //..............................................
    events : {
        "click .list-item .wnobj-thumbnail, .list-item .wnobj-nm" : function(e){
            this._do_click_list_item(e);
        },
        "click .ui-arena" : function(e){
            var UI  = this;
            var opt = UI.options;

            // 如果点击到了选中的项目的有效区域里，则啥都不做
            if($(e.target).closest('.list-item[li-actived]').length > 0){
                if($(e.target).closest(".wnobj-thumbnail, .wnobj-nm").length > 0)
                    return;
            }

            // 否则取消所有项目选中
            UI.setAllBure();
        },
        "click .list-item[li-actived] .wnobj-nm-con" : function(e) {
            var UI  = this;
            if(UI.options.renameable){
                var jq    = $(e.currentTarget);
                var obj = UI.getData(jq);
                $z.editIt(jq, {
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
                        jq.html('<i class="zmdi zmdi-rotate-right zmdi-hc-spin">');
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
        "dblclick .wnobj-thumbnail .img" : function(e){
            var jq  = $(e.currentTarget);
            var obj = this.getData(jq);
            var url = "/a/open/"+(window.wn_browser_appName||"wn.browser");
            $z.openUrl(url, "_blank", "GET", {
                "ph" : "id:" + obj.id
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
        jItem.html(Wn.gen_wnobj_thumbnail_html(this.options.renameable ? 'B' : 'A'));
        Wn.update_wnobj_thumbnail(UI, obj, jItem, opt.evalThumb, opt.nmMaxLen);
    }
    //...............................................................
});
//==================================================
});
})(window.NutzUtil);