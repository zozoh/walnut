/*
让当前选区所有子元素超出选区的部分折叠在一起
*/
(function($, $z){
//...........................................................
function is_in_viewport(jViewport, jLast){
    var viewport = $z.rect(jViewport);
    var rect     = $z.rect(jLast);
    return rect.right <= viewport.right;
}
function create_folder_ele(opt){
    jFolder = $('<'+opt.itemTag+'>');
    // 初始化这个折叠元素
    jFolder.addClass(opt.itemClass)
        .html('<div class="folder-btn"></div><div class="'+opt.dropClass+'"></div>');
    var jBtn = jFolder.children().eq(0);
    if(opt.itemIcon)
        $('<span class="folder-btn-icon">').appendTo(jBtn).html(opt.itemIcon);
    if(opt.itemText)
        $('<span class="folder-btn-text">').appendTo(jBtn).text(opt.itemText);
    // 折叠菜单必须是绝对位置
    jDrop = jFolder.children().eq(1).css("position","absolute");
    return jFolder;
}
//...........................................................
var methods = {
    // 从头部开始折叠
    "head" : function(opt){
        var jq = this;
        // 寻找尾部的元素看看是否超出了范围
        var jChildren = jq.children();
        var jLast     = jChildren.last();
        var jFolder, jDrop;

        // 如果还在以内范围，那么一个一个退回去看看效果
        if(is_in_viewport(jq, jLast)){
            jFolder = jq.children("."+opt.itemClass);
            if(jFolder.length>0){
                jDrop   = jFolder.children().eq(1);
                while(jDrop.children().length>0){
                    var jItem = jDrop.children().first();
                    jItem.insertAfter(jFolder);
                    if(!is_in_viewport(jq, jLast)){
                        jDrop.prepend(jItem);
                        break;
                    }
                }
                // 空了的话，就移除
                if(jDrop.children().length==0){
                    jFolder.remove();
                }
            }
            return;
        }

        // 超出了，需要创建一个折叠元素
        var jItem = jChildren.eq(opt.keep);
        if(jItem.length==0)
            return;

        // 在这个位置放置一个折叠元素
        if(jItem.hasClass(opt.itemClass)){
            jFolder = jItem;
            jItem = jItem.next();
            jDrop = jFolder.children().eq(1).css("position","absolute");
        }
        // 创建折叠元素
        else{
            jFolder = create_folder_ele(opt).insertBefore(jItem);
            jDrop   = jFolder.children().eq(1);
        }

        // 确保下拉是隐藏的
        jDrop.hide();

        // 逐次向菜单里加入元素，看看会不会让右边缩进视口
        do{
            var jNext = jItem.next();
            if(jNext.length == 0)
                break;
            jDrop.prepend(jItem);
            jItem = jNext;
            console.log("[" + jItem.text() + "]", jItem.length);
            // 无论如何都要保留最后一个
            if(jItem[0] == jLast[0])
                break;
        }while(!is_in_viewport(jq, jLast));


    },
    // 从尾部开始折叠
    "tail" : function(opt){
        var jq = this;
        // 寻找尾部的元素看看是否超出了范围
        var jChildren = jq.children();
        var jLast     = jChildren.last();
        var jFolder, jDrop;

        // 如果还在以内范围，那么一个一个退回去看看效果
        if(is_in_viewport(jq, jLast)){
            jFolder = jq.children("."+opt.itemClass);
            if(jFolder.length>0){
                jDrop   = jFolder.children().eq(1);
                while(jDrop.children().length>0){
                    var jItem = jDrop.children().first();
                    jItem.insertBefore(jFolder);
                    if(!is_in_viewport(jq, jLast)){
                        jDrop.prepend(jItem);
                        break;
                    }
                }
                // 空了的话，就移除
                if(jDrop.children().length==0){
                    jFolder.remove();
                }
            }
            return;
        }

        // 超出了，需要创建一个折叠元素
        var jItem = jChildren.eq(jChildren.length - opt.keep -1);
        if(jItem.length==0)
            return;

        // 在这个位置放置一个折叠元素
        if(jItem.hasClass(opt.itemClass)){
            jFolder = jItem;
            jItem = jItem.prev();
            jDrop = jFolder.children().eq(1).css("position","absolute");
        }
        // 创建折叠元素
        else{
            jFolder = create_folder_ele(opt).insertAfter(jItem);
            jDrop   = jFolder.children().eq(1);
        }

        // 确保下拉是隐藏的
        jDrop.hide();

        // 逐次向菜单里加入元素，看看会不会让右边缩进视口
        jLast = jq.children().last();
        do{
            var jNext = jItem.prev();
            jDrop.prepend(jItem);
            jItem = jNext;
            // 无论如何都要保留最后一个
            if(jItem[0] == jLast[0])
                break;
        }while(!is_in_viewport(jq, jLast));
    }
};
//...........................................................
$.fn.extend({ "folder" : function(opt){
    // 默认配置必须为对象
    opt = $z.extend({
        itemIcon  : null,             // 折叠后，项目的图标 HTML
        itemTag   : "DIV",            // 折叠后，项目的元素标签
        itemText  : "...",            // 折叠后，项目的文字标识
        itemClass : "folder-item",    // 折叠后，项目的选择器名称
        dropClass : "folder-drop",    // 折叠后，展出菜单的名称
        mode      : "head",           // 是从头部折叠还是尾部折叠 head|tail
        keep      : 0                 // 折叠的时候保留之前（后）多少个元素
    }, opt);

    // 没必要折叠
    if(this.children().length == 0)
        return;

    // 调用对应的折叠方法
    methods[opt.mode].call(this, opt);

    // 返回自身以便链式赋值
    return this;
}});
//...........................................................
})(window.jQuery, window.NutzUtil);

