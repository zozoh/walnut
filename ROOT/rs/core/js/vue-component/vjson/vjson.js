(function () {

    function randomInt(min, max) {
        return Math.floor(Math.random() * (max - min + 1) + min);
    }

    // treenode
    var tmpl_treenode = `
    <li class="ui-wjson-treenode" :class="{'no-root' : !showTn}">
        <!--当前节点-->
        <div class="tn" v-if="showTn" @click="selectNode"
             :class="{'mkey' : matchKey, 'mval': matchVal, 'empty': isEmptyTn }">
            <template v-if="isEmptyTn">
                <span class="tn-key emptytn">{{emptyTn}}</span>
            </template>
            <template v-else>
                <!--名称 or 序号-->
                <span class="tn-key" v-if="ptype != 'array'"
                      :class="{'empty': keyEmpty, 'hasTip': hasTip}"
                      v-vjson-tn-edit="model.name" contenteditable="plaintext-only"></span>
                <span class="tn-key" v-else>{{pindex}}</span>
                <!--分隔符 or 长度-->
                <span class="tn-sp" v-if="!isFolder">:</span>
                <span class="tn-length" v-else>{{length}}</span>
                <!--内容-->
                <input type="checkbox" v-if="isBoolean" :checked="model.value" @click="toggleBoolean"/>
                <span class="tn-val" v-if="!isFolder"
                      :class="{'boolean' : isBoolean, 'number': isNumber, 'string' : isString, 'empty': valEmpty}"
                      v-vjson-tn-edit="model.value" contenteditable="plaintext-only" data-balloon="{{tipMsg}}"
                      data-balloon-pos="{{tipPos}}"></span>
                <!--错误提示-->
                <span class="tn-err" data-balloon="重复的键" data-balloon-pos="right" v-show="model.dupkey">
                    <i class="fa fa-fw fa-info-circle"></i>
                </span>
                <!--展开，折叠-->
                <div class="tn-toggle" v-if="isFolder" @click="toggleFolder">
                    <i class="fa fa-fw" :class="{'fa-caret-down': model.open, 'fa-caret-right': !model.open}"></i>
                </div>
            </template>
            <!--动作菜单-->
            <div class="tn-menu" v-if="parseInt(depth) > 0"
                 :style="{ left: (depth * -16 + -48) + (sroot ? 0:16) + 'px' }"
                 @mousedown="showMenu"
                 @mouseleave="hideMenu"
                 :class="{'show': menuVisiable}">
                <div class="tn-menu-anchor">
                    <ul class="tn-menu-list">
                        <!--类型转换-->
                        <li v-if="!isEmptyTn">
                            <div class="tn-menu-btn" @click="expandTypes()">
                                <i class="fa fa-font act-tip btn-types"></i><span>类型</span>
                                <i class="fa fa-reorder sub-tip"></i>
                            </div>
                            <ul class="tn-menu-sublist" v-show="typeVisiable">
                                <li>
                                    <div class="tn-menu-btn" @click="convertTn('object')"
                                         :class="{'curType' : isObject}">
                                        <span>对象</span>
                                    </div>
                                </li>
                                <li>
                                    <div class="tn-menu-btn" @click="convertTn('array')"
                                         :class="{'curType' : isArray}">
                                        <span>数组</span>
                                    </div>
                                </li>
                                <li>
                                    <div class="tn-menu-btn" @click="convertTn('string')"
                                         :class="{'curType' : isString}">
                                        <span>字符串</span>
                                    </div>
                                </li>
                                <li>
                                    <div class="tn-menu-btn" @click="convertTn('number')"
                                         :class="{'curType' : isNumber}">
                                        <span>数字</span>
                                    </div>
                                </li>
                                <li>
                                    <div class="tn-menu-btn" @click="convertTn('boolean')"
                                         :class="{'curType' : isBoolean}">
                                        <span>布尔</span>
                                    </div>
                                </li>
                                <li>
                                    <div class="tn-menu-btn" @click="convertTn('null')"
                                         :class="{'curType' : isNull}">
                                        <span>空</span>
                                    </div>
                                </li>
                            </ul>
                        </li>
                        <!--空对象 空数组-->
                        <template v-if="isEmptyTn">
                            <li>
                                <div class="tn-menu-btn" @click="addItem()">
                                    <i class="fa fa-plus act-tip btn-insert"></i><span>添加</span>
                                </div>
                            </li>
                        </template>
                        <!--其他-->
                        <template v-else>
                            <li>
                                <div class="tn-menu-btn" @click="addItem()">
                                    <i class="fa fa-plus act-tip btn-insert"></i><span>添加</span>
                                </div>
                            </li>
                            <li>
                                <div class="tn-menu-btn" @click="appendItem()">
                                    <i class="fa fa-plus-circle act-tip btn-insert"></i><span>追加</span>
                                </div>
                            </li>
                            <li>
                                <div class="tn-menu-btn" @click="copyItem()">
                                    <i class="fa fa-copy act-tip btn-clone"></i><span>复制</span>
                                </div>
                            </li>
                            <template v-if="isFolder && this.model.children.length > 0">
                                <li>
                                    <div class="tn-menu-btn" @click="cleanChildren()">
                                        <i class="fa fa-remove act-tip btn-clean"></i><span>清空</span>
                                    </div>
                                </li>
                            </template>
                            <li>
                                <div class="tn-menu-btn" @click="deleteItem()">
                                    <i class="fa fa-minus act-tip btn-delete"></i><span>删除</span>
                                </div>
                            </li>
                        </template>
                    </ul>
                </div>
                <i class="fa fa-th-list fa-fw"></i>
            </div>
        </div>
        <!--子节点-->
        <ul v-show="model.open" v-if="isFolder">
            <template v-if="model.children && model.children.length > 0">
                <ui-wjson-treenode
                        v-for="child in model.children"
                        :sroot.sync="sroot"
                        :path="cpath"
                        :tips="tips"
                        :ptype="model.type"
                        :pindex="$index"
                        :qkey.sync="qkey"
                        :depth="parseInt(depth) + 1"
                        :model.sync="child">
                </ui-wjson-treenode>
            </template>
            <template v-else>
                <ui-wjson-treenode
                        :path="cpath"
                        :sroot.sync="sroot"
                        :ptype="model.type"
                        :depth="parseInt(depth) + 1"
                        :model.sync="emptyModel">
                </ui-wjson-treenode>
            </template>
        </ul>
    </li>
    `;

    Vue.directive('vjson-tn-edit', {
        twoWay: true,
        bind: function () {
            this.handler = function () {
                this.set(this.el.innerHTML)
            }.bind(this);
            this.el.addEventListener('keyup', this.handler)
        },
        update: function (newValue, oldValue) {
            if (newValue === false) {
                newValue = "false";
            }
            this.el.innerHTML = newValue || ''
        },
        unbind: function () {
            this.el.removeEventListener('keyup', this.handler)
        }
    });

    Vue.component('ui-wjson-treenode', {
        template: tmpl_treenode,
        props: {
            path: String,
            tips: Object,
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
            cpath: function () {
                return this.path + "." + this.model.name;
            },
            tipPos: function () {
                if (this.hasTip) {
                    return "down";
                }
                return "";
            },
            tipMsg: function () {
                return this.tips[this.cpath.substr(6)];
            },
            hasTip: function () {
                return this.tipMsg != null;
            },
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
                return this.model.value === "";
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
                if (val === "true" || val == "True") {
                    this.model.value = true;
                }
                if (val === "false" || val == "False") {
                    this.model.value = false;
                }
                this.model.type = this.valType(val);
            },
            'model.name': function (val) {
                // 父节点检查相邻节点是否有同名的问题
                this.$parent.checkChildren();
            }
        },
        events: {},
        methods: {
            selectNode: function () {
                this.$dispatch('selectTreeNode', this.$parent, this);
            },
            selectNextNode: function () {

            },
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
                if (this.model.type == 'array') { // 数组名称不唯一，免查询
                    return;
                }
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
            toggleFolder: function () {
                if (this.isFolder) {
                    this.model.open = !this.model.open;
                }
            },
            toggleBoolean: function () {
                if (this.model.value) {
                    this.model.value = "false";
                } else {
                    this.model.value = "true";
                }
            },
            valType: function (val) {
                var vt = "unknow";
                if (val === "") {
                    vt = "null";
                }
                // 判断bool
                else if (typeof val == "boolean" || val == "true" || val == "false" || val == "True" || val == "False") {
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
                var self = this;
                var cnm = (self.model.type == "array" ? ("arr" + randomInt(1, 100000)) : "");
                console.log("parent [" + this.model.name + "] add children [" + i + "]");
                self.model.children.splice(i, 0, child || {
                        name: cnm,
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

    // editor
    var tmpl_editor = `
        <!--json格式编辑器-->
        <div class="ui-wjson-editor" v-if="sjson">
            <div class="ui-wjson-menu">
                <div class="menu-item btn" @click="toogleEditor();"><i class="fa fa-fw fa-exchange"></i></div>
                <div class="menu-item btn" @click="expandTree(json.tree)"><i class="fa fa-fw fa-expand"></i></div>
                <div class="menu-item btn" @click="collapseTree(json.tree, true)"><i class="fa fa-fw fa-compress"></i>
                </div>
                <!--节点相关-->
                <template v-if="seltn != null">
                    <div class="menu-item btn btn-insert" @click="menuAdd();"><i class="fa fa-fw fa-plus"></i></div>
                    <div class="menu-item btn btn-insert" @click="menuAdd();"><i class="fa fa-fw fa-plus-circle "></i></div>
                    <div class="menu-item btn btn-delete" @click="menuDelete();"><i class="fa fa-fw fa-minus"></i></div>
                </template>
                <div class="menu-item right"><input type="text" v-model="qkey"></div>
            </div>
            <div class="ui-wjson-container">
                <ul class="ui-wjson-curb">
                </ul>
                <ul class="ui-wjson-content">
                    <ui-wjson-treenode
                            depth="0"
                            ptype="object"
                            path=""
                            :tips="tips"
                            :sroot.sync="sroot"
                            :model="json.tree"
                            :qkey.sync="qkey">
                    </ui-wjson-treenode>
                </ul>
            </div>
        </div>
        <!--源码格式编辑器-->
        <div class="ui-wjson-editor" v-else>
            <div class="ui-wjson-menu">
                <div class="menu-item btn" @click="toogleEditor();"><i class="fa fa-fw fa-exchange"></i></div>
                <div class="menu-item btn" @click="toogleCompress();"><i class="fa fa-fw"
                                                                         :class="{'fa-align-justify': compress, 'fa-align-right': !compress}"></i>
                </div>
                <div class="menu-item btn" @click="copyText();"><i class="fa fa-fw fa-copy"></i></div>
            </div>
            <div class="ui-wjson-container">
                <ul class="ui-wjson-curb source-line">
                    <li v-for="(i, line) in source.lines" track-by="$index">{{i+1}}</li>
                </ul>
                <ul class="ui-wjson-content">
                    <li v-for="(i, line) in source.lines" track-by="$index">{{{line}}}</li>
                </ul>
            </div>
        </div>
    `;

    Vue.component('ui-wjson-editor', {
        template: tmpl_editor,
        props: {
            sjson: true,
            sroot: false,
            tips: {}
        },
        data: function () {
            return {
                selparent: null,
                seltn: null,
                tabwidth: 2,
                compress: false,
                qkey: "",
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
                    content: "",
                    content_normal: "",
                    content_compress: ""
                }
            };
        },
        watch: {
            "json.root": function (obj) {
                // 更新tree
                this.json.tree = this.obj2tree(obj);
                this.updateContent();
                console.log("### JSON ###\n" + $z.toJson(obj, null, 2) + "\n");
                console.log("### TREE ###\n" + $z.toJson(this.json.tree, null, 2) + "\n");
            },
            "source.content": function (val) {
                // TODO 按照 key, value等进行处理， 可以高亮一些一些内容
                var lines = val.replace(/ /g, "&nbsp;").split("\n");
                // 最终插入到lines中为html元素
                this.$set("source.lines", lines);
            }
        },
        events: {
            selectTreeNode: function ($parent, $tn) {
                this.selparent = $parent;
                this.seltn = $tn;
                if (this.selparent) {
                    console.log("seltn: [" + this.selparent.model.name + "]>[" + this.seltn.model.name + "]");
                }
            },
        },
        methods: {
            copyText: function () {

            },
            // 菜单上的几个按钮
            menuAdd: function () {
                if (this.seltn) {
                    this.seltn.addItem();
                }
            },
            menuDelete: function () {
                if (this.seltn) {
                    this.seltn.deleteItem();
                }
            },
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
            // treenode转换为json
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
                    for (var i = 0; i < tn.children.length; i++) {
                        var ele = tn.children[i];
                        if (!self.tn2js(path + "/" + name, value, true, ele)) {
                            return false;
                        }
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
            // 为了解决{}中字段顺序不能按照添加顺序序列化的问题，采用拼接字符串的方式来做
            tree2objAsStr: function () {
                var self = this;
                var troot = this.json.tree;
                var objw = {
                    c: ""
                };
                for (var i = 0; i < troot.children.length; i++) {
                    var ele = troot.children[i];
                    if (!self.tn2jsAsStr("/root", 1, objw, true, ele)) {
                        return null;
                    }
                }
                var clength = objw.c.length;
                var json = "{\n" + objw.c.substr(0, clength - 2) + "\n}";
                return json;
            },
            tabStr: function (depth) {
                return $z.dupString(' ', this.tabwidth * depth);
            },
            tn2jsAsStr: function (path, depth, parent, isObj, tn) {
                var self = this;
                var name = tn.name;
                if (tn.dupkey) {
                    alert("对象路径：" + path + "下有重复的键[" + tn.name + "]，请检查");
                    return false;
                }
                if (name.trim() == "") {
                    alert("对象路径：" + path + "下有空键，请检查");
                    return false;
                }
                var tval = tn.value;
                var value = this.tabStr(depth) + (isObj ? ('"' + name + '": ') : "");
                // 根据类型转换
                if (tn.type == "object" || tn.type == "array") {
                    if (tn.type == "object") {
                        value += "{\n";
                    }
                    if (tn.type == "array") {
                        value += "[\n";
                    }
                    var objw = {c: ""};
                    for (var i = 0; i < tn.children.length; i++) {
                        var ele = tn.children[i];
                        if (!self.tn2jsAsStr(path + "/" + name, depth + 1, objw, tn.type == "object", ele)) {
                            return false;
                        }
                    }
                    var clength = objw.c.length;
                    value += objw.c.substr(0, clength - 2);
                    if (tn.type == "object") {
                        value += "\n" + this.tabStr(depth) + "}";
                    }
                    if (tn.type == "array") {
                        value += "\n" + this.tabStr(depth) + "]";
                    }
                }
                else if (tn.type == "null") {
                    value += "null";
                }
                else if (tn.type == "string") {
                    value += '"' + tn.value + '"';
                }
                else if (tn.type == "number") {
                    if (/^-?[1-9]\d*$/.test(tval)) {
                        value += parseInt(tval);
                    }
                    // 浮点数
                    else {
                        value += parseFloat(tval);
                    }
                }
                else if (tn.type == "boolean") {
                    if (tval == "true" || tval == "True") {
                        value += true;
                    } else {
                        value += false;
                    }
                }
                parent.c += value + ",\n";
                return true;
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
                // var njson = this.tree2obj();
                var njson = this.tree2objAsStr();
                if (njson) {
                    this.source.content_normal = njson; // 字符串
                    this.source.content_compress = njson.replace(/ /g, "").replace(/\n/g, ""); // 字符串
                    if (this.compress) {
                        this.source.content = this.source.content_compress;
                    } else {
                        this.source.content = this.source.content_normal;
                    }
                    return true;
                } else {
                    return false;
                }
            },
            getContent: function () {
                if (this.updateContent()) {
                    return this.source.content;
                }
                return "";
            },
            getRoot: function () {
                var rootJson = this.tree2obj();
                if (rootJson) {
                    return rootJson.root;
                }
                return null;
            },
            setRoot: function (root) {
                this.json.root = root;
            },
            // 切换编辑器
            toogleEditor: function () {
                if (this.sjson) {
                    if (!this.updateContent()) {
                        return;
                    }
                }
                this.sjson = !this.sjson;
            },
            toogleCompress: function () {
                this.compress = !this.compress;
                if (this.compress) {
                    this.source.content = this.source.content_compress;
                } else {
                    this.source.content = this.source.content_normal;
                }
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
                    if (isRoot && !this.sroot) {
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
})();