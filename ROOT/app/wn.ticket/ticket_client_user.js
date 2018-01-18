// 工单查询，过滤
// 工单系统人员管理，包含用户与客服
(function ($z) {
    $z.declare([
        'zui',
        'wn/util',
        "ui/mask/mask",
        'ui/search2/search',
        'ui/upload/upload',
        'app/wn.ticket/ticket_vuetmp',
    ], function (ZUI, Wn, MaskUI, SearchUI2, UploadUI, TkTmp) {
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
        <div class="ui-arena ticket-container" ui-gasket="ticketList" ui-fitparent="true"></div>
        `;
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
                    this.treply = TkTmp.ticketReply.create(UI, $main, obj);
                });
            },
            init: function () {
                var UI = this;
                // 获取我的信息
                UI.me = JSON.parse(Wn.exec("me -json"));

                // 工单类型
                UI.tkconf = Wn.execJ("ticket my -tkconf");
                var tps = [];
                for (var i = 0; i < UI.tkconf.tps.length; i++) {
                    tps.push({
                        text: UI.tkconf.tps[i],
                        value: UI.tkconf.tps[i]
                    })
                }
                UI._tps = tps;
            },
            redraw: function () {
                var UI = this;
                var tsmap = {
                    'new': "新工单",
                    'assign': "已分配客服",
                    'reassign': "重新分配客服",
                    'creply': "已回复",
                    'ureply': "待处理",
                    'done': "已完成",
                    'close': "已关闭"
                };
                var stepmap = {
                    1: "新工单",
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
                stepDrops[0].checked = true;
                // 加载对象编辑器
                UI.myTicketUI = new SearchUI2({
                    parent: UI,
                    gasketName: "ticketList",
                    data: "ticket my -list '<%=match%>' -skip {{skip}} -limit {{limit}}",
                    menu: [{
                        text: "新建工单",
                        handler: function () {
                            // 采用表单方式提交新工单
                            new MaskUI({
                                dom: UI.ccode("formmask").html(),
                                i18n: UI._msg_map,
                                exec: UI.exec,
                                dom_events: {
                                    "click .srh-qform-ok": function (e) {
                                        var uiMask = ZUI(this);
                                        var fd = uiMask.body.getData();
                                        // 如果数据不符合规范，form 控件会返回空的
                                        if (fd) {
                                            fd.text = (fd.text || '').trim();
                                            if (fd.text == '') {
                                                UI.toast('请输入工单标题后再提交');
                                                return;
                                            }
                                            var posCmd = "ticket my -post -c '" + JSON.stringify(fd, null, '') + "'";
                                            console.log(posCmd);
                                            Wn.exec(posCmd, function (re) {
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
                                        title: "新建工单",
                                        fields: [{
                                            key: "text",
                                            title: "工单标题",
                                            tip: "请不要超过20个字符",
                                            dft: ''
                                        }, {
                                            key: "ticketTp",
                                            title: "工单类型",
                                            type: "string",
                                            editAs: "droplist",
                                            uiWidth: "auto",
                                            dft: UI._tps[0].value,
                                            uiConf: {items: UI._tps}
                                        }]
                                    }
                                }
                            }).render(function () {
                                // 设置默认内容
                                this.body.setData({});
                            });
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
                        tabs: stepDrops,
                        tabsPosition: "left",
                        tabsMulti: false,
                        tabsKeepChecked: true
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