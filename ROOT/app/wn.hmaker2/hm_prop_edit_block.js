(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/form/form',
    'app/wn.hmaker2/hm__methods',
], function(ZUI, Wn, FormUI, HmMethods){
//==============================================
var html = function(){/*
<div class="ui-arena hm-prop-block">
    <div class="hmpb-pos" mode="abs">
        <div class="hmpb-pos-box">
            <div class="hmpb-pos-d" key="left"><span><b>{{hmaker.pos.left}}</b><em></em></span></div>
            <div class="hmpb-pos-d" key="right"><span><b>{{hmaker.pos.right}}</b><em></em></span></div>
            <div class="hmpb-pos-d" key="top"><span><b>{{hmaker.pos.top}}</b><em></em></span></div>
            <div class="hmpb-pos-d" key="bottom"><span><b>{{hmaker.pos.bottom}}</b><em></em></span></div>
            <div class="hmpb-pos-d" key="width"><span><b>{{hmaker.pos.width}}</b><em></em></span></div>
            <div class="hmpb-pos-d" key="height"><span><b>{{hmaker.pos.height}}</b><em></em></span></div>
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
        "click .hmpb-pos-abs label" : function() {
            console.log("haha")
            var UI = this;
            var md = UI.arena.find(".hmpb-pos").attr("mode") == "abs" ? "inflow" : "abs";
            this.fire("change:block", {
                "mode" : md
            });
        },
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
    update : function(prop) {
        var UI = this;

        // 模式
        if(prop.mode) {
            UI.arena.find(".hmpb-pos").attr("mode", prop.mode);
            // 绝对定位的提示
            if("abs" == prop.mode) {
                UI.balloon();
            }
            // 关闭提示
            else {
                UI.balloon(false);
            }
        }

        // 位置
        var posBy;
        if(prop.posBy){
            posBy = UI.changePosBy(prop.posBy);
        } else {
            posBy = UI.arena.find(".hmpb-pos-box").attr("pos-by");
        }

        if(posBy && prop.posVal) {
            var posKeys = posBy.split(/,/);
            var posVals = prop.posVal.split(/,/);
            for(var i=0; i<posKeys.length; i++) {
                var key = posKeys[i];
                var val = posVals[i];
                UI.arena.find('.hmpb-pos-d[key="'+key+'"] em').text(val);
            }
        }

        // 其他属性
        var dd = _.omit(prop, "mode", "posBy", "posVal");
        //console.log(dd)
        UI.gasket.form.update(dd);
    },
    //...............................................................
    redraw : function() {
        var UI = this;

        // 测试用代码
        UI.arena.find(".hmpb-pos-d em").text("nAn");
        
        // 创建其他属性
        new FormUI({
            parent : UI,
            gasketName : "form",
            uiWidth: "all",
            on_change : function(key, val) {
                UI.fire("change:block", $z.obj(key, val));
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
                uiConf : {
                    formatData : function(color){
                        return color ? color.RGBA : null;
                    }
                }
            }, {
                key    : "background",
                title  : "i18n:hmaker.prop.background",
                type   : "string",
                nullAsUndefined : true,
                editAs : "color",
                uiConf : {
                    formatData : function(color){
                        return color ? color.RGBA : null;
                    }
                }
            }, {
                key    : "boxShadow",
                title  : "i18n:hmaker.prop.boxShadow",
                type   : "string",
                editAs : "input"
            }]
        }).render(function(){
            // 位置编辑界面
            //UI.arena.find(".hmpb-pos-v").first().click();

            // 汇报加载成功
            UI.defer_report("form");
        });

        return  ["form"];
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