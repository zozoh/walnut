// 工单查询，过滤
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

                // 加载对象编辑器
                new SearchUI2({
                    parent: UI,
                    gasketName: "main",
                    data: "obj ~/ -match '<%=match%>' -skip {{skip}} -limit {{limit}} -l -json -pager -sort '<%=sort%>'",
                    menu: ["create", "refresh", "delete", "edit"],
                    edtCmdTmpl: {
                        "create": "obj -new '<%=json%>' -o",
                        "delete": "rm -rf id:{{id}}",
                        "edit": "obj id:{{id}} -u '<%=json%>' -o"
                    },
                    filter: {
                        keyField: ["nm", "alias"],
                        keyFieldIsOr: true,
                        formatData: function (obj) {
                            // 这里随便怎么改 obj 里的字段
                            return obj;
                        },
                        tabs: [{
                            icon: '<i class="zmdi zmdi-file"></i>',
                            text: "文件",
                            value: {
                                race: "FILE"
                            }
                        }, {
                            dchecked: true,
                            icon: '<i class="zmdi zmdi-folder"></i>',
                            text: "目录",
                            value: {
                                race: "DIR"
                            }
                        }, {
                            icon: '<i class="zmdi zmdi-apps"></i>',
                            text: "APP",
                            value: {
                                nm: "app"
                            }
                        }],
                        tabsPosition: "left",
                        dtabsMulti: true,
                        dtabsKeepChecked: true,
                        tabsStatusKey: "test_pet_search_tab",
                        dhideInputBox: true,
                    },
                    dsorter: {
                        setup: [{
                            icon: 'asc',
                            text: "按最后修改日期",
                            value: {lm: 1},
                        }, {
                            icon: 'desc',
                            text: "按名称",
                            value: {nm: -1},
                        }],
                        storeKey: "test_pet_search_sort"
                    },
                    list: {
                        fields: [{
                            key: "nm",
                            title: "名称",
                        }, {
                            key: "alias",
                            title: "别名"
                        }, {
                            key: "g",
                            title: "所在组",
                            uiType: '@label',
                        }, {
                            key: "race",
                            title: "种类",
                            uiType: "@switch",
                            uiConf: {
                                items: [{
                                    value: "FILE", text: "文件"
                                }, {
                                    value: "DIR", text: "目录"
                                }]
                            }
                        }, {
                            key: "lm",
                            title: "最后修改时间",
                            uiType: '@label',
                            display: function (o) {
                                return $z.parseDate(o.lm).format("yyyy-mm-dd HH:MM:ss");
                            }
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