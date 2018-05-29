(function($z){
$z.declare(['zui', 'ui/menu/menu'], function(ZUI, MenuUI){
//==============================================
var html = function(){/*
<div class="ui-code-template">
    <div code-id="tree.node" class="tree-node">
        <div class="tnd-self"><div class="tnd-handle"
            ></div><div class="tnd-check"
            ></div><div class="tnd-icon"
            ></div><div class="tnd-text"
        ></div></div>
        <div class="tnd-children" ></div>
    </div>
</div>
<div class="ui-arena tree" ui-fitparent="yes">
    <div class="tree-wrapper"></div>
</div>
*/};
//==============================================
return ZUI.def("ui.tree", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "ui/tree/theme/tree-{{theme}}.css",
    //...............................................................
    init : function(opt){
        var UI = this;
        $z.setUndefined(opt, "idKey", "id");
        $z.setUndefined(opt, "context", UI);
        $z.setUndefined(opt, "escapeHtml", true);
        if(_.isString(opt.idKey)){
            UI.getId = function(obj){
                return obj[opt.idKey];
            };
        }
        // 就是函数
        else if(_.isFunction(opt.idKey)){
            UI.getId = opt.idKey;
        }
        // 必须得有 idKey 的获取方式
        else{
            throw "e.tree.noIdKeyFinder";
        }

        // 声明名称
        if(_.isString(opt.nmKey)){
            UI.getName = function(obj){
                return obj[opt.nmKey]
            }
        }
        // 函数 
        else if(_.isFunction(opt.nmKey)){
            UI.getName = opt.nmKey;
        }

        // 默认获取数据的方法
        $z.setUndefined(opt, "data", function(jNode, obj){
            if(obj){
                jNode.data("@DATA", obj);
                return this;
            }
            return jNode.data("@DATA");
        });

        // 默认的手柄
        $z.setUndefined(opt, "handle", 
            '<i class="fa fa-caret-right"></i><i class="fa fa-caret-down"></i>');

        // 默认的选择框
        if(opt.checkable){
            $z.setUndefined(opt, "checkbox", 
                '<i class="fa fa-square-o"></i><i class="fa fa-check-square-o"></i>');
        }
    },
    //...............................................................
    redraw : function(){
        var UI = this;
        var opt = UI.options;

        // 开始重绘前，重新加载所有子节点
        UI.__reload_tops(function(){
            UI.defer_report("load_tops");
        })

        // 启用拖拽
        if(opt.drag) {
            UI.arena.moving({
                trigger    : '.tnd-self',
                maskClass  : 'wn-dragging',
                init : function() {
                    this.Event.preventDefault();
                },
                sensors  : function() {
                    var ing = this;
                    // 准备自己的内部可 drop 目标
                    var conf = _.extend({
                        ignoreIdMap : {},
                        ignoreAncestorMap : {},
                    }, opt.drag);
                    if(_.isArray(ing.data)) {
                        for(var i=0; i<ing.data.length; i++) {
                            var obj = ing.data[i];
                            conf.ignoreAncestorMap[obj.id]  = obj;
                        }
                    }
                    else if(ing.data) {
                        conf.ignoreIdMap[ing.data.pid] = Wn.getById(ing.data.pid);
                        conf.ignoreAncestorMap[ing.data.id] = ing.data;
                    }

                    // 因为考虑到可能要增加自己的父节点，那么看看自己的父控件是否
                    // 定义了 getDropSensors，如果有就调它
                    var senList;
                    if(UI.parent && _.isFunction(UI.parent.getDropSensors)) {
                        senList = UI.parent.getDropSensors(conf, ing);
                    }
                    // 否则直接用自己的
                    else {
                        senList = UI.getDropSensors(conf);
                    }
                    //console.log("tree", senList);

                    // 合并外部的 sensor
                    if(_.isArray(opt.drag.moreDragSensors)){
                        senList = senList.concat(opt.drag.moreDragSensors);
                    }
                    // 动态调用的
                    else if(_.isFunction(opt.drag.moreDragSensors)){
                        var sl2 = opt.drag.moreDragSensors.call(ing, UI);
                        //console.log("sl2", sl2);
                        if(_.isArray(sl2) && sl2.length>0) {
                            senList = senList.concat(sl2);
                        }
                    }
                    //console.log("senList", senList);

                    // 最后返回
                    return senList;
                },
                sensorFunc : {
                    "drag" : {
                        "enter" : function(sen){this.dropInSensor = sen;},
                        "leave" : function(sen){this.dropInSensor = null;}
                    }
                },
                on_begin : function() {
                    var ing   = this;
                    // 记录正在拖拽的数据
                    ing.data = UI.getNodeData(ing.$target);

                    // 复制对象以便显示拖拽
                    ing.mask.$target.append(ing.$target.clone());
                    ing.mask.$target.find(".tnd-handle").remove();
                    ing.mask.$target.find(".tnd-icon i:nth-child(2)").remove();

                    // 拖拽目标不要裁掉
                    ing.mask.$target.css("overflow", "visible");

                    // 调用回调
                    $z.invoke(opt.drag, "on_begin", [], ing);
                },
                on_ready : function(){
                    $z.invoke(opt.drag, "on_ready", [], this);
                },
                on_end : function() {
                    var ing   = this;
                    
                    //console.log(ing.dropInSensor);
                    var args = [];
                    if(ing.dropInSensor) {
                        args.push(ing.dropInSensor.data);
                        args.push(ing.dropInSensor.$ele);
                    }

                    // 调用回调
                    $z.invoke(opt.drag, "on_end", args, ing);
                }
            });
        }

        // 返回延迟加载
        return  ["load_tops"];
    },
    //...............................................................
    depose : function() {
        var UI = this;
        var opt = UI.options;

        // 启用拖拽
        if(opt.drag) {
            UI.arena.moving("destroy");
        }
    },
    //...............................................................
    /* 获取自己可以被 drop 的传感器
     - conf: 传感器的配置 : {
            name  : "drag",
            rect  : 1,
            scope : null,           // null 表示自动判断

            // 拖拽传感器类型
            drag_sen_type : "tree"

            // true 表示忽略所有选中的项目
            ignoreChecked : true,
            
            // true 表示忽略所有叶子节点
            ignoreLeaf : true,
            
            // 表示过滤方法, 返回 false 表示无视
            filter : F(o, jItem):Boolean,
            
            // 表示这些节点不许移动
            ignoreIdMap : {ID : {...}, ..},
            
            // 表示这些节点以及其下节点下都不许移动
            ignoreAncestorMap : {ID : {...}, ..},

            // 表示一个虚的根节点
            oRoot : {..}

            // 表示虚的根节点对应的 DOM
            $root : jQuery
        }                       
    */
    getDropSensors : function(conf) {
        var UI = this;

        // 准备默认值
        conf = conf || {};
        $z.setUndefined(conf, "ignoreChecked", false);
        $z.setUndefined(conf, "ignoreLeaf",    true);

        // 准备返回值
        var senList = [];

        // 增加根
        if(conf.oRoot) {
            senList.push({
                drag_sen_type : conf.drag_sen_type || "tree",
                name : "drag",
                rect : 1,
                text : "HOME",
                $ele : conf.$root || UI.$el,
                data : conf.oRoot,
            });
        }

        //console.log("conf.ignoreAncestorMap", conf.ignoreAncestorMap)

        // 搜索自己的 sensor
        UI.arena.find(".tree-node").each(function(){
            var jNode = $(this);
            var obj   = UI.getNodeData(jNode);

            var disabled;

            // 叶子节点要搞一下
            if(conf.ignoreLeaf && UI.isLeaf(jNode)){
                disabled = true;
            }
            // 自定义函数了
            else if(_.isFunction(conf.filter) && !conf.filter(obj, jNode)){
                disabled = true;   
            }
            // 选中的节点
            else if(conf.ignoreChecked && UI.isActived(jNode)) {
                disabled = true;
            }
            // 防止自己
            else if(conf.ignoreIdMap && conf.ignoreIdMap[obj.id]) {
                disabled = true;
            }
            // 防止祖先
            else if(conf.ignoreAncestorMap && !_.isEmpty(conf.ignoreAncestorMap)) {
                // 首先自己不能在列表中
                if(conf.ignoreAncestorMap[obj.id]){
                    disabled = true;
                }
                // 找到自己所有的祖先，也都不能在选中的 ID 里
                else {
                    var ans = Wn.getAncestors(obj);
                    for(var i=0; i<ans.length; i++) {
                        var an = ans[i];
                        if(conf.ignoreAncestorMap[an.id]) {
                            disabled = true;
                            break;
                        }
                    }
                }
            }
            // 推入传感器
            senList.push({
                drag_sen_type : conf.drag_sen_type || "tree",
                name  : conf.name || "drag",
                rect  : _.isNumber(conf.rect) ? conf.rect : -.5,
                scope : conf.scope,
                disabled : disabled,
                text : obj.nm,
                $ele : jNode.find(">.tnd-self"),
                data : obj,
            });
        });

        // 返回
        //console.log(senList)
        return senList;
    },
    //...............................................................
    __reload_tops : function(callback){
        var UI  = this;
        var opt = UI.options;
        var jW  = UI.arena.children(".tree-wrapper");
        var context = opt.context || UI;

        //console.log("__reload_tops");
        
        // 确保有 jW
        if(jW.size() == 0 ) {
            jW = $('<div class="tree-wrapper">').appendTo(UI.arena.empty());
        }

        // 根节点需要动态加载
        if(_.isFunction(opt.tops)){
            opt.tops(function(re){
                UI._draw_nodes(re, jW);
                if(_.isFunction(callback)){
                    callback.call(context, re);
                }
            });
            return
        }

        // 总得有顶级节点吧
        if(!opt.tops)
            throw "e.tree.without.tops";

        // 绘制
        UI._draw_nodes(opt.tops, jW);
        if(_.isFunction(callback)){
            callback.call(context, opt.tops);
        }
    },
    //...............................................................
    $node : function(nd){
        if(_.isUndefined(nd) || _.isNull(nd)){
            return this.arena.find(".tree-node-actived");
        }
        else if(_.isElement(nd)){
            return $(nd).closest(".tree-node");
        }
        else if($z.isjQuery(nd)){
            return nd.closest(".tree-node");
        }
        else if(_.isNumber(nd)){
            return this.arena.find(".tree-node").eq(nd);
        }
        else if(_.isString(nd)){
            return this.arena.find('.tree-node[oid="'+nd+'"]');
        }
        else if($z.isPlainObj(nd)){
            var jqs = this.arena.find(".tree-node");
            for(var i=0;i<jqs.length;i++){
                var jq = jqs.eq(i);
                var obj = this.options.data.call((this.options.context||this), jq);
                if(_.isMatch(obj, nd)){
                    return jq;
                }
            };
            return null;
        }
        // 靠，抛错
        throw "what the fuck you give me: " + nd;
    },
    //...............................................................
    findNode : function(arg) {
        var UI  = this;
        var opt = UI.options;

        // 没指定内容，为空
        if(_.isUndefined(arg)){
            return UI.arena.find(".tree-node");
        }
        // 如果是字符串表示 ID
        if(_.isString(arg)){
            // 名字吗
            if(/^nm:/.test(arg)){
                var nm = arg.substring(3);
                return UI.arena.find('.tree-node[onm="'+nm+'"]');    
            }
            // 那么就当做 ID 吧
            return UI.arena.find('.tree-node[oid="'+arg+'"]');
        }
        // 如果是个对象数组，则分别查找这个对象的集合
        if(_.isArray(arg)){
            var eles = [];
            for(var i=0; i<arg.length; i++) {
                var jIt = UI.findNode(arg[i]);
                if(jIt.length > 0)
                    eles.push(jIt[0]);
            }
            return $(eles);
        }
        // 正则表达式，则表示匹配名称
        if(_.isRegExp(arg)){
            var re = [];
            UI.arena.find('.tree-node').each(function(){
                var nm = $(this).attr("onm");
                if(arg.test(nm))
                    re.push(this);
            });
            return $(re);
        }
        // 是一个通用过滤函数
        if(_.isFunction(arg)){
            var re = [];
            UI.arena.find('.tree-node').each(function(){
                var jNode = $(this);
                var obj = UI.getNodeData(jNode);
                if(arg.call(UI, obj, jNode))
                    re.push(this);
            });
            return $(re);
        }
        // 本身就是 dom
        if(_.isElement(arg) || $z.isjQuery(arg)){
            return $(arg).closest(".tree-node");
        }
        // 数字
        if(_.isNumber(arg)){
            return UI.arena.find(".tree-node:eq("+arg+")");
        }
        // 如果是对象，那么试图获取 IDKey
        if(_.isObject(arg)) {
            var id = arg[opt.idKey];
            return UI.arena.find(".tree-node[oid="+id+"]");
        }
        // 靠不晓得了
        throw "unknowns $item selector: " + arg;
    },
    //...............................................................
    $top : function(nd){
        var UI = this;
        var jNode = UI.$node(nd);
        while(jNode.size()>0 && !jNode.parent().hasClass("tree-wrapper")){
            jNode = jNode.parent().closest(".tree-node");
        }
        return jNode;
    },
    //...............................................................
    $topByName : function(nm){
        var UI = this;
        var jW = UI.arena.find(".tree-wrapper");
        return jW.children(".tree-node[onm="+nm+"]");
    },
    //...............................................................
    isTop : function(nd){
        var UI = this;
        var jNode = UI.$node(nd);
        return jNode.parent().hasClass("tree-wrapper");
    },
    //...............................................................
    events : {
        "click .tnd-handle" : function(e){
            e.stopPropagation();

            var jNode = $(e.currentTarget).closest(".tree-node");
            if(jNode.attr("collapse") == "yes")
                this.openNode(jNode);
            else
                this.closeNode(jNode);
        },
        "click .tnd-self" : function(e){
            this.setActived(e.currentTarget);
        },
        "click .tree-node-actived > .tnd-self > .tnd-text" : function(e){
            var UI    = this;
            var opt   = UI.options;
            var jText = $(e.currentTarget);
            var jNode = UI.$node(jText);
            var obj   = UI.getNodeData(jNode);
            var context = opt.context || UI;
            $z.invoke(opt, "on_click_actived_text", [obj, jText, jNode], context);
        },
        "contextmenu .tree-node-actived > .tnd-self" : function(e){
            var jSelf = $(e.currentTarget);
            var jNode = jSelf.closest(".tree-node");
            //this.setActived(jNode);

            // 如果声明了 on_contextmenu， 则接管右键菜单 
            var UI = this;
            var opt = UI.options;
            if(_.isFunction(opt.on_contextmenu)){
                // 禁掉右键菜单
                e.preventDefault();
                // 得到菜单项的信息
                var context = opt.context || UI;
                var obj   = opt.data.call(context, jNode);
                var setup = opt.on_contextmenu.call(context, obj, jNode);
                // 显示菜单
                new MenuUI({
                    context  : UI,
                    setup    : setup,
                    position : {
                        x : e.pageX,
                        y : e.pageY
                    }
                }).render();
            }
        }
    },
    //...............................................................
    // 根据节点对象，深层展开全部给定节点
    __open_deeply : function(index, ans, callback) {
        var UI = this;
        var opt = UI.options;
        var context = opt.context || UI;

        if(_.isArray(ans) && ans.length > 0) {
            // 已经到头了
            if(index >= ans.length) {
                $z.doCallback(callback, [], context);
            }
            // 否则加载
            else {
                var an = ans[index];
                var nodeId = an[opt.idKey];
                UI.__open_node(nodeId, function(){
                    UI.__open_deeply(index+1, ans, callback);
                });
            }
        }
    },
    //...............................................................
    // 回调的格式 callback(jNode)
    __find_node : function(id, callback) {
        var UI = this;
        var opt = UI.options;
        var context = opt.context || UI;

        // 首先读取一下祖先节点
        $z.invoke(opt, "ancestor", [id, function(ans){
            //console.log(ans)
            UI.__open_deeply(0, ans, function(){
                var jNode = UI.$node(id);
                $z.doCallback(callback, [jNode], context);
            });
        }], context);
    },
    //...............................................................
    setActived : function(nd, quiet, callback){
        var UI = this;
        var opt = UI.options;
        var context = opt.context || UI;

        // 参数形式
        if(_.isFunction(quiet)) {
            callback = quiet;
            quiet = undefined;
        }

        // 试图获取节点设置高亮
        var jNode = UI.$node(nd);

        // 没有这个节点，那么看来需要加载这个节点的祖先
        if(jNode.size()==0) {
            var nodeId = _.isString(nd) ? nd : nd[opt.idKey];
            if(nodeId) {
                //console.log("active: find", nodeId);
                UI.__find_node(nodeId, function(jNode) {
                    UI.__active_node(jNode, quiet);
                    var obj = UI.getNodeData(jNode);
                    $z.doCallback(callback, [obj, jNode], UI);
                });
            }
        }
        // 执行高亮
        else {
            //console.log("active: set", nodeId);
            UI.__active_node(jNode, quiet);
            var obj = UI.getNodeData(jNode);
            $z.doCallback(callback, [obj, jNode], UI);
        }        
    },
    //...............................................................
    __active_node : function(jNode, quiet) {
        var UI = this;
        var opt = UI.options;
        var context = opt.context || UI;

        // 已经高亮了就啥也不做
        if(jNode.hasClass("tree-node-actived")){
            return;
        }
        // 去掉其他的高亮
        UI.arena.find(".tree-node-actived").removeClass("tree-node-actived").each(function(){
            var jq = $(this);
            var obj = opt.data.call(context, jq);
            $z.invoke(opt, "on_blur", [obj, jq], context);
        });
        // 高亮自己
        jNode.addClass("tree-node-actived");

        // 是否确保自己被展开
        if(opt.openWhenActived){
            UI.__open_node(jNode);
        }

        // 确保自己的祖先都被展开
        jNode.parents(".tree-node").attr("collapse", "no");


        // 调用回调
        if(!quiet){
            var obj = opt.data.call(context, jNode);
            $z.invoke(opt, "on_actived", [obj, jNode], context);
        }
    },
    //...............................................................
    // 叶子节点永远返回 true
    isClosed : function(nd) {
        return this.$node(nd).attr("collapse") == "yes";
    },
    isOpened : function(nd) {
        return this.$node(nd).attr("collapse") == "no";
    },
    isLeaf : function(nd) {
        return this.$node(nd).attr("ndtp") == "leaf";
    },
    isNode : function(nd) {
        return this.$node(nd).attr("ndtp") == "node";
    },
    //...............................................................
    disableNode : function(nd, deeply) {
        var UI = this;

        // 木有的话，就搞全部
        var jNodes = UI.findNode(nd);
        //console.log(nd, jNodes.length)

        // 标识
        jNodes.attr("nd-disabled", "yes");

        if(deeply) {
            jNodes.each(function(){
                $(this).find(".tree-node").attr("nd-disabled", "yes");
            });
        }
    },
    //...............................................................
    enableNode : function(nd, deeply) {
        var UI = this;

        // 木有的话，就搞全部
        var jNodes = UI.findNode(nd);

        // 标识
        jNodes.removeAttr("nd-disabled");

        if(deeply) {
            jNodes.each(function(){
                $(this).find(".tree-node").removeAttr("nd-disabled");
            });
        }
    },
    //...............................................................
    openNode : function(nd, callback, forceReload) {
        var UI = this;
        var opt = UI.options;
        var jNode = UI.$node(nd);
        var context = opt.context || UI;

        // 参数形式
        if(_.isBoolean(callback)) {
            forceReload = callback;
            callback = null;
        }

        // 没有这个节点，那么看来需要加载这个节点的祖先
        if(jNode.length==0) {
            var nodeId = _.isString(nd) ? nd : nd[opt.idKey];
            if(nodeId) {
                UI.__find_node(nodeId, function(jNode) {
                    // 尝试打开自己
                    UI.__open_node(jNode, function(children){
                        // 确保自己的祖先都被展开
                        jNode.parents(".tree-node").attr("collapse", "no");
                        // 回调
                        $z.doCallback(callback, [children, jNode], context);
                    });
                });
            }
            return;
        }
        // 存在的话，就直接打开一下
        else {
            UI.__open_node(nd, callback, forceReload);
        }
    },
    //...............................................................
    __open_node : function(nd, callback, forceReload) {
        var UI = this;
        var opt = UI.options;
        var jNode = UI.$node(nd);
        var context = opt.context || UI;

        // 没有这个节点，那么看来需要加载这个节点的祖先
        if(jNode.length==0) {
            return;
        }

        // 叶子节点不能展开
        if(UI.isLeaf(jNode)){
            //console.log("isLeaf");
            $z.doCallback(callback, [children, jNode], context);
            return;
        }

        // 标记
        jNode.attr("collapse", "no");

        // 看看能不能利用缓存
        if(!forceReload) {
            // 已经展开了，就不用展开了
            if(UI.isOpened(jNode)
                && jNode.find(">.tnd-children>.tree-node").length>0){
                //console.log("already opened");
                var children = UI.getNodeChildren(jNode);
                $z.doCallback(callback, [children, jNode], context);
                return;
            }

            // 如果已经有子节点了，就不用加载了
            var jSub = jNode.children(".tnd-children");
            if(jSub.children().size()>0) {
                var children = UI.getNodeChildren(jNode);
                if(children && children.length > 0) {
                    console.log("already has children");
                    $z.doCallback(callback, [children, jNode], context);
                    return;
                }
            }
        }

        // 读取子节点
        //console.log("do reload");
        UI.reload(jNode, callback, true);
    },
    //...............................................................
    refresh : function(callback) {
        return this.reload(callback);
    },
    //...............................................................
    reload : function(nd, callback, quiet) {
        var UI = this;
        var opt = UI.options;
        var context = opt.context || UI;

        // 没有指定节点的模式
        if(_.isFunction(nd)){
            callback = nd;
            nd = undefined;
        }

        // 保持激活: 先得到原始激活节点的 ID
        var aid = UI.getActivedId();

        // 没有指定节点，那么就加载 tops
        if(!nd){
            UI.__reload_tops(function(list){
                // 保持激活
                if(aid)
                    UI.setActived(aid, true);

                // 调用回调
                $z.doCallback(callback, [list], context);
            });
        }
        // 否则加载指定节点
        else{
            var jNode = UI.$node(nd);
            var jSub = jNode.children(".tnd-children");
            jSub.text(UI.msg("loadding"));
            var obj = opt.data.call(context, jNode);
            //console.log("__reload_children", obj.ph);
            // 重新加载
            opt.children.call(context, obj, function(list){
                // 绘制
                if(_.isArray(list) && list.length > 0) {
                    UI._draw_nodes(list, jSub.removeAttr("is-empty"));
                }
                // 否则绘制空
                else {
                    jSub.attr("is-empty","yes").text(UI.msg("empty"));
                }

                // 保持激活
                if(aid && !quiet)
                    UI.setActived(aid, true);

                // 调用回调
                $z.doCallback(callback, [list, jNode], context);
            });
        }
    },
    //...............................................................
    closeNode : function(nd){
        var UI = this;
        var jNode = UI.$node(nd);
        // 已经关闭了，就不用关闭了
        if(UI.isClosed(jNode)){
            return;
        }
        // 标记
        jNode.attr("collapse", "yes");

        // 如果子节点是有高亮的，则改成高亮自己
        if(UI.hasActivedSubNode(jNode)){
            UI.setActived(jNode);
        }
    },
    //...............................................................
    hasActivedSubNode : function(nd){
        var UI = this;
        var jNode = UI.$node(nd);
        return jNode.find(".tree-node-actived").size()>0;
    },
    //...............................................................
    getActivedSubNode : function(nd){
        var UI = this;
        var jNode = UI.$node(nd);
        return jNode.find(".tree-node-actived");
    },
    //...............................................................
    addNode : function(obj, direction){
        var UI = this;
        var jA = UI.arena.find(".tree-node-actived");
        // 没有活动节点，转义 direction
        if(jA.size() == 0){
            direction = "before" == direction ? "first" : "last";
        }
        // 生成节点
        var jq = UI.__gen_node(obj);
        var jW = UI.arena.find(".tree-wrapper");
        // 头
        if("first" == direction){
            jW.prepend(jq);
        }
        // 尾
        else if("last" == direction){
            jW.append(jq);
        }
        // 之前
        else if("before" == direction){
            jq.insertBefore(jA);
        }
        // 默认之后
        else {
            jq.insertAfter(jA); 
        }

        // 返回自身以便链式赋值
        return this;
    },
    updateNode : function(nd, obj, quiet) {
        var jNode = this.$node(nd);

        if(jNode.length > 0) {
            var isA = this.isActived(jNode);
            var jN2 = this.__gen_node(obj);
            jNode.replaceWith(jN2);
            if(isA) {
                this.setActived(jN2, quiet);
            }
        }
    },
    //...............................................................
    _draw_nodes : function(list, jP){
        var UI  = this;
        var opt = UI.options;
        // 确保是数组
        if(list && !_.isArray(list)){
            list = [list];
        }
        // 清空一下节点
        jP.empty();

        // 如果列表有内容才遍历吧
        if(list){
            list.forEach(function(obj){
                //console.log(obj.nm);
                // 如果有过滤函数，过滤一下
                if(_.isFunction(opt.filter)) {
                    obj = opt.filter(obj);
                }
                // 绘制
                if(obj)
                    UI.__gen_node(obj).appendTo(jP);
            });
        }
    },
    //...............................................................
    __gen_node : function(obj) {
        var UI  = this;
        var opt = UI.options;
        var context = opt.context || UI;

        var jNode = UI.ccode("tree.node");
        var id    = UI.getId.call(context, obj);
        var leaf  = _.isFunction(opt.isLeaf) ? opt.isLeaf.call(context, obj) : true;
        jNode.attr("oid", id)
          .attr("ndtp", leaf ? "leaf" : "node")
          .attr("collapse", "yes");

        // 补充上名称
        var nm;
        if(UI.getName){
            nm = UI.getName.call(context, obj);
            jNode.attr("onm", nm);
        }

        // 节点添加手柄
        if(!leaf)
            jNode.find(".tnd-handle").html(opt.handle);
        // 没有多选框
        if(!opt.checkable){
            jNode.find(".tnd-check").remove();
        }else{
            jNode.find(".tnd-check").html(opt.checkbox);
        }
        // 绘制图标
        if(_.isFunction(opt.icon))
            jNode.find(".tnd-icon").html(opt.icon.call(context, obj));
        else
            jNode.find(".tnd-icon").remove();

        // 显示文字
        var txt = nm || id;
        if(_.isFunction(opt.text)){
            txt = opt.text.call(context, obj);
        }
        // 简单文字
        if(opt.escapeHtml) {
            jNode.find(".tnd-text").text(txt);
        }
        // 复杂的 HTML
        else{
            jNode.find(".tnd-text").html(txt);
        }

        // 记录数据
        //jNode.data("@DATA", obj);
        opt.data.call(context, jNode, obj);

        // 调用配置项，自定义更多节点外观
        $z.invoke(opt, "on_draw_node", [jNode, obj], context);

        // 返回
        return jNode;
    },
    //...............................................................
    isActived : function(nd){
        var UI = this;
        var jNode = UI.$node(nd);
        return jNode.hasClass("tree-node-actived");
    },
    //...............................................................
    getActived : function(){
        var UI = this;
        var opt = UI.options;
        var context = opt.context||UI;

        return opt.data.call(context, UI.arena.find(".tree-node-actived"));
    },
    //...............................................................
    getActivedNode : function(){
        return this.arena.find(".tree-node-actived");
    },
    getActivedId : function(){
        return this.arena.find(".tree-node-actived").attr("oid");
    },
    //...............................................................
    getNodeId : function(nd){
        return this.$node(nd).attr("oid");
    },
    //...............................................................
    removeNode : function(nd, keepAtLeastOne){
        var UI = this;

        // 支持 removeNode(true) 这种形式
        if(_.isBoolean(nd)){
            keepAtLeastOne = nd;
            nd = null;
        }

        // 得到节点
        var jNode = UI.$node(nd);
        var jN2   = null;

        // 如果当前是高亮节点，则试图得到下一个高亮的节点，给调用者备选
        if(UI.isActived(jNode)){
            jN2 = jNode.next();
            if(jN2.size() == 0){
                jN2 = jNode.prev();
                if(jN2.size() == 0){
                    jN2 = jNode.parents(".tree-node").first();

                    // 返回 false 表示只剩下最后一个节点额
                    if(jN2.size() == 0 && keepAtLeastOne){
                        return false;
                    }
                }
            }
        }

        // 执行移除
        jNode.remove();

        // 返回下一个要激活的节点，由调用者来决定是否激活它
        return jN2 && jN2.size() > 0 ? jN2 : null;
    },
    //...............................................................
    moveNode : function(direction, nd){
        var UI = this;
        var jNode = UI.$node(nd);
        if("up" == direction){
            jNode.insertBefore(jNode.prev());
        }
        else if("down" == direction){
            jNode.insertAfter(jNode.next());
        }
    },
    //...............................................................
    getNodeData : function(nd){
        return this.options.data.call((this.options.context||this), this.$node(nd));
    },
    //...............................................................
    getNodeChildren : function(nd) {
        var UI = this;
        var jNode = UI.$node(nd);

        var list = [];
        jNode.find(">.tnd-children>.tree-node").each(function(){
            list.push(UI.getNodeData(this));
        });

        return list;
    },
    //...............................................................
    has : function(nd){
        return this.$node(nd).size() > 0;
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);