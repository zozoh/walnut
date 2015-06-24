define(function (require, exports, module) {
    var ZUI = require("zui");
    module.exports = ZUI.def("ext.project", {
        dom: "ext/project/project.html",
        css: "ext/project/project.css",
        init: function (options) {
            this.listenModel("show:err", this.on_show_err);
            this.listenModel("show:txt", this.on_show_txt);
            this.listenModel("show:end", this.on_show_end);
        },
        redraw: function () {
        },
        resize: function () {
        },
        events: {}
    });
//=======================================================================
});