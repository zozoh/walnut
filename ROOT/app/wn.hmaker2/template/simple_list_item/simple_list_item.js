(function($, $z){
//..........................................................

//..........................................................
$.fn.extend({ "simple_list_item" : function(obj, mapping){
    this.text("I am simple_list_item")

    // 返回自身以便链式赋值
    return this;
}});
//..........................................................
})(window.jQuery, window.NutzUtil);