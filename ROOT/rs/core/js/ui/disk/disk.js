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

    function on_click_sort(e) {
        var $st = $(e.currentTarget);
        var sort = $st.attr('sort');
        var asc = !$st.hasClass('asc');
        if (sort == undefined) {
            return;
        }
        this.model.set(SORT, sort);
        this.model.set(ASC, asc);
        this.render_disk();
    }

    var WOBJ = "walnut-obj";
    var WOList = "walnut-obj-list";
    var SORT = "SORT";
    var ASC = "ASC";

    module.exports = ZUI.def("ui.disk", {
        dom: "ui/disk/disk.html",
        css: "ui/disk/disk.css",
        init: function (options) {
            this.listenModel("show:err", this.on_show_err);
            this.listenModel("show:txt", this.on_show_txt);
            this.listenModel("show:end", this.on_show_end);
            this.load_disk("$PWD");
            this.model.set(WOList, []);
            this.model.set(SORT, "name");
            this.model.set(ASC, true);
        },
        redraw: function () {
        },
        resize: function () {
        },
        events: {
            "mousedown .md-disk .md-disk-container .md-disk-grid-item": on_keydown_at_gi,
            "mousedown .md-disk .md-disk-grid-body": on_keydown_at_gbody,
            "click .md-disk .md-disk-container .md-disk-grid-title .md-disk-gt-cell": on_click_sort,
        },
        clear_disk: function () {
            this.$el.find('.md-disk-grid-body').empty();
        },
        load_disk: function (id) {
            var UI = this;
            var Mod = UI.model;
            Mod.set(WOList, []);
            Mod.trigger("cmd:exec", "disk " + id, function () {
            });
        },
        sort_wolist: function () {
            var nm = this.model.get(SORT);
            var asc = this.model.get(ASC);
            $z.log("sortBy " + nm + ", " + (asc ? "asc" : "desc"));
            var wolist = this.model.get(WOList);
            // 排序
            wolist.sort(function (a, b) {
                var aval = a[nm] || 0;
                var bval = b[nm] || 0;
                return aval > bval ? 1 : -1
            });
            //
            if (!asc) {
                wolist.reverse();
            }
            this.model.set(WOList, wolist);
        },
        render_disk: function () {
            this.clear_disk();
            this.sort_wolist()
            var wolist = this.model.get(WOList);
            for (var i = 0; i < wolist.length; i++) {
                var obj = wolist[i];
                var $gi = this.ccode('gi-item');
                var isDir = obj.race == "DIR";
                var tp = isDir ? "folder" : obj.tp.toLowerCase();
                $gi.find('.gi-nm').append(obj.nm);
                $gi.find('.gi-nm .disk-icon').addClass(tp);
                $gi.find('.disk-preview').addClass(tp);
                $gi.find('.gi-owner').append(obj.c || "unknow");
                $gi.find('.gi-lm').append($z.currentTime(new Date(obj.lm)));
                $gi.find('.gi-len').append((obj.len ? $z.sizeText(obj.len) : "-" ));
                $gi.data(WOBJ, obj);
                this.$el.find('.md-disk-grid-body').append($gi);
            }
        },
        __print_txt: function (s) {
            var olines = s.split("\n");
            for (var i = 0; i < olines.length; i++) {
                var objstr = olines[i];
                if (_.isEmpty(objstr)) {
                    continue;
                }
                //console.log("======================");
                //console.log(objstr);
                $z.log(objstr);
                var obj = $z.fromJson(olines[i]);
                // 记录到wolist中
                this.model.get(WOList).push(obj);
            }
        },
        on_show_end: function () {
            if (this._old_s) {
                this.__print_txt(this._old_s);
                this._old_s = "";
            }
            this.render_disk();
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