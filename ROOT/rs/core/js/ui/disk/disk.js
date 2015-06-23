define(function (require, exports, module) {
    var ZUI = require("zui");

    function addMenuOnBody($menu, e) {
        $(document.body).append($menu);
        $menu.find('.rclick-menu').css({
            'top': e.clientY,
            'left': e.clientX,
        });
        $(document.body).one('mousedown', function () {
            $menu.remove();
        });
    }

    function on_keydown_at_gi(e) {
        e.stopPropagation();
        var $gi = $(e.currentTarget);
        if (!$gi.hasClass('active')) {
            $gi.siblings().removeClass('active');
            $gi.addClass('active');
        }
        // 左键
        if (1 == e.which) {
            // 如果是名字的话, 就是直接打开了
        }
        // 右键
        if (3 == e.which) {
            // 显示菜单
            addMenuOnBody(this.ccode('gi-r-menu'), e);
        }
    }

    function on_keydown_at_gbody(e) {
        e.stopPropagation();
        $(e.currentTarget).find('.md-disk-grid-item').removeClass('active');
        if (3 == e.which) {
            // 显示菜单
            addMenuOnBody(this.ccode('gbody-r-menu'), e);
        }
    }

    var WOBJ = "walnut-obj";


    module.exports = ZUI.def("ui.disk", {
        dom: "ui/disk/disk.html",
        css: "ui/disk/disk.css",
        init: function (options) {
            this.listenModel("show:err", this.on_show_err);
            this.listenModel("show:txt", this.on_show_txt);
            this.listenModel("show:end", this.on_show_end);
            this.load_disk("$PWD");
        },
        redraw: function () {
        },
        resize: function () {
        },
        events: {
            "mousedown .md-disk .md-disk-container .md-disk-grid-item": on_keydown_at_gi,
            "mousedown .md-disk .md-disk-grid-body": on_keydown_at_gbody
        },
        load_disk: function (id) {
            var UI = this;
            var Mod = UI.model;
            UI.$el.find('.md-disk-grid-body').empty();
            Mod.trigger("cmd:exec", "disk " + id, function () {
            });
        },
        __print_txt: function (s) {
            var olines = s.split("\n");
            for (var i = 0; i < olines.length; i++) {
                var objstr = olines[i];
                if (_.isEmpty(objstr)) {
                    continue;
                }
                console.log("======================");
                console.log(objstr);
                var obj = $z.fromJson(olines[i]);
                var $gi = this.ccode('gi-item');

                var isDir = obj.race == "DIR";
                var tp = isDir ? "folder" : obj.tp.toLowerCase();
                $gi.find('.gi-name').append(obj.nm);
                $gi.find('.gi-name .disk-icon').addClass(tp);
                $gi.find('.disk-preview').addClass(tp);
                $gi.find('.gi-owner').append(obj.c || "unknow");
                $gi.find('.gi-lm').append($z.currentTime(new Date(obj.lm)));
                $gi.find('.gi-size').append((obj.len ? $z.sizeText(obj.len) : "-" ));
                $gi.data(WOBJ, obj);
                this.$el.find('.md-disk-grid-body').append($gi);
            }
        },
        on_show_end: function () {
            if (this._old_s) {
                this.__print_txt(this._old_s);
                this._old_s = "";
            }
        },
        on_show_txt: function (s) {
            var old = this._old_s || "";
            s = old + s;
            // 寻找最后换行，之前的输出
            var i = s.length - 1;
            for (; i >= 0; i--) {
                var b = s.charCodeAt(i);
                if (b == 0x0a) {
                    i++;
                    var str = s.substring(0, i);
                    this.__print_txt(str);
                    break;
                }
            }
            // 记录还没显示的数据
            this._old_s = s.substring(i);
        },
        on_show_err: function (s) {
            alert(s);
        }
    });
//=======================================================================
});