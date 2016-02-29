(function($z){
$z.declare(['zui', 'jquery-plugin/zcal/zcal'], function(ZUI){
//==============================================
function wrap_func(UI, options, funcName, evenName){
    var func = options[funcName];
    
    options[funcName] = function(){
        var args = Array.prototype.slice.call(arguments);
        if(_.isFunction(func)){
           func.apply(UI, args)
        }
        UI.trigger.apply(UI, [evenName].concat(args));
    }
}    
//==============================================
var html = function(){/*
<div class="ui-arena" ui-fitparent="yes"></div>
*/};
//==============================================
return ZUI.def("ui.calendar", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "theme/jqp/zcal/zcal.css",
    init : function(options){
        var UI = this;
        wrap_func(UI, options, "on_actived"      ,"cal:actived");
        wrap_func(UI, options, "on_blur"         ,"cal:blur");
        wrap_func(UI, options, "on_switch"       ,"cal:switch");
        wrap_func(UI, options, "on_cell_resize"  ,"cal:cell:resize");
        wrap_func(UI, options, "on_cell_click"   ,"cal:cell:click");
        wrap_func(UI, options, "on_range_change" ,"cal:range:change");
    },
    //..............................................
    redraw : function(){
        var UI = this;
        UI.options.i18n = UI.msg("dt");
        UI.arena.zcal(UI.options);
    },
    //..............................................
    refresh : function(){
        var UI = this;
        var d = UI.arena.zcal("current");
        UI.arena.zcal("current", d);
    },
    //..............................................
    resize : function(){
        this.arena.zcal("resize");
    },
    //..............................................
    getCurrent : function(){
        return this.arena.zcal("current");
    },
    setCurrent : function(d){
        this.arena.zcal("current", d);
        return this;
    },
    viewport : function(){
        return this.arena.zcal("viewport");
    },
    getActived : function(){
        return this.arena.zcal("actived");
    },
    setActived : function(d){
        this.arena.zcal("active", d);
        return this;
    },
    getRange : function(mode){
        return this.arena.zcal("range", mode);
    },
    setRange : function(rg){
        this.arena.zcal("range", rg);
        return this;
    }
    //..............................................
});
//==================================================
});
})(window.NutzUtil);