(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/hm__methods_com',
    'app/wn.hmaker2/support/dynamic_data_setting',
    'ui/form/form',
    'ui/form/c_droplist',
], function(ZUI, Wn, HmComMethods, DynamicDataSettingUI, FormUI, DroplistUI){
//==============================================
var html = `
<div class="ui-arena hmc-objlist-prop" ui-fitparent="yes">
    <section class="olstp-dds">
        <h4><b>{{hmaker.com.objlist.dds}}</b></h4>
        <div ui-gasket="dds"></div>
    </section>
    <section class="olstp-filter">
        <h4>
            <span class="olstp-check"><i class="fa fa-square-o"></i><i class="fa fa-check-square"></i></span>
            <b>{{hmaker.com.objlist.filter}}</b>
            <ul>
                <li a="add"  balloon="up:hmaker.com.objlist.flt_add"> <i class="zmdi zmdi-plus"></i></li>
                <li a="del"  balloon="up:hmaker.com.objlist.flt_del"> <i class="zmdi zmdi-delete"></i></li>
                <li a="up"   balloon="up:hmaker.com.objlist.flt_mvup"><i class="zmdi zmdi-long-arrow-up"></i></li>
                <li a="down" balloon="up:hmaker.com.objlist.flt_mvdown"><i class="zmdi zmdi-long-arrow-down"></i></li>
            </ul>
        </h4>
        <div>
            <div>
                <div class="olstp-kwd" part-disabled="yes">
                    <div class="olstp-kwd-label">
                        <span class="olstp-check"><i class="fa fa-square-o"></i><i class="fa fa-check-square"></i></span>
                        <b>{{hmaker.com.objlist.flt_kwd}}</b>
                        <input placeholder="{{hmaker.com.objlist.flt_kwd_tip}}">
                    </div>
                    <div  class="olstp-kwd-tip">{{hmaker.com.objlist.flt_kwd_tip2}}</div>
                </div>
            </div>
            <div class="olstp-items"></div>
        </div>
    </section>
    <section class="olstp-sorter">
        <h4>
            <span class="olstp-check"><i class="fa fa-square-o"></i><i class="fa fa-check-square"></i></span>
            <b>{{hmaker.com.objlist.sort}}</b>
            <ul>
                <li a="add"  balloon="up:hmaker.com.objlist.sort_add"> <i class="zmdi zmdi-plus"></i></li>
                <li a="del"  balloon="up:hmaker.com.objlist.sort_del"> <i class="zmdi zmdi-delete"></i></li>
                <li a="up"   balloon="up:hmaker.com.objlist.sort_mvup"><i class="zmdi zmdi-long-arrow-up"></i></li>
                <li a="down" balloon="up:hmaker.com.objlist.sort_mvdown"><i class="zmdi zmdi-long-arrow-down"></i></li>
            </ul>
        </h4>
        <div><div class="olstp-items"></div></div>
    </section>
    <section class="olstp-pager">
        <h4>
            <span class="olstp-check"><i class="fa fa-square-o"></i><i class="fa fa-check-square"></i></span>
            <b>{{hmaker.com.objlist.pager}}</b>
        </h4>
        <div ui-gasket="pager"></div>
    </section>
</div>`;
//==============================================
return ZUI.def("app.wn.hm_com_objlist_prop", {
    dom  : html,
    //...............................................................
    init : function(){
        HmComMethods(this);
    },
    //...............................................................
    events : {
        // 启用/关闭 过滤器/排序器/分页器
        'click h4 .olstp-check' : function(e) {
            var jPart = $(e.currentTarget).closest("section");
            $z.toggleAttr(jPart, "part-disabled", "yes");
        },
        // 启用/关闭 关键字
        'click .olstp-kwd .olstp-check' : function(e) {
            var jKwd = $(e.currentTarget).closest(".olstp-kwd");
            $z.toggleAttr(jKwd, "part-disabled", "yes");
        },
    },
    //...............................................................
    redraw : function() {
        var UI = this;

        // 打开提示
        UI.balloon();

        // 得到 API 的主目录
        var oApiHome = Wn.fetch("~/.regapi/api");

        // DDS
        new DynamicDataSettingUI({
            parent : UI,
            gasketName : "dds",
        }).render(function(){
            UI.defer_report("dds");
        });

        // Pager
        new FormUI({
            parent : UI,
            gasketName : "pager",
            uiWidth : "all",
            fields : [{
                key   : "sizes",
                title : "i18n:hmaker.com.objlist.pg_sizes",
                type  : "object",
                editAs : "input",
                uiConf : {
                    formatData : function(str) {
                        return (str||"").split(/[ \t,，;；\n]+/g);
                    },
                    parseData : function(sizes) {
                        return (sizes || []).join(",");
                    }
                }
            }, {
                key   : "defaultPageSize",
                title : "i18n:hmaker.com.objlist.pg_dftpgsz",
                type  : "int",
                dft   : 50,
                editAs : "input"
            }, {
                key   : "localStoreKey",
                title : "i18n:hmaker.com.objlist.lskey",
                type  : "string",
                editAs : "input"
            }, {
                key   : "autoHide",
                title : "i18n:hmaker.com.objlist.pg_auto_hide",
                type  : "boolean",
                editAs : "switch"
            }, {
                key   : "style",
                title : "i18n:hmaker.com.objlist.pg_style",
                type  : "string",
                editAs : "switch",
                uiConf : {
                    items : [{
                        text : "i18n:hmaker.com.objlist.pg_style_normal",
                        val  : "normal",
                    }, {
                        text : "i18n:hmaker.com.objlist.pg_style_jump",
                        val  : "jump",
                    }]
                }
            }, {
                key   : "i18n",
                title : "i18n:hmaker.com.objlist.pg_i18n",
                type  : "object",
                editAs : "text",
                dft : UI.msg("hmaker.com.objlist.pg_i18n_dft"),
                uiConf : {
                    height: 110,
                    formatData : function(str) {
                        var i18n = {};
                        var lines = str.split(/(\r?\n)+/g);
                        for(var line of lines) {
                            var ss  = line.split(/:/);
                            var key = $.trim(ss[0]);
                            var val = $.trim(ss[1]);
                            i18n[key] = val;
                        }
                        return i18n;
                    },
                    parseData : function(i18n) {
                        var str = "";
                        for(var key in i18n){
                            str += key + " : " + i18n[key] + "\n";
                        }
                        return $.trim(str);
                    }
                }
            }]
        }).render(function(){
            UI.defer_report("pager");
        });


        // 返回延迟加载
        return ["dds", "pager"];
    },
    //...............................................................
    update : function(com) {
        var UI = this;
        
        console.log("I am update")

        // 更新翻页器
        UI.gasket.pager.setData(com.pager||{});

        // 最后在调用一遍 resize
        UI.resize(true);
    },
    //...............................................................
    resize : function() {
        var UI = this;

    }
});
//===================================================================
});
})(window.NutzUtil);