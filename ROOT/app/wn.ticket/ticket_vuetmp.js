define(function (require, exports, module) {

    var createCID = function () {
        return "v_ticket_chat_" + $z.randomInt(1, 10000);
    };

    var ticketReply = {
        html: function (id) {
            return `
                    <div class="ticket-reply" id="` + id + `"  :class="menuHide ? 'hide-menu': ''" >
                        <div class="ticket-title"><span class="text">{{tkTitle}}</span></div>
                        <ul class="ticket-chat">
                            <li class="chat-no-record" v-show="timeItems.length == 0">暂无内容</li>
                            <li class="chat-record clear" :class="isCS(item)? 'cservice': ''" v-for="(item, index) in timeItems" :key="item.time">
                                <div class="chat-user">
                                    <img class="chat-user-avatar" :src="userAvatar(item)" />
                                    <div class="chat-user-name">{{userName(item)}}</div>
                                </div>
                                <div class="chat-content">
                                    <div class="chat-content-text">
                                        <span>{{item.text}}</span>
                                    </div>
                                </div>
                                <div class="chat-content-time">{{timeText(item)}}</div>
                            </li>
                        </ul>
                        <div class="ticket-menu">
                            <div class="ticket-send-input">
                                <textarea name="" id="" cols="30" rows="3" placeholder="填写描述并点击'发送内容'，附件请拖拽至该窗口" v-model="text"></textarea>
                            </div>
                            <div class="ticket-send-btns">
                                <button @click="sendText">发送内容</button>
                                <button class="close" @click="sendClose">已解决并关闭</button>
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
                        return this.wobj.ticketStatus == 'done' || this.hideMenu;
                    },
                    tkTitle: function () {
                        var otext = this.wobj.text;
                        if (otext.length > 15) {
                            return otext.substr(0, 10) + "..."
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
                        return $z.parseDate(item.time).format("yyyy-MM-dd HH:mm");
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
