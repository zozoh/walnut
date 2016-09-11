(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/hm__methods_panel',
    'ui/form/form',
    'ui/form/c_switch',
], function(ZUI, Wn, HmMethods, FormUI, SwitchUI){
//==============================================
var html = function(){/*
<div class="ui-arena hm-prop-block">
    <div class="hmpb-pos" mode="abs">
        <div class="hmpb-pos-box">
            <div class="hmpb-margin" ui-gasket="margin"></div>
            <div class="hmpb-pos-d" key="width"><span><b>{{hmaker.pos.width}}</b><em></em></span></div>
            <div class="hmpb-pos-d" key="height"><span><b>{{hmaker.pos.height}}</b><em></em></span></div>
            <div class="hmpb-pos-d" key="left"><span><b>{{hmaker.pos.left}}</b><em></em></span></div>
            <div class="hmpb-pos-d" key="right"><span><b>{{hmaker.pos.right}}</b><em></em></span></div>
            <div class="hmpb-pos-d" key="top"><span><b>{{hmaker.pos.top}}</b><em></em></span></div>
            <div class="hmpb-pos-d" key="bottom"><span><b>{{hmaker.pos.bottom}}</b><em></em></span></div>
            <div class="hmpb-pos-v" md="lt" balloon="up:hmaker.pos.v_lt"   val="top,left,width,height"></div>
            <div class="hmpb-pos-v" md="rt" balloon="up:hmaker.pos.v_rt"   val="top,right,width,height"></div>
            <div class="hmpb-pos-v" md="lb" balloon="down:hmaker.pos.v_lb" val="bottom,left,width,height"></div>
            <div class="hmpb-pos-v" md="rb" balloon="down:hmaker.pos.v_rb" val="bottom,right,width,height"></div>
        </div>
        <div class="hmpb-pos-abs">
            <label>
                <dt><i class="fa fa-check-square"></i><i class="fa fa-square-o"></i></dt>
                <dd>{{hmaker.pos.abs}}</dd>
            </label>
        </div>
    </div>
    <div class="hmpb-form" ui-gasket="form"></div>
</div>
*/};
//==============================================
return ZUI.def("app.wn.hm_prop_edit_block", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    init : function() {
        var UI = HmMethods(this);
    },
    //...............................................................
    events : {
        // 切换绝对/相对定位选择框
        "click .hmpb-pos-abs label" : function() {
            var UI = this;
            var prop = UI.pageUI().getBlockProp();

            prop.mode = UI.arena.find(".hmpb-pos").attr("mode") == "abs" ? "inflow" : "abs";

            this.fire("change:block", prop);
        },
        // 点击顶点
        "click .hmpb-pos[mode=abs] .hmpb-pos-v" : function(e) {
            var jq = $(e.currentTarget);
            if(jq.attr("highlight")){
                return;
            }

            var UI = this;

            var posBy  = jq.attr("val");
            var rect   = UI.pageUI().getBlockRectInCss();
            var posVal = UI.transRectToPosVal(rect, posBy);

            this.fire("change:block", {
                "posBy"  : posBy,
                "posVal" : posVal
            });
        },
        // 编辑位置/宽高
        "click .hmpb-pos-d[show] em" : function(e) {
            var UI = this;
            var jq = $(e.currentTarget);
            var key = jq.parents(".hmpb-pos-d").attr("key");
            var prop = UI.pageUI().getBlockProp();

            // 得到模式
            var md = UI.arena.find(".hmpb-pos").attr("mode");

            // 声明后续处理方式
            var __after_input_ok = function(val) {
                // 绝对定位
                if("abs" == md) {
                    var pKeys = (prop.posBy||"").split(/\W+/);
                    var pVals = (prop.posVal||"").split(/[^\dpx%.-]+/);
                    var css = _.object(pKeys,pVals);
                    css[key] = val;
                    prop.posBy  = _.keys(css).join(",");
                    prop.posVal = _.values(css).join(",");
                }
                // 相对定位 
                else {
                    prop[key] = val;
                }
                // 通知
                UI.fire("change:block", prop);
            }

            // 监视编辑
            $z.editIt(jq, function(newval, oldval){
                var val = $.trim(newval) || "auto";
                if(val && val!=oldval) {
                    // 看看值是否合法，合法就进行后续处理
                    var m = /^(([\d.]+)(px)?(%)?|auto)$/.exec(val);
                    if(m) {
                        // 如果没有单位自动补上 px
                        if(m[2] && !m[3] && !m[4]){
                            val += "px";
                        }
                        // 后续处理
                        __after_input_ok(val);
                    }
                }
            });

        }
    },
    //...............................................................
    changePosBy : function(md) {
        var UI = this;
        var ss = md.split(/,/).sort();
        
        var jPosBox = UI.arena.find(".hmpb-pos-box").attr("pos-by", md);

        // 隐藏显示对应尺度
        jPosBox.find(".hmpb-pos-d").each(function(){
            var jD = $(this);
            if(md.indexOf(jD.attr("key")) >= 0) {
                jD.attr("show", "yes");
            }else {
                jD.removeAttr("show");
            }
        });
        // 处理对应的尺度显示点
        var md2 = ss.join(",");
        jPosBox.find(".hmpb-pos-v").each(function(){
            var jV = $(this);
            var vv = jV.attr("val").split(/,/).sort();
            if(md2 == vv.join(",")) {
                jV.attr("highlight", "yes");
            }else {
                jV.removeAttr("highlight");
            }
        });

        return md;
    },
    //...............................................................
    update : function(prop, full) {
        var UI   = this;
        var jPos = UI.arena.find(".hmpb-pos");

        // 模式
        if(prop.mode)
            jPos.attr("mode", prop.mode);
        else
            prop.mode = jPos.attr("mode");
        
        // 绝对定位
        if("abs" == prop.mode) {
            // 打开提示
            UI.balloon();

            // 更新位置信息
            var posBy;
            if(prop.posBy){
                posBy = UI.changePosBy(prop.posBy);
            } else {
                posBy = UI.arena.find(".hmpb-pos-box").attr("pos-by");
            }

            // 改变位置的值
            if(posBy && prop.posVal) {
                var posKeys = posBy.split(/,/);
                var posVals = prop.posVal.split(/,/);
                for(var i=0; i<posKeys.length; i++) {
                    var key = posKeys[i];
                    var val = posVals[i];
                    UI.arena.find('.hmpb-pos-d[key="'+key+'"] em').text(val);
                }
            }
        }
        // 相对定位
        else {
            // 关闭提示
            UI.balloon(false);

            // 更新显示
            UI.changePosBy("width,height");

            // 宽
            if(prop.width) {
                UI.arena.find('.hmpb-pos-d[key="width"] em').text(prop.width);
            }
            // 高
            if(prop.height) {
                UI.arena.find('.hmpb-pos-d[key="height"] em').text(prop.height);
            }
            // 排序
            UI.gasket.margin.setData(prop.margin);
        }

        // 其他属性
        var dd = _.omit(prop, "mode", "posBy", "posVal");
        //console.log(dd)

        // 完全更新
        if(full){
            UI.gasket.form.setData(dd);
        }
        // 增量更新
        else {
            UI.gasket.form.update(dd);
        }
    },
    //...............................................................
    redraw : function() {
        var UI = this;

        // 测试用代码
        UI.arena.find(".hmpb-pos-d em").text("nAn");

        // margin 
        new SwitchUI({
            parent : UI,
            gasketName : "margin",
            on_change : function(val){
                UI.fire("change:block", {margin:val});
            },
            items : [{
                icon : '<i class="fa fa-align-left">',
                val  : '',
            }, {
                icon : '<i class="fa fa-align-center">',
                val  : '0 auto',
            }, {
                icon : '<i class="fa fa-align-right">',
                val  : '0 0 0 auto',
            }]
        }).render(function(){
            UI.defer_report("margin");
        });
        
        // 创建其他属性
        new FormUI({
            parent : UI,
            gasketName : "form",
            uiWidth: "all",
            on_change : function(key, val) {
                UI.notifyBlockChange($z.obj(key, val));
            },
            fields : [{
                key    : "padding",
                title  : "i18n:hmaker.prop.padding",
                type   : "string",
                editAs : "input"
            }, {
                key    : "border",
                title  : "i18n:hmaker.prop.border",
                type   : "string",
                editAs : "input"
            }, {
                key    : "borderRadius",
                title  : "i18n:hmaker.prop.borderRadius",
                type   : "string",
                editAs : "input"
            }, {
                key    : "color",
                title  : "i18n:hmaker.prop.color",
                type   : "string",
                editAs : "color",
            }, {
                key    : "background",
                title  : "i18n:hmaker.prop.background",
                type   : "string",
                nullAsUndefined : true,
                editAs : "background",
                uiConf : UI.getBackgroundImageEditConf()
            }, {
                key    : "boxShadow",
                title  : "i18n:hmaker.prop.boxShadow",
                type   : "string",
                editAs : "input"
            }, {
                key    : "overflow",
                title  : "i18n:hmaker.prop.overflow",
                type   : "string",
                editAs : "switch", 
                uiConf : {
                    items : [{
                        text : 'i18n:hmaker.prop.overflow_visible',
                        val  : 'visible',
                    }, {
                        text : 'i18n:hmaker.prop.overflow_auto',
                        val  : 'auto',
                    }, {
                        text : 'i18n:hmaker.prop.overflow_hidden',
                        val  : 'hidden',
                    }]
                }
            }]
        }).render(function(){
            // 位置编辑界面
            //UI.arena.find(".hmpb-pos-v").first().click();

            // 汇报加载成功
            UI.defer_report("form");
        });

        return  ["margin", "form"];
    },
    //...............................................................
    resize : function() {
        var UI = this;
        var jPos  = UI.arena.find(".hmpb-pos");
        var jForm = UI.arena.find(".hmpb-form");

        jForm.css("top", jPos.outerHeight(true));
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);