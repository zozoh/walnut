define(function (require, exports, module) {
/*
<input class="ui-fld-edit ui-fld-edit-input">
    <div   code-id="e-label"  class="oform-e-label"></div>
    <ul    code-id="e-bool-switch"  class="oform-e-bool-switch">
        <li index="0"><span></span></li>
        <li index="1"><span></span></li>
    </ul>
*/
function $html(tagName, className){
    return $('<'+tagName+'>').addClass("ui-fld-edit-" + (className||tagName));
}

function _C(className){
    return "ui-fld-edit-" + className;
}

module.exports = {
    //.............................................................
    "input" : {
        set : function(fld, obj){
            fld.$val.empty();
            var v = $z.getValue(obj,fld.key);
            var jq = $html("input");
            jq.attr("placeholder", fld.tip);
            jq.val(v);
            jq.appendTo(fld.$val);
        },
        get : function(fld){
            var jq = fld.$val.find("input");
            var v =  jq.val() || fld.dft;
            return $z.strToJsObj(v, fld.type);
        }
    }, 
    //.............................................................
    "label" : {
        set : function(fld, obj){
            var UI = this;
            fld.$val.empty();
            var jq = $html("div", "label");
            var txt = UI.text(UI.val_display(fld, obj));
            jq.text(txt).appendTo(fld.$val);
        },
        get : function(fld){
            return undefined;
        }
    },
    //.............................................................
    "bool_switch" : {
        events : {
             "click .ui-fld-edit-bool-switch li" : function(e){
                var jq = $(e.currentTarget);
                jq.parents(".ui-fld-edit-bool-switch").find("li").removeClass("checked");
                jq.addClass("checked");
            }
        },
        set : function(fld, obj){
            var UI = this;
            fld.$val.empty();
            var jq = $html("ul", "bool-switch");

            // 得到值
            var v = $z.getValue(obj, fld.key, fld.dft);

            // 根据配置，将值转换成布尔，并设置控件
            var vIndex = (v === fld.setup[1].val) ? 1 : 0;
            
            // 设置两个开关
            for(var i=0; i<2; i++) {
                var jLi = $('<li index="'+i+'">').appendTo(jq);
                $('<span>').text(UI.text(fld.setup[i].text)).appendTo(jLi);
                if(vIndex == i){
                    jLi.addClass("checked");
                } 
            }

            // 加入到 DOM
            jq.appendTo(fld.$val);
        },
        get : function(fld){
            var index = fld.$val.find(".checked").attr("index") * 1;
            return fld.setup[index].val;
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