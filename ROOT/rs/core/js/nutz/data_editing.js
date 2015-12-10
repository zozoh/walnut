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
    "_init" : {},
    //.............................................................
    "text"  : {
        set : function(fld, obj){
            var v = this.val_edit(fld, obj);

            var jq = $html(fld, "textarea");
            if(!_.isUndefined(fld.dft))
                jq.attr("placeholder", fld.dft);
            else if(fld.tip && fld.hideTip)
                jq.attr("placeholder", fld.tip);

            if(_.isString(v)){
                jq.val(v);
            }
            else if(!_.isUndefined(v) && !_.isNull(v)) {
                 jq.val($z.toJson(v, null, 4));
            }
        },
        get : function(fld){
            var jq = fld.$val.find("textarea");
            var v =  jq.val() || fld.dft;
            //console.log(fld.key, ":", jq.size(), ":", v)
            return this.val_check(fld, v);
        }
    },
    //.............................................................
    "input" : {
        events : {
            "change .ui-fld-edit-input" : function(e){
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
            if(!_.isUndefined(fld.dft))
                jq.attr("placeholder", fld.dft);
            else if(fld.tip && fld.hideTip)
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
    "daterange" : {
        set : function(fld, obj){
            seajs.use("jquery-plugin/zcal/zcal.css");
            seajs.use("jquery-plugin/zcal/zcal", function() {
                var dr = obj[fld.key];
                fld.$val.zcal(_.extend({}, fld.setup,{
                    mode  : "range"
                }));
                if(dr){
                    fld.$val.zcal("range", dr);
                }
                if(_.isFunction(fld.on_set)){
                    fld.on_set(fld, dr, obj);
                }
            });
        },
        get : function(fld){
            return fld.$val.zcal("range", fld.dateType);
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
            //var v = this.val_check(fld, obj[fld.key] || fld.dft);
            var v = this.val_get(fld, obj);

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
    },
    //.............................................................
    /*
    标准配置结构为:
    setup : {
        data    : ...  // 同步函数或者数组
        key_txt : "txt",
        key_val : "val"
    }
    */
    "multiselectbox" : {
        events : {
            "click .ui-fld-edit-multiselectbox .msb-box" : function(e){
                e.stopPropagation();
                var jq = $(e.currentTarget);
                jq.parent().children(".msb-pad").fadeIn(500);
                $(document.body).one("click", function(){
                    $(".ui-fld-edit-multiselectbox .msb-pad").fadeOut(300);
                });
            },
            "click .ui-fld-edit-multiselectbox .msb-pad" : function(e){
                e.stopPropagation();
            },
            "click .ui-fld-edit-multiselectbox .msb-pad .msb-item" : function(e){
                e.stopPropagation();
                var jq = $(e.currentTarget);
                var jBox = jq.parents(".ui-fld-edit-multiselectbox").children(".msb-box");
                $z.removeIt(jq, function(){
                    if(jBox.attr("empty")){
                        jBox.empty().removeAttr("empty");
                    }
                    jq.appendTo(jBox);
                    $z.blinkIt(jq);
                });
            },
            "click .ui-fld-edit-multiselectbox .msb-box .msb-item" : function(e){
                e.stopPropagation();
                var jq = $(e.currentTarget);
                var UI = ZUI(jq);
                var jBox = jq.parents(".msb-box");
                var jPad = jq.parents(".ui-fld-edit-multiselectbox").children(".msb-pad");
                $z.removeIt(jq, function(){
                    jq.appendTo(jPad);
                    $z.blinkIt(jq);
                    if(jBox.children().size() == 0){
                        jBox.attr("empty","true").html('<div>'+UI.msg("editing.multiselectbox.empty")+'</div>');
                    }
                });
            }
        },
        set : function(fld, obj){
            var UI = this;

            var v = this.val_get(fld, obj);
            //console.log("obj v:",  v)

            // 生成 dom 结构 
            var list = fld._list;
            var jq = $html(fld, "div", "multiselectbox");
            var jBox = $('<div class="msb-box">').appendTo(jq);
            var jPad = $('<div class="msb-pad">').appendTo(jq);
            for(var i=0;i<list.length;i++){
                var ele = list[i];
                var txt = ele[fld.setup.key_txt];
                var val = ele[fld.setup.key_val];
                var jItem = $('<div class="msb-item">').attr("val", val).text(txt);
                // 显示出来，还是放入备选
                if(v.indexOf(val) >= 0){
                    jBox.append(jItem);
                }else{
                    jPad.append(jItem);
                }
            }

            // 显示帮助
            if(jBox.children().size()==0){
                jBox.attr("empty","true").html('<div>'+UI.msg("editing.multiselectbox.empty")+'</div>');
            }
        },
        get : function(fld){
            var re = [];
            fld.$val.find(".msb-box .msb-item").each(function(){
                re.push($(this).attr("val"));
            });
            return re;
        }
    }
};
// 附加事件
// var jBody = $(document.body);
// for(var key in module.exports){
//     var edit = module.exports[key];
//     if(edit.events){
//         for(var eKey in edit.events){
//             var handler = edit.events[eKey];
//             var pos = eKey.indexOf(' ');
//             if(pos>0){
//                 var eventType = eKey.substring(0, pos);
//                 var selector  = eKey.substring(pos+1);
//                 console.log("jBody.on", eventType, selector)
//                 jBody.on(eventType, selector, handler);
//             }
//         }
//     }
// }

});