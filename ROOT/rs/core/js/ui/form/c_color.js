(function($z){
$z.declare([
    'zui'
], function(ZUI){
//==============================================
function _TC(v) {
    return Math.max(0, Math.min(255, Math.round(v)));
}
function _S(v, min, max) {
    return min + (max-min)*v;
}
//==============================================
// 颜色转换器对象
// x:X轴比例(0-1)
// y:Y轴比例(0-1)
// z:Z轴比例(0-1)
var CC = {
    // YUV的取值范围是:
    // Y : [0,1)
    // U : [-0.436, 0.436)
    // V : [-0.615, 0.615)
    // 需要将传入的 y,u,v 按照这个区间进行缩放
    // 对应关系:
    //  x -> Y
    //  y -> U
    //  z -> V
    "YUV" : function(x,y,z) {
        var Y = _S(x, 0, 1);
        var U = _S(y, -0.436, 0.436);
        var V = _S(z, -0.615, 0.615);
        // var R = Y + 1.13983 * (V-128);
        // var G = Y - 0.39465 * (U-128) - 0.58060 * (V-128);
        // var B = Y + 2.03211 * (U-128);
        var R = Math.floor((Y + 1.13983 * V + 1) * 128);
        var G = Math.floor((Y - 0.39465 * U - 0.58060 * V + 1) * 128);
        var B = Math.floor((Y + 2.03211 * U + 1) * 128);

        // console.log("xyz:(",x,y,z,") :: ", 
        //              "Y:",Y, "U:",U, "V:", V, 
        //              "\n   =>  RGB(",R,G,B,")");
        R = _TC(R);
        G = _TC(G);
        B = _TC(B);
        
        return {
            red   : R,
            green : G,
            blue  : B,
        };
    },
    // RGB, 按照 255 缩放
    // 对应关系:
    //  x -> R
    //  y -> G
    //  z -> B
    "RGB" : function(x,y,z) {
        var R = _TC(x * 255);
        var G = _TC(y * 255);
        var B = _TC(z * 255);
        return {
            red   : R,
            green : G,
            blue  : B,
        };
    },
    // @see RGB
    "BGR" : function(x,y,z) {
        return this.RGB(z,y,x);
    },
    // HSV, 按照 255 缩放
    // H: 色相Hue          - 在0~360°的标准色环上，按照角度值标识。比如红是0°、橙色是30°等。
    // S: 饱和度saturation - 用从0(灰色)~1(完全饱和)的百分比来度量。
    // V: 亮度brightness   - 通常是从0(黑)~100%(白)的百分比来度量的
    // 对应关系:
    //  x -> H
    //  y -> S
    //  z -> V
    "HSV" : function(x,y,z) {
        var h = x * 360;
        var s = y;
        var v = z;


        // 计算
        var r = 0;
        var g = 0;
        var b = 0;  
        var i = parseInt( parseInt(h / 60) % 6);  
        var f = (h / 60) - i;  
        var p = v * (1 - s);  
        var q = v * (1 - f * s);  
        var t = v * (1 - (1 - f) * s);  
        switch (i) {  
            case 0:  
                r = v;  
                g = t;  
                b = p;  
                break;  
            case 1:  
                r = q;  
                g = v;  
                b = p;  
                break;  
            case 2:  
                r = p;  
                g = v;  
                b = t;  
                break;  
            case 3:  
                r = p;  
                g = q;  
                b = v;  
                break;  
            case 4:  
                r = t;  
                g = p;  
                b = v;  
                break;  
            case 5:  
                r = v;  
                g = p;  
                b = q;  
                break;  
            default:  
                break;  
        }
        //console.log("hsv:",h,s,v, " :: ifpqt", i,f,p,q,t, "\n   => RGB:",r,g,b)
        return {
            red   : _TC(r * 255),
            green : _TC(g * 255),
            blue  : _TC(b * 255),
        };
    },
    // @see HSV
    "BSH" : function(x,y,z) {
        return this.HSV(z,y,x);
    }
};
//==============================================
var html = `
<div class="ui-arena com-color com-square-drop">
    <div class="cc-box"><div class="ccb-preview"></div></div>
    <div class="cc-edit">
        <div class="cce-mask"></div>
        <div class="cce-con"><div>
            <div class="cce-colors"><div>
                <div class="ccec-head"><span></span></div>
                <div class="ccec-body"></div>
                <div class="ccec-ctip"></div>
            </div></div>
            <div class="cce-values">
                <div class="cce-rgb">
                    <div key="red"><b>{{com.color.red}}</b><input placeholder="0"></div>
                    <div key="green"><b>{{com.color.green}}</b><input placeholder="0"></div>
                    <div key="blue"><b>{{com.color.blue}}</b><input placeholder="0"></div>
                </div>
                <div class="cce-alpha">
                    <div key="alpha"><b>{{com.color.alpha}}</b><input placeholder="100"></div>
                </div>
                <div class="cce-hex">
                    <em>#</em><input placeholder="000">
                </div>
            </div>
        </div></div>
    </div>
</div>
`;
//===================================================================
return ZUI.def("ui.form_com_color", {
    dom  : html,
    css  : "theme/ui/form/component.css",
    i18n : "ui/form/i18n/{{lang}}.js",
    //...............................................................
    init : function(opt) {
        var UI = this;

        // Z轴实例颜色的 X,Y 值
        $z.setUndefined(opt, "Zx", 1);
        $z.setUndefined(opt, "Zy", 1);
        // Z轴: 亮度
        $z.setUndefined(opt, "Zn", 8);
        // X轴: 色度
        $z.setUndefined(opt, "Xn", 8);
        // Y轴: 饱和度
        $z.setUndefined(opt, "Yn", 8);

        // 颜色模式
        $z.setUndefined(opt, "mode", "BSH");

        // ESC 键，将会隐藏自己
        UI.watchKey(27, function(e){
            UI.hideDrop();
        });
    },
    //...............................................................
    events : {
        // 显示颜色提取器
        'click .cc-box' : function() {
            this.showDrop();
        },
        // 隐藏颜色提取器 
        'click .cce-mask' : function() {
            this.hideDrop();
        },
        // 切换色板
        'click .ccec-head u' : function(e){
            var UI = this;
            var jU = $(e.currentTarget);
            UI.__redraw_colors(jU.attr("U") * 1);

            var alpha = (UI.__get_alpha() || 100) / 100;
            var hex = jU.attr("value");
            var color = $z.parseColor(hex, alpha);
            UI.setData(color);
            UI.__on_change();
        },
        // 显示颜色提示
        'mouseenter .cce-colors u' : function(e){
            var UI = this;
            var jU = $(e.currentTarget);
            UI.arena.find(".ccec-ctip").text(jU.attr("value"));
        },
        'mouseleave .cce-colors' : function(e){
            this.arena.find(".ccec-ctip").text("");
        },
        // 提取颜色
        'click .ccec-body u' : function(e){
            var UI = this;
            var hex = $(e.currentTarget).attr("value");
            var alpha = (UI.__get_alpha() || 100) / 100;
            var color = $z.parseColor(hex, alpha);
            UI.setData(color);
            UI.__on_change();
        },
        // 监视 RGB 输入
        'change .cce-rgb input' : function(e) {
            var UI = this;
            var jq = $(e.currentTarget);
            var val = Math.min(255, Math.max(0, jq.val() * 1) );
            // 合法值
            if(val >=0 && val<=255) {
                var color = UI.__get_color() || {
                    red : 0, green:0, blue:0
                };
                color[jq.parent().attr("key")] = val;
                $z.updateColor(color);
                UI.setData(color);
                UI.__on_change();
            }
            // 非法值
            else {
                jq.val(jq.attr("old-val"));
            }
        },
        // 监视 Hex 输入
        'change .cce-hex input' : function(e) {
            var UI = this;
            var jq = $(e.currentTarget);
            var val = jq.val();
            // 空值表示删除
            if(!val) {
                UI.setData(null);
                UI.__on_change();
            }
            // 否则判断
            else {
                // 合法值
                try{
                    var alpha = UI.__get_alpha();
                    var color = $z.parseColor(jq.val(), alpha/100);
                    UI.setData(color);
                    UI.__on_change();
                }
                // 非法值
                catch(E) {
                    jq.val(jq.attr("old-val"));
                }
            }
        },
        // 监视 Alpha 输入
        'change .cce-alpha input' : function(e) {
            var UI = this;
            var jq = $(e.currentTarget);
            var val = UI.__get_alpha();
            // 合法值
            if(val >=0 && val<=100) {
                var color = UI.__get_color();
                color.alpha = val / 100;
                $z.updateColor(color);
                UI.setData(color);
                UI.__on_change();
            }
            // 非法值
            else {
                jq.val(jq.attr("old-val"));
            }
        },
    },
    //...............................................................
    redraw : function() {
        var UI = this;

        // 以色度为主轴
        with(UI.options){
            var cW    = (100 / (Zn + 1)) + "%";
            var jHead = UI.arena.find('.ccec-head>span');
            for(var i=0; i<=Zn; i++) {
                var color = UI.__eval_color(Zx, Zy, i/Zn);
                $z.updateColor(color);
                var jU = $('<u>').appendTo(jHead).css({
                    width:cW, backgroundColor : color.HEX
                }).attr({
                    "U": i,
                    "value" : color.HEX,
                });
            }
            // 绘制颜色板
            UI.__redraw_colors(Zn);
        }
    },
    //...............................................................
    // @see CC 转换器对象
    __eval_color : function(x,y,z) {
        return CC[this.options.mode](x,y,z)
    },
    //...............................................................
    __redraw_colors : function(Z) {
        var UI = this;
        var jBody = UI.arena.find('.ccec-body').empty();

        with(UI.options){
            // Z轴比例
            var z = Z / Zn;

            // 准备生成颜色板
            var cW    = 100 / (Xn+1) + "%";
            var cH    = 100 / (Yn+1) + "%";

            // 循环啊
            for(var i=0; i<=Yn; i++){
                // 准备行
                var jRow = $('<span>').css("height", cH).appendTo(jBody);

                // Y 轴比例
                var y = i / Yn;

                // 计算饱和度级别
                for(var j=0; j<=Xn; j++) {
                    // X 周比例
                    var x = j / Xn;
                    // 修改颜色
                    var color = UI.__eval_color(x,y,z);
                    $z.updateColor(color);
                    
                    // 更新 & 加入 DOM
                    var jU = $('<u>').prependTo(jRow).css({
                        width:cW, backgroundColor : color.HEX, color:color.HEX
                    }).attr("value", color.HEX);
                }
            }
        }
    },
    //...............................................................
    depose: function(){
        var UI = this;
        
    },
    //...............................................................
    __get_color : function(){
        var UI    = this;
        var hex   = $.trim(UI.arena.find(".cce-hex input").val());
        var alpha = UI.__get_alpha();
        if(!hex)
            return null;
        return $z.parseColor(hex, alpha/100);
    },
    //...............................................................
    __get_alpha : function() {
        var s = $.trim(this.arena.find(".cce-alpha input").val());
        if("" === s) {
            return 100;
        }
        return Math.min(100, Math.max(0, parseInt(s * 1)));
    },
    //...............................................................
    __on_change : function(){
        var UI  = this;
        var opt = UI.options;
        var context = opt.context || UI.parent;
        var v = UI.getData();
        $z.invoke(opt, "on_change", [v], context);
        UI.trigger("change", v);
    },
    //...............................................................
    __update : function(val){
        var UI     = this;
        var color  = val ? $z.parseColor(val) : null;
        var jPrew = UI.arena.find(".ccb-preview");

        // 没颜色，清空
        if(!color){
            UI.arena.find("input").val("");
            jPrew.css("background-color","").attr("empty","yes");
        }
        // 设置颜色
        else {
            UI.arena.find('.cce-rgb [key=red]   input').val(color.red)
                .attr("old-val", color.red);
            UI.arena.find('.cce-rgb [key=green] input').val(color.green)
                .attr("old-val", color.green);
            UI.arena.find('.cce-rgb [key=blue]  input').val(color.blue)
                .attr("old-val", color.blue);
            var alpha = parseInt(color.alpha * 100);
            UI.arena.find('.cce-alpha input').val(alpha)
                .attr("old-val", alpha);
            var hex = color.HEX.substring(1);
            UI.arena.find('.cce-hex input').val(hex)
                .attr("old-val", hex);

            jPrew.css("background-color",color.RGBA).removeAttr("empty");
        }
    },
    //...............................................................
    getData : function(){
        var UI = this;
        return this.ui_format_data(function(opt){
            var color = UI.__get_color();
            if(color && "string" == opt.dataType){
                switch(opt.colorFormat) {
                    case "HEX":
                        return color.HEX;
                    case "RGB":
                        return color.RGB;
                    case "RGBA":
                        return color.RGBA;
                    case "AARRGGBB":
                        return color.AARRGGBB;
                    default:
                        return color.alpha < 1.0 ? color.RGBA : color.HEX;
                }
            }
            return color;
        });
    },
    //...............................................................
    setData : function(val, jso){
        //console.log(val)
        var UI = this;
        this.ui_parse_data(val, function(s){
            UI.__update(s)
        });
    },
    //...............................................................
    showDrop : function() {
        var UI = this;
        
        // 显示
        var jBox  = UI.arena.find(".cc-box");
        var jDrop = UI.arena.find(".cce-con");
        UI.arena.attr("show", "yes");
        jDrop.css({"top":"", "left":""});
        $z.dock(jBox,jDrop,"HA");

        // 下面不要让下拉框超出窗口
        var rect = $z.rect(jDrop);
        var viewport = $z.winsz();
        var rect2 = $z.rect_clip_boundary(rect, viewport);
        jDrop.css($z.rectObj(rect2, "top,left"));
    },
    //...............................................................
    // 隐藏颜色提取器 
    hideDrop : function() {
        var UI = this;
        UI.arena.removeAttr("show");
        UI.arena.find(".cce-con").css({"top":"", "left":""});
    },
    //...............................................................
    resize : function() {
        // 改变大小的时候，一定要隐藏
        this.arena.removeAttr("show");
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);