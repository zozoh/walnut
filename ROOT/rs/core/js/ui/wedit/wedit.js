(function ($z) {
    $z.declare(['zui', 'wn/util'], function (ZUI, Wn) {
        return ZUI.def("ui.wedit", {
            dom  : "ui/wedit/wedit.html",
            css  : "ui/wedit/theme/wedit-{{theme}}.css",
            i18n : "ui/wedit/i18n/{{lang}}.js",
            events: {
                "click .ui-wedit-save": function () {
                    var UI = this;
                    var content = UI.arena.find('.ui-wedit-textarea').val();
                    Wn.writeObj(UI, null, content, UI.on_save_start, UI.on_save_done, UI.on_save_fail);
                }
            },
            redraw: function () {
                var UI = this;
                Wn.readObj(UI, null, UI.on_redraw_content);
            },
            //...............................................................
            on_redraw_content: function (content) {
                //console.log("on_redraw_content: " + content)
                this.arena.find('.ui-wedit-textarea').val(content);
                this.on_change_obj(this.app.obj);
            },
            on_change_obj: function (obj) {
                this.arena.find('.ui-wedit-title .ui-tt').text(obj.nm);
                this.arena.find('.ui-wedit-footer').text(obj.ph);
            },
            on_save_start: function () {
                var jq = this.arena.find(".ui-wedit-save .fa");
                jq.removeClass("fa-save fa-warning").addClass("fa-spinner fa-spin");
            },
            on_save_done: function () {
                this.on_change_obj(this.app.obj);
                var jq = this.arena.find(".ui-wedit-save .fa");
                jq.removeClass("fa-spinner fa-spin").addClass("fa-check");
                window.setTimeout(function () {
                    jq.removeClass("fa-check").addClass("fa-save");
                }, 1000);
            },
            on_save_fail: function () {
                var jq = this.arena.find(".ui-wedit-save .fa");
                jq.removeClass("fa-spinner fa-spin").addClass("fa-warning");
            }
        });
    });
})(window.NutzUtil);