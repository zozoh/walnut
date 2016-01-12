(function($z){
$z.declare('zui', function(ZUI, Wn){
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
            UI.getIdKey = function(obj){
                return obj[idKey];
            };
        }
        // 就是函数
        else if(_.isFunction(options.idKey)){
            UI.getIdKey = options.idKey;
        }
        // 必须得有 idKey 的获取方式
        else{
            throw "e.tree.noIdKeyFinder";
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
    events : {
        "click .tnd-handle" : function(e){
            var jNode = $(e.currentTarget).closest(".tree-node");
            if(jNode.attr("collapse") == "yes")
                this.openNode(jNode);
            else
                this.closeNode(jNode);
        },
        "click .tnd-text" : function(e){
            var jText = $(e.currentTarget);
            var jNode = jText.closest(".tree-node");
            // 高亮节点文本被点击
            if(jNode.hasClass("tree-node-actived")){

            }
            // 高亮文本节点
            else{
                this.active(jNode);
            }
        }
    },
    //...............................................................
    active : function(nd, quiet){
        var UI = this;
        var opt = UI.options;
        var jNode = UI.$node(nd);
        var context = opt.context || UI;
        // 已经高亮了就啥也不做
        if(jNode.hasClass("tree-node-actived")){
            return;
        }
        // 去掉其他的高亮
        UI.arena.find(".tree-node-actived").removeClass("tree-node-actived").each(function(){
            var jq = $(this);
            var ly = jq.data("@DATA");
            $z.invoke(opt, "on_blur", [ly, jq], context);
        });
        // 高亮自己
        jNode.addClass("tree-node-actived");

        // 调用回调
        if(!quiet){
            var ly = jNode.data("@DATA");
            $z.invoke(opt, "on_actived", [ly, jNode], context);
        }
    },
    //...............................................................
    openNode : function(nd) {
        var UI = this;
        var opt = UI.options;
        var jNode = UI.$node(nd);
        var context = opt.context || UI;
        // 已经展开了，就不用展开了
        if(jNode.attr("collapse") == "no"){
            return;
        }
        // 标记
        jNode.attr("collapse", "no");

        // 如果已经有子节点了，就不用加载了
        var jSub = jNode.children(".tnd-children");
        if(jSub.children().size()>0)
            return;

        // 读取子节点
        jSub.text(UI.msg("loadding"));
        var obj = jNode.data("@DATA");
        opt.children.call(context, obj, function(list){
            UI._draw_nodes(list, jSub);
        });
    },
    //...............................................................
    closeNode : function(nd){
        var UI = this;
        var jNode = UI.$node(nd);
        // 已经关闭了，就不用关闭了
        if(jNode.attr("collapse") == "yes"){
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

        var jq   = UI.ccode("tree.node");
        var id   = UI.getIdKey.call(context, obj);
        var leaf = _.isFunction(opt.isLeaf) ? opt.isLeaf.call(context, obj) : true;
        jq.attr("oid", id)
          .attr("ndtp", leaf ? "leaf" : "node")
          .attr("collapse", "yes");
        // 节点添加手柄
        if(!leaf)
            jq.find(".tnd-handle").html(opt.handle);
        // 没有多选框
        if(!opt.checkable){
            jq.find(".tnd-check").remove();
        }else{
            jq.find(".tnd-check").html(opt.checkbox);
        }
        // 绘制图标
        if(_.isFunction(opt.icon))
            jq.find(".tnd-icon").html(opt.icon.call(context, obj));
        else
            jq.find(".tnd-icon").remove();
        // 显示文字
        if(_.isFunction(opt.text))
            jq.find(".tnd-text").text(opt.text.call(context, obj));
        else
            jq.find(".tnd-text").text(id);

        // 记录数据
        jq.data("@DATA", obj);

        // 调用配置项，自定义更多节点外观
        $z.invoke(opt, "on_draw_node", [jq], context);

        // 返回
        return jq;
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