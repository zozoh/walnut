(function ($z) {
    $z.declare(['zui', 'wn/util'], function (ZUI, Wn) {
        return ZUI.def("ui.wjson", {
            pkg: "wjson",
            vue: true,
            css: "ui/wedit/wedit.css", // 依赖wedit的css
            $wj_main: null,
            $wj_vm: null,
            events: {
                "click .ui-wjson-save": function () {
                    this.write_obj();
                },
                "click .ui-wjson-reload": function () {
                    this.read_obj();
                }
            },
            redraw: function () {
                this.$wj_main = this.arena.find('.ui-wjson-main');
                var mid = "wjson-id-" + Math.floor((Math.random() * 1000000) + 1); // $z.guid();
                this.$wj_main.attr('id', mid);
                // 注册控件
                Vue.directive('wjson-tn-edit', {
                    twoWay: true,
                    bind: function () {
                        this.handler = function () {
                            this.set(this.el.innerHTML)
                        }.bind(this);
                        this.el.addEventListener('keyup', this.handler)
                    },
                    update: function (newValue, oldValue) {
                        this.el.innerHTML = newValue || ''
                    },
                    unbind: function () {
                        this.el.removeEventListener('keyup', this.handler)
                    }
                });
                Vue.component('ui-wjson-treenode', {
                    template: '#ui-wjson-template-treenode',
                    props: {
                        ptype: String,
                        pindex: Number,
                        model: Object,
                        qkey: String,
                        depth: 0,
                        sroot: true,
                    },
                    data: function () {
                        return {
                            menuVisiable: false,
                            typeVisiable: false,
                            emptyModel: {
                                name: "",
                                value: "",
                                type: 'empty',
                                dupkey: false
                            }
                        }
                    },
                    computed: {
                        showTn: function () {
                            if (!this.isRootTn) {
                                return true;
                            }
                            return this.sroot;
                        },
                        isRootTn: function () {
                            return this.depth == 0;
                        },
                        isEmptyTn: function () {
                            return this.model && this.model.type == "empty";
                        },
                        emptyTn: function () {
                            if (this.ptype == "array") {
                                return "[ empty array ]";
                            }
                            return "{ empty object }";
                        },
                        isFolder: function () {
                            return _.isArray(this.model.children);
                        },
                        isObject: function () {
                            return this.model.type == "object";
                        },
                        isArray: function () {
                            return this.model.type == "array";
                        },
                        isBoolean: function () {
                            return this.model.type == "boolean";
                        },
                        isNumber: function () {
                            return this.model.type == "number";
                        },
                        isString: function () {
                            return this.model.type == "string";
                        },
                        isNull: function () {
                            return this.model.type == "null";
                        },
                        keyEmpty: function () {
                            return !this.model.name;
                        },
                        valEmpty: function () {
                            return !this.model.value;
                        },
                        length: function () {
                            if (this.model.type == "object") {
                                return "{" + this.model.children.length + "}";
                            } else {
                                return "[" + this.model.children.length + "]";
                            }
                        },
                        hasValue: function () {
                            if (this.model && (this.model.value != null || this.model.value != undefined || this.model.value != "")) {
                                return true;
                            }
                            return false;
                        },
                        matchKey: function () {
                            if (!this.model.name || this.qkey == "") {
                                return false;
                            }
                            return this.model.name.indexOf(this.qkey) != -1;
                        },
                        matchVal: function () {
                            if (!this.model.value || this.qkey == "" || !this.hasValue) {
                                return false;
                            }
                            return ("" + this.model.value).indexOf(this.qkey) != -1;
                        }
                    },
                    watch: {
                        'model.value': function (val) {
                            this.model.type = this.valType(val);
                        },
                        'model.name': function (val) {
                            // 父节点检查相邻节点是否有同名的问题
                            this.$parent.checkChildren();
                        }
                    },
                    events: {},
                    methods: {
                        showMenu: function () {
                            this.menuVisiable = true;
                        },
                        hideMenu: function () {
                            this.menuVisiable = false;
                            this.typeVisiable = false;
                        },
                        expandTypes: function () {
                            this.typeVisiable = !this.typeVisiable;
                        },
                        checkChildren: function () {
                            var nmMap = {};
                            this.model.children.forEach(function (ele, index) {
                                ele.dupkey = false;
                                var enm = ele.name;
                                if (nmMap[enm]) {
                                    // 添加提示
                                    nmMap[enm].dupkey = true;
                                    ele.dupkey = true;
                                } else {
                                    nmMap[enm] = ele;
                                }
                            });
                        },
                        convertTn: function (ttp) {
                            var self = this;
                            var ctp = this.model.type;
                            if (ctp == ttp) {
                                return;
                            }
                            // 注意：所有value最后都是字符串类型的，字面量类型的
                            // 转换为数组或对象
                            if (ttp == "array" || ttp == 'object') {
                                this.model.open = true;
                                this.model.value = null;
                                // obj可以转换为数组
                                if ((ttp == "array" && ctp == "object") || (ttp == "object" && ctp == "array")) {
                                    this.model.children.forEach(function (ele, index, array) {
                                        ele.name = "" + index;
                                    });
                                } else {
                                    this.$set("model.children", []);
                                }
                            }
                            // 转换为基本类型
                            else {
                                this.model.open = false;
                                this.$set("model.children", null);
                                var bval = this.model.value;
                                // 字符串
                                if (ttp == "string") {
                                    if (ctp == "number" || ctp == "boolean") {
                                        this.model.value = "" + bval;
                                    } else {
                                        this.model.value = "";
                                    }
                                }
                                // 数字
                                else if (ttp == "number") {
                                    if (ctp == "string" && (/^-?[1-9]\d*$/.test(bval) || /^-?([1-9]\d*.\d+|0.\d+|0?.0+|0)$/.test(bval))) {
                                        // 不需要改
                                    }
                                    else {
                                        this.model.value = "0";
                                    }
                                }
                                // 布尔
                                else if (ttp == "boolean") {
                                    if (ctp == "string" && (bval == "true" || bval == "True")) {
                                        this.model.value = "true";
                                    } else {
                                        this.model.value = "false";
                                    }
                                }
                                // null
                                else if (ttp == "null") {
                                    this.model.value = "";
                                }
                            }
                            // 隐藏菜单
                            this.hideMenu();
                            // 检车value会修改type，所以需要强制设置一下
                            setTimeout(function () {
                                self.model.type = ttp;
                            }, 0);
                        },
                        toggle: function () {
                            if (this.isFolder) {
                                this.model.open = !this.model.open;
                            }
                        },
                        valType: function (val) {
                            var vt = "unknow";
                            if (val == "") {
                                vt = "null";
                            }
                            // 判断bool
                            else if (val == "true" || val == "false" || val == "True" || val == "False") {
                                vt = "boolean";
                            }
                            // 整数
                            else if (/^-?[1-9]\d*$/.test(val)) {
                                vt = "number";
                            }
                            // 浮点数
                            else if (/^-?([1-9]\d*.\d+|0.\d+|0?.0+|0)$/.test(val)) {
                                vt = "number";
                            }
                            // 字符串
                            else if (_.isString(val)) {
                                vt = "string";
                            }
                            return vt;
                        },
                        addItem: function () {
                            this.$parent.addChildren(this.pindex);
                            this.hideMenu();
                        },
                        appendItem: function () {
                            this.$parent.addChildren(this.pindex + 1);
                            this.hideMenu();
                        },
                        copyItem: function () {
                            this.$parent.addChildren(this.pindex + 1, JSON.parse(JSON.stringify(this.model)));
                            this.hideMenu();
                        },
                        addChildren: function (i, child) {
                            console.log("parent [" + this.model.name + "] add children [" + i + "]");
                            this.model.children.splice(i, 0, child || {
                                    name: "",
                                    value: "",
                                    type: "string",
                                    open: true,
                                    dupkey: false
                                });
                            this.checkChildren();
                        },
                        cleanChildren: function () {
                            this.$set('model.children', []);
                            this.hideMenu();
                        },
                        deleteItem: function () {
                            this.$parent.deleteChildren(this.model);
                            this.hideMenu();
                        },
                        deleteChildren: function (child) {
                            console.log("parent [" + this.model.name + "] delete children [" + child.name + "]");
                            this.model.children.$remove(child);
                        }
                    }
                });
                // 初始化页面
                this.$wj_vm = new Vue({
                    el: "#" + mid,
                    data: {
                        // 显示相关
                        showJson: true,
                        showRoot: false,
                        compress: false,
                        sindex: 0,
                        jindex: 0,
                        qkey: "",
                        // 内容相关
                        json: {
                            root: {},
                            tree: {
                                name: "root",
                                type: "object",
                                value: null,
                                children: [],
                                open: true,
                                dupkey: false
                            }
                        },
                        source: {
                            lines: [],
                            content: ""
                        }
                    },
                    computed: {
                        jtab: function () {
                            return this.compress ? 0 : 2;
                        }
                    },
                    watch: {
                        "json.root": function (val) {
                            // 转换为字符串
                            this.source.content = $z.toJson(val, null, this.jtab);
                            // 更新tree
                            this.json.tree = this.obj2tree(val);
                            // console.log("### JSON ###\n" + $z.toJson(val, null, 2) + "\n");
                            console.log("### TREE ###\n" + $z.toJson(this.json.tree, null, 2) + "\n");
                        },
                        "source.content": function (val) {
                            // TODO 按照 key, value等进行处理， 可以高亮一些一些内容
                            var lines = val.replace(/ /g, "&nbsp;").split("\n");
                            // 最终插入到lines中为html元素
                            this.$set("source.lines", lines);
                        }
                    },
                    methods: {
                        // 对象与显示用的tree相互转换
                        obj2tree: function (obj) {
                            var self = this;
                            var tree = {
                                name: "root",
                                value: null,
                                type: self.valType(obj),
                                open: true,
                                children: [],
                                dupkey: false
                            };
                            if (_.isObject(obj) || _.isArray(obj)) {
                                self.js2properties(tree, obj);
                            }
                            else {
                                throw "Not a Json Object";
                            }
                            return tree;
                        },
                        tree2obj: function () {
                            var self = this;
                            var troot = this.json.tree;
                            var objw = {};
                            var isOk = this.tn2js("", objw, true, troot);
                            if (isOk) {
                                return objw;
                            }
                            return null;
                        },
                        tn2js: function (path, parent, isObj, tn) {
                            var self = this;
                            var name = tn.name;
                            var tval = tn.value;
                            var value = null;
                            if (tn.dupkey) {
                                alert("对象路径：" + path + "下有重复的键[" + tn.name + "]，请检查");
                                return false;
                            }
                            if (name.trim() == "") {
                                alert("对象路径：" + path + "下有空键，请检查");
                                return false;
                            }
                            // 根据类型转换
                            if (tn.type == "object" || tn.type == "array") {
                                if (tn.type == "object") {
                                    value = {};
                                } else {
                                    value = [];
                                }
                                var herr = false;
                                tn.children.forEach(function (ele, index) {
                                    if (!self.tn2js(path + "/" + name, value, true, ele)) {
                                        herr = true;
                                        return false;
                                    }
                                });
                                if (herr) {
                                    return false;
                                }
                            }
                            else if (tn.type == "null") {
                                value = null;
                            }
                            else if (tn.type == "string") {
                                value = tn.value;
                            }
                            else if (tn.type == "number") {
                                if (/^-?[1-9]\d*$/.test(tval)) {
                                    value = parseInt(tval);
                                }
                                // 浮点数
                                else {
                                    value = parseFloat(tval);
                                }
                            }
                            else if (tn.type == "boolean") {
                                if (tval == "true" || tval == "True") {
                                    value = true;
                                } else {
                                    value = false;
                                }
                            }
                            if (isObj) {
                                parent[name] = value;
                            } else {
                                parent[parseInt(name)] = value;
                            }
                            return true;
                        },
                        // js转换为treenode
                        js2tn: function (parent, key, val) {
                            var self = this;
                            var treeNode = {
                                name: key,
                                value: null,
                                type: self.valType(val),
                                open: true,
                                dupkey: false
                            };
                            if (_.isObject(val) || _.isArray(val)) {
                                treeNode.children = [];
                                self.js2properties(treeNode, val);
                            } else {
                                treeNode.value = val;
                            }
                            // 加入到父节点中
                            if (parent.children) {
                                parent.children.push(treeNode);
                            } else {
                                parent.children = [treeNode];
                            }
                        },
                        // 遍历一个js对象或数组
                        js2properties: function (parent, obj) {
                            var self = this;
                            var key, val;
                            if (_.isObject(obj)) {
                                for (key in obj) {
                                    val = obj[key];
                                    self.js2tn(parent, key, val);
                                }
                            } else {
                                for (key = 0; key < obj.length; key++) {
                                    val = obj[key];
                                    self.js2tn(parent, key, val);
                                }
                            }
                        },
                        valType: function (val) {
                            var vt = "unknow";
                            if (_.isNull(val)) {
                                vt = "null";
                            }
                            else if (_.isUndefined(val)) {
                                vt = "undefined";
                            }
                            else if (_.isBoolean(val)) {
                                vt = "boolean";
                            }
                            else if (_.isNumber(val)) {
                                vt = "number";
                            }
                            else if (_.isString(val)) {
                                vt = "string";
                            }
                            else if (_.isArray(val)) {
                                vt = "array";
                            }
                            else if (_.isObject(val)) {
                                vt = "object";
                            }
                            return vt;
                        },
                        // 更新content
                        updateContent: function () {
                            // 获取最新的的json对象
                            var njson = this.tree2obj();
                            if (njson) {
                                this.source.content = $z.toJson(njson.root, null, this.jtab); // root节点
                                return true;
                            } else {
                                return false;
                            }
                        },
                        // 切换编辑器
                        toogleEditor: function () {
                            if (this.showJson) {
                                if (!this.updateContent()) {
                                    return;
                                }
                            }
                            this.showJson = !this.showJson;
                        },
                        toogleCompress: function () {
                            this.compress = !this.compress;
                            this.source.content = $z.toJson($z.fromJson(this.source.content), null, this.jtab);
                        },
                        // 全部展开
                        expandTree: function (tn) {
                            var self = this;
                            var children = tn.children;
                            if (children) {
                                tn.open = true;
                                for (var i = 0; i < children.length; i++) {
                                    var child = tn.children[i];
                                    self.expandTree(child);
                                }
                            }
                        },
                        // 全部折叠
                        collapseTree: function (tn, isRoot) {
                            var self = this;
                            var children = tn.children;
                            if (children) {
                                tn.open = false;
                                if (isRoot && !this.showRoot) {
                                    tn.open = true;
                                }
                                for (var i = 0; i < children.length; i++) {
                                    var child = tn.children[i];
                                    self.collapseTree(child);
                                }
                            }
                        }

                    }
                });
                // 读取对象
                this.read_obj();
            },
            // 读写方法
            write_obj: function () {
                var UI = this;
                this.$wj_vm.updateContent();
                Wn.writeObj(UI, null, this.$wj_vm.source.content);
            },
            read_obj: function () {
                var UI = this;
                Wn.readObj(UI, null, UI.update_content); // TODO 改成funciton 不要直接放UI中的方法
            },
            // 更新内容
            update_content: function (content) {
                var UI = this;
                var json = content;
                if (!content) {
                    // 没内容 ？？？
                    this.load_fail("对象内容为空，无法解析");
                    return;
                }
                if (typeof content == "string") {
                    try {
                        UI.$wj_vm.json.root = $z.fromJson(content);
                    } catch (e) {
                        // 提示这不是一个json格式的文本
                        this.load_fail("对象内容非json格式，解析失败");
                        return;
                    }
                }
                this.update_obj();
            },
            // 更新对象信息
            update_obj: function () {
                var obj = this.app.obj;
                this.arena.find('.ui-wedit-title .ui-tt').text(obj.nm);
                this.arena.find('.ui-wedit-footer').text(obj.ph);
            },
            // 加载对象内容是吧
            load_fail: function (errMsg) {
                // TODO 更友好的提示方式
                alert(errMsg);
            }
        });
    });
})(window.NutzUtil);