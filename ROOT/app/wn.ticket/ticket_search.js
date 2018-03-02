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
        var html = `
        <div class="ui-code-template">
                <div code-id="formmask">
                    <div class="ui-arena srh-mask-form" ui-fitparent="yes">
                        <div class="ui-mask-bg"></div>
                        <div class="ui-mask-main"><div class="srh-mask">
                            <div class="srh-qform" ui-gasket="main"></div>
                            <div class="srh-qbtns">
                                <b class="srh-qform-ok"><i class="ing fa fa-spinner fa-spin"></i>{{ok}}</b>
                                <b class="srh-qform-cancel">{{cancel}}</b>
                            </div>
                        </div></div>
                        <div class="ui-mask-closer"></div>
                    </div>
                </div>
        </div>
        <div class="ui-arena ticket-contaner" ui-gasket="ticketList" ui-fitparent="true"></div>`;
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

                UI.cslist = Wn.execJ("obj ~/.ticket/cservice/*") || [];

                // // 工单类型
                // UI.tkconf = Wn.execJ("ticket my -tkconf");
                // var tps = [];
                // for (var i = 0; i < UI.tkconf.tps.length; i++) {
                //     tps.push({
                //         text: UI.tkconf.tps[i],
                //         value: UI.tkconf.tps[i]
                //     })
                // }
                // UI._tps = tps;
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
                    gasketName: "ticketList",
                    data: "obj -match '<%=match%>' -skip {{skip}} -limit {{limit}} -l -json -pager -sort '<%=sort%>'",
                    menu: [{
                        text: "指派客服",
                        handler: function () {
                            // 先选中一个
                            var obj = UI.myTicketUI.uiList.getActived();
                            if (!obj) {
                                UI.alert("请选中一个工单后再操作");
                                return;
                            }
                            if (obj.ticketStep == '3') {
                                UI.alert("该工单已经无法再进行指派操作");
                                return;
                            }
                            // 默认是自己，可以选择其他人
                            var cslist = [];
                            for (var i = 0; i < UI.cslist.length; i++) {
                                var c = UI.cslist[i];
                                cslist.push({
                                    text: c.usrNm + "(" + (c.usrId == UI.me.id ? "我" : c.usrAlias) + ")",
                                    value: c.usrId
                                })
                            }
                            new MaskUI({
                                dom: UI.ccode("formmask").html(),
                                i18n: UI._msg_map,
                                exec: UI.exec,
                                width: 600,
                                height: 120,
                                dom_events: {
                                    "click .srh-qform-ok": function (e) {
                                        var uiMask = ZUI(this);
                                        var formData = uiMask.body.getData();
                                        if (formData) {
                                            console.log(JSON.stringify(formData));
                                            var csId = formData.cservice;
                                            Wn.exec("ticket my -assign " + obj.id + " -tu " + csId, function (re) {
                                                var re = JSON.parse(re);
                                                if (re.ok) {
                                                    UI.myTicketUI.refresh();
                                                } else {
                                                    UI.alret(re.data);
                                                }
                                                uiMask.close();
                                            });
                                        }
                                    },
                                    "click .srh-qform-cancel": function (e) {
                                        var uiMask = ZUI(this);
                                        uiMask.close();
                                    }
                                },
                                setup: {
                                    uiType: "ui/form/form",
                                    uiConf: {
                                        uiWidth: "all",
                                        title: "",
                                        fields: [{
                                            key: "cservice",
                                            title: "指定客服",
                                            tip: "默认情况选中自己",
                                            type: "string",
                                            editAs: "droplist",
                                            uiWidth: "auto",
                                            uiConf: {
                                                items: cslist
                                            }
                                        }]
                                    }
                                }
                            }).render(function () {
                                this.body.setData({
                                    cservice: obj.csId || UI.me.id
                                });
                            });
                        }
                    }, "refresh"],
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
                            key: "id",
                            title: "工单ID",
                            uiType: '@label',
                            display: function (o) {
                                return o.id.substr(0, 10).toUpperCase();
                            }
                        }, {
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
                            key: "lbls",
                            title: "标签",
                            uiType: '@label',
                            display: function (o) {
                                var lbls = o.lbls || [];
                                return lbls.join(" ");
                            }
                        }, {
                            key: "usrAlias",
                            title: "用户",
                            uiType: '@label',
                            display: function (o) {
                                return o.usrAlias || "用户" + o.usrId.substr(0, 4);
                            }
                        }, {
                            key: "csAlias",
                            title: "客服",
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
                            sizeHint: [100, 80, 80, '*', 150, 100, 100, 150, 150]
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
                    UI.defer_report("ticketList");
                });
                // 返回延迟加载
                return ["ticketList"];
            },
            //..............................................
            update: function (o) {
                this.gasket.ticketList.refresh();
            }
            //..............................................
        });
//==================================================
    });
})(window.NutzUtil);