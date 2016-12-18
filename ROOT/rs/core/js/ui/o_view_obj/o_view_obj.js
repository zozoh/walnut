(function ($z) {
    $z.declare([
        'zui',
        'wn/util',
        'ui/o_view_obj/o_view_preview',
        'ui/o_view_obj/o_view_meta'
    ], function (ZUI, Wn, ObjPreviewUI, ObjMetaUI) {
//==============================================
        var html = `
<div class="ui-arena oview" ui-fitparent="yes">
    <div class="oview-main" ui-gasket="preview"></div>
    <div class="oview-meta" ui-gasket="meta"></div>
    <div class="oview-showmeta"
        data-balloon="{{oview.showmeta}}"
        data-balloon-pos="left">
        <i class="fa fa-info-circle"></i>
    </div>
</div>
`;
//==============================================
        return ZUI.def("ui.o_view_obj", {
            dom: html,
            css: "theme/ui/o_view_obj/o_view_obj.css",
            i18n: "ui/o_view_obj/i18n/{{lang}}.js",
            //...............................................................
            init: function (opt) {
                $z.setUndefined(opt, "showMeta", true);
            },
            //...............................................................
            events: {
                "dblclick .oview-main [mode=pic] img": function (e) {
                    e.stopPropagation();
                    var jImg = $(e.currentTarget);
                    jImg.toggleClass("autofit");
                },
                "dragstart .oview-main [mode=pic] img": function (e) {
                    e.preventDefault();
                },
                "click .form-title": function () {
                    var UI = this;
                    UI.arena.attr("show-meta", "no");
                },
                "click .oview-showmeta": function () {
                    var UI = this;
                    UI.arena.attr("show-meta", "yes");
                },
                "click .video-ctrl": function () {
                    var UI = this;
                    var jCon = UI.arena.find(".video-con");
                    console.log(jCon)
                    // 正在播放 -> 暂停
                    if ("play" == jCon.attr("video-status")) {
                        jCon.attr("video-status", "pause")
                            .find("video")[0].pause();
                    }
                    // 正在暂停 -> 播放
                    else {
                        jCon.attr("video-status", "play")
                            .find("video")[0].play();
                    }
                },
            },
            //...............................................................
            redraw: function () {
                var UI = this;
                var opt = UI.options;

                var sub_uis = ["preview"];

                // 根据配置显示/隐藏信息区域
                UI.arena.attr({
                    "show-meta": opt.showMeta ? "yes" : "no"
                }).find(".oview-meta, .oview-showmeta")
                    .css({
                        "display": (opt.showMeta ? "" : "none")
                    });

                // 显示预览
                new ObjPreviewUI({
                    parent: UI,
                    gasketName: "preview"
                }).render(function () {
                    UI.defer_report("preview");
                });

                // 如果显示信息，则创建元数据视图
                if (opt.showMeta) {
                    new ObjMetaUI({
                        parent: UI,
                        gasketName: "meta"
                    }).render(function () {
                        UI.defer_report("meta");
                    });
                    sub_uis.push("meta");
                }

                // 延迟加载
                return sub_uis;
            },
            //...............................................................
            getCurrentEditObj: function () {
                var oid = this.$el.attr("oid");
                var o = Wn.getById(oid);
                return o;
            },
            //...............................................................
            update: function (o) {
                var UI = this;
                var opt = UI.options;

                // 记录
                Wn.saveToCache(o);
                UI.$el.attr("oid", o.id);

                // 更新预览
                UI.refresh();
            },
            //...............................................................
            refresh: function () {
                var UI = this;
                var opt = UI.options;

                // 得到对象
                var oid = UI.$el.attr("oid");
                var o = Wn.getById(oid);

                // 更新预览
                UI.gasket.preview.update(o);

                // 更新元数据视图
                if (opt.showMeta) {
                    UI.gasket.meta.update(o);
                }
            },
            //...............................................................
        });
//===================================================================
    });
})(window.NutzUtil);