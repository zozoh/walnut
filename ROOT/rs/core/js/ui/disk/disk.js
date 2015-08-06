define(function (require, exports, module) {
    var ZUI = require("zui");

    // obj缓存
    var objCache = {};

    function getObj(id) {
        if (objCache[id]) {
            return objCache[id];
        }
        var obj = $http.syncGet("/o/get/id:" + id).data;
        objCache[id] = obj;
        return obj;
    }

    function getObjPath(path) {
        if (objCache[path]) {
            return objCache[path];
        }
        var obj = $http.syncPost("/o/fetch", {'str': path}).data;
        objCache[path] = obj;
        return obj;
    }

    function on_keydown_at_gi(e) {
        var $gi = $(e.currentTarget);
        if (!$gi.hasClass('active')) {
            $gi.siblings().removeClass('active');
            $gi.addClass('active');
        }
        var nobj = $gi.data(WOBJ);
        this.load_objinfo(nobj);
        // 左键
        if (1 == e.which) {
            // 如果是名字的话, 就是直接打开了
        }
        // 右键
        if (3 == e.which) {
            $(document.body).find(".md-menu.lvl-0").remove();
            // 显示选中菜单
            var $menu = $mp.menu({
                'e': e,
                'menu': [{
                    "label": '查看详情',
                    "action": "info"
                }, {
                    "label": '打开',
                    "action": "open"
                }, {
                    "label": '重命名',
                    "action": "rename"
                }, {
                    "label": '其他操作',
                    "menu": [{
                        "label": '移动到',
                        "action": "move"
                    }, {
                        "label": '复制到',
                        "action": "copy"
                    }, {
                        "label": '制作副本',
                        "action": "dup"
                    }]
                }, {
                    "label": '删除',
                    "action": "delete"
                }]
            });
            $(document.body).one('mousedown', function () {
                $menu.remove();
            });
        }
    }

    function on_keydown_at_gbody(e) {
        $(e.currentTarget).find('.md-disk-grid-item').removeClass('active');
        if (3 == e.which) {
            $(document.body).find(".md-menu.lvl-0").remove();
            // 显示菜单
            var $menu = $mp.menu({
                'e': e,
                'menu': [{
                    "label": '返回上一层',
                    "action": "back"
                }, {
                    "label": '上传文件',
                    "action": "upload"
                }, {
                    "label": '新建文件夹',
                    "action": "mkdir"
                }, {
                    "label": '新建文件',
                    "action": "touch"
                }]
            });
            $(document.body).one('mousedown', function () {
                $menu.remove();
            });
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

    function on_switch_infotab(e) {
        var $li = $(e.currentTarget);
        var tnm = $li.attr('tnm');
        var $tc = $li.parents('.md-disk-info').find('.tab-containers .tc.' + tnm);
        if ($tc.hasClass('active')) {
            return;
        } else {
            $tc.siblings().removeClass('active');
            $tc.addClass('active');
        }
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
            if (options.fitself == undefined) {
                options.fitself = true;
            }
        },
        redraw: function () {
            var cobj = _app.obj ? getObj(_app.obj.id) : getObjPath(_app.session.envs["HOME"]);
            this._root_obj = cobj;
            this.open_file(cobj);
            this.model.set(WOList, []);
            this.model.set(SORT, "name");
            this.model.set(ASC, true);
            var UI = this;
            $(document.body).delegate('.md-menu li.has-action', 'mousedown', function () {
                var $li = $(this);
                var act = $li.attr('action');
                UI.rclick_action(act);
            });
        },
        events: {
            "mousedown .md-disk .md-disk-container .md-disk-grid-item": on_keydown_at_gi,
            "mousedown .md-disk .md-disk-grid-body": on_keydown_at_gbody,
            "click .md-disk .md-disk-container .md-disk-grid-title .md-disk-gt-cell": on_click_sort,
            "click .md-disk .disk-path-obj": on_click_path_item,
            "click .md-gi-group .gi-nm": on_click_gi_item,
            "click .md-disk .md-disk-info .tabs li": on_switch_infotab,
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
                this.load_objinfo(cobj);
                this.$el.find('.md-disk-container').addClass('info');
            }
            else if (act == "rename") {
                $mp.prompt("重命名", cobj.nm, function (new_nm) {
                    if (new_nm) {
                        new_nm = new_nm.trim();
                        if (new_nm == cobj.nm) {
                            return;
                        }
                        UI.model.trigger("cmd:exec", "mv " + cobj.nm + " " + new_nm, function () {
                            UI.open_file();
                        });
                    }
                });
            }
            else if (act == "move") {
                // TODO
                $mp.message('还有bug,暂停使用!')
            }
            else if (act == "copy") {
                // TODO
                $mp.message('还有bug,暂停使用!')
            }
            else if (act == "dup") {
                //UI.model.trigger("cmd:exec", "cp " + cobj.ph + " " + cobj.ph, function () {
                //    UI.open_file();
                //});
                $mp.message('还有bug,暂停使用!')
            }
            else if (act == "delete") {
                $mp.confirm('确定要删除"' + cobj.nm + '"', '删除文件', function (re) {
                    if (re) {
                        UI.model.trigger("cmd:exec", "rm " + cobj.ph, function () {
                            UI.open_file();
                        });
                    }
                });
            }
            else if (act == "back") {
                var $cpo = $po;
                if ($cpo.prev().length > 0) {
                    var nobj = getObjPath($cpo.prev().attr('path'));
                    UI.open_file(nobj);
                } else {
                    $mp.message('没有上一层, 无法返回');
                }
            }
            else if (act == "mkdir") {
                $mp.prompt("新建文件夹", "无标题文件夹", function (re) {
                    if (re) {
                        var cpath = $po.attr('path');
                        UI.model.trigger("cmd:exec", "mkdir " + cpath + "/" + re, function () {
                            UI.open_file();
                        });
                    }
                });
            }
            else if (act == "touch") {
                $mp.prompt("新建文件", "无标题文件", function (re) {
                    if (re) {
                        var cpath = $po.attr('path');
                        UI.model.trigger("cmd:exec", "touch " + cpath + "/" + re, function () {
                            UI.open_file();
                        });
                    }
                });
            }
            else if (act == "upload") {
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
                this.load_objinfo(obj);
            } else {
                this.$el.find('.md-disk-container').addClass('obj');
                this.render_obj(obj);
                this.load_objinfo(obj);
            }
        },
        open_dir: function (obj) {
            var UI = this;
            var Mod = UI.model;
            UI.beforeLoading();
            Mod.trigger("cmd:exec", "disk id:" + obj.id, function () {
                UI.afterLoading(true);
            });
        },
        beforeLoading: function () {
            var $db = this.$el.find('.md-disk-body');
            $db.removeClass('empty').addClass('loading');
        },
        afterLoading: function (chkEmpty) {
            var $db = this.$el.find('.md-disk-body')
            $db.removeClass('loading');
            if (chkEmpty && $db.find('.md-disk-grid-body .md-disk-grid-item').length == 0) {
                $db.addClass('empty');
            }
        },
        load_objinfo: function (obj) {
            var $minfo = this.$el.find('.md-disk-info');
            var isDir = obj.race == "DIR";
            var tp = isDir && !obj.tp ? "folder" : obj.tp.toLowerCase();
            $minfo.find('.title img').attr("src", "/p/default/thumbnail?tp=" + tp + "&size=32");
            $minfo.find('.title span').html(obj.alias ? obj.alias : obj.nm);
            // 详细信息
            //<div class="obj-attr-key"></div><div class="obj-attr-val"></div>
            var $attr = $minfo.find('.tc.detail .obj-attrs');
            var dtmap = [{
                nm: 'id',
                label: 'ID'
            }, {
                nm: 'nm',
                label: '文件名'
            }, {
                nm: 'alias',
                label: '别名',
                render: function (alias) {
                    if(alias){
                        return alias;
                    }
                    return "-";
                }
            }, {
                nm: 'ph',
                label: '位置'
            }, {
                nm: 'len',
                label: '大小',
                render: function (len) {
                    if (len) {
                        return $z.sizeText(len);
                    } else {
                        return "-";
                    }
                }
            }, {
                nm: 'race',
                label: '文件种类'
            }, {
                nm: 'tp',
                label: '文件类型'
            }, {
                nm: 'c',
                label: '所有者'
            }, {
                nm: 'g',
                label: '所属组'
            }, {
                nm: 'md',
                label: '权限'
            }, {
                nm: 'ct',
                label: '创建时间',
                render: function (ct) {
                    return $z.currentTime(new Date(ct));
                }
            }, {
                nm: 'lt',
                label: '上次修改',
                render: function (ct) {
                    return $z.currentTime(new Date().setTime(ct));
                }
            }];
            $attr.empty();
            for (var i = 0; i < dtmap.length; i++) {
                var dc = dtmap[i];
                var html = '';
                html += '<div class="obj-attr-row">';
                html += '<div class="key">' + dc.label + '</div>'
                html += '<div class="val">' + (dc.render ? dc.render(obj[dc.nm]) : obj[dc.nm]) + '</div>'
                html += '</div>';
                $attr.append(html);
            }
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
                var tp = isDir && !obj.tp ? "folder" : obj.tp.toLowerCase();
                $gi.find('.gi-nm').append(obj.alias ? obj.alias : obj.nm);
                $gi.find('.gi-nm .disk-icon').css('background-image', "url('" + '/p/default/thumbnail?tp=' + tp + "&size=16')");
                $gi.find('.disk-preview').css('background-image', "url('" + '/p/thumbnail?obj=' + obj.id + "&size=256')");
                $gi.find('.gi-owner').append(obj.c || "unknow");
                $gi.find('.gi-lm').append($z.currentTime(new Date(obj.lm)));
                $gi.find('.gi-len').append((obj.len ? $z.sizeText(obj.len) : "-" ));
                $gi.data(WOBJ, obj);
                this.$el.find('.md-disk-grid-body').append($gi);
            }
        },
        render_path: function () {
            var rootCObj = this._root_obj;
            var cobj = this.model.get(COBJ);
            var $dp = this.$el.find('.disk-path').empty();
            var i = 0;
            var $lgip = null;
            var path = cobj.ph;
            var pis = path.substr(1).split('/');
            var cph = '';
            var addToPath = false;
            for (var i = 0; i < pis.length - 1; i++) {
                cph += '/' + pis[i];
                var phobj = getObjPath(cph);
                var isDir = phobj.race == "DIR";
                var tp = isDir && !phobj.tp ? "folder" : phobj.tp.toLowerCase();
                var $gip = this.ccode('gi-path-item');
                $gip.find('.disk-icon').css('background-image', "url('" + '/p/default/thumbnail?tp=' + tp + "&size=16')");
                $gip.find('.disk-path-nm').append(phobj.alias ? phobj.alias : phobj.nm);
                $gip.attr('path', cph);
                if (!addToPath && cph == rootCObj.ph) {
                    addToPath = true;
                }
                if (addToPath) {
                    $dp.append($gip);
                }
            }
            var isDir = cobj.race == "DIR";
            var tp = isDir && !cobj.tp ? "folder" : cobj.tp.toLowerCase();
            var $gip = this.ccode('gi-path-item');
            $gip.addClass('active');
            $gip.find('.disk-icon').css('background-image', "url('" + '/p/default/thumbnail?tp=' + tp + "&size=16')");
            $gip.find('.disk-path-nm').append(cobj.alias ? cobj.alias : cobj.nm);
            $gip.attr('path', path);
            $dp.append($gip);
        },
        render_obj: function (obj) {
            this.clear_disk();
            this.render_path();
            var UI = this;
            var $sel = this.$el.find('.md-disk-data').empty();
            var obj = this.model.get(COBJ);
            var tp = obj.tp.toLowerCase();
            // 扎到应类型的播放器打开它
            UI.beforeLoading();
            seajs.use("ui/open/" + getPlayer(tp), function (player) {
                UI.afterLoading();
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
            $mp.message(s);
        }
    });
//=======================================================================
});