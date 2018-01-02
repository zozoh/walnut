define(function (require, exports, module) {
    var methods = {
        tkList: function (id) {
            return `
                <div class="ticket-re-list" id="` + id + `">
                    <div class="ticket-re-item" v-for="(item, index) in timeItems" :key="item.time">
                        <div class="text">{{item.csId ? "客服":"用户"}}</div>
                        <div class="time">{{timeText(item)}}</div>
                        <div class="text">{{item.text}}</div>
                    </div>
                </div>
            `;
        }
    };
    module.exports = methods;
});
