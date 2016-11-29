(function ($z) {
    $z.declare([
        'zui'
    ], function (ZUI) {
//==============================================
        var html = function () {/*
         <div class="ui-arena com-button"><div class="ck-btn"></div>
         */
        };
//===================================================================
        return ZUI.def("ui.form_com_button", {
            //...............................................................
            dom: $z.getFuncBodyAsStr(html.toString()),
            //...............................................................
            events: {
                "click .ck-btn": function () {
                    this.options.on_click();
                }
            },
            //...............................................................
            redraw: function () {
                var UI = this;
                var opt = UI.options;
                UI.arena.find('.ck-btn').text(opt.buttonText);
            },
            //...............................................................
            resize: function () {
                var UI = this;
            },
            getData: function () {
                return null;
            },
            //...............................................................
            setData: function (val, jso) {
                var UI = this;
            }
            //...............................................................
        });
//===================================================================
    });
})(window.NutzUtil);