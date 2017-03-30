(function ($z) {
    $z.declare(['zui', 'wn/util'], function (ZUI, Wn) {
        return ZUI.def("ui.wjson", {
            pkg: "wjson",
            css: "ui/wedit/theme/wedit-{{theme}}.css", 
            $vm: null,
            _obj: null,
            events: {
                "click .ui-wjson-save": function () {
                    this.write_obj();
                },
                "click .ui-wjson-reload": function () {
                    this.read_obj();
                }
            },
            redraw: function () {
                var UI = this;
                var opt = $.extend({
                    sjson: true,
                    sroot: false,
                    tips: {}
                }, UI.options);
                var mid = "wjson-id-" + Math.floor((Math.random() * 1000000) + 1); // $z.guid();
                UI.arena.find('.ui-wjson-main').attr('id', mid);
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
                            // UI.read_obj();
                        }
                    });
                });
            },
            // 读写方法
            write_obj: function () {
                var UI = this;
                Wn.writeObj(UI, UI._obj, UI.$vm.getContent());
            },
            read_obj: function () {
                var UI = this;
                Wn.readObj(UI, UI._obj, UI.update_content); // TODO 改成funciton 不要直接放UI中的方法
            },
            // 更新内容
            update_content: function (content) {
                var UI = this;
                if (!content) {
                    this.load_fail("对象内容为空，无法解析");
                    return;
                }
                if (typeof content == "string") {
                    UI.$vm.setContent(content);
                }
                this.update_obj();
            },
            // 更新对象信息
            update_obj: function (obj) {
                var obj = this._obj || this.app.obj;
                this.arena.find('.ui-wedit-title .ui-tt').text(obj.nm);
                this.arena.find('.ui-wedit-footer').text(obj.ph);
            },
            // 加载对象内容是吧
            load_fail: function (errMsg) {
                // TODO 更友好的提示方式
                alert(errMsg);
            },
            update: function (o) {
                this._obj = o;
                this.read_obj();
            },
            getCurrentTextContent: function () {
                return UI.$vm.getContent();
            }
        });
    });
})(window.NutzUtil);