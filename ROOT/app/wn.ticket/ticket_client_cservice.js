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
        return ZUI.def("app.wn.ticket.client.cservice", {
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
                    width: "75%",
                    height: "80%",
                    closer: true,
                    on_close: function () {
                        this.treply.$destroy();
                        UI.myTicketUI.refresh();
                    }
                }).render(function () {
                    var $main = this.$el.find('.ui-mask-main');
                    this.treply = TkTmp.ticketReply.create(UI, $main, obj, {
                        hideMenu: obj.ticketStep == '1'
                    });
                });
            },
            init: function () {
                var UI = this;
                // 获取所有客服信息
                UI.cslist = JSON.parse(Wn.exec("ticket my -cservice")) || [];
                // 获取我的信息
                UI.me = JSON.parse(Wn.exec("me -json"));
                // 通知
                UI.lconf = Wn.execJ("ticket my -conf");
                UI.notiObj = UI.lconf.notiObj || null;
                if (UI.notiObj) {
                    UI.myWS = TkTmp.ticketNoti.myWS(UI, UI.notiObj);
                }
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
                    2: "我在处理",
                    3: "我已完成"
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
                stepDrops[0].value.mode = "search";  // 新工单要查全局 其他的只查自己
                // 加载对象编辑器
                UI.myTicketUI = new SearchUI2({
                    parent: UI,
                    gasketName: "ticketList",
                    data: "ticket my -list '<%=match%>' -skip {{skip}} -limit {{limit}}",
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
                            key: "id",
                            title: "工单ID",
                            uiType: '@label',
                            display: function (o) {
                                return o.id.substr(0, 10).toUpperCase();
                            }
                        }, {
                            key: "usrAlias",
                            title: "用户",
                            uiType: '@label',
                            display: function (o) {
                                return o.usrAlias || "用户" + o.usrId.substr(0, 4);
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
                            sizeHint: [100, 100, 80, 100, '*', 200, 150]
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
                var UI = this;
                UI.gasket.ticketList.refresh();
                // 检查url是否有 ::rid=
                var hash = window.location.hash;
                if (hash.indexOf('::rid=') != -1) {
                    var rid = hash.substr(hash.indexOf('::rid=') + 6);
                    console.log("find rid: " + rid);
                    var robj = Wn.execJ("obj id:" + rid);
                    UI.showTicketChat(robj);
                }
            }
            //..............................................
        });
//==================================================
    });
})(window.NutzUtil);