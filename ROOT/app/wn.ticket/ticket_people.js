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
                var udir = Wn.exec("obj ~/.ticket/user");
                var cdir = Wn.exec("obj ~/.ticket/cservice");
                // 加载对象编辑器
                new SearchUI2({
                    parent: UI,
                    gasketName: "main",
                    data: "obj -match '<%=match%>' -skip {{skip}} -limit {{limit}} -l -json -pager -sort '<%=sort%>'",
                    menu: ["create", "refresh", "delete", "edit"],
                    edtCmdTmpl: {
                    },
                    filter: {
                        keyField: ["usrNm", "usrAlias"],
                        keyFieldIsOr: true,
                        formatData: function (obj) {
                            return obj;
                        },
                        tabs: [{
                            icon: '<i class="fa fa-user"></i>',
                            text: "用户",
                            value: {
                                tp: "user"
                            }
                        }, {
                            dchecked: true,
                            icon: '<i class="fa fa-user-md"></i>',
                            text: "客服",
                            value: {
                                tp: "cservice"
                            }
                        }],
                        tabsPosition: "left",
                        tabsMulti: false,
                        tabsKeepChecked: false,
                        tabsStatusKey: "test_pet_search_tab"
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