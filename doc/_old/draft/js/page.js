(function($){
//------------------------------------------------------
$(function(){

    // Setup outline
    var outline = $(".doc-outline");
    var off = outline.offset();
    $(".doc-content").find("h1,h2,h3,h4,h5,h6").each(function(){
        var a = $('<a class="doc-outline-item">').appendTo(outline);
        var s = $(this).text();
        a.text(s)
         .addClass("doi-"+this.tagName.toLowerCase())
         .attr("href", "#"+s);
        a = $("<a>").prependTo(this).attr("name",s);
    });

    // Watch scrolling
    window.onscroll = function(){
        var sy = window.scrollY;
        console.log(sy + "/" + off.top)
        if(sy > off.top)
            outline.css("top", sy - off.top);
        else
            outline.css("top", 0);
    }

    // show/hide outline
    outline.find(".doi-tt").click(function(){
        outline.toggleClass("doc-outline-hide");
    });
});
//------------------------------------------------------
})(window.jQuery);
