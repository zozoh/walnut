(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/support/hm__methods_panel',
    'ui/form/form',
    'ui/form/c_switch',
    'ui/pop/pop'
], function(ZUI, Wn, HmMethods, FormUI, SwitchUI, POP){
//==============================================
var html = `
<div class="ui-arena hm-prop-block">
    <div class="hmpb-pos">
        <div class="hmpb-dis-mode">
            <ul>
                <li head="yes">
                    <span>{{hmaker.pos.dismode}}</span>
                </li>
                <li v="desktop" balloon="right:hmaker.pos.d_desktop_tip">
                    <span><i class="zmdi zmdi-laptop"></i></span>
                </li>
                <li v="mobile" balloon="right:hmaker.pos.d_mobile_tip">
                    <span><i class="zmdi zmdi-smartphone-android"></i></span>
                </li>
            </ul>
        </div>
        <div class="hmpb-pos-box-con"><div class="hmpb-pos-box">
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
        </div></div>
        <div class="hmpb-pos-mode">
            <ul>
                <li head="yes">
                    <span>{{hmaker.pos.posmode}}</span>
                </li>
                <li v="inflow" balloon="left:hmaker.pos.inflow_tip">
                    <span>{{hmaker.pos.inflow}}</span>
                </li>
                <li v="abs" balloon="left:hmaker.pos.abs_tip">
                    <span>{{hmaker.pos.abs}}</span>
                </li>
                <li v="fix" balloon="left:hmaker.pos.fix_tip">
                    <span>{{hmaker.pos.fix}}</span>
                </li>
            </ul>
        </div>
    </div>
    <div class="hmpb-skin">
        <em>{{hmaker.prop.block_skin}}</em>
        <a m="reset">{{hmaker.prop.block_reset}}</a>
        <a m="quickedit">{{hmaker.prop.quickedit}}</a>
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
        // 切换手机桌面显示
        "click .hmpb-dis-mode li[v]" : function(e) {
            var jLi = $(e.currentTarget);
            $z.toggleAttr(jLi, "on", "yes");
            var jDM = this.arena.find(".hmpb-dis-mode");
            var val = [];
            jDM.find('li[v][on]').each(function(){
                val.push($(this).attr("v"));
            });
            var dm = val.length!=1 ? "show" : val[0];
            this.uiCom.setComDisplayMode(dm);

            // 通知一下回调
            this.uiCom.notifyBlockChange();
        },
        // 切换绝对/相对定位选择框
        "click .hmpb-pos-mode li[v]" : function(e) {
            var block = this.uiCom.getBlock();
            var jLi = $(e.currentTarget);

            // 确保模式正确
            block.mode = jLi.attr("v");
            if(!/^(inflow|abs|fix)$/.test(block.mode))
                block.mode = "inflow";
            
            // 如果切换到了绝对定位，需要默认设置其宽高
            this.uiCom.checkBlockMode(block);

            // 格式化块信息
            this.uiCom.formatBlockDimension(block);

            // 保存数据 
            this.uiCom.saveBlock(null, block);
        },
        // 点击顶点
        'click .hmpb-pos[mode=abs] .hmpb-pos-v, .hmpb-pos[mode=fix] .hmpb-pos-v' : function(e) {
            var UI = this;
            var jq = $(e.currentTarget);
            if(jq.attr("highlight")){
                return;
            }

            // 得到块信息
            var block = this.uiCom.getBlock();
            //console.log("block", block);

            // 转换
            var posBy = jq.attr("val");
            var rect  = this.uiCom.getMyRectCss();
            var css = this.uiCom.pickCssForMode(rect, posBy);

            // 格式化
            UI.uiCom.formatBlockDimension(css, block.measureBy);

            // 保存数据 
            block.posBy  = posBy;
            _.extend(block, {
                top:"",left:"",right:"",bottom:"",width:"",height:""
            }, css);
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

            // 监视编辑
            $z.editIt(jq, function(newval, oldval, jEle){
                var val = $.trim(newval).toLowerCase() || "unset";
                // 嗯，值有变动
                if(val && val!=oldval) {                   
                    // 得到单位
                    var m = /^[\d.]+(px|rem|%)$/.exec(val);
                    var u = m ? m[1] : (block.measureBy || "px");
                    
                    // 看看是否修改了单位，如果改了单位，
                    // 那么需要整个面板都更新
                    var mode = "panel";
                    if(u != block.measureBy) {
                        block.measureBy = u;
                        mode = null;
                    }

                    // 设置到 block
                    if($D.dom.isUnset(val)){
                        block[key] = val;
                    } else {
                        block[key] = val + (m ? "" : u);
                    }
                    UI.uiCom.formatBlockDimension(block);

                    // 保存数据 
                    UI.uiCom.setBlock(block);
                    
                    // 通知
                    //console.log("mode", mode)
                    UI.uiCom.notifyBlockChange(mode, block);

                        
                    // 显示修正后的值
                    jEle.text(block[key]);
                }
            });

        },
        // 重置外观
        'click .hmpb-skin a[m="reset"]' : function(e) {
            // 得到外观数据，清除除了尺寸以外其他的设置
            var block = this.uiCom.getBlock();
            var b2 = $z.pick(block, /^(mode|posBy|width|height|top|left|right|bottom)$/);

            // 保存数据并通知
            this.uiCom.setBlock(null, b2);
            this.uiCom.notifyBlockChange(null, b2);
        },
        // 快速编辑
        'click .hmpb-skin a[m="quickedit"]' : function(e) {
            var UI = this;
            var block = this.uiCom.getBlock();

            // 打开编辑界面
            POP.openEditTextPanel({
                title    : "i18n:hmaker.prop.quickedit",
                data     : $z.toJson(block, function(k, v){
                                if(!_.isNull(v))
                                    return v;
                           }, '   '),
                width    : "80%",
                height   : "90%",
                callback : function(str){
                    //console.log(str)
                    try{
                        var block = $z.fromJson(str);
                        UI.uiCom.saveBlock(null, block, {});
                    }
                    // 格式错误
                    catch(E) {
                        alert("非法的JSON格式");
                    }
                }
            }, UI);
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
            var val = block[key];
            
            jD.find("em").text(val);
        });
    },
    //...............................................................
    update : function(uiCom, block) {
        var UI = this;
        UI.uiCom = uiCom;

        // console.log("edit_block_update", block);

        // 更新位置信息
        UI.__update_pos(block);

        // 更新表单
        UI.__update_form(block, function(){
            // TODO 这个会引起问题，因为 form callback 的问题
            // 加载 form 时，子控件没加载完就触发 callback 了
            // 所有导致 form.getData 返回的都是空值
            // 所以，暂时先去掉这个逻辑，看看以后怎么办吧 -_-!
            //...............................................
            // 如果表单内容有变，则触发控件重绘
            // var b2 = this.getData();
            // if(!_.isEqual(b2, block)) {
            //     console.log("b2", b2)
            //     console.log("block", block)
            //     UI.uiCom.saveBlock("panel", b2, block);
            // }
        });
    },
    //...............................................................
    __update_pos : function(block) {
        var UI   = this;
        var jPos = UI.arena.find(".hmpb-pos");

        // 标记显示模式
        var dm = UI.uiCom.getComDisplayMode();
        var jDMLi = jPos.find('.hmpb-dis-mode li[v]');
        if(/^(desktop|mobile)$/.test(dm)) {
            jDMLi.removeAttr("on").filter('[v="'+dm+'"]').attr("on","yes");
        }
        // 全部标绿
        else {
            jDMLi.attr("on","yes");
        }

        // 标记定位
        var mode = block.mode || "inflow";
        jPos.attr("mode", mode);

        // 定位标识
        jPos.find('.hmpb-pos-mode li').removeAttr("current")
                .filter('[v="'+mode+'"]')
                    .attr("current", "yes");

        // 绝对定位
        if(/^(abs|fix)$/.test(block.mode)) {
            // 打开提示
            UI.balloon(".hmpb-pos-box");

            // 更新显示
            UI.changePosBy(block, block.posBy || "TLWH");
        }
        // 相对定位
        else {
            // 关闭提示
            UI.balloon(".hmpb-pos-box", false);

            // 更新显示
            UI.changePosBy(block, "WH");
        }
    },
    //...............................................................
    __update_form : function(block, callback) {
        var UI = this;
        var jForm = UI.arena.find(".hmpb-form");
                
        // 得到块的默认属性列表
        var blockFields = UI.uiCom.getBlockPropFields(block);

        // 如果当前的 UI 使用了皮肤，看看皮肤里有木有声明特殊的属性列表
        var skinName = UI.uiCom.getComSkin();
        if(skinName) {
            var ctype    = UI.uiCom.getComType();
            var skinItem = UI.getSkinItemForCom(ctype, skinName);
            if(skinItem)
                blockFields  = skinItem.blockFields || blockFields;
        }

        // 如果没有可用编辑字段
        if(blockFields.length == 0) {
            UI.__current_block_fields = "";
            // 注销 form 控件
            if(UI.gasket.form) {
                UI.gasket.form.destroy();
            }
            // 显示空消息
            jForm.attr("no-setting","yes").html(UI.msg('hmaker.com._.no_setting'));

            // 不需要进行后续逻辑了
            return;
        }
        
        // 看看是否需要重绘字段
        var bf_finger = blockFields.join(",");

        // 无需重绘 form, 直接设置数据
        if(UI.gasket.form && UI.__current_block_fields == bf_finger) {
            UI.gasket.form.setData(block);
            $z.doCallback(callback, [], UI.gasket.form);
        }
        // 创建编辑表单
        else {
            // 确保移除消息
            if(!UI.gasket.form)
                jForm.removeAttr("no-setting").empty();
            
            // 创建其他属性
            new FormUI({
                parent : UI,
                gasketName : "form",
                mergeData : false,
                uiWidth: "all",
                on_change : function(key, val) {
                    var block = this.getData();
                    //console.log("block", block)
                    UI.uiCom.saveBlock("panel", block);
                    UI.pageUI().invokeSkin("ready");
                    UI.pageUI().invokeSkin("resize");
                },
                fields : UI.__gen_block_fields(blockFields)
            }).render(function(){
                // 设置数据
                UI.gasket.form.setData(block);
                // 收起全部组
                UI.gasket.form.collapseGroup();
                // 记录最后的修改
                UI.__current_block_fields = bf_finger;
                // 调用回调
                $z.doCallback(callback, [], this);
            });
        }
    },
    //...............................................................
    redraw : function() {
        var UI = this;

        // 测试用代码
        UI.arena.find(".hmpb-pos-d em").text("unset");

        // 启用提示
        UI.balloon();

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
        var form = {fields: []};
        var grp  = form;
        for(var i=0; i<blockFields.length; i++) {
            var key = blockFields[i];
            if(!key)
                continue;

            // 如果是普通对象（即不是字符串，那么就直接用）
            if(!_.isString(key)) {
                grp.fields.push(_.extend({}, key));
                continue;
            }

            // 如果是标题，则重开一组
            var m = /^[-]{3,}(.+)$/.exec(key);
            if(m) {
                if(grp != form && grp.fields.length > 0) {
                    form.fields.push(grp);
                }
                grp = {title:$.trim(m[1]), fields:[]};
                continue;
            }


            // 看看是不是直接就是 CSS 属性
            var fld = UI.getCssFieldConf(key);
            if(fld) {
                grp.fields.push(fld);
                continue;
            }

            
            // 自定义颜色选择模式
            m = /^(#([BCL])> *[^{}]+)(\{([^}]+)\})(=(.*))?/.exec(key);
            if(m) {
                var a_key = m[1];
                var a_tp  = m[2];
                var a_txt = m[4] || m[1];
                var a_dft = m[6] || null;
                // 背景
                if("B" == a_tp) {
                    grp.fields.push(UI.getCssFieldConf("background",a_dft,a_txt,null,a_key));
                }
                // 前景
                else  if("C" == a_tp) {
                    grp.fields.push(UI.getCssFieldConf("color",a_dft,a_txt,null,a_key));
                }
                // 边框颜色
                else if("L" == a_tp) {
                    grp.fields.push(UI.getCssFieldConf("borderColor",a_dft,a_txt,null,a_key));
                }
                // 错误
                else {
                    console.warn("unsupport blockField:", key, UI.uiCom);
                }
                continue;
            }

            // 尺度设置模式
            // +[name]: [selector] ([title]/[tip])
            // +margin: > ul > li  (内边距/按钮的内边距)
            m = /^(\+([A-Za-z_-]+):([^{}]+))\{([^\/\)]+)(\/([^\)]+))?\}$/.exec(key);
            if(m) {
                var a_key = m[1];
                var a_tp  = m[2];
                var a_sel = m[3];
                var a_txt = m[4];
                var a_tip = m[6];

                //console.log(m)
                fld = UI.getCssFieldConf(a_tp, null, a_txt, a_tip, a_key);
                if(fld) {
                    grp.fields.push(fld);
                }
                // 错误
                else {
                    console.warn("unsupport blockField:", key, UI.uiCom);
                }

                continue;
            }
            
            // 布尔模式
            m = /^@([0-9a-zA-Z_-]+)(\(([^\)]+)\))?(\{yes(\/no)?\})(=(yes|no))?/
                    .exec(key);
            if(m) {
                //console.log(m)
                var a_key = m[1];
                var a_txt = m[3] || a_key;
                var a_dft = m[7] || "no";
                // 属性指明了 yes/no
                if(m[5] == "/no"){
                    grp.fields.push({
                        key    : "sa-" + a_key,
                        title  : a_txt,
                        type   : "string",
                        dft    : a_dft,
                        editAs : "switch",
                        uiConf : {
                            singleKeepOne : false,
                            items : [{
                                value : "no",
                                text  : "i18n:no"
                            },{
                                value : "yes",
                                text  : "i18n:yes"
                            }]
                        }
                    });
                }
                // 仅仅是属性开关
                else {
                    grp.fields.push({
                        key    : "sa-" + a_key,
                        title  : a_txt,
                        type   : "boolean",
                        dft    : a_dft == "yes",
                        editAs : "toggle", 
                    });
                }
                continue;
            }

            // 列表模式
            m = /^([@-])([0-9a-zA-Z_-]+)(\(([^\)]+)\))?(\[([^\]]+)\])(=(.+))?/.exec(key);
            if(m) {
                // console.log(m)
                var a_etp = m[1];
                var a_key = m[2];
                var a_txt = m[4] || a_key;
                var a_val = m[6];
                var a_dft = m[8];

                // 解析值列表
                var items = UI.parseStringItems(a_val);

                // 默认值
                a_dft = a_dft || undefined;

                // 加入字段
                grp.fields.push({
                    key    : "sa-" + a_key,
                    title  : a_txt,
                    type   : "string",
                    dft    : a_dft,
                    editAs : a_etp == "-" ? "droplist" : "switch",
                    uiConf : {
                        items:items,
                    } 
                });

                continue;
            }

            // 还是搞不定，那么打印一个警告无视它
            console.warn("unsupport blockField:", key, UI.uiCom);
        }

        // 处理最后一组
        if(grp != form && grp.fields.length > 0) {
            form.fields.push(grp);
        }
        
        // 返回字段列表
        return form.fields;
    },
    //...............................................................
    resize : function() {
        // var UI = this;
        // var jPos     = UI.arena.find(".hmpb-pos");
        // var jSkinBox = UI.arena.find(".hm-skin-box");
        // var jForm    = UI.arena.find(".hmpb-form");

        // jForm.css("top", jPos.outerHeight(true) + jSkinBox.outerHeight(true));
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);