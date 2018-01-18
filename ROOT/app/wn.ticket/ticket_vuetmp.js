define(function (require, exports, module) {

    var createCID = function () {
        return "v_ticket_chat_" + $z.randomInt(1, 10000);
    };

    var ticketReply = {
        html: function (id) {
            return `
                    <div class="ticket-reply" id="` + id + `"  :class="menuHide ? 'hide-menu': ''" >
                        <div class="ticket-title" @click="editTk"><span class="tp">[{{tkTp}}]</span><span class="text">{{tkTitle}}</span></div>
                        <ul class="ticket-chat">
                            <li class="chat-no-record" v-show="timeItems.length == 0">暂无内容</li>
                            <li class="chat-record clear" :class="isCS(item)? 'cservice': ''" v-for="(item, index) in timeItems" :key="item.time">
                                <div class="chat-user">
                                    <img class="chat-user-avatar" :src="userAvatar(item)" />
                                    <div class="chat-user-name">{{userName(item)}}</div>
                                </div>
                                <div class="chat-content">
                                    <div class="chat-content-text">
                                        <div class="text-con">{{item.text}}<img class="atta-image" :src="attaImage(afid)" v-for="afid in (item.attachments || [])"></div>
                                        <div class="chat-content-time">{{timeText(item)}}</div>
                                    </div>
                                </div>
                            </li>
                        </ul>
                        <div class="ticket-menu">
                            <div class="ticket-send-input">
                                <textarea name="" id="" cols="30" rows="3" placeholder="填写描述并点击'发送内容'" v-model="text"></textarea>
                            </div>
                            <div class="ticket-send-btns">
                                <button @click="sendText">发送内容</button>
                                <button @click="sendFile" class="file">上传附件</button>
                                <button @click="sendClose" class="close" >已解决并关闭</button>
                            </div>
                        </div>
                    </div>
                `;
        },
        create: function (UI, $area, wobj, opt) {
            var cid = createCID();
            var html = ticketReply.html(cid);
            $area.html(html);

            var data = $.extend({
                wobj: wobj,
                items: [],
                // 发送相关
                text: "",
                sending: false
            }, opt || {});

            return new Vue({
                el: '#' + cid,
                data: data,
                computed: {
                    menuHide: function () {
                        return this.wobj.ticketStep == '3' || this.hideMenu;
                    },
                    tkTp: function () {
                        return this.wobj.ticketTp;
                    },
                    tkTitle: function () {
                        var otext = this.wobj.text;
                        if (otext.length > 20) {
                            return otext.substr(0, 20) + "..."
                        }
                        return otext;
                    },
                    timeItems: function () {
                        return this.items.sort(function (a, b) {
                            return a.time > b.time;
                        });
                    },
                    ticketId: function () {
                        return this.wobj.id;
                    }
                },
                methods: {
                    isCS: function (item) {
                        return item.csId != undefined;
                    },
                    timeText: function (item) {
                        return $z.currentTime(item.time).substr(0, 16);
                    },
                    userAvatar: function (item) {
                        if (this.isCS(item)) {
                            return "/u/avatar/usr?nm=id:" + item.csId;
                        }
                        return "/u/avatar/usr?nm=id:" + wobj.usrId;
                    },
                    userName: function (item) {
                        if (this.isCS(item)) {
                            return item.csAlias;
                        }
                        return "用户";
                    },
                    attaImage: function (fid) {
                        return "/api/" + this.wobj.d1 + "/ticket/obj/read?ph=id:" + fid;
                    },
                    // 更新工单标题
                    editTk: function () {
                        var self = this;
                        var obj = this.wobj;
                        var MaskUI = require("ui/mask/mask");
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
                                        var posCmd = "ticket my -post '" + self.ticketId + "' -m true -c '" + JSON.stringify(fd, null, '') + "'";
                                        console.log(posCmd);
                                        Wn.exec(posCmd, function (re) {
                                            var re = JSON.parse(re);
                                            if (re.ok) {
                                                self.wobj = re.data;
                                                self.refreshItems();
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
                                    title: "更新工单",
                                    fields: [{
                                        key: "text",
                                        title: "工单标题",
                                        tip: "请不要超过20个字符",
                                    }, {
                                        key: "ticketTp",
                                        title: "工单类型",
                                        type: "string",
                                        editAs: "droplist",
                                        uiWidth: "auto",
                                        uiConf: {items: UI._tps}
                                    }]
                                }
                            }
                        }).render(function () {
                            // 设置默认内容
                            this.body.setData({
                                text: obj.text,
                                ticketTp: obj.ticketTp
                            });
                        });
                    },
                    // 更新内容
                    refreshItems: function () {
                        // 准备数据
                        var ritems = [];
                        ritems = ritems.concat(this.wobj.request || []);
                        ritems = ritems.concat(this.wobj.response || []);
                        this.items = ritems;
                    },
                    // 发送内容
                    sendText: function () {
                        var self = this;
                        var stext = this.text;
                        stext = stext.trim();
                        if (stext == '') {
                            UI.toast("写点内容再提交吧");
                            return;
                        }
                        self.sending = true;
                        Wn.exec("ticket my -post '" + self.ticketId + "' -c 'text: \"" + stext + "\"'", function (re) {
                            var re = JSON.parse(re);
                            if (re.ok) {
                                self.text = '';
                                self.wobj = re.data;
                                self.refreshItems();
                            } else {
                                UI.alert(re.data);
                            }
                            self.sending = false;
                        });
                    },
                    sendClose: function () {
                        var self = this;
                        UI.confirm("问题已经得到了解决，该工单将被关闭", {
                            ok: function () {
                                self.sending = true;
                                Wn.exec("ticket my -post '" + self.ticketId + "' -c 'finish: true'", function (re) {
                                    var re = JSON.parse(re);
                                    if (re.ok) {
                                        self.text = '';
                                        self.wobj = re.data;
                                        self.refreshItems();
                                        UI.close();
                                    } else {
                                        UI.alert(re.data);
                                    }
                                    self.sending = false;
                                });
                            }
                        });
                    },
                    sendFile: function () {
                        var self = this;
                        Wn.uploadPanel({
                            title: "附件上传(仅支持图片)",
                            parent: UI,
                            target: {
                                ph: "~/.ticket_upload",
                                race: "DIR"
                            },
                            validate: "^.+[.](png|jpg|jpeg|gif)$",
                            finish: function (objs) {
                                var cUI = this.parent;
                                // 执行添加命令
                                if (objs != null && objs.length > 0) {
                                    var fids = [];
                                    for (var i = 0; i < objs.length; i++) {
                                        fids.push(objs[i].id);
                                    }
                                    self.sending = true;
                                    Wn.exec("ticket my -post '" + self.ticketId + "' -atta '" + fids.join(",") + "'", function (re) {
                                        var re = JSON.parse(re);
                                        if (re.ok) {
                                            self.text = '';
                                            self.wobj = re.data;
                                            self.refreshItems();
                                            cUI.close();
                                        } else {
                                            UI.alert(re.data);
                                        }
                                        self.sending = false;
                                    });
                                }
                            },
                            replaceable: true
                        });
                    }
                },
                mounted: function () {
                    console.log("ticket_reply is ready");
                    this.refreshItems();
                },
                destroyed: function () {
                    console.log("ticket_reply is close");
                }
            });
        }
    };


    var methods = {
        ticketReply: ticketReply
    };
    module.exports = methods;
});
