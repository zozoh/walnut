(function ($z) {
    $z.declare([
        'zui',
        'wn/util'
    ], function (ZUI, Wn) {
//==============================================
        var html = `
         <div class="ui-arena ojson" ui-fitparent="yes">
            <ui-wjson-editor :sjson="conf.sjson" :sroot="conf.sroot" :tips="conf.tips" v-ref:wjson></ui-wjson-editor>
         </div>
        `;
//==============================================
        return ZUI.def("ui.o_edit_json", {
            dom: html,
            css: "ui/o_edit_json/o_edit_json.css",
            $vm: null,
            cobj: null,
            redraw: function () {
                var UI = this;
                var opt = $.extend({
                    sjson: true,
                    sroot: false,
                    tips: {}
                }, UI.options);
                var mid = "ojson-id-" + Math.floor((Math.random() * 1000000) + 1); // $z.guid();
                UI.arena.attr('id', mid);
                // 使用vjson
                seajs.use(["vue-component/vjson/vjson.js", "vue-component/vjson/vjson.css"], function () {
                    UI.$vm = new Vue({
                        el: "#" + mid,
                        data: {
                            conf: opt
                        },
                        methods: {
                            setContent: function (content) {
                                var rootJson = null;
                                try {
                                    rootJson = $z.fromJson(content);
                                } catch (e) {
                                    // 提示这不是一个json格式的文本
                                    UI.load_fail("对象内容非json格式，解析失败");
                                    rootJson = {};
                                }
                                this.$refs.wjson.setRoot(rootJson);
                            },
                            getContent: function () {
                                return this.$refs.wjson.getContent();
                            }
                        },
                        ready: function () {
                            // 读取对象
                            UI.defer_report("vjson");
                        }
                    });
                });
                // 延迟加载
                return ["vjson"];
            },
            load_fail: function (errMsg) {
                alert(errMsg);
            },
            //...............................................................
            update: function (o) {
                var UI = this;
                UI.cobj = o;
                Wn.read(o, function (content) {
                    UI.$vm.setContent(content);
                });
            },
            //...............................................................
            getCurrentEditObj: function () {
                return this.cobj;
            },
            //...............................................................
            getCurrentTextContent: function () {
                return this.$vm.getContent();
            }
            //...............................................................
        });
//===================================================================
    });
})(window.NutzUtil);