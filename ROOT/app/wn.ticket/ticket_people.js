// 工单系统人员管理，包含用户与客服
(function ($z) {
    $z.declare([
        'zui',
        'wn/util',
        'ui/search2/search',
    ], function (ZUI, Wn, SearchUI2) {
        var html = `<div class="ui-arena ticket-contaner" ui-gasket="main" ui-fitparent="true"></div>`;
        return ZUI.def("app.wn.ticket.people", {
            dom: html,
            css: "app/wn.ticket/theme/ticket-{{theme}}.css",
            i18n: "app/wn.ticket/i18n/{{lang}}.js",
            tmplSettings: {
                escape: ""
            },
            redraw: function () {
                var UI = this;
                // 获取指定目录的pid
                var udir = Wn.execJ("obj ~/.ticket/user");
                var cdir = Wn.execJ("obj ~/.ticket/cservice");
                // 加载对象编辑器
                new SearchUI2({
                    parent: UI,
                    gasketName: "main",
                    data: "obj -match '<%=match%>' -skip {{skip}} -limit {{limit}} -l -json -pager -sort '<%=sort%>'",
                    menu: ["refresh", "edit", "delete"],
                    edtCmdTmpl: {
                        "delete": "rm -rf id:{{id}}",
                        "edit": "obj id:{{id}} -u '<%=json%>' -o"
                    },
                    filter: {
                        keyField: ["usrNm", "usrAlias"],
                        keyFieldIsOr: true,
                        formatData: function (obj) {
                            return obj;
                        },
                        tabs: [{
                            checked: true,
                            icon: '<i class="fa fa-user"></i>',
                            text: "用户",
                            value: {
                                serviceTp: "user",
                                pid: udir.id
                            }
                        }, {
                            dchecked: true,
                            icon: '<i class="fa fa-user-md"></i>',
                            text: "客服",
                            value: {
                                serviceTp: "cservice",
                                pid: cdir.id
                            }
                        }],
                        tabsPosition: "left",
                        tabsMulti: false,
                        tabsKeepChecked: true
                    },
                    list: {
                        fields: [{
                            key: "serviceTp",
                            title: "类型",
                            uiType: '@label',
                            display: function (o) {
                                if (o.serviceTp == 'user') {
                                    return "用户";
                                }
                                if (o.serviceTp == 'cservice') {
                                    return "客服";
                                }
                            }
                        }, {
                            key: "usrNm",
                            title: "名称",
                            uiType: '@label'
                        }, {
                            key: "usrAlias",
                            title: "别名"
                        }, {
                            key: "ct",
                            title: "注册时间",
                            uiType: '@label',
                            display: function (o) {
                                return $z.parseDate(o.lm).format("yyyy-mm-dd HH:MM:ss");
                            }
                        }],
                        checkable: false,
                        multi: false,
                        layout: {
                            sizeHint: [50, 120, 120, 150, "*"]
                        }
                    },
                    sorter: {
                        setup: [{
                            icon: 'asc',
                            text: "按注册日期",
                            value: {ct: 1}
                        }, {
                            icon: 'desc',
                            text: "按名称",
                            value: {nm: -1}
                        }]
                    }
                }).render(function () {
                    this.uiFilter.setData({
                        "d1": Wn.app().session.grp
                    });
                    UI.defer_report("main");
                });

                // 返回延迟加载
                return ["main"];
            },
            //..............................................
            update: function (o) {
                this.gasket.main.refresh();
            }
            //..............................................
        });
//==================================================
    });
})(window.NutzUtil);