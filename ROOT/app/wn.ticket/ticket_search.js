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
            redraw: function () {
                var UI = this;
                // 获取指定目录的pid
                var rdir = Wn.execJ("obj ~/.ticket/record");
                var tsmap = {
                    'new': "新工单待分派",
                    'assign': "工单已分派",
                    'reassign': "工单重新分派 ",
                    'creply': "待用户反馈",
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
                    events: {
                        "dblclick .list-item": function (e) {
                            var jq = $(e.currentTarget);
                            var obj = this.uiList.getData(jq)
                            console.log(obj);
                            // mask显示下
                            var ticketUI = new MaskUI({
                                exec: UI.exec,
                                width: 600,
                                height: "80%",
                                closer: true
                            }).render(function () {
                                // 添加html
                                var cid = "ticket_mask_" + $z.randomInt(1000, 10000);
                                var html = "";
                                html += TkTmp.tkList(cid);
                                var $main = this.$el.find('.ui-mask-main');
                                $main.html(html);

                                // 准备数据
                                var ritems = [];
                                ritems = ritems.concat(obj.request || []);
                                ritems = ritems.concat(obj.response || []);

                                new Vue({
                                    el: '#' + cid,
                                    data: {
                                        items: ritems
                                    },
                                    computed: {
                                        timeItems: function () {
                                            return this.items.sort(function (a, b) {
                                                return a.time > b.time;
                                            });
                                        }
                                    },
                                    methods: {
                                        timeText: function (item) {
                                            return $z.currentTime(item.time)
                                        }
                                    },
                                    mounted: function () {
                                    }
                                });
                            });
                        }
                    },
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
                            sizeHint: [100, 100, 250, 100, 150, 150, "*"]
                        }
                    },
                    sorter: {
                        setup: [{
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