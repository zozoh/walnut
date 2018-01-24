// 工单查询，过滤
// 工单系统人员管理，包含用户与客服
(function ($z) {
    $z.declare([
        'zui',
        'wn/util',
        "ui/mask/mask",
        'ui/search2/search',
        'app/wn.ticket/ticket_vuetmp',
    ], function (ZUI, Wn, MaskUI, SearchUI2, TkTmp) {
        var html = `<div class="ui-arena ticket-contaner" ui-gasket="main" ui-fitparent="true"></div>`;
        return ZUI.def("app.wn.ticket.search", {
            dom: html,
            css: "app/wn.ticket/theme/ticket-{{theme}}.css",
            i18n: "app/wn.ticket/i18n/{{lang}}.js",
            tmplSettings: {
                escape: ""
            },
            showTicketChat: function (obj) {
                var UI = this;
                console.log(obj);
                // mask显示下
                var ticketUI = new MaskUI({
                    exec: UI.exec,
                    width: 600,
                    height: "80%",
                    closer: true,
                    on_close: function () {
                        this.treply.$destroy();
                        UI.myTicketUI.refresh();
                    }
                }).render(function () {
                    var $main = this.$el.find('.ui-mask-main');
                    this.treply = TkTmp.ticketReply.create(UI, $main, obj, {hideMenu: true});
                });
            },
            init: function () {
                var UI = this;
                // 获取我的信息
                UI.me = JSON.parse(Wn.exec("me -json"));
            },
            redraw: function () {
                var UI = this;
                // 获取指定目录的pid
                var rdir = Wn.execJ("obj ~/.ticket/record");
                var tsmap = {
                    'new': "新工单",
                    'assign': "已分派",
                    'reassign': "重新分派",
                    'creply': "待用户反馈",
                    'ureply': "待继续处理",
                    'done': "已完成",
                    'close': "已关闭"
                };
                var stepmap = {
                    1: "待分配",
                    2: "处理中",
                    3: "已完成"
                };
                var stepDrops = [];
                for (var k in stepmap) {
                    stepDrops.push({
                        text: stepmap[k],
                        value: {
                            ticketStep: k
                        }
                    });
                }
                var statusDrops = [];
                for (var k in tsmap) {
                    statusDrops.push({
                        text: tsmap[k],
                        value: {
                            ticketStep: k
                        }
                    });
                }
                // 加载对象编辑器
                UI.myTicketUI = new SearchUI2({
                    parent: UI,
                    gasketName: "main",
                    data: "obj -match '<%=match%>' -skip {{skip}} -limit {{limit}} -l -json -pager -sort '<%=sort%>'",
                    menu: ["refresh"],
                    edtCmdTmpl: {},
                    events: {
                        "dblclick .list-item": function (e) {
                            var jq = $(e.currentTarget);
                            var obj = this.uiList.getData(jq)
                            console.log(obj);
                            UI.showTicketChat(obj);
                        }
                    },
                    filter: {
                        keyField: ["text"],
                        keyFieldIsOr: false,
                        formatData: function (obj) {
                            return obj;
                        },
                        tabs: stepDrops,
                        tabsPosition: "left",
                        tabsMulti: false,
                        tabsKeepChecked: false
                    },
                    list: {
                        fields: [{
                            key: "ticketTp",
                            title: "工单类型",
                            uiType: '@label'
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
                                if (otext.length > 20) {
                                    return otext.substr(0, 20) + "..."
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
                                return $z.parseDate(o.tickerStart).format("yyyy-mm-dd HH:MM");
                            }
                        }, {
                            key: "ticketEnd",
                            title: "结束时间",
                            uiType: '@label',
                            display: function (o) {
                                if (o.ticketEnd < 0) {
                                    return "未结束";
                                }
                                return $z.parseDate(o.ticketEnd).format("yyyy-mm-dd HH:MM");
                            }
                        }],
                        checkable: false,
                        multi: false,
                        layout: {
                            sizeHint: [80, 80, '*', 100, 150, 150]
                        }
                    },
                    sorter: {
                        setup: [{
                            icon: 'desc',
                            text: "按更新日期",
                            value: {lm: -1}
                        }, {
                            icon: 'desc',
                            text: "按提交日期",
                            value: {tickerStart: -1}
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