define(function (require, exports, module) {
    var ZUI = require("zui");

    function getObj(id) {
        return $http.syncGet("/o/get/id:" + id).data;
    }

    function getObjPath(path) {
        return $http.syncPost("/o/fetch", {'str': path}).data;
    }

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
            e.stopPropagation();
        }
    }

    function on_keydown_at_gbody(e) {
        $(e.currentTarget).find('.md-disk-grid-item').removeClass('active');
        if (3 == e.which) {
            // 显示菜单
            addMenuOnBody(this.ccode('gbody-r-menu'), e);
            e.stopPropagation();
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

    function on_click_path_item(e) {
        var path = null;
        var $pli = $(e.currentTarget);
        var $next = $pli.next();
        var $prev = $pli.prev();
        if ($next.length == 0) {
            if ($prev.length > 0 && $prev.css('display') == 'none') {
                // mobile模式
                path = $prev.attr('path');
            }
            if (path == null) {
                return;
            }
        } else {
            path = $pli.attr('path');
        }
        var nobj = getObjPath(path);
        this.open_file(nobj);
    }

    function on_click_gi_item(e) {
        var $gi = $(e.currentTarget).parent().parent();
        var nobj = $gi.data(WOBJ);
        this.open_file(nobj);
    }

    var COBJ = "current-walnut-obj";
    var WOBJ = "walnut-obj";
    var WOList = "walnut-obj-list";
    var SORT = "SORT";
    var ASC = "ASC";

    var PLAY_MAP = {
        'txt': 'plainText',
        'jpg': 'image',
        'jpeg': 'image',
        'png': 'image',
        'gif': 'image',
        'bmp': 'image',
        'mp4': 'video',
        'avi': 'video',
        'vod': 'video',
        'wmv': 'video',
        'mov': 'video'
    }

    function getPlayer(tp) {
        var player = PLAY_MAP[tp];
        if (player == null) {
            return "unknow";
        }
        return player;
    }

    module.exports = ZUI.def("ui.disk", {
        dom: "ui/disk/disk.html",
        css: "ui/disk/disk.css",
        init: function (options) {
            this.listenModel("show:err", this.on_show_err);
            this.listenModel("show:txt", this.on_show_txt);
            this.listenModel("show:end", this.on_show_end);
        },
        redraw: function () {
            var cobj = _app.obj !== undefined ? getObj(_app.obj.id) : getObjPath(_app.session.envs["PWD"]);
            this.open_file(cobj);
            this.model.set(WOList, []);
            this.model.set(SORT, "name");
            this.model.set(ASC, true);
            var UI = this;
            $(document.body).delegate('.rclick-menu .menu-action', 'mousedown', function () {
                var act = $(this).attr('action');
                UI.rclick_action(act);
            });
        },
        events: {
            "mousedown .md-disk .md-disk-container .md-disk-grid-item": on_keydown_at_gi,
            "mousedown .md-disk .md-disk-grid-body": on_keydown_at_gbody,
            "click .md-disk .md-disk-container .md-disk-grid-title .md-disk-gt-cell": on_click_sort,
            "click .md-disk .disk-path-obj": on_click_path_item,
            "click .md-gi-group .gi-nm": on_click_gi_item,
        },
        rclick_action: function (act) {
            var UI = this;
            var $ac = this.$el.find('.md-disk-grid-item.active');
            var $po = this.$el.find('.disk-path-obj.active');
            var cobj = $ac.length > 0 ? $ac.data(WOBJ) : null;
            if (act == "open") {
                this.open_file(cobj);
            }
            else if (act == "info") {
                // TODO
            }
            else if (act == "rename") {
                // TODO
            }
            else if (act == "move") {
                // TODO
            }
            else if (act == "copy") {
                // TODO
            }
            else if (act == "dup") {
                // TODO
            }
            else if (act == "move") {
                // TODO
            }
            else if (act == "delete") {
                // TODO
            }
            else if (act == "back") {
                var $cpo = $po;
                if ($cpo.prev().length > 0) {
                    var nobj = getObjPath($cpo.prev().attr('path'));
                    UI.open_file(nobj);
                } else {
                    alert('没有上一层, 无法返回');
                }
            }
            else if (act == "mkdir") {
                setTimeout(function () {
                    // TODO
                    var dirnm = prompt("请输入文件夹名称", "新建文件夹");
                    if (dirnm == undefined || dirnm == null) {
                        return;
                    }
                    var cpath = $po.attr('path');
                    UI.model.trigger("cmd:exec", "mkdir " + cpath + "/" + dirnm, function () {
                        UI.open_file();
                    });
                }, 100);
            }
            else if (act == "touch") {
                setTimeout(function () {
                    // TODO
                    var dirnm = prompt("请输入文件名称", "新建文件");
                    if (dirnm == undefined || dirnm == null) {
                        return;
                    }
                    var cpath = $po.attr('path');
                    UI.model.trigger("cmd:exec", "touch " + cpath + "/" + dirnm, function () {
                        UI.open_file();
                    });
                }, 100);
            }
            else if (act == "upload") {
                // FIXME
                UI.model.trigger("cmd:exec", "open upload " + $po.attr('path'), function () {
                });
            }
        },
        clear_disk: function () {
            this.$el.find('.md-disk-grid-body').empty();
        },
        open_file: function (obj) {
            var UI = this;
            var Mod = UI.model;
            if (obj == null) {
                obj = Mod.get(COBJ);
            }
            Mod.set(WOList, []);
            Mod.set(COBJ, obj);
            var isDir = obj.race == "DIR";
            var tp = isDir ? "folder" : obj.tp.toLowerCase();
            if (isDir) {
                this.$el.find('.md-disk-container').removeClass('obj');
                this.open_dir(obj);
            } else {
                this.$el.find('.md-disk-container').addClass('obj');
                this.render_obj(obj);
            }
        },
        open_dir: function (obj) {
            var UI = this;
            var Mod = UI.model;
            Mod.trigger("cmd:exec", "disk id:" + obj.id, function () {
                Mod.trigger("cmd:exec", "cd id:" + obj.id);
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
            this.render_path();
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
        render_path: function () {
            var cobj = this.model.get(COBJ);
            var $dp = this.$el.find('.disk-path').empty();
            // 重新编译
            var i = 0;
            var $lgip = null;
            var path = "";
            while (true) {
                var pnm = cobj["d" + i];
                if (pnm != undefined) {
                    path += '/' + pnm;
                    var $gip = this.ccode('gi-path-item');
                    $gip.find('.disk-icon').addClass('folder');
                    $gip.find('.disk-path-nm').append(pnm);
                    $gip.attr('path', path);
                    if ($lgip != null) {
                        $dp.append($lgip);
                    }
                    $lgip = $gip;
                } else {
                    // 到头了, 显示自己然后退出
                    var isDir = cobj.race == "DIR";
                    var tp = isDir ? "folder" : cobj.tp.toLowerCase();
                    var $gip = this.ccode('gi-path-item');
                    $gip.addClass('active');
                    $gip.find('.disk-icon').addClass(tp);
                    $gip.find('.disk-path-nm').append(cobj.nm);
                    $gip.attr('path', path);
                    $dp.append($gip);
                    break;
                }
                i++;
            }
        },
        render_obj: function (obj) {
            this.clear_disk();
            this.render_path();
            var $sel = this.$el.find('.md-disk-data').empty();
            var obj = this.model.get(COBJ);
            var tp = obj.tp.toLowerCase();
            // 扎到应类型的播放器打开它
            seajs.use("ui/open/" + getPlayer(tp), function (player) {
                player.open($sel, obj);
            });
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