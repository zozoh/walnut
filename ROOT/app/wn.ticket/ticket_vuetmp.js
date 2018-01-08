define(function (require, exports, module) {

    var chat = {
        html: function (id) {
            return `
                    <ul class="ticket-chat" id="` + id + `">
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
                `;
        },
        create: function ($area, wobj) {
            var cid = "v_ticket_chat_" + $z.randomInt(1000, 10000);
            var html = chat.html(cid);
            $area.html(html);

            // 准备数据
            var ritems = [];
            ritems = ritems.concat(wobj.request || []);
            ritems = ritems.concat(wobj.response || []);

            return new Vue({
                el: '#' + cid,
                data: {
                    wobj: wobj,
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
                    isCS: function (item) {
                        return item.csId != undefined;
                    },
                    timeText: function (item) {
                        return $z.currentTime(item.time)
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
                    }
                },
                mounted: function () {
                    console.log("v_ticket_chat is ready");
                }
            });
        }
    };


    var methods = {
        chat: chat
    };
    module.exports = methods;
});
