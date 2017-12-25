// 工单查询，过滤
// 工单系统人员管理，包含用户与客服
(function ($z) {
    $z.declare([
        'zui',
        'wn/util',
        'ui/search2/search',
    ], function (ZUI, Wn, SearchUI2) {
        var html = `<div class="ui-arena ticket-contaner" ui-gasket="main" ui-fitparent="true"></div>`;
        return ZUI.def("app.wn.ticket.search", {
            dom: html,
            css: "app/wn.ticket/theme/ticket-{{theme}}.css",
            i18n: "app/wn.ticket/i18n/{{lang}}.js",
            tmplSettings: {
                escape: ""
            },
            redraw: function () {
                var UI = this;
                // 获取指定目录的pid
                var rdir = Wn.execJ("obj ~/.ticket/record");
                var tsmap = {
                    'new': "新工单待分派",
                    'assign': "工单已分派",
                    'reassign': "工单重新分派 ",
                    'creply': "待您反馈",
                    'ureply': "待客服继续处理",
                    'done': "工单处理完毕",
                    'close': "工单已关闭"
                };
                var ttmap = {
                    'issue': "Issue",
                    'question': "普通问题"
                };
                var stabs = [];
                for (var k in tsmap) {
                    stabs.push({
                        text: tsmap[k],
                        value: {
                            ticketStatus: k
                        }
                    });
                }
                // 加载对象编辑器
                new SearchUI2({
                    parent: UI,
                    gasketName: "main",
                    data: "obj -match '<%=match%>' -skip {{skip}} -limit {{limit}} -l -json -pager -sort '<%=sort%>'",
                    menu: ["refresh"],
                    edtCmdTmpl: {},
                    filter: {
                        keyField: ["text"],
                        keyFieldIsOr: false,
                        formatData: function (obj) {
                            return obj;
                        },
                        tabs: stabs,
                        tabsPosition: "drop",
                        tabsMulti: false,
                        tabsKeepChecked: false
                    },
                    list: {
                        fields: [{
                            key: "ticketTp",
                            title: "工单类型",
                            uiType: '@label',
                            display: function (o) {
                                var s = o.ticketStatus;
                                return tsmap[s] || "未定义状态";
                            }
                        }, {
                            key: "ticketStatus",
                            title: "工单状态",
                            uiType: '@label',
                            display: function (o) {
                                var s = o.ticketStatus;
                                return tsmap[s] || "未定义状态";
                            }
                        }, {
                            key: "text",
                            title: "问题概述",
                            uiType: '@label',
                            display: function (o) {
                                var otext = o.text;
                                if (otext.length > 15) {
                                    return otext.substr(0, 10) + "..."
                                }
                                return otext;
                            }
                        }, {
                            key: "csAlias",
                            title: "处理客服",
                            uiType: '@label',
                            display: function (o) {
                                return o.csAlias || "";
                            }
                        }, {
                            key: "tickerStart",
                            title: "开始时间",
                            uiType: '@label',
                            display: function (o) {
                                return $z.parseDate(o.tickerStart).format("yyyy-mm-dd HH:MM:ss");
                            }
                        }, {
                            key: "ticketEnd",
                            title: "结束时间",
                            uiType: '@label',
                            display: function (o) {
                                if (o.ticketEnd < 0) {
                                    return "未结束";
                                }
                                return $z.parseDate(o.ticketEnd).format("yyyy-mm-dd HH:MM:ss");
                            }
                        }],
                        checkable: false,
                        multi: false,
                        layout: {
                            sizeHint: [100, 100, 250, 100, 250, 250, "*"]
                        }
                    },
                    sorter: {
                        setup: [{
                            icon: 'asc',
                            text: "按提交日期",
                            value: {tickerStart: 1}
                        }, {
                            icon: 'asc',
                            text: "按工单类型",
                            value: {ticketTp: 1}
                        }]
                    }
                }).render(function () {
                    this.uiFilter.setData({
                        "d1": Wn.app().session.grp,
                        "pid": rdir.id
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