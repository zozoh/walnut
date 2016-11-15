(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/hm__methods_panel',
    'ui/form/form',
    'ui/form/c_switch',
], function(ZUI, Wn, HmMethods, FormUI, SwitchUI){
//==============================================
var html = `
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
</div>`;
//==============================================
return ZUI.def("app.wn.hm_prop_edit_block", {
    dom  : html,
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
    update : function(uiCom, block) {
        this.__uiCom = uiCom;
        this.__update_pos(block);
        this.__update_form(block);
    },
    //...............................................................
    __update_pos : function(block) {
        var UI   = this;
        var jPos = UI.arena.find(".hmpb-pos");

        // 绝对定位
        if("abs" == block.mode) {
            // 打开提示
            UI.balloon();

            // 更新位置信息
            var posBy;
            if(block.posBy){
                posBy = UI.changePosBy(block.posBy);
            } else {
                posBy = UI.arena.find(".hmpb-pos-box").attr("pos-by");
            }

            // 改变位置的值
            if(posBy && block.posVal) {
                var posKeys = posBy.split(/,/);
                var posVals = block.posVal.split(/,/);
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
            if(block.width) {
                UI.arena.find('.hmpb-pos-d[key="width"] em').text(block.width);
            }
            // 高
            if(block.height) {
                UI.arena.find('.hmpb-pos-d[key="height"] em').text(block.height);
            }
            // 排序
            UI.gasket.margin.setData(block.margin);
        }
    },
    //...............................................................
    __update_form : function(block) {
        var UI = this;
        var uiCom = UI.__uiCom;
        
        // 得到块属性列表
        var blockFields = uiCom.getBlockPropFields(block);
        
        // 看看是否需要重绘字段
        var bf_finger = blockFields.join(",");
        if(UI.__current_block_fields != bf_finger) {
            // 创建其他属性
            new FormUI({
                parent : UI,
                gasketName : "form",
                uiWidth: "all",
                on_change : function(key, val) {
                    UI.notifyBlockChange("panel", $z.obj(key, val));
                },
                fields : UI.__gen_block_fields(blockFields, block)
            }).render(function(){
                // 设置数据
                UI.gasket.form.setData(block);
                // 记录最后的修改
                UI.__current_block_fields = bf_finger;
            });
        }
        // 直接设置数据
        else {
            UI.gasket.form.setData(block);
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
        
        return  ["margin"];
    },
    //...............................................................
    __gen_block_fields : function(blockFields, block) {
        var UI = this;
        var re = [];
        for(var key of blockFields) {
            if("padding" == key) {
                re.push({
                    key    : "padding",
                    title  : "i18n:hmaker.prop.padding",
                    type   : "string",
                    editAs : "input"
                });
            }
            else if("margin" == key) {
                if("abs" != block.mode){
                    re.push({
                        key    : "margin",
                        title  : "i18n:hmaker.prop.margin",
                        type   : "string",
                        editAs : "input"
                    });
                }
            }
            else if("border" == key) {
                re.push({
                    key    : "border",
                    title  : "i18n:hmaker.prop.border",
                    type   : "string",
                    editAs : "input"
                });
            }
            else if("borderRadius" == key) {
                re.push({
                    key    : "borderRadius",
                    title  : "i18n:hmaker.prop.borderRadius",
                    type   : "string",
                    editAs : "input"
                });
            }
            else if("color" == key) {
                re.push({
                    key    : "color",
                    title  : "i18n:hmaker.prop.color",
                    type   : "string",
                    editAs : "color",
                });
            }
            else if("background" == key) {
                re.push({
                    key    : "background",
                    title  : "i18n:hmaker.prop.background",
                    type   : "string",
                    nullAsUndefined : true,
                    editAs : "background",
                    uiConf : UI.getBackgroundImageEditConf()
                });
            }
            else if("boxShadow" == key) {
                re.push({
                    key    : "boxShadow",
                    title  : "i18n:hmaker.prop.boxShadow",
                    type   : "string",
                    editAs : "input"
                });
            }
            else if("overflow" == key) {
                re.push({
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
                });
            }
            else {
                console.warn("unsupport blockField:", key, uiCom);
            }
        }
        
        // 返回字段列表
        return re;
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