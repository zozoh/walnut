(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/form/c_input'
], function(ZUI, Wn, InputUI){
//==============================================
var html = function(){/*
<div class="ui-arena" style="padding:20px;">
    <div ui-gasket="t0"></div>
    <br>
    <div ui-gasket="t1"></div>
    <br>
    <div ui-gasket="t2"></div>
</div>
*/};
//===================================================================
return ZUI.def("ui.form_test_input", {
    //...............................................................
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    redraw : function(){
        var UI = this;

        //...........................................................
        new InputUI({
            parent : UI,
            gasketName : "t0",
            placeholder : "t0",
            width: 300,
            on_change : function(val) {
                console.log("new value::", val);
            },
            // assist : {
            //     uiType : "ui/form/c_date_range"
            // }
            assist : {
                //text : "更多",
                uiType : "ui/form/c_list",
                uiConf : {
                    drawOnSetData : true,
                    items : 'obj ~ -match \'race:"DIR", nm:"^{{val}}"\' -limit 10 -json -l -e "^(id|tp|race|nm)$"',
                    itemArgs : {val : ".+"},
                    icon : function(o){
                        return Wn.objIconHtml(o);
                    },
                    text : function(o) {
                        return Wn.objDisplayName(UI, o.nm, 0);
                    },
                    value : function(o) {
                        return o.nm;
                    }
                }
            }
        }).render(function(){
            UI.defer_report("t0");
        });

        //...........................................................
        // 同步的示例
        new InputUI({
            parent : UI,
            gasketName : "t1",
            width: 300,
            placeholder : "t1",
            assist : {
                uiType : "ui/form/c_list",
                uiConf : {
                    drawOnSetData : true,
                    items : function(val){
                        val = val || "--";
                        console.log(" value=", val);
                        // 这个 val 就是 input 的值
                        // 随便根据这个值假冒几个
                        var re = [];
                        for(var i=0; i<10; i++) {
                            re.push({
                                text  : "T" + val,
                                value : "v" + val,
                            });
                        }
                        // ! 注意这个函数必须是同步的，不能有异步调用
                        return re;
                    },
                }
            }
        }).render(function(){
            UI.defer_report("t1");
        });

        //...........................................................
        // 异步的示例
        new InputUI({
            parent : UI,
            gasketName : "t2",
            width: 300,
            placeholder : "t2",
            assist : {
                uiType : "ui/form/c_list",
                uiConf : {
                    drawOnSetData : true,
                    items : function(val, callback){
                        // 准备命令
                        var cmdText = 'obj ~ -match \'race:"DIR", nm:"^{{val}}"\' '
                                      +' -limit 10 -json -l -e "^(id|tp|race|nm)$"';

                        // 异步执行
                        Wn.execf(cmdText, {
                            val : val,
                        }, function(re) {
                            var list = $z.fromJson(re);
                            callback(list);
                        });
                    },
                    icon : function(o){
                        return Wn.objIconHtml(o);
                    },
                    text : function(o) {
                        return Wn.objDisplayName(UI, o.nm, 0);
                    },
                    value : function(o) {
                        return o.nm;
                    }
                }
            }
        }).render(function(){
            UI.defer_report("t2");
        });
        //...........................................................
        return ["t0", "t1", "t2"];
    },
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);