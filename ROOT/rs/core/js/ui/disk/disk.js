define(function (require, exports, module) {
    var ZUI = require("zui");
    module.exports = ZUI.def("ui.disk", {
        dom: "ui/disk/disk.html",
        css: "ui/disk/disk.css",
        init: function (options) {
            this.listenModel("show:err", this.on_show_err);
            this.listenModel("show:txt", this.on_show_txt);
            this.listenModel("show:end", this.on_show_end);
        },
        redraw: function () {
        },
        resize: function () {
        },
        events: {

        },
        disk: {

        }
    });
//=======================================================================
});