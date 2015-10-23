define(function (require, exports, module) {
/*
<input class="ui-fld-edit ui-fld-edit-input">
    <div   code-id="e-label"  class="oform-e-label"></div>
    <ul    code-id="e-bool-switch"  class="oform-e-bool-switch">
        <li index="0"><span></span></li>
        <li index="1"><span></span></li>
    </ul>
*/
function $html(fld, tagName, className){
    fld.$val.empty();
    var jq = $('<'+tagName+'>').addClass("ui-fld-edit-" + (className||tagName));
    fld.$val.addClass("ui-fld").data("@FLD", fld);
    return jq.appendTo(fld.$val);
}

module.exports = {
    //.............................................................
    "input" : {
        events : {
            "change .ui-fld-edit-input" : function(e){
                console.log($(this))
                var fld = $(this).parents(".ui-fld").data("@FLD");
                var val = $(e.currentTarget).val();
                var UI = ZUI(fld.$val);
                try{
                    UI.val_test(fld, val);
                }
                catch(E){
                    var str = UI.text(E.code) + "\nvalue: '" + E.val + "'\n" + $z.toJson(E.fld,null,4);
                    alert(str);
                }
            }
        },
        set : function(fld, obj){
            var v = this.val_edit(fld, obj);

            var jq = $html(fld, "input");
            jq.attr("placeholder", fld.tip);
            jq.val(v);
        },
        get : function(fld){
            var jq = fld.$val.find("input");
            var v =  jq.val() || fld.dft;
            //console.log(fld.key, ":", jq.size(), ":", v)
            return this.val_check(fld, v);
        }
    }, 
    //.............................................................
    "label" : {
        set : function(fld, obj){
            var UI = this;
            var jq = $html(fld, "div", "label");
            var txt = UI.text(UI.val_display(fld, obj));
            jq.text(txt);
        },
        get : function(fld){
            return undefined;
        }
    },
    //.............................................................
    "switchs" : {
        events : {
             "click .ui-fld-edit-switchs li" : function(e){
                var jq = $(e.currentTarget);
                jq.parents(".ui-fld-edit-switchs").find("li").removeClass("checked");
                jq.addClass("checked");
            }
        },
        set : function(fld, obj){
            var UI = this;
            var jq = $html(fld, "ul", "switchs");

            // 得到值
            var v = this.val_check(fld, obj[fld.key] || fld.dft);

            // 绘制所有的项目
            fld.setup.value.forEach(function(item, index){
                var jLi = $('<li index="'+index+'">').appendTo(jq);
                $('<span>').text(UI.text(item.text)).appendTo(jLi);
                if(item.val == v){
                    jLi.addClass("checked");
                } 
            });
        },
        get : function(fld){
            var index = fld.$val.find(".checked").attr("index") * 1;
            return fld.setup.value[index].val;
        }
    }
};
// 附加事件
var jBody = $(document.body);
for(var key in module.exports){
    var edit = module.exports[key];
    if(edit.events){
        for(var eKey in edit.events){
            var handler = edit.events[eKey];
            var pos = eKey.indexOf(' ');
            if(pos>0){
                var eventType = eKey.substring(0, pos);
                var selector  = eKey.substring(pos+1);
                jBody.on(eventType, selector, handler);
            }
        }
    }
}

});