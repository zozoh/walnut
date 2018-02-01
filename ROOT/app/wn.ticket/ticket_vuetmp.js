define(function (require, exports, module) {

    var createCID = function () {
        return "v_ticket_chat_" + $z.randomInt(1, 10000);
    };

    var ticketReply = {
        html: function (id) {
            return `
                    <div class="ticket-reply" id="` + id + `"  :class="tkStatusClass" >
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
                                        <div class="text-con">{{item.text}}
                                            <div class="atta-preview" v-for="atta in (item.attachments || [])" :class="canDownload(atta)? 'dw':''" @click="dwAtta(atta)">
                                                <img :src="attaOther(atta)" v-if="isOther(atta)"><span v-if="isOther(atta)" class="filenm">{{atta.nm}}</span>
                                                <img :src="attaImage(atta)" v-if="isImage(atta)">
                                                <video :src="attaVideo(atta)" controls v-if="isVideo(atta)">
                                            </div>
                                        <div class="chat-content-footer left">
                                            <span class="time">{{timeText(item)}}</span>
                                            <i class="fa fa-edit" @click="editContent(item, index)" v-show="isMyContent(item) && canEdit(item)"></i>
                                            <i class="fa fa-remove" @click="removeContent(item, index)" v-show="isMyContent(item)"></i>
                                        </div>
                                        <div class="chat-content-footer right">
                                            <i class="fa fa-edit" @click="editContent(item, index)" v-show="isMyContent(item) && canEdit(item)"></i>
                                            <i class="fa fa-remove" @click="removeContent(item, index)" v-show="isMyContent(item)"></i>
                                            <span class="time">{{timeText(item)}}</span>
                                        </div>
                                    </div>
                                </div>
                            </li>
                        </ul>
                        <div class="ticket-menu step2">
                            <div class="ticket-send-input">
                                <textarea name="" id="" cols="30" rows="3" placeholder="填写描述并点击'发送内容'" v-model="text"></textarea>
                            </div>
                            <div class="ticket-send-btns">
                                <button @click="sendText" class="tk-btn">发送内容</button>
                                <button @click="sendFile" class="tk-btn file">上传附件</button>
                                <button @click="sendClose" class="tk-btn close" >已解决并关闭</button>
                            </div>
                        </div>
                        <div class="ticket-menu step3">
                            <div class="ticket-send-btns">
                                <button @click="sendReopen" class="tk-btn reopen" >重新打开工单</button>
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
                sending: false,
                expmin: 30
            }, opt || {});

            return new Vue({
                el: '#' + cid,
                data: data,
                computed: {
                    menuHide: function () {
                        return this.tkStep == '3' || this.hideMenu;
                    },
                    tkId: function () {
                        return this.wobj.id;
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
                    tkStep: function () {
                        return this.wobj.ticketStep;
                    },
                    tkStatusClass: function () {
                        var self = this;
                        return {
                            'step1': self.tkStep == '1',
                            'step2': self.tkStep == '2',
                            'step3': self.tkStep == '3'
                        }
                    },
                    timeItems: function () {
                        return this.items.sort(function (a, b) {
                            return a.time > b.time;
                        });
                    }
                },
                methods: {
                    chat2bottom: function () {
                        $z.scroll2bottom($area.find('.ticket-chat'));
                    },
                    isCS: function (item) {
                        return item.csId != undefined;
                    },
                    isMyContent: function (item) {
                        if (item.stp == 'req') {
                            return this.wobj.usrId == UI.me.id;
                        }
                        if (item.stp == 'resp') {
                            return item.csId == UI.me.id;
                        }
                        return false;
                    },
                    canEdit: function (item) {
                        var atta = item.attachments || [];
                        return atta.length == 0;
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
                    dwAtta: function (atta) {
                        if (this.canDownload(atta)) {
                            window.open("/gu/id:" + atta.id);
                        }
                    },
                    canDownload: function (atta) {
                        return this.isOther(atta);
                    },
                    isOther: function (atta) {
                        return !this.isImage(atta) && !this.isVideo(atta);
                    },
                    attaOther: function (atta) {
                        return "/o/thumbnail/id:" + atta.id + "?sh=64";
                    },
                    isImage: function (atta) {
                        return atta.tp == "jpg" || atta.tp == "png" || atta.tp == "gif" || atta.tp == "jpeg";
                    },
                    attaImage: function (atta) {
                        return "/api/" + this.wobj.d1 + "/ticket/obj/read?ph=id:" + atta.id;
                    },
                    isVideo: function (atta) {
                        return atta.tp == "mp4";
                    },
                    attaVideo: function (atta) {
                        return "/api/" + this.wobj.d1 + "/ticket/obj/read?ph=id:" + atta.id;
                    },
                    // 更新工单标题
                    editTk: function () {
                        var self = this;
                        // 如果是用户或处理客服的话
                        var obj = this.wobj;
                        if (obj.usrId == UI.me.id || obj.csId == UI.me.id) {
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
                                            var posCmd = "ticket my -post '" + self.tkId + "' -m true -c '" + JSON.stringify(fd, null, '') + "'";
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
                        } else {
                            UI.alert('您无权修改该工单');
                        }
                    },
                    // 更新内容
                    refreshItems: function () {
                        var self = this;
                        // 准备数据
                        var ritems = [];
                        for (var i = 0; i < this.wobj.request.length; i++) {
                            this.wobj.request[i].stp = 'req';
                            this.wobj.request[i].sindex = i;
                        }
                        ritems = ritems.concat(this.wobj.request || []);
                        for (var i = 0; i < this.wobj.response.length; i++) {
                            this.wobj.response[i].stp = 'resp';
                            this.wobj.response[i].sindex = i;
                        }
                        ritems = ritems.concat(this.wobj.response || []);
                        this.items = ritems;
                        setTimeout(function () {
                            self.chat2bottom(); // 200ms为了让图片，视频加载完
                        }, 200);
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
                        Wn.exec("ticket my -post '" + self.tkId + "' -c 'text: \"" + stext + "\"'", function (re) {
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
                    sendReopen: function () {
                        var self = this;
                        // 如果是用户或处理客服的话
                        var obj = this.wobj;
                        if (obj.usrId == UI.me.id || obj.csId == UI.me.id) {
                            UI.confirm("该工单已被关闭，确定要重新打开继续处理吗？", {
                                ok: function () {
                                    Wn.exec("ticket my -post '" + self.tkId + "' -open true", function (re) {
                                        var re = JSON.parse(re);
                                        if (re.ok) {
                                            self.text = '';
                                            self.wobj = re.data;
                                            self.refreshItems();
                                        } else {
                                            UI.alert(re.data);
                                        }
                                    });
                                }
                            });
                        } else {
                            UI.alert('您无权修改该工单');
                        }
                    },
                    sendClose: function () {
                        var self = this;
                        UI.confirm("问题已经得到了解决，该工单将被关闭", {
                            ok: function () {
                                self.sending = true;
                                Wn.exec("ticket my -post '" + self.tkId + "' -close true", function (re) {
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
                            // validate: "^.+[.](png|jpg|jpeg|gif|mp4)$",
                            finish: function (objs) {
                                var cUI = this.parent;
                                // 执行添加命令
                                if (objs != null && objs.length > 0) {
                                    var fids = [];
                                    for (var i = 0; i < objs.length; i++) {
                                        fids.push(objs[i].id);
                                    }
                                    self.sending = true;
                                    Wn.exec("ticket my -post '" + self.tkId + "' -atta '" + fids.join(",") + "'", function (re) {
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
                    },
                    isPassTime: function (time) {
                        var ct = new Date().getTime();
                        return ct - (this.expmin * 1000 * 60) > time;
                    },
                    editContent: function (item, index) {
                        var self = this;
                        if (!self.isPassTime(item.time)) {
                            console.log("updateConent: " + item.stp + " No." + item.sindex);
                        } else {
                            UI.alert('提交已超过了' + this.expmin + "分钟，不能再做修改");
                        }
                    },
                    removeContent: function (item, index) {
                        var self = this;
                        if (!self.isPassTime(item.time)) {
                            console.log("removeConent " + item.stp + " No." + item.sindex);
                            UI.confirm("确定要删除这条内容吗？", {
                                ok: function () {
                                    Wn.exec("ticket my -post '" + self.tkId + "' -del " + item.sindex, function (re) {
                                        var re = JSON.parse(re);
                                        if (re.ok) {
                                            self.text = '';
                                            self.wobj = re.data;
                                            self.refreshItems();
                                        } else {
                                            UI.alert(re.data);
                                        }
                                    });
                                }
                            });
                        } else {
                            UI.alert('提交已超过了' + this.expmin + "分钟，不能被删除");
                        }
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
