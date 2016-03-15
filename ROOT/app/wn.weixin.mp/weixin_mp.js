(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/list/list',
    'ui/form/form'
], function(ZUI, Wn, ListUI, FormUI){
//==============================================
var html = function(){/*
<div class="ui-arena weixin-mp" ui-fitparent="yes">
    <h2><i class="fa fa-list"></i> <b>{{weixin.mp_list}}</b></h2>
    <div class="wxmp-list" ui-gasket="list"></div>
    <div class="wxmp-main" ui-gasket="main"></div>
</div>
*/};
//==============================================
return ZUI.def("app.wn.weixin.mp", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "theme/app/wn.weixin.mp/weixin_mp.css",
    i18n : "app/wn.weixin.mp/i18n/{{lang}}.js",
    //...............................................................
    init : function(){
        this.my_fields = $z.loadResource("jso:///a/load/wn.weixin.mp/form_weixin_mp.js");
    },
    //...............................................................
    do_del : function(){
        var UI  = this;
        var oGh = UI.gasket.list.getActived();
        console.log(oGh)
        // 提示用户
        if(!window.confirm(UI.msg("weixin.mp_del_confirm")))
            return;
        // 执行删除
        var cmdText = 'rm -rf id:'+oGh.id;
        Wn.exec(cmdText, function(){
            var jN2 = UI.gasket.list.remove(oGh.id);
            UI.gasket.list.setActived(jN2);
        });
    },
    //...............................................................
    do_add : function(){
        var UI  = this;
        var oid = UI.$el.attr("oid");
        var o   = Wn.getById(oid);

        // 要求用户输入微信号
        var pnb = $.trim(window.prompt(UI.msg("weixin.mp_add_pnb")));
        if(!pnb) {
            return;
        }

        // 检查微信号格式
        if(!/^gh_[\d\w]{6,}/.test(pnb)){
            alert(UI.msg("weixin.mp_add_invalid"));
            return;
        }

        // 执行添加操作
        var cmdText = "mkdir -o id:"+o.id+"/"+pnb;
        console.log(cmdText)
        Wn.exec(cmdText, function(re){
            var oGh = $z.fromJson(re);
            UI.refresh(function(){
                UI.gasket.list.setActived(oGh.id);
            });
        });
    },
    //...............................................................
    do_save : function(callback){
        var UI     = this;
        var wxconf = UI.gasket.main.getData();

        // 得到内容
        var content = $z.toJson(wxconf, null, '    ');
        //console.log(content);
        
        // 得到公众号对象
        var oGh = UI.gasket.list.getActived();
        var oConf = Wn.fetch(oGh.ph + "/wxconf");

        // 执行保存
        Wn.write(oConf, content, callback);

    },
    //...............................................................
    // o 表示公众号的索引对象
    _draw_gh : function(o){
        var UI = this;

        // 记录一下以便下次自动高亮
        UI.local("gh_actived", o.id);

        // 获取公众号的配置信息
        var oConf = Wn.fetchBy("touch -o id:"+o.id+"/wxconf;")
        var json  = $.trim(Wn.read(oConf));

        var wxconf = json ? $z.fromJson(json) : {};

        // 显示表单
        new FormUI({
            parent : UI,
            gasketName : 'main',
            cols : 2,
            colSizeHint : [0.4],
            fields : UI.my_fields
        }).render(function(){
            //console.log("setdata")
            this.setData(wxconf);
        });
    },
    //...............................................................
    redraw : function(){
        var UI  = this;
        var opt = UI.options;

        // 显示列表
        new ListUI({
            parent : UI,
            gasketName : "list",
            escapeHtml : false,
            display : function(o) {
                var html = '<i class="fa fa-weixin"></i>';
                html += '<b>' + o.nm + '</b>';
                if(o.lbls && o.lbls.length > 0){
                    html += '<div class="lbls">';
                    for(var i=0; i<o.lbls.length; i++){
                        html += '<em>' + o.lbls[i] + '</em>';
                    }
                    html += '</div>'    
                }
                return html;
            },
            on_actived : function(o){
                UI._draw_gh(o);
            }
        }).render(function(){
            UI.defer_report("list");
        });
        
        // 返回延迟
        return ["list"];
    },
    //...............................................................
    refresh : function(callback, autoActiveOne){
        var UI = this;
        var oid = UI.$el.attr("oid");

        // 支持 refresh(true) 这种格式
        if(_.isBoolean(callback)){
            autoActiveOne = callback;
            callback  = undefined;
        }

        // 清理缓存
        // if(clearCache){
        //     Wn.clearCache("oid:" + oid);
        // }

        // 得到主目录
        var o = Wn.getById(oid);

        // 清除 children 的缓存
        if(o && o.children && o.children.length > 0){
            Wn.saveToCache(o, true);
        }

        // 更新列表
        Wn.getChildren(o, null, function(objs){
            //console.log(objs)
            UI.gasket.list.setData(objs);

            // 刷新后，是否自动高亮一个
            if(autoActiveOne){
                var ghaid = UI.local("gh_actived");
                if(ghaid) {
                    UI.gasket.list.setActived(ghaid);
                }
                if(!UI.gasket.list.isActived(ghaid)){
                    UI.gasket.list.setActived(0);
                }
            }

            // 调用回调
            $z.doCallback(callback, [objs]);
        });
    },
    //...............................................................
    update : function(o, callback) {
        var UI = this;

        // 记录
        UI.$el.attr("oid", o.id);
        
        // 更新列表
        UI.refresh(callback, true);
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);