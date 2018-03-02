define(function (require, exports, module) {

    var createCID = function () {
        return "v_ticket_chat_" + $z.randomInt(1, 10000);
    };


    // 回复框
    Vue.directive('focus', {
        update: function (el) {
            el.focus()
        }
    });

    var ticketReply = {
        html: function (id) {
            return `
                    <div class="ticket-reply" id="` + id + `"  :class="tkStatusClass" >
                        <div class="ticket-title" @click="editTk"><span class="tp">[{{tkTp}}]</span><span class="text">{{tkTitle}}</span></div>
                        <ul class="ticket-chat">
                            <li class="chat-no-record" v-show="timeItems.length == 0">暂无内容</li>
                            <li class="chat-record-wrap" v-for="(item, index) in timeItems" :key="item.time">
                                <div v-if="item.stp != 'ophis'" class="chat-record clear" :class="isCS(item)? 'cservice': ''">
                                    <div class="chat-user">
                                        <img class="chat-user-avatar" :src="userAvatar(item)" />
                                        <div class="chat-user-name">{{userName(item)}}</div>
                                    </div>
                                    <div class="chat-content">
                                        <div class="chat-content-text" :class="{edit: editIndex == index}">
                                            <div class="text-con">{{item.text}}
                                                <div class="atta-preview" v-for="atta in (item.attachments || [])" :class="canDownload(atta)? 'dw':''" @click="dwAtta(atta)">
                                                    <img :src="attaOther(atta)" v-if="isOther(atta)"><span v-if="isOther(atta)" class="filenm">{{atta.nm}}</span>
                                                    <img :src="attaImage(atta)" v-if="isImage(atta)">
                                                    <video :src="attaVideo(atta)" controls v-if="isVideo(atta)">
                                                </div>
                                                <div class="chat-content-footer left" >
                                                    <span class="time">{{timeText(item)}}</span>
                                                    <i class="fa fa-edit" @click="editContent(item, index)" v-show="isMyContent(item) && canEdit(item) && tkStep != '3'"></i>
                                                    <i class="fa fa-remove" @click="removeContent(item, index)" v-show="isMyContent(item) && tkStep != '3'"></i>
                                                </div>
                                                <div class="chat-content-footer right" >
                                                    <i class="fa fa-edit" @click="editContent(item, index)" v-show="isMyContent(item) && canEdit(item) && tkStep != '3'"></i>
                                                    <i class="fa fa-remove" @click="removeContent(item, index)" v-show="isMyContent(item) && tkStep != '3'"></i>
                                                    <span class="time">{{timeText(item)}}</span>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <div class="chat-history" v-else>
                                    <div class="his-time">{{timeText(item)}} {{item.content}}</div>
                                </div>
                            </li>
                        </ul>
                        <div class="ticket-menu step1 step2" :class="!isMyTicket()? 'cservice': 'user'">
                            <div class="ticket-send-input">
                                <div class="ticket-label-edit" @click="labelFocus()">
                                    <span class="lbl-item" v-for="(lbl, bindex) in lbls">{{lbl}}<div class="lbl-del" @click="delLabel(bindex)">x</div></span>
                                    <span class="lbl-input">
                                        <input type="text" v-model="label" @change="addLabel" spellcheck="false">
                                    </span>
                                    <span class="lbl-placeholder" v-show="lbls.length == 0 && label.trim() == ''">请输入标签</span>
                                </div>
                                <textarea class="ticket-text-edit" cols="30" rows="3" placeholder="填写描述并点击'发送内容'" v-model="text"></textarea>
                            </div>
                            <div class="ticket-send-btns">
                                <button @click="sendText" class="tk-btn">{{editing? "更新内容": "发送内容"}}</button>
                                <button @click="sendFile" class="tk-btn file" v-show="!editing">上传附件</button>
                                <button @click="sendClose" class="tk-btn close" v-show="!editing">已解决并关闭</button>
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
                label: "",
                lbls: [],
                sending: false,
                editing: false,
                editIndex: -1,
                editSndex: -1,
                expday: 2,
                expmin: 30,
                focusLabel: false,
                focusStatus: false
            }, opt || {});

            return new Vue({
                el: '#' + cid,
                data: data,
                computed: {
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
                watch: {
                    editing: function (val) {
                        if (val == false) {
                            this.editIndex = -1;
                            this.editSndex = -1;
                        }
                    },
                    label: function (val) {
                        if (val.indexOf(' ') != -1) {
                            this.addLabel();
                        }
                    }
                },
                methods: {
                    delLabel: function (bindex) {
                        this.lbls.splice(bindex, 1);
                        this.updatelbls();
                    },
                    addLabel: function () {
                        var clbl = this.label.trim();
                        var clblarr = clbl.split(/\s+/);
                        if (clblarr.length == 1) {
                            this.lbls.push(clbl);
                        } else {
                            this.lbls = this.lbls.concat(clblarr);
                        }
                        this.label = '';
                        this.updatelbls();
                    },
                    updatelbls: function () {
                        var self = this;
                        // 推送到后台
                        var lblstr = this.lbls.join("','");
                        if (lblstr != '') {
                            lblstr = "'" + lblstr + "'";
                        }
                        var posCmd = "ticket my -post '" + self.tkId + "' -m true -c \"lbls:[" + lblstr + "]\"";
                        console.log(posCmd);
                        Wn.exec(posCmd, function (re) {
                            var re = JSON.parse(re);
                            if (re.ok) {
                                // nothing
                            } else {
                                UI.alert(re.data);
                            }
                        });
                    },
                    labelFocus: function () {
                        $area.find('.lbl-input input').focus();
                    },
                    chat2bottom: function () {
                        $z.scroll2bottom($area.find('.ticket-chat'));
                    },
                    isCS: function (item) {
                        return item.csId != undefined;
                    },
                    isMyTicket: function () {
                        return this.wobj.usrId == UI.me.id;
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
                        } else {
                            if (item.usrAlias) {
                                return item.usrAlias;
                            } else {
                                if (this.wobj.usrAlias) {
                                    return this.wobj.usrAlias;
                                } else {
                                    return "用户" + this.wobj.usrId.substr(0, 4);
                                }
                            }
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
                        this.wobj.request = this.wobj.request || [];
                        this.wobj.response = this.wobj.response || [];
                        // 准备数据
                        var ritems = [];
                        for (var i = 0; i < this.wobj.request.length; i++) {
                            this.wobj.request[i].stp = 'req';
                            this.wobj.request[i].sindex = i;
                        }
                        ritems = ritems.concat(this.wobj.request);
                        for (var i = 0; i < this.wobj.response.length; i++) {
                            this.wobj.response[i].stp = 'resp';
                            this.wobj.response[i].sindex = i;
                        }
                        ritems = ritems.concat(this.wobj.response);
                        // history
                        ritems = ritems.concat(this.wobj.history || []);
                        this.items = ritems;
                        this.lbls = this.wobj.lbls || [];
                        setTimeout(function () {
                            self.chat2bottom(); // 200ms为了让图片，视频加载完
                        }, 200);
                    },
                    filterStr: function (str) {
                        var pattern = new RegExp("[\"'`~!@#$^&*()=|{}':;',\\[\\].<>/?~！@#￥……&*（）——|{}【】‘；：”“'。，、？%+_]");
                        var result = "";
                        var len = str.length;
                        for (var i = 0; i < len; i++) {
                            var cCode = str.charAt(i);
                            if (pattern.test(cCode)) {
                                console.log("fcode: " + cCode);
                                cCode = this.toDBC(str.charCodeAt(i));
                            }
                            result += cCode;
                        }
                        return result;
                    },
                    alltoDBC: function (str) {
                        var result = "";
                        var len = str.length;
                        for (var i = 0; i < len; i++) {
                            var cCode = str.charCodeAt(i);
                            result += this.toDBC(cCode);
                        }
                        return result;
                    },
                    toDBC: function (cCode) {
                        //全角与半角相差（除空格外）：65248(十进制)
                        cCode = (cCode >= 0x0021 && cCode <= 0x007E) ? (cCode + 65248) : cCode;
                        //处理空格
                        cCode = (cCode == 0x0020) ? 0x03000 : cCode;
                        return String.fromCharCode(cCode);
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
                        // 替换里面的非法字符
                        stext = self.filterStr(stext);
                        self.sending = true;
                        Wn.exec("ticket my -post '" + self.tkId + "' -c 'text: \"" + stext + "\"' -edit " + self.editSndex, function (re) {
                            var re = JSON.parse(re);
                            if (re.ok) {
                                if (self.editing) {
                                    self.editing = false;
                                }
                                self.text = '';
                                self.wobj = re.data;
                                self.focusStatus = false;
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
                        // return ct - (this.expmin * 1000 * 60) > time;
                        return ct - (this.expday * 24 * 60 * 1000 * 60) > time;
                    },
                    editContent: function (item, index) {
                        var self = this;
                        if (!self.isPassTime(item.time)) {
                            console.log("updateConent: " + item.stp + " No." + item.sindex);
                            self.editIndex = index;
                            self.editSndex = item.sindex;
                            self.editing = true;
                            self.text = item.text;
                            self.focusStatus = true;
                        } else {
                            UI.alert('提交已超过了' + this.expday + "天，不能再做修改");
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
                            UI.alert('提交已超过了' + this.expday + "天，不能被删除");
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


    // 页面通知
    window._ticket_noti_ = window._ticket_noti_ || {};
    var ticketNoti = {
        myWS: function (UI, nid) {
            var wscon = window._ticket_noti_[nid];
            if (!wscon) {
                var WS_URL = window.location.host + "/websocket"
                if (location.protocol == 'http:') {
                    WS_URL = "ws://" + WS_URL;
                } else {
                    WS_URL = "wss://" + WS_URL;
                }
                var sock = new WebSocket(WS_URL);
                sock.isAlive = true;
                sock.onopen = function () {
                    console.log('sock-open');
                    var initCmd = JSON.stringify({
                        method: 'watch',
                        user: _app.session.me,
                        match: {id: nid}
                    });
                    console.log("initCmd: " + initCmd);
                    sock.send(initCmd);
                    var sockInt = setInterval(function () {
                        if (sock.isAlive) {
                            sock.send(JSON.stringify({
                                hi: nid + ", I'm live"
                            }));
                        } else {
                            clearInterval(sockInt);
                        }
                    }, 1000 * 60);
                };
                sock.onmessage = function (e) {
                    var content = e.data;
                    console.log('sock-message', content);
                    var re = eval('(' + content + ')');
                    if (re.action == 'noti') {
                        var rid = re.rid;
                        // 桌面通知
                        UI.notification({
                            title: re.title,
                            onclick: function () {
                                // 实现直接打开工单
                                event.preventDefault();
                                var openUrl = window.location.origin + window.location.pathname + "?" + re.link;
                                console.log('open:' + openUrl);
                                window.open(openUrl, '_blank');
                                this.close();
                            }
                        });
                        // 闪标题
                        $z.changeWindowTitle(re.title, true);
                    }
                };
                sock.onclose = function () {
                    console.log('sock-close');
                    sock.isAlive = false;
                    window._ticket_noti_[nid] = null;
                };
                wscon = sock;
            }
            return wscon;
        }
    };

    var methods = {
        ticketReply: ticketReply,
        ticketNoti: ticketNoti
    };
    module.exports = methods;
});
