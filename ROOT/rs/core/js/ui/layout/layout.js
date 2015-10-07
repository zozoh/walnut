define(function(require, exports, module) {
//=======================================================================
var ZUI = require("zui");
module.exports = ZUI.def("ui.layout", {
    dom : '/*<div class="ui-arena" ui-fitparent="true"></div>*/',
    css  : "ui/layout/layout.css",
    //...............................................................
    init : function(options){
        if(typeof options.fitparent == "undefined")
            options.fitparent = true;
        options.mode = options.mode || "vertical";
    },
    //...............................................................
    events : {
        
    },
    //...............................................................
    redraw : function(){
        var UI = this;
        // 确保自己的布局
        UI.arena.attr("layout-mode", UI.options.mode);
        // 根据配置，创建各个 UI 的子节点
        var jqs = [];
        (UI.options.display || []).forEach(function(val, index){
            var jq = $('<div class="ui-layout-wrapper">').appendTo(UI.arena);
            jq.attr("layout-nb",  index);
            jq.attr("layout-val", val);
            jq.attr("ui-gasket", "l" + index);
            jqs.push(jq);
        });
        // 为每个区域创建子控件
        var setups = (UI.options.setup || []);
        var uiTypes = [];
        setups.forEach(function(conf, index){
            if(index<jqs.length && conf.uiType){
                uiTypes.push(conf.uiType);
            }
        });
        seajs.use(uiTypes, function(){
            for(var index in arguments){
                var subUI = arguments[index];
                var conf = setups[index];
                var jq = jqs[index];
                var gnm = jq.attr("ui-gasket");
                var G = {
                    ui : [new subUI(_.extend({}, conf.uiConf, {
                        $pel : jq
                    }))],
                    jq : jq,
                    multi : false
                }
                UI.gasket[gnm] = G;
                (function(index, uiType){
                    G.ui[0].render(function(){
                        UI.defer_report(index, uiType);
                    });
                })(index, uiTypes[index]);
            };
        });
        // 需要延迟
        return uiTypes;
        /*(UI.options.setup || []).forEach(function(conf, index){
            if(index<jqs.length){
                if(!conf.uiType)
                    return;
                var jq = jqs[index];
                var gnm = jq.attr("ui-gasket");
                seajs.use(conf.uiType, function(subUI){
                    var G = {
                        ui : [new subUI(_.extend({}, conf.uiConf, {
                            $pel : jq
                        }))],
                        jq : jq,
                        multi : false
                    }
                    UI.gasket[gnm] = G;
                    G.ui[0].render();
                });
            }
        });*/
    },
    //...............................................................
    resize : function(){
        //console.log("I am layout resize", this.cid, this.arena.height());
        var UI = this;
        var _c = {avgs : [], jqs:[]}
        var jWrappers = UI.arena.children(".ui-layout-wrapper");
        // 垂直布局
        if(UI.options.mode == "vertical") {
            _c.sum = UI.arena.height();
            _c.cssName = "height";
            jWrappers.css("width", UI.arena.width());
        }
        // 水平布局
        else{
            _c.sum = UI.arena.width();
            _c.cssName = "width";
            jWrappers.css("height", UI.arena.height());
        }
        // 开始计算
        _c.remain = _c.sum;
        jWrappers.each(function(index){
            var jq = $(this);
            _c.jqs.push(jq);
            var v = jq.attr("layout-val");
            // *
            if("*" == v){
                _c.avgs.push(jq);
                return;
            }
            // 20%
            var m = /^([0-9]{1,2})%$/g.exec(v);
            var w;
            if(m)
                w = parseInt(m[1] * 1 * _c.sum / 100);
            // 341
            else
                w = v * 1;
            jq.__new_val = w;
            _c.remain -= w;
        });
        // 平均分配剩下的点数
        if(_c.avgs.length > 0 && _c.remain > 0){
            var n = parseInt(_c.remain / _c.avgs.length);
            _c.avgs.forEach(function(jq){
                jq.__new_val = n;
                _c.remain -= n;
            });
            if(_c.remain > 0){
               for(var i=0;i<_c.remain;i++) {
                    _c.avgs[i].__new_val += 1;
               }
            }
        }
        // 最后执行修改
        _c.jqs.forEach(function(jq){
            jq.css(_c.cssName, jq.__new_val);
        });
        
    }
    //...............................................................
});
//===================================================================
});
