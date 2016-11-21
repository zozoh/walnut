(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/support/hm__methods_panel',
    'ui/form/form',
    'ui/form/c_switch',
], function(ZUI, Wn, HmMethods, FormUI, SwitchUI){
//==============================================
var html = `
<div class="ui-arena hm-prop-block">
    <div class="hmpb-pos">
        <div class="hmpb-pos-box">
            <div class="hmpb-pos-d" key="width"><span><b>{{hmaker.pos.width}}</b><em></em></span></div>
            <div class="hmpb-pos-d" key="height"><span><b>{{hmaker.pos.height}}</b><em></em></span></div>
            <div class="hmpb-pos-d" key="left"><span><b>{{hmaker.pos.left}}</b><em></em></span></div>
            <div class="hmpb-pos-d" key="right"><span><b>{{hmaker.pos.right}}</b><em></em></span></div>
            <div class="hmpb-pos-d" key="top"><span><b>{{hmaker.pos.top}}</b><em></em></span></div>
            <div class="hmpb-pos-d" key="bottom"><span><b>{{hmaker.pos.bottom}}</b><em></em></span></div>
            <div class="hmpb-pos-v" md="TL" balloon="up:hmaker.pos.TL"   val="TLWH"></div>
            <div class="hmpb-pos-v" md="TR" balloon="up:hmaker.pos.TR"   val="TRWH"></div>
            <div class="hmpb-pos-v" md="LB" balloon="down:hmaker.pos.LB" val="LBWH"></div>
            <div class="hmpb-pos-v" md="BR" balloon="down:hmaker.pos.BR" val="BRWH"></div>
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
            var block = this.uiCom.getBlock();

            block.mode = this.arena.find(".hmpb-pos").attr("mode") == "abs"
                            ? "inflow"
                            : "abs";
            
            // 如果切换到了绝对定位，需要默认设置其宽高
            this.uiCom.checkBlockMode(block);

            // 保存数据 
            this.uiCom.setBlock(block);
            
            // 通知
            this.uiCom.notifyBlockChange(null, block);
        },
        // 点击顶点
        "click .hmpb-pos[mode=abs] .hmpb-pos-v" : function(e) {
            var jq = $(e.currentTarget);
            if(jq.attr("highlight")){
                return;
            }

            var block = this.uiCom.getBlock();

            var posBy  = jq.attr("val");
            var rect   = this.uiCom.getMyRectCss();
            var css    = this.uiCom.pickCssForMode(rect, posBy);
            block.posBy  = posBy;
            _.extend(block, css);

            // 保存数据 
            this.uiCom.setBlock(block);
            
            // 通知
            this.uiCom.notifyBlockChange(null, block);
        },
        // 编辑位置/宽高
        "click .hmpb-pos-d em" : function(e) {
            var UI = this;
            var jq = $(e.currentTarget);
            var key = jq.parents(".hmpb-pos-d").attr("key");
            var block = UI.uiCom.getBlock();

            // 得到模式
            var md = UI.arena.find(".hmpb-pos").attr("mode");

            // 声明后续处理方式
            var __after_input_ok = function(val) {
                
                // 修改值
                block[key] = val;
                
                // 保存数据 
                UI.uiCom.setBlock(block);
                
                // 通知
                UI.uiCom.notifyBlockChange("panel", block);
            }

            // 监视编辑
            $z.editIt(jq, function(newval, oldval, jEle){
                var val = $.trim(newval) || "auto";
                if(val && val!=oldval) {
                    var v2 = $z.toCssDimension(val);
                    if(v2) {
                        // 后续处理
                        __after_input_ok(v2);
                        
                        // 显示修正后的值
                        jEle.text(v2);
                    }
                }
            });

        }
    },
    //...............................................................
    changePosBy : function(block, posBy) {
        var jPos = this.arena.find(".hmpb-pos-box").attr("pos-by", posBy);
        
        // 取消所有的高亮节点
        jPos.find('.hmpb-pos-v[highlight]').removeAttr("highlight");
        
        // 寻找高亮的顶点 
        // 因为 posBy 的格式一定是 [T][L][B][R][W][H]
        // 每个顶点的 md 也是这个顺序，那么简单判断一下字符串包含
        // 就能来决定当前模式，这个顶点是否要高亮了
        jPos.find(".hmpb-pos-v").each(function(){
            var jV = $(this);
            var md = jV.attr("md");
            if(posBy.indexOf(md) >= 0)
                jV.attr("highlight", "yes");
        });

        // 改变位置的值
        this.arena.find(".hmpb-pos-d").each(function(){
            var jD  = $(this);
            var key = jD.attr("key");
            var val = $z.toCssDimension(block[key], "NaN");
            
            jD.find("em").text(val);
        });
    },
    //...............................................................
    update : function(uiCom, block) {
        this.uiCom = uiCom;
        this.__update_pos(block);
        this.__update_form(block);
    },
    //...............................................................
    __update_pos : function(block) {
        var UI   = this;
        var jPos = UI.arena.find(".hmpb-pos");

        // 绝对定位
        if("abs" == block.mode) {
            // 标志
            jPos.attr("mode", "abs");
            
            // 打开提示
            UI.balloon();

            // 更新显示
            UI.changePosBy(block, block.posBy || "TLWH");
        }
        // 相对定位
        else {
            // 标志
            jPos.attr("mode", "inflow");
            
            // 关闭提示
            UI.balloon(false);

            // 更新显示
            UI.changePosBy(block, "WH");
        }
    },
    //...............................................................
    __update_form : function(block) {
        var UI = this;
                
        // 得到块属性列表
        var blockFields = UI.uiCom.getBlockPropFields(block);
        
        // 看看是否需要重绘字段
        var bf_finger = blockFields.join(",");
        if(UI.__current_block_fields != bf_finger) {
            // 创建其他属性
            new FormUI({
                parent : UI,
                gasketName : "form",
                uiWidth: "all",
                on_change : function(key, val) {
                    UI.uiCom.saveBlock("panel", $z.obj(key, val));
                },
                fields : UI.__gen_block_fields(blockFields)
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
        UI.arena.find(".hmpb-pos-d em").text("NaN");

        // // margin 
        // new SwitchUI({
        //     parent : UI,
        //     gasketName : "margin",
        //     on_change : function(val){
        //         UI.fire("change:block", {margin:val});
        //     },
        //     items : [{
        //         icon : '<i class="fa fa-align-left">',
        //         val  : '',
        //     }, {
        //         icon : '<i class="fa fa-align-center">',
        //         val  : '0 auto',
        //     }, {
        //         icon : '<i class="fa fa-align-right">',
        //         val  : '0 0 0 auto',
        //     }]
        // }).render(function(){
        //     UI.defer_report("margin");
        // });
        // 
        // return  ["margin"];
    },
    //...............................................................
    __gen_block_fields : function(blockFields) {
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
                re.push({
                    key    : "margin",
                    title  : "i18n:hmaker.prop.margin",
                    type   : "string",
                    editAs : "input"
                });
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
                console.warn("unsupport blockField:", key, UI.uiCom);
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