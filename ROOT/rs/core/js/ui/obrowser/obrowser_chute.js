(function ($z) {
    $z.declare([
        'zui',
        'wn/util',
        'ui/obrowser/support/browser__methods'
    ], function (ZUI, Wn, BrowserMethods) {
//==============================================
        var html = `
<div class="ui-arena obrowser-chute ui-clr" ui-fitparent="yes">
    <div class="chute-menu chute-mode-toggle">
        <i class="fa"></i>
    </div>
    <div class="chute-nav" ui-gasket="sidebar"></div>
</div>`;
//==============================================
        return ZUI.def("ui.obrowser_chute", {
            dom: html,
            //..............................................
            init: function () {
                BrowserMethods(this);
            },
            //..............................................
            events: {
                "click .chute-mode-toggle": function (e) {
                    var $i = $(e.currentTarget).find('i');
                    $i.toggleClass('fa-angle-double-left').toggleClass('fa-angle-double-right');
                    this.browser().toogleChuteMode();
                }
            },
            //..............................................
            redraw: function () {
                var UI = this;
                UI.refresh(function () {
                    UI.defer_report("sidebar");
                });
                return ["sidebar"];
            },
            //..............................................
            refresh: function (callback) {
                var UI = this;
                var opt = UI.browser().options.sidebar;

                // 显示加载中
                UI.showLoading();

                // 已经有了 UI 那么就更新
                if (UI.gasket.sidebar) {
                    UI.gasket.sidebar.refresh(function () {
                        UI.hideLoading();
                        UI.gasket.sidebar.update(UI.__obj, UI.__asetup);
                        $z.doCallback(callback, [this], UI);
                    });
                }
                // 否则重新建立
                else {
                    seajs.use(opt.uiType, function (SubUI) {
                        new SubUI(_.extend({}, opt.uiConf, {
                            parent: UI,
                            gasketName: "sidebar"
                        })).render(function () {
                            UI.hideLoading();
                            this.update(UI.__obj, UI.__asetup);
                            $z.doCallback(callback, [this], UI);
                        });
                    });
                }

                // 根据cookie设定显示模式
                var cmode = Cookies.get('chute-mode') || "normal";
                if (cmode == 'normal') {
                    UI.arena.find('.chute-mode-toggle i').addClass('fa-angle-double-left')
                }
                if (cmode == 'mini') {
                    UI.arena.find('.chute-mode-toggle i').addClass('fa-angle-double-right')
                }
            },
            //..............................................
            update: function (o, asetup) {
                this.__obj = o;
                this.__asetup = asetup;
                this.gasket.sidebar.update(o, asetup);

            },
            //..............................................
            resize: function () {
                var UI = this;
            }
            //..............................................
        });
//==================================================
    });
})(window.NutzUtil);



