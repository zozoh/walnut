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
    css  : "theme/ui/tree/tree.css",
    //...............................................................
    init : function(options){
        var UI = this;
        $z.setUndefined(options, "idKey", "id");
        $z.setUndefined(options, "context", UI);
        if(_.isString(options.idKey)){
            UI.getId = function(obj){
                return obj[options.idKey];
            };
        }
        // 就是函数
        else if(_.isFunction(options.idKey)){
            UI.getId = options.idKey;
        }
        // 必须得有 idKey 的获取方式
        else{
            throw "e.tree.noIdKeyFinder";
        }

        // 声明名称
        if(_.isString(options.nmKey)){
            UI.getName = function(obj){
                return obj[options.nmKey]
            }
        }
        // 函数 
        else if(_.isFunction(options.nmKey)){
            UI.getName = options.nmKey;
        }

        // 默认的手柄
        $z.setUndefined(options, "handle", 
            '<i class="fa fa-caret-right"></i><i class="fa fa-caret-down"></i>');

        // 默认的选择框
        if(options.checkable){
            $z.setUndefined(options, "checkbox", 
                '<i class="fa fa-square-o"></i><i class="fa fa-check-square-o"></i>');
        }
    },
    //...............................................................
    redraw : function(){
        var UI = this;
        var opt = UI.options;
        var jWrapper = UI.arena.children(".tree-wrapper");

        // 根节点需要动态加载
        if(_.isFunction(opt.tops)){
            opt.tops(function(re){
                UI._draw_nodes(re, jWrapper);
                UI.defer_report(0, "load_tops");
            });
            // 返回延迟加载
            return  ["load_tops"];
        }

        // 总得有顶级节点吧
        if(!opt.tops)
            throw "e.tree.without.tops";

        // 绘制
        UI._draw_nodes(opt.tops, jWrapper);
    },
    //...............................................................
    $node : function(nd){
        if(_.isUndefined(nd)){
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
            return this.arena.find(".tree-node[oid="+nd+"]");
        }
        else if($z.isPlainObj(nd)){
            var jqs = this.arena.find(".tree-node");
            for(var i=0;i<jqs.length;i++){
                var jq = jqs.eq(i);
                var obj = jq.data("@DATA");
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
            this.active(e.currentTarget);
        },
        "click .tree-node-actived .tnd-text" : function(e){
            var UI    = this;
            var opt   = UI.options;
            var jText = $(e.currentTarget);
            var jNode = UI.$node(jText);
            var obj   = UI.getNodeData(jNode);
            var context = opt.context || UI;
            $z.invoke(opt, "on_click_actived_text", [obj, jText, jNode], context);

        },
        "contextmenu .tnd-self" : function(e){
            var jSelf = $(e.currentTarget);
            var jNode = jSelf.closest(".tree-node");
            this.active(jNode);

            // 如果声明了 on_contextmenu， 则接管右键菜单 
            var UI = this;
            var opt = UI.options;
            if(_.isFunction(opt.on_contextmenu)){
                // 禁掉右键菜单
                e.preventDefault();
                // 得到菜单项的信息
                var context = opt.context || UI;
                var obj   = jNode.data("@DATA");
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
    setActived : function(nd, quiet){
        var UI = this;
        var opt = UI.options;
        var jNode = UI.$node(nd);
        if(jNode.size()==0)
            return;
        var context = opt.context || UI;
        // 已经高亮了就啥也不做
        if(jNode.hasClass("tree-node-actived")){
            return;
        }
        // 去掉其他的高亮
        UI.arena.find(".tree-node-actived").removeClass("tree-node-actived").each(function(){
            var jq = $(this);
            var obj = jq.data("@DATA");
            $z.invoke(opt, "on_blur", [obj, jq], context);
        });
        // 高亮自己
        jNode.addClass("tree-node-actived");

        // 是否确保自己被展开
        if(opt.openWhenActived){
            UI.openNode(jNode);
        }

        // 调用回调
        if(!quiet){
            var obj = jNode.data("@DATA");
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
    openNode : function(nd, callback) {
        var UI = this;
        var opt = UI.options;
        var jNode = UI.$node(nd);
        var context = opt.context || UI;
        // 叶子节点不能展开
        if(UI.isLeaf(jNode)){
            return;
        }
        // 已经展开了，就不用展开了
        if(UI.isOpened(jNode)){
            return;
        }
        // 标记
        jNode.attr("collapse", "no");

        // 如果已经有子节点了，就不用加载了
        var jSub = jNode.children(".tnd-children");
        if(jSub.children().size()>0)
            return;

        // 读取子节点
        UI.reload(jNode, callback);
    },
    //...............................................................
    reload : function(nd, callback) {
        var UI = this;
        var opt = UI.options;
        var jNode = UI.$node(nd);
        var context = opt.context || UI;
        var jSub = jNode.children(".tnd-children");
        jSub.text(UI.msg("loadding"));
        var obj = jNode.data("@DATA");
        opt.children.call(context, obj, function(list){
            UI._draw_nodes(list, jSub);
            if(_.isFunction(callback)){
                callback.call(context, list, jNode);
            }
        });
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
            UI.active(jNode);
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
            direction = "before" == direction
                            ? "first"
                            : ("after" == direction ? "last" : direction);
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

    },
    //...............................................................
    _draw_nodes : function(list, jP){
        var UI  = this;
        var opt = UI.options;
        // 确保是数组
        if(!_.isArray(list)){
            list = [list];
        }
        // 遍历数组进行绘制
        jP.empty();
        list.forEach(function(obj){
            UI.__gen_node(obj).appendTo(jP);
        });
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
        if(UI.getName){
            jNode.attr("onm", UI.getName.call(context, obj));
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
        if(_.isFunction(opt.text))
            jNode.find(".tnd-text").text(opt.text.call(context, obj));
        else
            jNode.find(".tnd-text").text(id);

        // 记录数据
        jNode.data("@DATA", obj);

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
        return this.arena.find(".tree-node-actived").data("@DATA");
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
    removeNode : function(nd){
        var UI = this;
        var jNode = UI.$node(nd);
        jNode.remove();
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
        var UI = this;
        var jNode = UI.$node(nd);
        return jNode.data("@DATA");
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);