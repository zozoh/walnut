(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/form/form'
], function(ZUI, Wn, FormUI){
//==============================================
var html = function(){/*
<div class="ui-arena bc-step2-params" ui-fitparent="yes">
    <header></header>
    <section ui-gasket="form"></section>
</div>
*/};
//==============================================
return ZUI.def("ui.ext.bc_step2_params", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    redraw : function() {
        var UI = this;

        new FormUI({
            parent : UI,
            gasketName : "form",
            uiWidth : "auto",
            mergeData : false,
            on_change : function(key, val) {
                UI.sync_form_status();
            },
            fields : [{
                key: "mode",
                title: "Mode",
                type: "string",
                editAs: "switch",
                dft: "M",
                uiConf: {
                    items: [
                        {text: "手动", val: "M"}, 
                        {text: "定时", val: "A"}
                    ]
                }
            }, {
                key: "op",
                title: "操作",
                type: "string",
                editAs: "switch",
                dft: "off",
                uiConf: {
                    items: [
                        {text: "关闭", val: "off"},
                        {text: "打开", val: "on"}]
                }
            }, {
                "key": "auto",
                "title": "定时",
                "type": "object",
                "editAs": "combotable",
                "uiConf": {
                    "fields": [{
                        key: "op",
                        title: "状态",
                        type: "string",
                        editAs: "switch",
                        dft: "off",
                        uiConf: {
                            items: [
                                {text: "断电", val: "off"}, 
                                {text: "通电", val: "on"}]
                        }
                    }, {
                        key: "time",
                        title: "时间",
                        type: "string",
                        dft: null,
                        editAs: "time",
                        format: "HHmmss"
                    }],
                    "adder": {
                        "icon": "<i class=\"zmdi zmdi-plus\"></i>",
                        "text": "新增",
                        "data": {}
                    }
                }
            }]
        }).render(function(){
            UI.defer_report("form");
        });

        return ["form"];
    },
    //...............................................................
    sync_form_status : function() {
        var UI = this;
        var params = UI.gasket.form.getData();
        if('M' == params.mode) {
            UI.gasket.form.disableField("auto");
        }else {
            UI.gasket.form.enableField("auto");
        }
    },
    //...............................................................
    isDataReady : function(){
    	return true;
    },
    //...............................................................
    getData : function(data) {
        return {
            params : this.gasket.form.getData()
        };
    },
    //...............................................................
    setData : function(data) {
        var UI = this;
        var opt = UI.options;
        var jH = UI.arena.find('>header');

        if(!_.isArray(data.targets)){
            return;
        }

        //---------------------------------------
        // 描述目标
        var names = [];
        for(var i=0; i<data.targets.length; i++) {
            if(i>2) {
                names.push('...');
                break;
            }
            var ta = data.targets[i];
            names.push(ta[opt.nameKey]);
        }
        jH.text(UI.msg('batchcmds.cf_target', {
            names : names.join(', '),
            nb : data.targets.length
        }));
        //---------------------------------------
        // 设置参数
        var params = data.params || {};
        UI.gasket.form.setData(params);
        UI.sync_form_status();
    },
    //...............................................................
    resize : function() {
        var UI = this;
        var jH = UI.arena.find('>header');
        var jS = UI.arena.find('>section');

        jS.css('height', UI.arena.height() - jH.outerHeight());
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);