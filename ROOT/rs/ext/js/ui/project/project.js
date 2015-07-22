define(function (require, exports, module) {
    var ZUI = require("zui");

    function on_click_project(e) {
        var i = parseInt($(e.currentTarget).attr('index'));
        var pinfo = this._project_list[i];
        this.render_project_tasks(pinfo);
    }

    function getObj(id) {
        return $http.syncGet("/o/get/id:" + id).data;
    }

    function on_click_task(e) {
        var id = $(e.currentTarget).attr('oid');
        window._app.obj = getObj(id);
        // 修改当前
        seajs.use(['ui/mask/mask', 'ui/wedit/wedit', 'wn/walnut.obj'], function (Mask, wedit, wobj) {
            new Mask({
                closer: true,
                escape: true,
                onclose: function () {
                    // TODO reload
                }
            }).render(function () {
                    new wedit({
                        $el: this.arena,
                        model: new wobj(window._app),
                        fitparent: true
                    }).render();
                });
        });
    }

    module.exports = ZUI.def("ext.project", {
        dom: "ext/project/project.html",
        css: "ext/project/project.css",
        init: function (options) {
            this.listenModel("show:err", this.on_show_err);
            this.listenModel("show:txt", this.on_show_txt);
            this.listenModel("show:end", this.on_show_end);
        },
        redraw: function () {
            if ($mp) {
                $mp.home.header.notHeader();
            }
            this.model.trigger("cmd:exec", "myproject", function () {
            });
        },
        resize: function () {
        },
        events: {
            'click .project-tabs.md-tab-group li': on_click_project,
            'click .task-name': on_click_task
        },
        on_show_end: function () {
            this.render_project(this._old_s);
            this._old_s = null;
        },
        on_show_txt: function (s) {
            var old = this._old_s || "";
            s = old + s;
            this._old_s = s;
        },
        on_show_err: function (s) {
            alert(s);
        },
        render_project: function (s) {
            var plist = $z.fromJson(s);
            this._project_list = plist;

            var $tabs = this.$el.find('.project-tabs.md-tab-group');
            $tabs.find('li').remove();

            var $f_li = null;
            for (var i = 0; i < plist.length; i++) {
                var pinfo = plist[i];
                var $li = this.ccode('project-tab-tmpl');
                $li.attr('index', i);
                $li.find('.ripple-button').html(pinfo.name);
                $tabs.append($li);

                if (i == 0) {
                    $f_li = $li;
                }
            }
            // 显示第一个
            if ($f_li) {
                $f_li.click();
            }
        },
        render_project_tasks: function (pinfo) {
            var $pi = this.$el.find('.project-info');
            this._render_tasks($pi.find('.tasks-overdue'), pinfo.tasks_overdue);
            this._render_tasks($pi.find('.tasks-7day'), pinfo.tasks_7day);
            this._render_tasks($pi.find('.tasks-after'), pinfo.tasks_after);
            this._render_tasks($pi.find('.tasks-done'), pinfo.tasks_done);
        },
        _render_tasks: function ($con, tasks) {
            var $ul = $con.find('ul');
            $ul.empty();
            var $em = $con.find('.project-task-title em');
            $em.html(tasks.length);
            if (tasks.length > 0) {
                tasks.sort(function (x, y) {
                    return x.leftDays - y.leftDays;
                });
                for (var i = 0; i < tasks.length; i++) {
                    var pt = tasks[i];
                    var $ptask = this.ccode('project-task-tmpl');
                    $ptask.find('.task-name').html(pt.name).attr("oid", pt.id);
                    $ptask.find('.task-description').html(pt.description);
                    $ptask.find('.task-priority span').html(pt.priority);
                    $ptask.find('.task-level span').html(pt.level);
                    $ptask.find('.task-progress .task-progress-tip').html(pt.progress + "%");
                    $ptask.find('.task-progress .task-progress-bar').css('width', pt.progress + "%");
                    $ptask.find('.dl').html(pt.deadline);
                    $ptask.find('.dl-left span').html(pt.leftDays);
                    $ul.append($ptask);
                }
            }
        }
    });
//=======================================================================
});