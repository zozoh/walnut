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
        var html = `<div class="ui-arena ticket-container" ui-gasket="main" ui-fitparent="true"></div>`;
        return ZUI.def("app.wn.ticket.client.user", {
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
                    this.treply = TkTmp.ticketReply.create(this, $main, obj);
                });
            },
            redraw: function () {
                var UI = this;
                // 获取指定目录的pid
                var tsmap = {
                    'new': "新工单",
                    'assign': "已分配客服",
                    'reassign': "重新分配客服",
                    'creply': "已回复",
                    'ureply': "待处理",
                    'done': "已关闭"
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
                UI.myTicketUI = new SearchUI2({
                    parent: UI,
                    gasketName: "main",
                    data: "ticket my -list '<%=match%>' -skip {{skip}} -limit {{limit}}",
                    menu: [{
                        text: "新建工单",
                        handler: function () {
                            // 起个标题，然后提交后弹出对话窗口
                            UI.prompt("请填写工单标题", {
                                width: "60%",
                                title: "问题概况描述",
                                ok: function (text) {
                                    console.log(text);
                                    if (text.trim() == '') {
                                        return;
                                    }
                                    Wn.exec("ticket my -post -c 'text: \"" + text + "\"'", function (re) {
                                        var re = JSON.parse(re);
                                        if (re.ok) {
                                            // 拿到了新的工单对象
                                            var obj = re.data;
                                            UI.showTicketChat(obj);
                                            // 后台刷新列表
                                            UI.myTicketUI.refresh();
                                        } else {
                                            UI.alert(re.data);
                                        }
                                    });
                                }
                            })
                        }
                    }, "refresh"],
                    edtCmdTmpl: {
                        "create": "ticket my -post -c '<%=json%>' -sort '<%=sort%>'",
                    },
                    events: {
                        "dblclick .list-item": function (e) {
                            var jq = $(e.currentTarget);
                            var obj = this.uiList.getData(jq)
                            console.log(obj);
                            // mask显示下
                            UI.showTicketChat(obj);
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
                                var s = o.ticketTp;
                                return ttmap[s] || "未定义状态";
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
                            title: "提交时间",
                            uiType: '@label',
                            display: function (o) {
                                return $z.currentTime(o.tickerStart);
                            }
                        }],
                        checkable: false,
                        multi: false,
                        layout: {
                            sizeHint: [80, 80, '*', 100, 150]
                        }
                    },
                    sorter: {
                        setup: [{
                            icon: 'desc',
                            text: "按更新时间",
                            value: {lm: -1}
                        }]
                    }
                }).render(function () {
                    this.uiFilter.setData({});
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